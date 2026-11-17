package co.edu.uniquindio.universidad.util;

import java.time.LocalDateTime;

/** Reloj controlable, usado en las pruebas unitarias. */
public class RelojFijo implements Reloj {
    private LocalDateTime instante;
    public RelojFijo(LocalDateTime instante) { this.instante = instante; }
    public void fijar(LocalDateTime nuevo) { this.instante = nuevo; }
    public void avanzarHoras(long horas) { this.instante = instante.plusHours(horas); }
    @Override public LocalDateTime ahora() { return instante; }
}