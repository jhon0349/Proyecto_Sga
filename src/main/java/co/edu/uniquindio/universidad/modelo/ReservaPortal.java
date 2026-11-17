package co.edu.uniquindio.universidad.modelo;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Canal Portal: el huésped reserva solo; queda Pendiente hasta pagar un anticipo del 30 %. */
public class ReservaPortal extends Reserva {
    public ReservaPortal(String id, Huesped titular, Apartamento apartamento, LocalDate entrada, LocalDate salida,
                         List<Ocupante> alojados, Cotizacion cotizacion, PoliticaCancelacion politica, LocalDateTime creadaEn) {
        super(id, titular, apartamento, entrada, salida, alojados, cotizacion, politica, creadaEn);
    }
    @Override public CanalReserva getCanal() { return CanalReserva.PORTAL; }
    @Override public long anticipoRequerido() { return Reglas.porcentaje(getValorEstancia(), Reglas.PORCENTAJE_ANTICIPO_PORTAL); }
    @Override public long comision() { return 0; }
}
