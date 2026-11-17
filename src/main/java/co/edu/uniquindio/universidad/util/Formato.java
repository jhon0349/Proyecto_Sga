package co.edu.uniquindio.universidad.util;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formato {
    private Formato() {}
    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    /** Pesos colombianos, sin decimales. */
    public static String pesos(long valor) {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));
        return "$" + nf.format(valor);
    }
}
