package co.edu.uniquindio.universidad.modelo;

public enum TipoCargo {
    ALOJAMIENTO("Alojamiento"), SERVICIO("Servicio adicional"), PENALIDAD("Penalidad");
    private final String etiqueta;
    TipoCargo(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
    @Override public String toString() { return etiqueta; }
}
