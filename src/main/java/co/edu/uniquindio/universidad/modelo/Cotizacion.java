package co.edu.uniquindio.universidad.modelo;

import java.io.Serializable;
import java.util.List;

public class Cotizacion implements Serializable {
    private final List<NocheLiquidada> noches;
    public Cotizacion(List<NocheLiquidada> noches) { this.noches = List.copyOf(noches); }
    public List<NocheLiquidada> getNoches() { return noches; }
    public int getNumeroNoches() { return noches.size(); }
    public long getTotal() { return noches.stream().mapToLong(NocheLiquidada::subtotal).sum(); }
}
