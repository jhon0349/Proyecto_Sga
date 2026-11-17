package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.modelo.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FolioTest {
    private final LocalDateTime t = LocalDateTime.of(2026, 11, 16, 10, 0);

    private Folio folio() {
        Folio f = new Folio();
        f.agregarCargo(new Cargo(TipoCargo.ALOJAMIENTO, "Alojamiento", 200_000, t));
        f.agregarCargo(new Cargo(TipoCargo.SERVICIO, "Desayuno", 30_000, t));
        return f;
    }
    @Test void saldoEsCargosMenosPagos() {
        Folio f = folio();
        f.agregarPago(new Pago(MedioPago.EFECTIVO, 50_000, t, "abono"));
        f.agregarPago(new Pago(MedioPago.TARJETA, 100_000, t, "abono"));
        assertEquals(230_000, f.totalCargos());
        assertEquals(150_000, f.totalPagos());
        assertEquals(80_000, f.saldo());
    }
    @Test void admiteVariosPagosConDistintoMedio() {
        Folio f = folio();
        f.agregarPago(new Pago(MedioPago.EFECTIVO, 1, t, "a"));
        f.agregarPago(new Pago(MedioPago.TRANSFERENCIA, 2, t, "b"));
        assertEquals(MedioPago.TRANSFERENCIA, f.getPagos().get(1).getMedio());
    }
    @Test void correccionEsMovimientoContrarioNoBorrado() {
        Folio f = folio();
        f.revertirCargo(1, t, "error");
        assertEquals(3, f.getCargos().size());
        assertEquals(200_000, f.totalCargos());
    }
    @Test void noSePuedeRevertirDosVecesNiRevertirUnReverso() {
        Folio f = folio();
        f.revertirCargo(1, t, "error");
        assertThrows(ReglaNegocioException.class, () -> f.revertirCargo(1, t, "otra vez"));
        assertThrows(ReglaNegocioException.class, () -> f.revertirCargo(2, t, "reverso"));
    }
    @Test void losMovimientosNoSeEliminanDesdeAfuera() {
        Folio f = folio();
        assertThrows(UnsupportedOperationException.class, () -> f.getCargos().remove(0));
        assertThrows(UnsupportedOperationException.class, () -> f.getPagos().clear());
    }
    @Test void valorCeroSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> new Cargo(TipoCargo.SERVICIO, "x", 0, t));
        assertThrows(ReglaNegocioException.class, () -> new Pago(MedioPago.EFECTIVO, 0, t, "x"));
    }
}