package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.excepciones.*;
import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.servicio.ServicioReservas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicioReservasTest {
    private Fixture fx;
    private ServicioReservas srv;

    @BeforeEach void preparar() { fx = new Fixture(); srv = fx.sistema.reservas(); }

    // ---------- Cotización ----------
    @Test void cotizacionLiquidaNochePorNocheCruzandoTemporadas() {
        fx.sistema.inventario().definirPeriodo(Temporada.ALTA, LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 31), "Fin de año");
        Cotizacion c = srv.cotizar(fx.a101, LocalDate.of(2026, 11, 29), LocalDate.of(2026, 12, 2), List.of(Ocupante.de(fx.titular), new Ocupante("Ana", LocalDate.of(1992, 1, 1))));
        assertEquals(3, c.getNumeroNoches());
        assertEquals(Temporada.BAJA, c.getNoches().get(1).temporada());
        assertEquals(Temporada.ALTA, c.getNoches().get(2).temporada());
        assertEquals(2 * 80_000 * 2 + 150_000 * 2, c.getTotal()); // 29 y 30 nov (baja) + 1 dic (alta), 2 pagantes
    }
    @Test void menoresDeSeisOcupanCupoPeroNoPagan() {
        List<Ocupante> g = List.of(Ocupante.de(fx.titular), new Ocupante("Bebé", LocalDate.of(2024, 5, 5)));
        Cotizacion c = srv.cotizar(fx.a101, fx.hoy().plusDays(5), fx.hoy().plusDays(7), g);
        assertEquals(2 * 80_000, c.getTotal()); // solo paga el titular
    }
    @Test void capacidadIncluyeATodosLosOcupantes() {
        List<Ocupante> cinco = List.of(Ocupante.de(fx.titular), new Ocupante("a", LocalDate.of(2024, 1, 1)), new Ocupante("b", LocalDate.of(2024, 1, 1)),
                new Ocupante("c", LocalDate.of(2024, 1, 1)), new Ocupante("d", LocalDate.of(2024, 1, 1)));
        assertThrows(CapacidadExcedidaException.class, () -> srv.cotizar(fx.a101, fx.hoy().plusDays(5), fx.hoy().plusDays(6), cinco));
    }
    @Test void capacidadSeValidaAlCrearLaReserva() {
        List<Ocupante> extra = List.of(new Ocupante("a", LocalDate.of(2000, 1, 1)), new Ocupante("b", LocalDate.of(2000, 1, 1)));
        assertThrows(CapacidadExcedidaException.class,
                () -> srv.crearReservaDirecta(fx.titular, fx.a102, fx.hoy().plusDays(3), fx.hoy().plusDays(4), extra)); // 3 personas, cupo 2
    }

    // ---------- Fechas ----------
    @Test void salidaDebeSerPosteriorAEntrada() {
        assertThrows(ReglaNegocioException.class, () -> srv.crearReservaDirecta(fx.titular, fx.a101, fx.hoy().plusDays(3), fx.hoy().plusDays(3), List.of()));
    }
    @Test void noSeReservaConEntradaAnteriorAHoy() {
        assertThrows(ReglaNegocioException.class, () -> srv.crearReservaDirecta(fx.titular, fx.a101, fx.hoy().minusDays(1), fx.hoy().plusDays(1), List.of()));
    }
    @Test void sePuedeReservarConEntradaHoy() { assertNotNull(fx.directa(fx.a101, 0, 1)); }

    // ---------- Doble venta ----------
    @Test void mismoApartamentoMismasNochesSeRechazaEnCualquierCanal() {
        fx.directa(fx.a101, 5, 3);
        assertThrows(DobleVentaException.class, () -> fx.portal(fx.a101, 6, 3));
    }
    @Test void estanciasContiguasSePermiten() {
        fx.directa(fx.a101, 5, 2);
        assertNotNull(fx.directa(fx.a101, 7, 2)); // sale el 7, entra el 7
    }
    @Test void otroApartamentoNoSeAfecta() {
        fx.directa(fx.a101, 5, 3);
        assertNotNull(fx.directa(fx.a102, 5, 3));
    }
    @Test void cancelarLiberaLasNoches() {
        Reserva r = fx.directa(fx.a101, 20, 3);
        srv.cancelar(r);
        assertNotNull(fx.directa(fx.a101, 20, 3));
    }
    @Test void bloqueoImpideVenderEsaNoche() {
        fx.sistema.inventario().bloquear(fx.a101, fx.hoy().plusDays(10), fx.hoy().plusDays(12), "Mantenimiento eléctrico");
        assertFalse(srv.estaDisponible(fx.a101, fx.hoy().plusDays(11), fx.hoy().plusDays(13)));
        assertThrows(ReglaNegocioException.class, () -> fx.directa(fx.a101, 9, 2));
        assertNotNull(fx.directa(fx.a101, 13, 2));
    }
    @Test void disponiblesFiltraPorCapacidadYFechas() {
        fx.directa(fx.a101, 5, 2);
        List<Apartamento> d = srv.disponibles(fx.hoy().plusDays(5), fx.hoy().plusDays(7), 2);
        assertEquals(List.of(fx.a102), d);
        assertTrue(srv.disponibles(fx.hoy().plusDays(5), fx.hoy().plusDays(7), 4).isEmpty());
    }

    // ---------- Ciclo de vida ----------
    @Test void portalSeConfirmaAlCompletarElAnticipo() {
        Reserva r = fx.portal(fx.a101, 5, 2); // anticipo 48.000
        srv.registrarPago(r, MedioPago.TARJETA, 40_000);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        srv.registrarPago(r, MedioPago.EFECTIVO, 8_000);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }
    @Test void recepcionNoConfirmaPortalSinAnticipo() {
        Reserva r = fx.portal(fx.a101, 5, 2);
        assertThrows(ReglaNegocioException.class, () -> srv.confirmar(r));
    }
    @Test void pendienteSinConfirmarVence24HorasYLiberaNoches() {
        Reserva r = fx.directa(fx.a101, 5, 2);
        fx.reloj.avanzarHoras(25);
        assertEquals(1, srv.liberarVencidas().size());
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertTrue(srv.estaDisponible(fx.a101, fx.hoy().plusDays(4), fx.hoy().plusDays(6)));
    }
    @Test void checkInExigeConfirmadaFechaYApartamentoPreparado() {
        Reserva r = fx.directa(fx.a101, 0, 2);
        assertThrows(TransicionInvalidaException.class, () -> srv.checkIn(r));       // aún Pendiente
        srv.confirmar(r);
        fx.a101.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION, null);
        assertThrows(ReglaNegocioException.class, () -> srv.checkIn(r));            // apto sin preparar
        fx.a101.cambiarEstado(EstadoApartamento.PREPARADO, null);
        srv.checkIn(r);
        assertEquals(EstadoReserva.EN_CURSO, r.getEstado());
        assertEquals(EstadoApartamento.OCUPADO, fx.a101.getEstado());
    }
    @Test void checkInAntesDeLaFechaDeEntradaSeRechaza() {
        Reserva r = fx.directa(fx.a101, 3, 2);
        srv.confirmar(r);
        assertThrows(ReglaNegocioException.class, () -> srv.checkIn(r));
    }
    @Test void checkOutFinalizaYDejaElApartamentoPendienteDePreparacion() {
        Reserva r = fx.directa(fx.a101, 0, 2);
        srv.confirmar(r);
        srv.checkIn(r);
        srv.checkOut(r);
        assertEquals(EstadoReserva.FINALIZADA, r.getEstado());
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, fx.a101.getEstado());
    }
    @Test void checkOutSinCheckInSeRechaza() {
        Reserva r = fx.directa(fx.a101, 0, 2);
        srv.confirmar(r);
        assertThrows(TransicionInvalidaException.class, () -> srv.checkOut(r));
    }
    @Test void noShowSoloDespuesDeLas10pm() {
        Reserva r = fx.directa(fx.a101, 0, 2);
        srv.registrarPago(r, MedioPago.EFECTIVO, 160_000);
        srv.confirmar(r);
        assertThrows(ReglaNegocioException.class, () -> srv.declararNoShow(r));
        fx.reloj.fijar(LocalDateTime.of(fx.hoy(), java.time.LocalTime.of(22, 30)));
        srv.declararNoShow(r);
        assertEquals(EstadoReserva.NO_SHOW, r.getEstado());
        assertEquals(160_000, r.getRetenido()); // 100 %
        assertEquals(0, r.getDevuelto());
    }

    // ---------- Cancelación ----------
    private Reserva pagada(int diasDesdeHoy) {
        Reserva r = fx.directa(fx.a101, diasDesdeHoy, 2); // 160.000
        srv.registrarPago(r, MedioPago.TRANSFERENCIA, 160_000);
        srv.confirmar(r);
        return r;
    }
    @Test void conMasDeSieteDiasSeDevuelveTodo() {
        Reserva r = pagada(10);
        ResultadoCancelacion previo = srv.calcularCancelacion(r);
        assertEquals(160_000, previo.devolucion());
        srv.cancelar(r);
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertEquals(160_000, r.getDevuelto());
        assertEquals(0, r.getFolio().saldo());
    }
    @Test void entreDosYSieteDiasSeRetieneLaMitad() {
        Reserva r = pagada(5);
        srv.cancelar(r);
        assertEquals(80_000, r.getRetenido());
        assertEquals(80_000, r.getDevuelto());
        assertEquals(0, r.getFolio().saldo());
    }
    @Test void conMenosDe48HorasSeRetieneTodo() {
        Reserva r = pagada(1);
        srv.cancelar(r);
        assertEquals(160_000, r.getRetenido());
        assertEquals(0, r.getDevuelto());
    }
    @Test void reservaFinalizadaNoSePuedeCancelar() {
        Reserva r = pagada(0);
        srv.checkIn(r);
        srv.checkOut(r);
        assertThrows(TransicionInvalidaException.class, () -> srv.cancelar(r));
    }
    @Test void valorYPoliticaQuedanCongelados() {
        Reserva r = fx.directa(fx.a101, 5, 2);
        fx.sistema.inventario().definirTarifa(fx.a101, Temporada.BAJA, 500_000);
        fx.sistema.inventario().configurarPolitica(30, 90, 72);
        assertEquals(160_000, r.getValorEstancia());
        assertEquals(7, r.getPolitica().getDiasDevolucionTotal());
        assertEquals(160_000, r.getFolio().totalCargos());
    }

    // ---------- Folio ----------
    @Test void pagoNoPuedeSuperarElSaldo() {
        Reserva r = fx.directa(fx.a101, 5, 2);
        assertThrows(ReglaNegocioException.class, () -> srv.registrarPago(r, MedioPago.EFECTIVO, 160_001));
    }
    @Test void variosPagosParcialesReducenElSaldo() {
        Reserva r = fx.directa(fx.a101, 5, 2);
        srv.registrarPago(r, MedioPago.EFECTIVO, 60_000);
        srv.registrarPago(r, MedioPago.TARJETA, 40_000);
        assertEquals(60_000, r.getFolio().saldo());
    }
    @Test void serviciosSoloSeCarganEnCurso() {
        Reserva r = fx.directa(fx.a101, 0, 2);
        assertThrows(ReglaNegocioException.class, () -> srv.registrarCargoServicio(r, "Desayuno", 20_000));
        srv.confirmar(r);
        srv.checkIn(r);
        srv.registrarCargoServicio(r, "Desayuno", 20_000);
        assertEquals(180_000, r.getFolio().saldo());
    }
    @Test void correccionDePagoEsMovimientoContrario() {
        Reserva r = fx.directa(fx.a101, 5, 2);
        srv.registrarPago(r, MedioPago.EFECTIVO, 50_000);
        srv.corregirPago(r, 0, "error de digitación");
        assertEquals(2, r.getFolio().getPagos().size());
        assertEquals(160_000, r.getFolio().saldo());
    }

    // ---------- Canal externo ----------
    @Test void externaSeConfirmaYRegistraSuCodigo() {
        Reserva r = srv.crearReservaExterna("BKG-1", fx.titular, fx.a101, fx.hoy().plusDays(5), fx.hoy().plusDays(7), List.of());
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }
    @Test void externaEnConflictoSeRechazaYSeRegistraSinReemplazarLaExistente() {
        Reserva existente = fx.directa(fx.a101, 5, 3);
        assertThrows(ConflictoCanalException.class,
                () -> srv.crearReservaExterna("BKG-2", fx.titular, fx.a101, fx.hoy().plusDays(6), fx.hoy().plusDays(8), List.of()));
        assertEquals(1, fx.alojamiento.getConflictos().size());
        assertEquals(EstadoReserva.PENDIENTE, existente.getEstado());
        assertEquals(1, fx.alojamiento.getReservas().size());
    }
    @Test void mismoCodigoExternoNoCreaDosReservas() {
        srv.crearReservaExterna("BKG-3", fx.titular, fx.a101, fx.hoy().plusDays(5), fx.hoy().plusDays(7), List.of());
        assertThrows(ReglaNegocioException.class,
                () -> srv.crearReservaExterna("BKG-3", fx.titular, fx.a102, fx.hoy().plusDays(20), fx.hoy().plusDays(22), List.of()));
    }
    @Test void reservasDeUnHuespedSoloIncluyenLasSuyas() {
        fx.directa(fx.a101, 5, 2);
        Huesped otro = fx.sistema.usuarios().obtenerOCrearHuesped("Otra", "999", LocalDate.of(1991, 1, 1), "300", "o@o.co");
        assertEquals(1, srv.reservasDe(fx.titular).size());
        assertTrue(srv.reservasDe(otro).isEmpty());
    }
}
