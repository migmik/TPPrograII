package tijetravel.persistencia;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import tijetravel.modelos.Agencia;
import tijetravel.modelos.ClaseVuelo;
import tijetravel.modelos.Hotel;
import tijetravel.modelos.Reserva;
import tijetravel.modelos.Sucursal;
import tijetravel.modelos.TipoHospedaje;
import tijetravel.modelos.Turista;
import tijetravel.modelos.Vuelo;

public class ArchivoReservas extends ArchivoTexto {
    private static final Path RUTA_ARCHIVO = rutaDatos("reservas.txt");

    public ArrayList<Reserva> cargar(Agencia agencia) {
        ArrayList<Reserva> reservas = new ArrayList<>();
        if (!Files.exists(RUTA_ARCHIVO)) {
            return reservas;
        }

        try (BufferedReader lector = abrirLector(RUTA_ARCHIVO)) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                if (lineaVacia(linea)) {
                    continue;
                }

                String[] partes = separarCampos(linea);

                if (partes.length < 9) {
                    System.out.println("Linea de reserva incompleta: " + linea);
                    continue;
                }

                int codigoReserva = Integer.parseInt(partes[0]);
                int codigoTurista = Integer.parseInt(partes[1]);
                int codigoSucursal = Integer.parseInt(partes[2]);
                int numeroVuelo = Integer.parseInt(partes[3]);
                int codigoHotel = Integer.parseInt(partes[4]);
                ClaseVuelo claseVuelo = ClaseVuelo.valueOf(partes[5]);
                TipoHospedaje tipoHospedaje = TipoHospedaje.valueOf(partes[6]);
                LocalDate fechaLlegada = LocalDate.parse(partes[7]);
                LocalDate fechaPartida = LocalDate.parse(partes[8]);

                Turista turista = agencia.buscarTuristaPorCodigo(codigoTurista);
                Sucursal sucursal = agencia.buscarSucursalPorCodigo(codigoSucursal);
                Vuelo vuelo = agencia.buscarVueloPorNumero(numeroVuelo);
                Hotel hotel = agencia.buscarHotelPorCodigo(codigoHotel);

                if (turista == null || sucursal == null || vuelo == null || hotel == null) {
                    System.out.println("Reserva omitida por referencias inexistentes: " + linea);
                    continue;
                }

                if (!fechaLlegada.isBefore(fechaPartida)) {
                    System.out.println("Reserva omitida por fechas invalidas: " + linea);
                    continue;
                }

                if (!fechaLlegada.equals(vuelo.getFechaYHora().toLocalDate())
                        || !hotel.getCiudad().trim().equalsIgnoreCase(vuelo.getDestino().trim())) {
                    System.out.println("Reserva omitida por vuelo y hotel incompatibles: " + linea);
                    continue;
                }

                Reserva reserva = new Reserva(
                        codigoReserva,
                        turista,
                        sucursal,
                        vuelo,
                        hotel,
                        claseVuelo,
                        tipoHospedaje,
                        fechaLlegada,
                        fechaPartida);
                reservas.add(reserva);
            }
        } catch (IOException | RuntimeException e) {
            throw errorCarga(RUTA_ARCHIVO, e);
        }

        return reservas;
    }

    public void guardar(List<Reserva> reservas) {
        guardarAtomico(RUTA_ARCHIVO, escritor -> {
            for (Reserva reserva : reservas) {
                escritor.write(
                        reserva.getCodigo() + ";"
                                + reserva.getTurista().getCodigo() + ";"
                                + reserva.getSucursal().getCodigo() + ";"
                                + reserva.getVuelo().getNumero() + ";"
                                + reserva.getHotel().getCodigo() + ";"
                                + reserva.getClaseVuelo() + ";"
                                + reserva.getTipoHospedaje() + ";"
                                + reserva.getFechaLlegada() + ";"
                                + reserva.getFechaPartida());
                escritor.newLine();
            }
        });
    }
}
