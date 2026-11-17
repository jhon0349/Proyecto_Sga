package co.edu.uniquindio.universidad.modelo;


import java.time.Duration;
import java.time.LocalTime;

/** Constantes y utilidades de las reglas de negocio del enunciado. */
public final class Reglas {
    private Reglas() {}
    public static final int EDAD_MINIMA_PAGO = 6;
    public static final LocalTime HORA_ENTRADA = LocalTime.of(15, 0);
    public static final LocalTime HORA_SALIDA = LocalTime.of(11, 0);
    public static final LocalTime HORA_LIMITE_NO_SHOW = LocalTime.of(22, 0);
    public static final int HORAS_PREPARACION_MINIMA = 3;
    public static final int HORAS_PLAZO_CONFIRMACION = 24;
    public static final int PORCENTAJE_ANTICIPO_PORTAL = 30;
    public static final int PORCENTAJE_COMISION_EXTERNA = 15;

    /** Porcentaje en pesos enteros (redondeo half-up), sin decimales. */
    public static long porcentaje(long valor, int pct) { return (valor * pct + 50) / 100; }

    /** Entre la salida (11:00) y la siguiente entrada (15:00) deben pasar al menos 3 horas. */
    public static boolean preparacionSuficiente() {
        return Duration.between(HORA_SALIDA, HORA_ENTRADA).toHours() >= HORAS_PREPARACION_MINIMA;
    }
}
