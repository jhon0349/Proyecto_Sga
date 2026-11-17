package co.edu.uniquindio.universidad.modelo;

public enum CanalReserva {
    PORTAL("Portal"), DIRECTO("Directo"), EXTERNO("Externo");
    private final String etiqueta;
    CanalReserva(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
    @Override public String toString() { return etiqueta; }
}
