package co.edu.uniquindio.universidad.modelo;


import java.time.LocalDate;

/** Empleado del alojamiento. La desvinculación es lógica para conservar el historial. */
public abstract class Empleado extends Persona {
    private boolean activo = true;

    protected Empleado(String nombre, String documento, LocalDate fechaNacimiento, String telefono, String correo) {
        super(nombre, documento, fechaNacimiento, telefono, correo);
    }
    public boolean isActivo() { return activo; }
    public void desvincular() { this.activo = false; }
}
