package co.edu.uniquindio.universidad.excepciones;

/** Se lanza cuando una operación viola una regla de negocio del SGA. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) { super(mensaje); }
}