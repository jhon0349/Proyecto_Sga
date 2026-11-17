package co.edu.uniquindio.universidad.modelo;

import java.time.LocalDate;

public class Huesped extends Persona {
    private String fotoRuta; // opcional: verificación de identidad

    public Huesped(String nombre, String documento, LocalDate fechaNacimiento, String telefono, String correo) {
        super(nombre, documento, fechaNacimiento, telefono, correo);
    }
    public String getFotoRuta() { return fotoRuta; }
    public void setFotoRuta(String fotoRuta) { this.fotoRuta = fotoRuta; }
    @Override public Rol getRol() { return Rol.HUESPED; }
}
