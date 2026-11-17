package co.edu.uniquindio.universidad.modelo;


import sga.util.Validar;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;


public abstract class Persona implements Autenticable, Serializable {
    private String nombre;
    private String documento;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private Credencial credencial; // opcional: huéspedes registrados por recepción no tienen acceso

    protected Persona(String nombre, String documento, LocalDate fechaNacimiento, String telefono, String correo) {
        setNombre(nombre);
        setDocumento(documento);
        setFechaNacimiento(fechaNacimiento);
        setTelefono(telefono);
        setCorreo(correo);
    }

    public final void setNombre(String v) { this.nombre = Validar.texto(v, "nombre"); }
    public final void setDocumento(String v) { this.documento = Validar.texto(v, "documento"); }
    public final void setFechaNacimiento(LocalDate v) { this.fechaNacimiento = Validar.noNulo(v, "fecha de nacimiento"); }
    public final void setTelefono(String v) { this.telefono = Validar.texto(v, "teléfono"); }
    public final void setCorreo(String v) { this.correo = Validar.correo(v); }

    public String getNombre() { return nombre; }
    public String getDocumento() { return documento; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }

    /** La edad nunca se almacena: se calcula a la fecha indicada. */
    public int edadA(LocalDate fecha) { return Period.between(fechaNacimiento, fecha).getYears(); }

    public void definirCredenciales(String usuario, String clave) { this.credencial = new Credencial(usuario, clave); }
    public void cambiarClave(String actual, String nueva) {
        Validar.noNulo(credencial, "credenciales").cambiarClave(actual, nueva);
    }
    @Override public String getUsuario() { return credencial == null ? null : credencial.getUsuario(); }
    @Override public boolean validarClave(String clave) { return credencial != null && credencial.verificar(clave); }

    @Override public String toString() { return nombre + " (" + documento + ")"; }
}
