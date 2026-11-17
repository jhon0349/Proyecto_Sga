package co.edu.uniquindio.universidad.modelo;


import co.edu.uniquindio.universidad.modelo.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.util.Validar;

import java.io.Serializable;
import java.time.LocalDate;

public class PeriodoTemporada implements Serializable {
    private final Temporada temporada;
    private final LocalDate inicio;
    private final LocalDate fin;
    private final String descripcion;

    public PeriodoTemporada(Temporada temporada, LocalDate inicio, LocalDate fin, String descripcion) {
        this.temporada = Validar.noNulo(temporada, "temporada");
        if (temporada == Temporada.BAJA) throw new ReglaNegocioException("La temporada Baja es la base y no se define por fechas");
        this.inicio = Validar.noNulo(inicio, "fecha de inicio");
        this.fin = Validar.noNulo(fin, "fecha de fin");
        if (fin.isBefore(inicio)) throw new ReglaNegocioException("La fecha final del periodo no puede ser anterior a la inicial");
        this.descripcion = descripcion == null ? "" : descripcion.trim();
    }
    public Temporada getTemporada() { return temporada; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFin() { return fin; }
    public String getDescripcion() { return descripcion; }
    public boolean contiene(LocalDate f) { return !f.isBefore(inicio) && !f.isAfter(fin); }
}