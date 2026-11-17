package co.edu.uniquindio.universidad.modelo;

import sga.excepciones.ReglaNegocioException;
import sga.util.Validar;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Cuenta de la reserva. Los movimientos no se modifican ni se eliminan:
 * solo se agregan; una corrección se registra como movimiento contrario.
 */
public class Folio implements Serializable {
    private final List<Cargo> cargos = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();
    private final Set<Integer> cargosRevertidos = new HashSet<>();
    private final Set<Integer> pagosRevertidos = new HashSet<>();

    public void agregarCargo(Cargo c) { cargos.add(Validar.noNulo(c, "cargo")); }
    public void agregarPago(Pago p) { pagos.add(Validar.noNulo(p, "pago")); }
    public List<Cargo> getCargos() { return Collections.unmodifiableList(cargos); }
    public List<Pago> getPagos() { return Collections.unmodifiableList(pagos); }

    public long totalCargos() { return cargos.stream().mapToLong(Cargo::getValor).sum(); }
    public long totalPagos() { return pagos.stream().mapToLong(Pago::getValor).sum(); }
    public long saldo() { return totalCargos() - totalPagos(); }

    public void revertirCargo(int indice, LocalDateTime fecha, String motivo) {
        Cargo o = cargos.get(indice);
        if (o.getValor() < 0 || cargosRevertidos.contains(indice)) throw new ReglaNegocioException("Ese cargo no se puede revertir");
        cargos.add(new Cargo(o.getTipo(), "Reverso: " + o.getDescripcion() + " (" + motivo + ")", -o.getValor(), fecha));
        cargosRevertidos.add(indice);
    }
    public void revertirPago(int indice, LocalDateTime fecha, String motivo) {
        Pago o = pagos.get(indice);
        if (o.getValor() < 0 || pagosRevertidos.contains(indice)) throw new ReglaNegocioException("Ese pago no se puede revertir");
        pagos.add(new Pago(o.getMedio(), -o.getValor(), fecha, "Reverso: " + o.getDescripcion() + " (" + motivo + ")"));
        pagosRevertidos.add(indice);
    }
    /** Reversa los cargos de alojamiento aún vigentes (usado al cancelar). */
    public void revertirAlojamiento(LocalDateTime fecha, String motivo) {
        for (int i = 0; i < cargos.size(); i++) {
            Cargo c = cargos.get(i);
            if (c.getTipo() == TipoCargo.ALOJAMIENTO && c.getValor() > 0 && !cargosRevertidos.contains(i))
                revertirCargo(i, fecha, motivo);
        }
    }
}
