package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import com.tijetravel.tijeback.modelos.*;
import com.tijetravel.tijeback.repositorios.*;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;

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
    private final Administrador admin = new Administrador("admin", "hash", com.tijetravel.tijeback.DnisPrueba.siguiente());

    @Test
    void empleadosRequierenDniYNoPuedenRepetirElDeOtroEmpleado() {
        var vendedor = usuariosServicio.crear(admin, "vendedor-dni", "clave", com.tijetravel.tijeback.enums.RolUsuario.VENDEDOR, null, "34567890");
        assertEquals("34567890", vendedor.getDni());
        assertThrows(IllegalArgumentException.class, () -> usuariosServicio.crear(admin, "sin-dni", "clave",
                com.tijetravel.tijeback.enums.RolUsuario.ADMINISTRADOR, null, null));
        assertThrows(EntidadDuplicadaException.class, () -> usuariosServicio.crear(admin, "otro-dni", "clave",
                com.tijetravel.tijeback.enums.RolUsuario.ADMINISTRADOR, null, "34567890"));
        usuariosServicio.modificarCredenciales(admin, vendedor.getCodigo(), "vendedor-dni", "otra-clave", "34567890");
        var otro = usuariosServicio.crear(admin, "otro", "clave", com.tijetravel.tijeback.enums.RolUsuario.ADMINISTRADOR, null, "45678901");
        assertThrows(EntidadDuplicadaException.class, () -> usuariosServicio.modificarCredenciales(admin,
                otro.getCodigo(), "otro", "clave", "34567890"));
        assertEquals("45678901", otro.getDni());
    }

    @Test
    void clienteConsultaDniDelTuristaSinUnaSegundaCopia() {
        Integer sucursal = sucursales.save(new Sucursal("Calle DNI", "1")).getCodigo();
        Turista titular = servicio.crearTitular(admin, "12345678", "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal);
        var cliente = usuariosServicio.crear(admin, "cliente-dni", "clave", com.tijetravel.tijeback.enums.RolUsuario.CLIENTE, titular.getCodigo(), null);
        assertEquals("12345678", cliente.getDni());
        usuarios.flush();
        assertNull(jdbc.queryForObject("SELECT dni FROM usuarios WHERE codigo=?", String.class, cliente.getCodigo()));
        servicio.modificar(admin, titular.getCodigo(), "23456789", "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal);
        assertEquals("23456789", cliente.getDni());
        assertThrows(IllegalArgumentException.class, () -> usuariosServicio.modificarCredenciales(admin,
                cliente.getCodigo(), "cliente-dni", "clave", "34567890"));
    }

    @Test
    void mysqlImpideRepetirDocumentoDeEmpleadoOAlmacenarOtraCopiaEnCliente() {
        var uno = usuariosServicio.crear(admin, "uno", "clave", com.tijetravel.tijeback.enums.RolUsuario.VENDEDOR, null, "34567890");
        var dos = usuariosServicio.crear(admin, "dos", "clave", com.tijetravel.tijeback.enums.RolUsuario.ADMINISTRADOR, null, "45678901");
        usuarios.flush();
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("UPDATE usuarios SET dni=? WHERE codigo=?", uno.getDni(), dos.getCodigo()));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("UPDATE usuarios SET dni='abc' WHERE codigo=?", dos.getCodigo()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"123456", "123456789", "12.345.678", "1234567a", " 12345678", "12345678 ", "-1234567"})
    void modeloRechazaDniInvalido(String dni) {
        assertThrows(IllegalArgumentException.class, () -> new Turista(dni, "Ana", "Perez", "Calle",
                "ana@example.test", "1", "2", new Sucursal("Calle", "1")));
    }

    @Test
    void guardaDniYNoPermiteCompartirloEntreTitularYFamiliar() {
        Integer sucursal = sucursales.save(new Sucursal("Calle DNI", "1")).getCodigo();
        Turista titular = servicio.crearTitular(admin, "12345678", "Ana", "Perez", "Calle",
                "ana@example.test", "1", "2", sucursal);
        assertEquals("12345678", turistas.findById(titular.getCodigo()).orElseThrow().getDni());
        assertThrows(EntidadDuplicadaException.class, () -> servicio.crearFamiliar(admin, titular.getCodigo(),
                "12345678", "Luis", "Perez", "Calle", "luis@example.test", "1", "2"));
    }

    @Test
    void editarAceptaElPropioDniPeroRechazaElAjeno() {
        Integer sucursal = sucursales.save(new Sucursal("Calle DNI", "1")).getCodigo();
        Turista uno = servicio.crearTitular(admin, "12345678", "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal);
        Turista dos = servicio.crearTitular(admin, "23456789", "Luis", "Perez", "Calle", "luis@example.test", "1", "2", sucursal);
        servicio.modificar(admin, uno.getCodigo(), "12345678", "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal);
        assertThrows(EntidadDuplicadaException.class, () -> servicio.modificar(admin, uno.getCodigo(), dos.getDni(),
                "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal));
        assertEquals("12345678", uno.getDni());
    }

    @Test
    void baseImpideDuplicadosYFormatosInvalidosInclusoSinPasarPorLaApi() {
        Integer sucursal = sucursales.save(new Sucursal("Calle DNI", "1")).getCodigo();
        Turista uno = servicio.crearTitular(admin, "12345678", "Ana", "Perez", "Calle", "ana@example.test", "1", "2", sucursal);
        Turista dos = servicio.crearTitular(admin, "23456789", "Luis", "Perez", "Calle", "luis@example.test", "1", "2", sucursal);
        turistas.flush();
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("UPDATE turistas SET dni = ? WHERE codigo = ?", uno.getDni(), dos.getCodigo()));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("UPDATE turistas SET dni = 'abc' WHERE codigo = ?", dos.getCodigo()));
    }
}
