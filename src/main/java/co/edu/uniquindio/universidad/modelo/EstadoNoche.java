package co.edu.uniquindio.universidad.modelo;

public enum EstadoNoche {
    LIBRE("Libre"), OCUPADA("Ocupada"), BLOQUEADA("Bloqueada");
    private final String etiqueta;
    EstadoNoche(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
    @Override public String toString() { return etiqueta; }
}