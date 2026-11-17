package co.edu.uniquindio.universidad.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Contrato de acceso al tiempo; permite probar reglas dependientes de la hora. */
public interface Reloj {
    LocalDateTime ahora();
    default LocalDate hoy() { return ahora().toLocalDate(); }
}