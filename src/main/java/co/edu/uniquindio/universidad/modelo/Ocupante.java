package co.edu.uniquindio.universidad.modelo;


import co.edu.uniquindio.universidad.util.Formato;
import co.edu.uniquindio.universidad.util.Validar;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;

/** Persona que se aloja. Guarda la fecha de nacimiento, nunca la edad. */
public class Ocupante implements Serializable {
    private final String nombre;
    private final LocalDate fechaNacimiento;

    public Ocupante(String nombre, LocalDate fechaNacimiento) {
        this.nombre = Validar.texto(nombre, "nombre del ocupante");
        this.fechaNacimiento = Validar.noNulo(fechaNacimiento, "fecha de nacimiento del ocupante");
    }
    public static Ocupante de(Persona p) { return new Ocupante(p.getNombre(), p.getFechaNacimiento()); }

    public String getNombre() { return nombre; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public int edadA(LocalDate fecha) { return Period.between(fechaNacimiento, fecha).getYears(); }
    /** Los menores de 6 años ocupan cupo pero no pagan. La edad se calcula a la fecha de entrada. */
    public boolean paga(LocalDate fechaEntrada) { return edadA(fechaEntrada) >= Reglas.EDAD_MINIMA_PAGO; }
    @Override public String toString() { return nombre + " (nac. " + fechaNacimiento.format(Formato.FECHA) + ")"; }
}
