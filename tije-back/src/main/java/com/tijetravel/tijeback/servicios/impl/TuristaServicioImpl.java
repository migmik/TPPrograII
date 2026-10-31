package com.tijetravel.tijeback.servicios.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.ValidacionModelo;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;
import com.tijetravel.tijeback.servicios.AutorizacionServicio;
import com.tijetravel.tijeback.servicios.BloqueoEscrituras;
import com.tijetravel.tijeback.servicios.TuristaServicio;

@Service
@Transactional(readOnly = true)
public class TuristaServicioImpl implements TuristaServicio {
    private final BloqueoEscrituras bloqueoEscrituras;
    private final TuristaRepositorio turistaRepositorio;
    private final SucursalRepositorio sucursalRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final AutorizacionServicio autorizacion;

    public TuristaServicioImpl(
            TuristaRepositorio turistaRepositorio,
            SucursalRepositorio sucursalRepositorio,
            ReservaRepositorio reservaRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            AutorizacionServicio autorizacion,
            BloqueoEscrituras bloqueoEscrituras) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.turistaRepositorio = turistaRepositorio;
        this.sucursalRepositorio = sucursalRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.autorizacion = autorizacion;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Turista crear(Usuario actor, String dni, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular,
            Integer codigoSucursal, Integer codigoTitular) {
        if (codigoTitular == null) {
            if (codigoSucursal == null) {
                throw new IllegalArgumentException("El codigo de sucursal es obligatorio para un turista titular");
            }
            return crearTitular(actor, dni, nombre, apellido, direccion, email,
                    telefonoFijo, telefonoCelular, codigoSucursal);
        }
        if (codigoSucursal != null) {
            throw new IllegalArgumentException("Un turista familiar hereda la sucursal del titular");
        }
        return crearFamiliar(actor, codigoTitular, dni, nombre, apellido, direccion, email,
                telefonoFijo, telefonoCelular);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Turista crearTitular(
            Usuario actor,
            String dni,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular,
            Integer codigoSucursal) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        bloqueoEscrituras.adquirir();
        Sucursal sucursal = encontrarSucursal(codigoSucursal);
        Turista turista = new Turista(
                dni, nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, sucursal);
        verificarDniDisponible(turista.getDni());
        verificarEmailDisponible(turista.getEmail());
        return turistaRepositorio.save(turista);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Turista crearFamiliar(
            Usuario actor,
            Integer codigoTitular,
            String dni,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        bloqueoEscrituras.adquirir();
        Turista titular = encontrarPorId(codigoTitular);
        if (!titular.isTitular()) {
            throw new OperacionNoPermitidaException("El turista indicado no es titular");
        }

        Turista familiar = new Turista(
                dni,
                nombre,
                apellido,
                direccion,
                email,
                telefonoFijo,
                telefonoCelular,
                titular.getSucursalContratacion(),
                titular);
        verificarDniDisponible(familiar.getDni());
        verificarEmailDisponible(familiar.getEmail());
        return turistaRepositorio.save(familiar);
    }

    @Override
    public Page<Turista> buscarPaginaPara(Usuario actor, String dni, Boolean titular, int pagina, int tamanio) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        Pageable paginado = FiltrosConsulta.pagina(pagina, tamanio, Sort.by("codigo"));
        return turistaRepositorio.buscarPagina(limpiarDni(dni), titular, paginado);
    }

