package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.servicio.ServicioReportes;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicioReportesTest {
    private Fixture fx;
    private ServicioReportes rep;

    @BeforeEach void preparar() { fx = new Fixture(); rep = fx.sistema.reportes(); }

    @Test void ocupacionCuentaNochesDentroDelPeriodo() {
        Reserva r = fx.directa(fx.a101, 2, 4);
        fx.sistema.reservas().confirmar(r);
        var filas = rep.ocupacionPorApartamento(fx.hoy(), fx.hoy().plusDays(9)); // 10 noches
        var fila = filas.stream().filter(f -> f.apartamento() == fx.a101).findFirst().orElseThrow();
        assertEquals(4, fila.nochesOcupadas());
        assertEquals(10, fila.nochesPeriodo());
        assertEquals(40.0, fila.porcentaje(), 0.001);
    }
    @Test void ocupacionIgnoraReservasPendientesYCanceladas() {
        fx.directa(fx.a101, 2, 4); // Pendiente
        var fila = rep.ocupacionPorApartamento(fx.hoy(), fx.hoy().plusDays(9)).get(0);
        assertEquals(0, fila.nochesOcupadas());
    }
    @Test void ingresosSumanPagosYRestanDevolucionesYComisiones() {
        Reserva r = fx.directa(fx.a101, 5, 2);
        fx.sistema.reservas().registrarPago(r, MedioPago.EFECTIVO, 160_000);
        fx.sistema.reservas().cancelar(r); // retiene 80.000, devuelve 80.000
        fx.sistema.reservas().crearReservaExterna("BKG-9", fx.titular, fx.a102, fx.hoy().plusDays(8), fx.hoy().plusDays(10), List.of());
        var ing = rep.ingresosPorPeriodo(fx.hoy(), fx.hoy().plusDays(30));
        assertEquals(160_000, ing.cobrado());
        assertEquals(80_000, ing.devuelto());
        assertEquals(18_000, ing.comisiones()); // 15 % de 120.000
        assertEquals(62_000, ing.neto());
    }
    @Test void reservasPorCanalIncluyeCanceladasYNoShow() {
        fx.sistema.reservas().cancelar(fx.directa(fx.a101, 20, 1));
        fx.portal(fx.a102, 5, 1);
        var directo = rep.reservasPorCanal().stream().filter(f -> f.canal() == CanalReserva.DIRECTO).findFirst().orElseThrow();
        var portal = rep.reservasPorCanal().stream().filter(f -> f.canal() == CanalReserva.PORTAL).findFirst().orElseThrow();
        assertEquals(1, directo.de(EstadoReserva.CANCELADA));
        assertEquals(1, portal.total());
    }
    @Test void saldosPendientesListaFoliosConDeuda() {
        Reserva a = fx.directa(fx.a101, 5, 1);
        Reserva b = fx.directa(fx.a102, 5, 1);
        fx.sistema.reservas().registrarPago(b, MedioPago.EFECTIVO, 60_000); // salda b
        assertEquals(List.of(a), rep.saldosPendientes());
    }
    @Test void apartamentosConMasNovedadesOrdenaDescendente() {
        fx.sistema.inventario().registrarNovedad(fx.a102, "1", "r");
        fx.sistema.inventario().registrarNovedad(fx.a101, "1", "r");
        fx.sistema.inventario().registrarNovedad(fx.a101, "2", "r");
        assertEquals(List.of(fx.a101, fx.a102), rep.apartamentosConMasNovedades());
    }
}
