package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.modelo.PoliticaCancelacion;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaCancelacionTest {
    private final PoliticaCancelacion p = PoliticaCancelacion.porDefecto();

    @Test void masDeSieteDiasDevuelveTodo() { assertEquals(0, p.porcentajeRetencion(Duration.ofDays(8), false)); }
    @Test void exactamenteSieteDiasRetieneCincuenta() { assertEquals(50, p.porcentajeRetencion(Duration.ofDays(7), false)); }
    @Test void entreDosYSieteDiasRetieneCincuenta() { assertEquals(50, p.porcentajeRetencion(Duration.ofDays(4), false)); }
    @Test void exactamente48HorasRetieneCincuenta() { assertEquals(50, p.porcentajeRetencion(Duration.ofHours(48), false)); }
    @Test void menosDe48HorasRetieneTodo() { assertEquals(100, p.porcentajeRetencion(Duration.ofHours(47), false)); }
    @Test void noShowRetieneTodoSinImportarLaAntelacion() { assertEquals(100, p.porcentajeRetencion(Duration.ofDays(30), true)); }
    @Test void retencionSobreElValorDeLaEstancia() { assertEquals(80_000, p.retencion(160_000, Duration.ofDays(4), false)); }
    @Test void politicaIncoherenteSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> new PoliticaCancelacion(2, 50, 100));
        assertThrows(ReglaNegocioException.class, () -> new PoliticaCancelacion(7, 120, 48));
    }
}