package co.edu.uniquindio.universidad.modelo;


import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.util.Validar;

import java.io.Serializable;
import java.time.LocalDateTime;

public final class Pago implements Movimiento, Serializable {
    private final MedioPago medio;
    private final long valor;
    private final LocalDateTime fecha;
    private final String descripcion;

    public Pago(MedioPago medio, long valor, LocalDateTime fecha, String descripcion) {
        if (valor == 0) throw new ReglaNegocioException("El pago no puede ser de valor cero");
        this.medio = Validar.noNulo(medio, "medio de pago");
        this.valor = valor;
        this.fecha = fecha;
        this.descripcion = descripcion == null ? "" : descripcion;
    }
    public MedioPago getMedio() { return medio; }
    @Override public long getValor() { return valor; }
    @Override public LocalDateTime getFecha() { return fecha; }
    @Override public String getDescripcion() { return descripcion; }
}
