package co.edu.uniquindio.universidad.util;

import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;

public final class Validar {
    private Validar() {}
    public static String texto(String v, String campo) {
        if (v == null || v.isBlank()) throw new ReglaNegocioException("El campo '" + campo + "' es obligatorio");
        return v.trim();
    }
    public static <T> T noNulo(T v, String campo) {
        if (v == null) throw new ReglaNegocioException("El campo '" + campo + "' es obligatorio");
        return v;
    }
    public static long positivo(long v, String campo) {
        if (v <= 0) throw new ReglaNegocioException("El campo '" + campo + "' debe ser mayor que cero");
        return v;
    }
    public static String correo(String v) {
        String c = texto(v, "correo");
        if (!c.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new ReglaNegocioException("El correo no es válido");
        return c;
    }
}
