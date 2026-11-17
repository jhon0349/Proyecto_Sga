package co.edu.uniquindio.universidad.modelo;

public enum MedioPago {
    EFECTIVO("Efectivo"), TARJETA("Tarjeta"), TRANSFERENCIA("Transferencia");
    private final String etiqueta;
    MedioPago(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
    @Override public String toString() { return etiqueta; }
}
