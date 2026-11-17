package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.servicio.ServicioInventario;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicioInventarioTest {
    private Fixture fx;
    private ServicioInventario inv;

    @BeforeEach void preparar() { fx = new Fixture(); inv = fx.sistema.inventario(); }

    @Test void sinPeriodosTodoEsTemporadaBaja() { assertEquals(Temporada.BAJA, inv.temporadaDe(LocalDate.of(2026, 5, 5))); }
    @Test void periodoMediaYAltaSeAplican() {
        inv.definirPeriodo(Temporada.MEDIA, LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 16), "Puente");
        inv.definirPeriodo(Temporada.ALTA, LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 31), "Fin de año");
        assertEquals(Temporada.MEDIA, inv.temporadaDe(LocalDate.of(2026, 11, 14)));
        assertEquals(Temporada.ALTA, inv.temporadaDe(LocalDate.of(2026, 12, 25)));
        assertEquals(Temporada.BAJA, inv.temporadaDe(LocalDate.of(2026, 11, 20)));
    }
    @Test void altaPrevaleceSobreMediaCuandoSeSuperponen() {
        inv.definirPeriodo(Temporada.MEDIA, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), "x");
        inv.definirPeriodo(Temporada.ALTA, LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 31), "y");
        assertEquals(Temporada.ALTA, inv.temporadaDe(LocalDate.of(2026, 12, 25)));
    }
    @Test void bajaNoSeDefinePorFechas() {
        assertThrows(ReglaNegocioException.class, () -> inv.definirPeriodo(Temporada.BAJA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), "x"));
    }
    @Test void codigoDeApartamentoDebeSerUnico() {
        assertThrows(ReglaNegocioException.class, () -> inv.crearApartamento("A101", "Otro", 1, 2, List.of(), 1, 1, 1));
    }
    @Test void eliminacionLogicaBloqueadaConReservasActivas() {
        fx.directa(fx.a101, 5, 2);
        assertThrows(ReglaNegocioException.class, () -> inv.eliminarApartamento(fx.a101));
        inv.eliminarApartamento(fx.a102);
        assertEquals(List.of(fx.a101), inv.apartamentosActivos());
    }
    @Test void bloqueoNoPuedeChocarConReservaActiva() {
        fx.directa(fx.a101, 5, 3);
        assertThrows(ReglaNegocioException.class, () -> inv.bloquear(fx.a101, fx.hoy().plusDays(6), fx.hoy().plusDays(7), "Daño"));
        assertNotNull(inv.bloquear(fx.a101, fx.hoy().plusDays(20), fx.hoy().plusDays(22), "Pintura"));
    }
    @Test void ocupadoSoloLoGestionanCheckInYCheckOut() {
        assertThrows(ReglaNegocioException.class, () -> inv.cambiarEstado(fx.a101, EstadoApartamento.OCUPADO, null));
        Reserva r = fx.directa(fx.a102, 0, 1);
        fx.sistema.reservas().confirmar(r);
        fx.sistema.reservas().checkIn(r);
        assertThrows(ReglaNegocioException.class, () -> inv.cambiarEstado(fx.a102, EstadoApartamento.PREPARADO, null));
    }
    @Test void recepcionCambiaEstadoOperativo() {
        inv.cambiarEstado(fx.a101, EstadoApartamento.EN_PREPARACION, null);
        inv.cambiarEstado(fx.a101, EstadoApartamento.PREPARADO, null);
        assertEquals(EstadoApartamento.PREPARADO, fx.a101.getEstado());
    }
    @Test void novedadQuedaRegistradaConAutor() {
        Novedad n = inv.registrarNovedad(fx.a101, "Bombillo dañado", "recep");
        assertEquals("recep", n.getAutor());
        assertEquals(1, fx.a101.getNovedades().size());
    }
    @Test void politicaConfigurableSoloAfectaReservasNuevas() {
        inv.configurarPolitica(10, 60, 24);
        assertEquals(10, fx.alojamiento.getPolitica().getDiasDevolucionTotal());
    }
}