    @Override
    public List<Turista> listarTitularesSinCuentaPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_USUARIOS);
        return turistaRepositorio.listarTitularesSinCuenta();
    }

    @Override
    public List<Turista> listarPara(Usuario actor, String dni, Boolean titular) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        String dniBusqueda = limpiarDni(dni);
        Integer codigoGrupoFamiliar = actor.getCodigoGrupoFamiliar();
        if (codigoGrupoFamiliar == null) {
            return listarParaPersonal(dniBusqueda, titular);
        }

        List<Turista> grupoFamiliar = turistaRepositorio.findByCodigoOrTitularCodigoOrderByCodigo(
                codigoGrupoFamiliar, codigoGrupoFamiliar);
        if (grupoFamiliar.isEmpty()) {
            throw new EntidadNoEncontradaException("No se encontro el turista " + codigoGrupoFamiliar);
        }
        List<Turista> resultado = new ArrayList<>();
        for (Turista turista : grupoFamiliar) {
            if (coincideDni(turista, dniBusqueda) && coincideTipo(turista, titular)) {
                resultado.add(turista);
            }
        }
        return resultado;
    }

    private List<Turista> listarParaPersonal(String dni, Boolean titular) {
        List<Turista> turistas;
        if (dni != null) {
            turistas = turistaRepositorio.findByDni(dni);
        } else if (titular == null) {
            return turistaRepositorio.findAll(Sort.by("codigo"));
        } else if (titular) {
            return turistaRepositorio.findByTitularIsNullOrderByCodigo();
        } else {
            return turistaRepositorio.findByTitularIsNotNullOrderByCodigo();
        }
        List<Turista> resultado = new ArrayList<>();
        for (Turista turista : turistas) {
            if (coincideTipo(turista, titular)) {
                resultado.add(turista);
            }
        }
        return resultado;
    }

    @Override
    public List<Turista> listarPorCodigosPara(Usuario actor, List<Integer> codigos) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        List<Turista> resultado = new ArrayList<>();
        if (codigos == null || codigos.isEmpty()) {
            return resultado;
        }
        for (Turista turista : turistaRepositorio.findByCodigoInOrderByCodigo(codigos)) {
            if (actor.puedeVerTurista(turista)) {
                resultado.add(turista);
            }
        }
        return resultado;
    }

    private boolean coincideDni(Turista turista, String dni) {
        return dni == null || dni.equals(turista.getDni());
    }

    private boolean coincideTipo(Turista turista, Boolean titular) {
        return titular == null || turista.isTitular() == titular;
    }

    private String limpiarDni(String dni) {
        if (dni == null || dni.isBlank()) {
            return null;
        }
        return dni.trim();
    }

    private Turista encontrarPorId(Integer codigo) {
        Turista turista = turistaRepositorio.findById(codigo).orElse(null);
        if (turista == null) {
            throw new EntidadNoEncontradaException("No se encontro el turista " + codigo);
        }
        return turista;
    }

    @Override
    public Turista encontrarVisiblePara(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        Turista turista = encontrarPorId(codigo);
        autorizacion.verificarAccesoATurista(actor, turista,
                "El cliente no puede consultar turistas de otro grupo familiar");
        return turista;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Turista modificar(
            Usuario actor,
            Integer codigo,
            String dni,
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular,
            Integer codigoSucursal) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        bloqueoEscrituras.adquirir();
        Sucursal sucursal = encontrarSucursal(codigoSucursal);
        Turista turista = encontrarPorId(codigo);
        String dniValidado = ValidacionModelo.dni(dni);
        if (turistaRepositorio.existsByDniAndCodigoNot(dniValidado, codigo)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese DNI");
        }
        String emailNormalizado = ValidacionModelo.email(email);
        if (turistaRepositorio.existsByEmailIgnoreCaseAndCodigoNot(emailNormalizado, codigo)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese email");
        }
        turista.actualizarDatos(
                dniValidado, nombre, apellido, direccion, emailNormalizado, telefonoFijo, telefonoCelular, sucursal);
        // Los familiares siempre pertenecen a la misma sucursal que su titular.
        if (turista.isTitular()) {
            for (Turista familiar : turistaRepositorio.findByTitularCodigo(codigo)) {
                familiar.cambiarSucursal(sucursal);
            }
        }
        return turista;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void eliminar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        bloqueoEscrituras.adquirir();
        Turista turista = encontrarPorId(codigo);
        if (reservaRepositorio.existsByTuristaCodigo(codigo)) {
            throw new OperacionNoPermitidaException("No se puede eliminar un turista que tiene reservas");
        }
        if (turistaRepositorio.existsByTitularCodigo(codigo)) {
            throw new OperacionNoPermitidaException("No se puede eliminar un titular que tiene familiares");
        }
        if (usuarioRepositorio.contarClientesPorTurista(codigo) > 0) {
            throw new OperacionNoPermitidaException("No se puede eliminar un turista asociado a un usuario");
        }
        turistaRepositorio.delete(turista);
    }

    private Sucursal encontrarSucursal(Integer codigo) {
        Sucursal sucursal = sucursalRepositorio.findById(codigo).orElse(null);
        if (sucursal == null) {
            throw new EntidadNoEncontradaException("No se encontro la sucursal " + codigo);
        }
        return sucursal;
    }

    private void verificarDniDisponible(String dni) {
        if (turistaRepositorio.existsByDni(dni)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese DNI");
        }
    }

    private void verificarEmailDisponible(String email) {
        if (turistaRepositorio.existsByEmailIgnoreCase(email)) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese email");
        }
    }

}
