package co.edu.uniquindio.universidad.modelo;

public enum EstadoReserva {
    PENDIENTE("Pendiente"), CONFIRMADA("Confirmada"), EN_CURSO("En curso"),
    FINALIZADA("Finalizada"), CANCELADA("Cancelada"), NO_SHOW("No-show");

    private final String etiqueta;
    EstadoReserva(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
    @Override public String toString() { return etiqueta; }

    public boolean puedePasarA(EstadoReserva nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == CONFIRMADA || nuevo == CANCELADA;
            case CONFIRMADA -> nuevo == EN_CURSO || nuevo == CANCELADA || nuevo == NO_SHOW;
            case EN_CURSO -> nuevo == FINALIZADA;
            default -> false; // Finalizada, Cancelada y No-show son estados finales
        };
    }
    public boolean esFinal() { return this == FINALIZADA || this == CANCELADA || this == NO_SHOW; }
    public boolean esActiva() { return this == PENDIENTE || this == CONFIRMADA || this == EN_CURSO; }
}
