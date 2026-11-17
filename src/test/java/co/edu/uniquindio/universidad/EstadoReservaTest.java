package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.modelo.EstadoReserva;

import static org.junit.jupiter.api.Assertions.*;
import static co.edu.uniquindio.universidad.modelo.EstadoReserva.*;

class EstadoReservaTest {
    @Test void pendientePuedeConfirmarseOCancelarse() {
        assertTrue(PENDIENTE.puedePasarA(CONFIRMADA));
        assertTrue(PENDIENTE.puedePasarA(CANCELADA));
        assertFalse(PENDIENTE.puedePasarA(EN_CURSO));
    }
    @Test void confirmadaPuedeIrAEnCursoCanceladaONoShow() {
        assertTrue(CONFIRMADA.puedePasarA(EN_CURSO));
        assertTrue(CONFIRMADA.puedePasarA(CANCELADA));
        assertTrue(CONFIRMADA.puedePasarA(NO_SHOW));
        assertFalse(CONFIRMADA.puedePasarA(FINALIZADA));
    }
    @Test void enCursoSoloFinaliza() {
        assertTrue(EN_CURSO.puedePasarA(FINALIZADA));
        assertFalse(EN_CURSO.puedePasarA(CANCELADA));
    }
    @Test void estadosFinalesNoTienenSalida() {
        for (EstadoReserva f : new EstadoReserva[]{FINALIZADA, CANCELADA, NO_SHOW}) {
            assertTrue(f.esFinal());
            for (EstadoReserva otro : values()) assertFalse(f.puedePasarA(otro));
        }
    }
    @Test void soloPendienteConfirmadaYEnCursoSonActivas() {
        assertTrue(PENDIENTE.esActiva() && CONFIRMADA.esActiva() && EN_CURSO.esActiva());
        assertFalse(FINALIZADA.esActiva() || CANCELADA.esActiva() || NO_SHOW.esActiva());
    }
}