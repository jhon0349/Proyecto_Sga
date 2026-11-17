package co.edu.uniquindio.universidad.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Registro de una reserva externa rechazada por chocar con una reserva vigente o un bloqueo. */
public record ConflictoCanal(String codigoExterno, String codigoApartamento, LocalDate entrada, LocalDate salida,
                             String motivo, LocalDateTime fecha) implements Serializable {}
