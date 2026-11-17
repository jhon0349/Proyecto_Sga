package co.edu.uniquindio.universidad.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Canal Directo: la recepción registra la reserva (teléfono, mensajería o presencial); no exige anticipo. */
public class ReservaDirecta extends Reserva {
    public ReservaDirecta(String id, Huesped titular, Apartamento apartamento, LocalDate entrada, LocalDate salida,
                          List<Ocupante> alojados, Cotizacion cotizacion, PoliticaCancelacion politica, LocalDateTime creadaEn) {
        super(id, titular, apartamento, entrada, salida, alojados, cotizacion, politica, creadaEn);
    }
    @Override public CanalReserva getCanal() { return CanalReserva.DIRECTO; }
    @Override public long anticipoRequerido() { return 0; }
    @Override public long comision() { return 0; }
}
