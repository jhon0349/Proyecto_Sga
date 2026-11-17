package co.edu.uniquindio.universidad.modelo;

public enum Temporada {
    BAJA("Baja"), MEDIA("Media"), ALTA("Alta");
    private final String etiqueta;
    Temporada(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
    @Override public String toString() { return etiqueta; }
}
