package co.edu.uniquindio.universidad.modelo;


import co.edu.uniquindio.universidad.util.Validar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Canal Externo: plataforma de terceros con su propio código; el alojamiento le paga 15 % de comisión. */
public class ReservaExterna extends Reserva {
    private final String codigoExterno;

    public ReservaExterna(String codigoExterno, String id, Huesped titular, Apartamento apartamento, LocalDate entrada,
                          LocalDate salida, List<Ocupante> alojados, Cotizacion cotizacion,
                          PoliticaCancelacion politica, LocalDateTime creadaEn) {
        super(id, titular, apartamento, entrada, salida, alojados, cotizacion, politica, creadaEn);
        this.codigoExterno = Validar.texto(codigoExterno, "código externo");
    }
    public String getCodigoExterno() { return codigoExterno; }
    @Override public CanalReserva getCanal() { return CanalReserva.EXTERNO; }
    @Override public long anticipoRequerido() { return 0; }
    @Override public long comision() { return Reglas.porcentaje(getValorEstancia(), Reglas.PORCENTAJE_COMISION_EXTERNA); }
}
