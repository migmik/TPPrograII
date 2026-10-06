package com.tijetravel.tijeback.repositorios;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

class DniMigracionTest {
    @Test
    void actualizaUnaBaseAnteriorSinInventarDniNiPerderFamiliares() {
        var fuente = new SingleConnectionDataSource("jdbc:h2:mem:migracion-dni;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "", true);
        Flyway.configure().dataSource(fuente).locations("classpath:db/migration").target("5").load().migrate();
        JdbcTemplate jdbc = new JdbcTemplate(fuente);
        jdbc.update("INSERT INTO sucursales(codigo,direccion,telefono) VALUES (1,'Calle','1')");
        jdbc.update("INSERT INTO usuarios(codigo,tipo_usuario,nombre_usuario,contrasenia,rol) VALUES (1,'ADMINISTRADOR','admin','hash','ADMINISTRADOR')");
        jdbc.update("""
                INSERT INTO turistas(codigo,nombre,apellido,direccion,email,telefono_fijo,telefono_celular,sucursal_codigo,titular_codigo)
                VALUES (1,'Ana','Perez','Calle','ana@example.test','1','2',1,NULL),
                       (2,'Luis','Perez','Calle','luis@example.test','1','2',1,1)
                """);
        Flyway.configure().dataSource(fuente).locations("classpath:db/migration").load().migrate();
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM turistas WHERE dni IS NULL", Integer.class));
        assertNull(jdbc.queryForObject("SELECT dni FROM usuarios WHERE codigo=1", String.class));
        assertEquals("hash", jdbc.queryForObject("SELECT contrasenia FROM usuarios WHERE codigo=1", String.class));
        assertEquals(1, jdbc.queryForObject("SELECT titular_codigo FROM turistas WHERE codigo=2", Integer.class));
        jdbc.update("UPDATE turistas SET dni='12345678' WHERE codigo=1");
        assertEquals("12345678", jdbc.queryForObject("SELECT dni FROM turistas WHERE codigo=1", String.class));
        fuente.destroy();
    }
}
