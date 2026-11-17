package co.edu.uniquindio.universidad.modelo;


import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;

import java.io.Serializable;
import java.time.Duration;

/**
 * Política de cancelación inmutable (por eso puede "congelarse" dentro de cada reserva).
 * Por defecto: más de 7 días -> devuelve todo; entre 48 h y 7 días -> retiene 50 %; menos de 48 h o no-show -> retiene 100 %.
 */
public final class PoliticaCancelacion implements Serializable {
    private final int diasDevolucionTotal;
    private final int porcentajeRetencionIntermedia;
    private final int horasRetencionTotal;

    public PoliticaCancelacion(int diasDevolucionTotal, int porcentajeRetencionIntermedia, int horasRetencionTotal) {
        if (diasDevolucionTotal <= 0 || horasRetencionTotal < 0 || horasRetencionTotal > diasDevolucionTotal * 24)
            throw new ReglaNegocioException("Los plazos de la política de cancelación no son coherentes");
        if (porcentajeRetencionIntermedia < 0 || porcentajeRetencionIntermedia > 100)
            throw new ReglaNegocioException("El porcentaje de retención debe estar entre 0 y 100");
        this.diasDevolucionTotal = diasDevolucionTotal;
        this.porcentajeRetencionIntermedia = porcentajeRetencionIntermedia;
        this.horasRetencionTotal = horasRetencionTotal;
    }
    public static PoliticaCancelacion porDefecto() { return new PoliticaCancelacion(7, 50, 48); }

    public int getDiasDevolucionTotal() { return diasDevolucionTotal; }
    public int getPorcentajeRetencionIntermedia() { return porcentajeRetencionIntermedia; }
    public int getHorasRetencionTotal() { return horasRetencionTotal; }

    public int porcentajeRetencion(Duration antelacion, boolean noShow) {
        if (noShow) return 100;
        if (antelacion.compareTo(Duration.ofDays(diasDevolucionTotal)) > 0) return 0;
        if (antelacion.compareTo(Duration.ofHours(horasRetencionTotal)) >= 0) return porcentajeRetencionIntermedia;
        return 100;
    }
    /** Valor retenido sobre el VALOR DE LA ESTANCIA. */
    public long retencion(long valorEstancia, Duration antelacion, boolean noShow) {
        return Reglas.porcentaje(valorEstancia, porcentajeRetencion(antelacion, noShow));
    }
}
