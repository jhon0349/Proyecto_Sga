package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.servicio.ServicioReportes;
import co.edu.uniquindio.universidad.servicio.ServicioReservas;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Funcionalidad extendida: estadísticas por temporada, comportamiento de cancelaciones e historial por huésped. */
class ReportesExtendidosTest {
    private Fixture fx;
    private ServicioReportes rep;
    private ServicioReservas srv;

    @BeforeEach void preparar() { fx = new Fixture(); rep = fx.sistema.reportes(); srv = fx.sistema.reservas(); }

    @Test void ocupacionPorTemporadaSumaNochesEIngresosDeReservasVigentes() {
        Reserva r = fx.directa(fx.a101, 3, 2);   // temporada baja: 2 noches x 1 pagante x 80.000
        srv.confirmar(r);
        long noches = rep.ocupacionPorTemporada().stream().mapToLong(ServicioReportes.FilaTemporada::nochesVendidas).sum();
        long ingresos = rep.ocupacionPorTemporada().stream().mapToLong(ServicioReportes.FilaTemporada::ingresos).sum();
        assertEquals(2, noches);
        assertEquals(r.getValorEstancia(), ingresos);
    }
    @Test void ocupacionPorTemporadaIgnoraPendientesYCanceladas() {
        fx.directa(fx.a101, 3, 2);                        // Pendiente
        srv.cancelar(fx.directa(fx.a102, 3, 2));          // Cancelada
        assertEquals(0, rep.ocupacionPorTemporada().stream().mapToLong(ServicioReportes.FilaTemporada::nochesVendidas).sum());
    }
    @Test void estadisticasDeCancelacionContabilizanRetencionYDevolucion() {
        Reserva r = fx.directa(fx.a101, 5, 2);
        srv.registrarPago(r, MedioPago.EFECTIVO, 160_000);
        srv.cancelar(r);                                  // entre 2 y 7 días: retiene 50 %
        fx.directa(fx.a102, 20, 1);                       // sigue Pendiente
        var c = rep.estadisticasCancelaciones();
        assertEquals(2, c.totalReservas());
        assertEquals(1, c.canceladas());
        assertEquals(0, c.noShow());
        assertEquals(80_000, c.retenido());
        assertEquals(80_000, c.devuelto());
        assertEquals(50.0, c.porcentajeCancelacion(), 0.001);
    }
    @Test void estadisticasSinReservasNoDividenPorCero() {
        assertEquals(0.0, rep.estadisticasCancelaciones().porcentajeCancelacion(), 0.001);
    }
    @Test void historialSoloIncluyeEstanciasFinalizadasDelHuesped() {
        Reserva hecha = fx.directa(fx.a101, 0, 2);
        srv.confirmar(hecha); srv.checkIn(hecha); srv.checkOut(hecha);
        fx.directa(fx.a102, 10, 1);                       // futura: no es historial
        Huesped otro = fx.sistema.usuarios().obtenerOCrearHuesped("Ana Gómez", "456", java.time.LocalDate.of(1995, 5, 5), "3000000000", "ana@mail.co");
        assertEquals(List.of(hecha), rep.historialDe(fx.titular));
        assertTrue(rep.historialDe(otro).isEmpty());
    }
}