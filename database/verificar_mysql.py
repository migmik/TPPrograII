"""Prepara bases aisladas y ejecuta la suite contra MySQL local. No elimina bases."""
import argparse
import datetime
import json
import os
from pathlib import Path
import re
import secrets
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
BACK = ROOT / 'tije-back'
LOCAL = BACK / 'config/mysql-pruebas'
STATE = LOCAL / 'ejecucion.json'
MYSQL = Path(r'C:\Program Files\MySQL\MySQL Server 9.7\bin\mysql.exe')
SCHEMAS = ['base', 'api', 'catalogos', 'dev', 'escrituras', 'recursos', 'seguridad', 'reglas', 'demo']


def properties(path):
    values = {}
    def unescape(value):
        return re.sub(r'\\(u[0-9a-fA-F]{4}|.)', lambda m: chr(int(m[1][1:], 16))
                      if m[1].startswith('u') else {'n': '\n', 'r': '\r', 't': '\t'}.get(m[1], m[1]), value)
    for line in path.read_text(encoding='utf-8-sig').splitlines():
        if line.strip() and not line.lstrip().startswith(('#', '!')) and '=' in line:
            key, value = line.split('=', 1)
            value = unescape(value.strip())
            placeholder = re.fullmatch(r'\$\{([^:}]+)(?::(.*))?\}', value)
            if placeholder:
                value = os.environ.get(placeholder[1], placeholder[2] or '')
            values[key.strip()] = value
    return values


def mysql(credentials, sql):
    LOCAL.mkdir(parents=True, exist_ok=True)
    option_file = LOCAL / 'cliente-temporal.cnf'
    def quote(value):
        return '"' + str(value).replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n') + '"'
    option_file.write_text('[client]\n' + ''.join(
        f'{key}={quote(credentials[key])}\n' for key in ('host', 'port', 'user', 'password')),
        encoding='utf-8')
    try:
        result = subprocess.run([str(MYSQL), f'--defaults-extra-file={option_file.name}',
                                 '--protocol=TCP', '--batch', '--skip-column-names', '--default-character-set=utf8mb4'],
                                input=sql, text=True, encoding='utf-8', capture_output=True, cwd=LOCAL)
        if result.returncode:
            raise RuntimeError(result.stderr.replace(credentials['password'], '[OCULTA]')
                               if credentials['password'] else result.stderr)
        return result.stdout.strip()
    finally:
        option_file.unlink(missing_ok=True)


def preparar():
    if STATE.exists():
        raise RuntimeError('Ya existe una ejecucion preparada. No se sobrescriben sus credenciales ni bases.')
    config = properties(BACK / 'config/application.properties')
    match = re.match(r'jdbc:mysql://([^/:?]+)(?::(\d+))?/', config['spring.datasource.url'])
    if not match or match[1] not in ('localhost', '127.0.0.1'):
        raise RuntimeError('Esta herramienta solo prepara pruebas en el MySQL local configurado.')
    admin = dict(host=match[1], port=match[2] or '3306',
                 user=config['spring.datasource.username'], password=config['spring.datasource.password'])
    version = mysql(admin, 'SELECT VERSION();')
    run = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    prefix = 'tije_it_' + run
    credentials = dict(host=admin['host'], port=admin['port'], user='tije_it_' + run,
                       password=secrets.token_hex(24))
    schemas = {name: prefix + '_' + name for name in SCHEMAS}
    sql = [f"CREATE USER '{credentials['user']}'@'localhost' IDENTIFIED BY '{credentials['password']}';"]
    for schema in schemas.values():
        sql += [f'CREATE DATABASE `{schema}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;',
                f"GRANT ALL PRIVILEGES ON `{schema}`.* TO '{credentials['user']}'@'localhost';"]
    # Guardar el estado antes de aprovisionar permite inspeccionar un fallo parcial sin repetir CREATE.
    LOCAL.mkdir(parents=True, exist_ok=True)
    state = dict(credentials=credentials, schemas=schemas, version=version,
                 run=run, provisionada=False)
    STATE.write_text(json.dumps(state, indent=2), encoding='utf-8')
    mysql(admin, '\n'.join(sql))
    state['provisionada'] = True
    STATE.write_text(json.dumps(state, indent=2), encoding='utf-8')
    print('MySQL:', version)
    print('Usuario de pruebas:', credentials['user'] + '@localhost')
    print('Bases creadas:', ', '.join(schemas.values()))
    print('Credenciales locales guardadas en', STATE.relative_to(ROOT), '(ignorado por Git).')


def estado():
    state = json.loads(STATE.read_text(encoding='utf-8'))
    if not state['provisionada']:
        raise RuntimeError('La preparacion no termino; inspeccionar antes de continuar.')
    return state


def url(state, name):
    c = state['credentials']
    return (f"jdbc:mysql://{c['host']}:{c['port']}/{state['schemas'][name]}"
            '?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC')


def pruebas():
    state = estado()
    if state.get('suite_iniciada'):
        raise RuntimeError('La suite ya se ejecuto o inicio sobre estas bases. No se reutilizan datos persistentes automaticamente.')
    env = os.environ.copy()
    env['TEST_DB_USER'] = state['credentials']['user']
    env['TEST_DB_PASSWORD'] = state['credentials']['password']
    for name in SCHEMAS:
        env['TEST_DB_URL' + ('' if name == 'base' else '_' + name.upper())] = url(state, name)
    state['suite_iniciada'] = True
    STATE.write_text(json.dumps(state, indent=2), encoding='utf-8')
    log = LOCAL / 'maven-mysql.log'
    with log.open('w', encoding='utf-8') as out:
        result = subprocess.run(['mvn.cmd', 'test', '-q'], cwd=BACK, env=env, stdout=out, stderr=subprocess.STDOUT)
    totals = dict(tests=0, failures=0, errors=0, skipped=0)
    for report in (BACK/'target/surefire-reports').glob('TEST-*.xml'):
        suite = ET.parse(report).getroot()
        for key in totals:
            totals[key] += int(suite.attrib[key])
    state['suite_resultado'] = dict(exit_code=result.returncode, **totals)
    STATE.write_text(json.dumps(state, indent=2), encoding='utf-8')
    print('Resultado Maven:', result.returncode, totals)
    print('Log local:', log.relative_to(ROOT))
    print('Esquemas con Flyway:')
    for name, schema in state['schemas'].items():
        if name != 'demo':
            print(name, mysql(state['credentials'],
                f'SELECT version, success FROM `{schema}`.flyway_schema_history ORDER BY installed_rank;'))
    return result.returncode


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('accion', choices=['preparar', 'pruebas', 'estado'])
    args = parser.parse_args()
    try:
        if args.accion == 'preparar': preparar()
        elif args.accion == 'pruebas': sys.exit(pruebas())
        else:
            state = estado()
            print(json.dumps({key: value for key, value in state.items() if key != 'credentials'}, indent=2))
    except Exception as error:
        print('ERROR:', error, file=sys.stderr)
        sys.exit(1)
