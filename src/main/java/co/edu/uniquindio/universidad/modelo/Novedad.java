package co.edu.uniquindio.universidad.modelo;

import sga.util.Validar;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Daño, faltante o situación reportada en un apartamento. */
public class Novedad implements Serializable {
    private final LocalDateTime fecha;
    private final String descripcion;
    private final String autor;

    public Novedad(LocalDateTime fecha, String descripcion, String autor) {
        this.fecha = fecha;
        this.descripcion = Validar.texto(descripcion, "descripción de la novedad");
        this.autor = autor;
    }
    public LocalDateTime getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public String getAutor() { return autor; }
}
