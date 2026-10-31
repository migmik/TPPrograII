package com.tijetravel.tijeback.servicios.impl;

import java.time.LocalDateTime;
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
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.EntidadBase;
import com.tijetravel.tijeback.modelos.Reserva;
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
import com.tijetravel.tijeback.servicios.DisponibilidadServicio;
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
    private final DisponibilidadServicio disponibilidadServicio;

    public TuristaServicioImpl(
            TuristaRepositorio turistaRepositorio,
            SucursalRepositorio sucursalRepositorio,
            ReservaRepositorio reservaRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            AutorizacionServicio autorizacion,
            BloqueoEscrituras bloqueoEscrituras,
            DisponibilidadServicio disponibilidadServicio) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.turistaRepositorio = turistaRepositorio;
        this.sucursalRepositorio = sucursalRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.autorizacion = autorizacion;
        this.disponibilidadServicio = disponibilidadServicio;
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
        verificarDniDisponible(turista.getDni(), null);
        verificarEmailDisponible(turista.getEmail(), null);
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
        verificarDniDisponible(familiar.getDni(), null);
        verificarEmailDisponible(familiar.getEmail(), null);
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

        List<Turista> grupoFamiliar = turistaRepositorio.listarGrupoFamiliar(codigoGrupoFamiliar);
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
            turistas = turistaRepositorio.findByDniAndFechaBajaIsNull(dni);
        } else if (titular == null) {
            return turistaRepositorio.findByFechaBajaIsNull(Sort.by("codigo"));
        } else if (titular) {
            return turistaRepositorio.findByTitularIsNullAndFechaBajaIsNullOrderByCodigo();
        } else {
            return turistaRepositorio.findByTitularIsNotNullAndFechaBajaIsNullOrderByCodigo();
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
        for (Turista turista : turistaRepositorio.findByCodigoInAndFechaBajaIsNullOrderByCodigo(codigos)) {
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
        if (turista == null || !turista.estaActivo()) {
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
        verificarDniDisponible(dniValidado, codigo);
        String emailNormalizado = ValidacionModelo.email(email);
        verificarEmailDisponible(emailNormalizado, codigo);
        turista.actualizarDatos(
                dniValidado, nombre, apellido, direccion, emailNormalizado, telefonoFijo, telefonoCelular, sucursal);
        // Los familiares siempre pertenecen a la misma sucursal que su titular.
        if (turista.isTitular()) {
            for (Turista familiar : turistaRepositorio.findByTitularCodigoAndFechaBajaIsNull(codigo)) {
                familiar.cambiarSucursal(sucursal);
            }
        }
        return turista;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void darDeBaja(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        bloqueoEscrituras.adquirir();
        Turista turista = encontrarPorId(codigo);

        // Todo el grupo se da de baja con la misma fecha; asi despues se sabe que reactivar junto.
        LocalDateTime fechaBaja = EntidadBase.ahora();
        List<Turista> grupo = new ArrayList<>();
        grupo.add(turista);
        if (turista.isTitular()) {
            grupo.addAll(turistaRepositorio.findByTitularCodigoAndFechaBajaIsNull(codigo));
            for (Cliente cliente : usuarioRepositorio.buscarClientesDeTurista(codigo)) {
                if (cliente.estaActivo()) {
                    cliente.darDeBaja(fechaBaja);
                }
            }
        }
        for (Turista integrante : grupo) {
            // Las reservas pagadas quedan como historial; las pendientes liberan su lugar.
            for (Reserva reserva : reservaRepositorio.findByTuristaCodigoAndPagadaFalseAndFechaBajaIsNull(
                    integrante.getCodigo())) {
                reserva.darDeBaja(fechaBaja);
            }
            integrante.darDeBaja(fechaBaja);
        }
    }

    @Override
    public List<Turista> listarBajasPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        return turistaRepositorio.findByFechaBajaIsNotNullOrderByFechaBajaDesc();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Turista reactivar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_TURISTAS);
        bloqueoEscrituras.adquirir();
        Turista turista = turistaRepositorio.findById(codigo).orElse(null);
        if (turista == null || turista.estaActivo()) {
            throw new EntidadNoEncontradaException("No hay un turista dado de baja con el codigo " + codigo);
        }
        if (!turista.isTitular() && !turista.getTitular().estaActivo()) {
            throw new OperacionNoPermitidaException("Primero hay que reactivar al titular del grupo familiar");
        }
        if (!turista.getSucursalContratacion().estaActivo()) {
            throw new OperacionNoPermitidaException("La sucursal del turista esta dada de baja");
        }

        // Se arma la lista antes de reactivar, porque se compara la fecha de baja.
        List<Turista> grupo = new ArrayList<>();
        grupo.add(turista);
        List<Cliente> cuentas = new ArrayList<>();
        if (turista.isTitular()) {
            for (Turista familiar : turistaRepositorio.findByTitularCodigo(codigo)) {
                if (familiar.seDioDeBajaJuntoCon(turista)) {
                    grupo.add(familiar);
                }
            }
            for (Cliente cliente : usuarioRepositorio.buscarClientesDeTurista(codigo)) {
                if (cliente.seDioDeBajaJuntoCon(turista)) {
                    cuentas.add(cliente);
                }
            }
        }
        List<Reserva> reservas = new ArrayList<>();
        for (Turista integrante : grupo) {
            verificarDniDisponible(integrante.getDni(), integrante.getCodigo());
            verificarEmailDisponible(integrante.getEmail(), integrante.getCodigo());
            for (Reserva reserva : reservaRepositorio.findByTuristaCodigo(integrante.getCodigo())) {
                if (reserva.seDioDeBajaJuntoCon(integrante)) {
                    reservas.add(reserva);
                }
            }
        }

        for (Turista integrante : grupo) {
            integrante.reactivar();
        }
        for (Cliente cliente : cuentas) {
            if (!usuarioRepositorio.existsByNombreUsuarioIgnoreCaseAndFechaBajaIsNull(cliente.getNombreUsuario())) {
                cliente.reactivar();
            }
        }
        // Una reserva vuelve solo si todavia hay lugar; si no, queda en los dados de baja.
        for (Reserva reserva : reservas) {
            if (puedeVolver(reserva)) {
                reserva.reactivar();
            }
        }
        return turista;
    }

    private boolean puedeVolver(Reserva reserva) {
        if (!reserva.getVuelo().estaActivo() || !reserva.getHotel().estaActivo()) {
            return false;
        }
        if (reservaRepositorio.existsByTuristaCodigoAndVueloNumeroAndFechaBajaIsNull(
                reserva.getTurista().getCodigo(), reserva.getVuelo().getNumero())) {
            return false;
        }
        int plazasVuelo = disponibilidadServicio.plazasDisponiblesVuelo(
                reserva.getVuelo().getNumero(), reserva.getClaseVuelo());
        int plazasHotel = disponibilidadServicio.plazasDisponiblesHotel(
                reserva.getHotel(), reserva.getFechaLlegada(), reserva.getFechaPartida());
        return plazasVuelo > 0 && plazasHotel > 0;
    }

    private Sucursal encontrarSucursal(Integer codigo) {
        Sucursal sucursal = sucursalRepositorio.findById(codigo).orElse(null);
        if (sucursal == null || !sucursal.estaActivo()) {
            throw new EntidadNoEncontradaException("No se encontro la sucursal " + codigo);
        }
        return sucursal;
    }

    private void verificarDniDisponible(String dni, Integer codigoPropio) {
        if (dni == null) {
            // Turistas cargados antes de que el DNI fuera obligatorio.
            return;
        }
        boolean ocupado;
        if (codigoPropio == null) {
            ocupado = turistaRepositorio.existsByDniAndFechaBajaIsNull(dni);
        } else {
            ocupado = turistaRepositorio.existsByDniAndFechaBajaIsNullAndCodigoNot(dni, codigoPropio);
        }
        if (ocupado) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese DNI");
        }
    }

    private void verificarEmailDisponible(String email, Integer codigoPropio) {
        boolean ocupado;
        if (codigoPropio == null) {
            ocupado = turistaRepositorio.existsByEmailIgnoreCaseAndFechaBajaIsNull(email);
        } else {
            ocupado = turistaRepositorio.existsByEmailIgnoreCaseAndFechaBajaIsNullAndCodigoNot(email, codigoPropio);
        }
        if (ocupado) {
            throw new EntidadDuplicadaException("Ya existe un turista con ese email");
        }
    }
}
