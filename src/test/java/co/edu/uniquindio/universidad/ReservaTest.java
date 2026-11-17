package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.excepciones.*;
import co.edu.uniquindio.universidad.modelo.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {
    private final Fixture fx = new Fixture();

    @Test void del10Al12SonDosNoches() {
        Reserva r = new ReservaDirecta("R", fx.titular, fx.a101, LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 12),
                List.of(Ocupante.de(fx.titular)), fx.sistema.reservas().cotizar(fx.a101, LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 12), List.of(Ocupante.de(fx.titular))),
                PoliticaCancelacion.porDefecto(), fx.reloj.ahora());
        assertEquals(2, r.noches());
        assertTrue(r.incluyeNoche(LocalDate.of(2027, 3, 11)));
        assertFalse(r.incluyeNoche(LocalDate.of(2027, 3, 12))); // la noche de salida no se ocupa
    }
    @Test void estanciasContiguasNoChocan() {
        Reserva r = fx.directa(fx.a101, 5, 2); // salida en hoy+7
        assertFalse(r.chocaCon(fx.hoy().plusDays(7), fx.hoy().plusDays(9)));
        assertTrue(r.chocaCon(fx.hoy().plusDays(6), fx.hoy().plusDays(9)));
    }
    @Test void anticipoPortalEsTreintaPorCiento() {
        Reserva r = fx.portal(fx.a101, 5, 2); // 160.000
        assertEquals(160_000, r.getValorEstancia());
        assertEquals(48_000, r.anticipoRequerido());
        assertEquals(CanalReserva.PORTAL, r.getCanal());
    }
    @Test void directaNoExigeAnticipoYNoTieneComision() {
        Reserva r = fx.directa(fx.a101, 5, 2);
        assertEquals(0, r.anticipoRequerido());
        assertEquals(0, r.comision());
    }
    @Test void externaPagaQuinceDeComision() {
        Reserva r = fx.sistema.reservas().crearReservaExterna("EXT-1", fx.titular, fx.a101, fx.hoy().plusDays(5), fx.hoy().plusDays(7), List.of());
        assertEquals(24_000, r.comision());
    }
    @Test void nuevaReservaNaceConCargoDeAlojamientoYEstadoPendiente() {
        Reserva r = fx.directa(fx.a101, 5, 2);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertEquals(160_000, r.getFolio().saldo());
    }
    @Test void transicionInvalidaSeRechaza() {
        Reserva r = fx.directa(fx.a101, 5, 2);
        assertThrows(TransicionInvalidaException.class, () -> r.cambiarEstado(EstadoReserva.FINALIZADA));
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }
    @Test void vence24HorasDespuesDeCreada() {
        Reserva r = fx.directa(fx.a101, 5, 2);
        assertFalse(r.venceConfirmacion(fx.reloj.ahora().plusHours(23)));
        assertTrue(r.venceConfirmacion(fx.reloj.ahora().plusHours(24)));
    }
    @Test void horariosDeEntradaYSalidaPermitenPreparacion() {
        assertTrue(Reglas.preparacionSuficiente());
        Reserva r = fx.directa(fx.a101, 5, 2);
        assertEquals(LocalDateTime.of(fx.hoy().plusDays(5), Reglas.HORA_ENTRADA), r.getFechaHoraEntrada());
    }
}
