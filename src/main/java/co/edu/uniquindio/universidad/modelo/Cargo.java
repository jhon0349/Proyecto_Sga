package co.edu.uniquindio.universidad.modelo;

import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.util.Validar;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Cargo inmutable. Un valor negativo representa un reverso (movimiento contrario). */
public final class Cargo implements Movimiento, Serializable {
    private final TipoCargo tipo;
    private final String descripcion;
    private final long valor;
    private final LocalDateTime fecha;

    public Cargo(TipoCargo tipo, String descripcion, long valor, LocalDateTime fecha) {
        if (valor == 0) throw new ReglaNegocioException("El cargo no puede ser de valor cero");
        this.tipo = Validar.noNulo(tipo, "tipo de cargo");
        this.descripcion = Validar.texto(descripcion, "descripción");
        this.valor = valor;
        this.fecha = fecha;
    }
    public TipoCargo getTipo() { return tipo; }
    @Override public String getDescripcion() { return descripcion; }
    @Override public long getValor() { return valor; }
    @Override public LocalDateTime getFecha() { return fecha; }
}
