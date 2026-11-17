package co.edu.uniquindio.universidad.modelo;

public enum Rol {
    HUESPED("Huésped"), RECEPCIONISTA("Recepcionista"), ADMINISTRADOR("Administrador");
    private final String etiqueta;
    Rol(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
    @Override public String toString() { return etiqueta; }
}
