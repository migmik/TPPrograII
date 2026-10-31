package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.DnisPrueba;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.modelos.*;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.repositorios.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:dni;MODE=MySQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
@Transactional
class DniIntegracionTest {
    @Autowired TuristaServicio servicio;
    @Autowired SucursalRepositorio sucursales;
    @Autowired TuristaRepositorio turistas;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioServicio usuariosServicio;
    @Autowired UsuarioRepositorio usuarios;
    private final Administrador admin = new Administrador("admin", "hash", DnisPrueba.siguiente());

    @Test
    void empleadosRequierenDniYNoPuedenRepetirElDeOtroEmpleado() {
        Usuario vendedor = usuariosServicio.crear(admin, "vendedor-dni", "clave", RolUsuario.VENDEDOR, null, "34567890");
        assertEquals("34567890", vendedor.getDni());
        try {
            usuariosServicio.crear(admin, "sin-dni", "clave",
                    RolUsuario.ADMINISTRADOR, null, null);
            fail("Se esperaba IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // Es la excepcion que se esperaba.
        }
        try {
            usuariosServicio.crear(admin, "otro-dni", "clave",
                    RolUsuario.ADMINISTRADOR, null, "34567890");
            fail("Se esperaba EntidadDuplicadaException");
        } catch (EntidadDuplicadaException esperada) {
            // Es la excepcion que se esperaba.
        }
        usuariosServicio.modificarCredenciales(admin, vendedor.getCodigo(), "vendedor-dni", "otra-clave", "34567890");
        Usuario otro = usuariosServicio.crear(admin, "otro", "clave", RolUsuario.ADMINISTRADOR, null, "45678901");
        try {
            usuariosServicio.modificarCredenciales(admin,
                    otro.getCodigo(), "otro", "clave", "34567890");
            fail("Se esperaba EntidadDuplicadaException");
        } catch (EntidadDuplicadaException esperada) {
            // Es la excepcion que se esperaba.
        }
        assertEquals("45678901", otro.getDni());
    }

    @Test
    void clienteConsultaDniDelTuristaSinUnaSegundaCopia() {
        Integer sucursal = sucursales.save(new Sucursal("Calle DNI", "1")).getCodigo();
        Turista titular = servicio.crearTitular(admin, "12345678", "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal);
        Usuario cliente = usuariosServicio.crear(admin, "cliente-dni", "clave", RolUsuario.CLIENTE, titular.getCodigo(), null);
        assertEquals("12345678", cliente.getDni());
        usuarios.flush();
        assertNull(jdbc.queryForObject("SELECT dni FROM usuarios WHERE codigo=?", String.class, cliente.getCodigo()));
        servicio.modificar(admin, titular.getCodigo(), "23456789", "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal);
        assertEquals("23456789", cliente.getDni());
        try {
            usuariosServicio.modificarCredenciales(admin,
                    cliente.getCodigo(), "cliente-dni", "clave", "34567890");
            fail("Se esperaba IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // Es la excepcion que se esperaba.
        }
    }

    @Test
    void mysqlImpideRepetirDocumentoDeEmpleadoOAlmacenarOtraCopiaEnCliente() {
        Usuario uno = usuariosServicio.crear(admin, "uno", "clave", RolUsuario.VENDEDOR, null, "34567890");
        Usuario dos = usuariosServicio.crear(admin, "dos", "clave", RolUsuario.ADMINISTRADOR, null, "45678901");
        usuarios.flush();
        try {
            jdbc.update("UPDATE usuarios SET dni=? WHERE codigo=?", uno.getDni(), dos.getCodigo());
            fail("Se esperaba DataIntegrityViolationException");
        } catch (DataIntegrityViolationException esperada) {
            // Es la excepcion que se esperaba.
        }
        try {
            jdbc.update("UPDATE usuarios SET dni='abc' WHERE codigo=?", dos.getCodigo());
            fail("Se esperaba DataIntegrityViolationException");
        } catch (DataIntegrityViolationException esperada) {
            // Es la excepcion que se esperaba.
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"123456", "123456789", "12.345.678", "1234567a", " 12345678", "12345678 ", "-1234567"})
    void modeloRechazaDniInvalido(String dni) {
        try {
            new Turista(dni, "Ana", "Perez", "Calle",
                    "ana@example.test", "1", "2", new Sucursal("Calle", "1"));
            fail("Se esperaba IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // Es la excepcion que se esperaba.
        }
    }

    @Test
    void guardaDniYNoPermiteCompartirloEntreTitularYFamiliar() {
        Integer sucursal = sucursales.save(new Sucursal("Calle DNI", "1")).getCodigo();
        Turista titular = servicio.crearTitular(admin, "12345678", "Ana", "Perez", "Calle",
                "ana@example.test", "1", "2", sucursal);
        assertEquals("12345678", turistas.findById(titular.getCodigo()).get().getDni());
        try {
            servicio.crearFamiliar(admin, titular.getCodigo(),
                    "12345678", "Luis", "Perez", "Calle", "luis@example.test", "1", "2");
            fail("Se esperaba EntidadDuplicadaException");
        } catch (EntidadDuplicadaException esperada) {
            // Es la excepcion que se esperaba.
        }
    }

    @Test
    void editarAceptaElPropioDniPeroRechazaElAjeno() {
        Integer sucursal = sucursales.save(new Sucursal("Calle DNI", "1")).getCodigo();
        Turista uno = servicio.crearTitular(admin, "12345678", "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal);
        Turista dos = servicio.crearTitular(admin, "23456789", "Luis", "Perez", "Calle", "luis@example.test", "1", "2", sucursal);
        servicio.modificar(admin, uno.getCodigo(), "12345678", "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal);
        try {
            servicio.modificar(admin, uno.getCodigo(), dos.getDni(),
                    "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal);
            fail("Se esperaba EntidadDuplicadaException");
        } catch (EntidadDuplicadaException esperada) {
            // Es la excepcion que se esperaba.
        }
        assertEquals("12345678", uno.getDni());
    }

    @Test
    void baseImpideDuplicadosYFormatosInvalidosInclusoSinPasarPorLaApi() {
        Integer sucursal = sucursales.save(new Sucursal("Calle DNI", "1")).getCodigo();
        Turista uno = servicio.crearTitular(admin, "12345678", "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal);
        Turista dos = servicio.crearTitular(admin, "23456789", "Luis", "Perez", "Calle", "luis@example.test", "1", "2", sucursal);
        turistas.flush();
        try {
            jdbc.update("UPDATE turistas SET dni = ? WHERE codigo = ?", uno.getDni(), dos.getCodigo());
            fail("Se esperaba DataIntegrityViolationException");
        } catch (DataIntegrityViolationException esperada) {
            // Es la excepcion que se esperaba.
        }
        try {
            jdbc.update("UPDATE turistas SET dni = 'abc' WHERE codigo = ?", dos.getCodigo());
            fail("Se esperaba DataIntegrityViolationException");
        } catch (DataIntegrityViolationException esperada) {
            // Es la excepcion que se esperaba.
        }
    }
}
