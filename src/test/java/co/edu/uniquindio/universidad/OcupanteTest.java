package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.modelo.Ocupante;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OcupanteTest {
    private final LocalDate entrada = LocalDate.of(2026, 12, 10);

    @Test void menorDeSeisAniosNoPaga() { assertFalse(new Ocupante("Sofía", LocalDate.of(2023, 1, 1)).paga(entrada)); }
    @Test void mayorDeSeisPaga() { assertTrue(new Ocupante("Juan", LocalDate.of(2010, 1, 1)).paga(entrada)); }
    @Test void cumpleSeisElDiaDeEntradaPaga() { assertTrue(new Ocupante("Ana", LocalDate.of(2020, 12, 10)).paga(entrada)); }
    @Test void cumpleSeisAlDiaSiguienteNoPaga() { assertFalse(new Ocupante("Ana", LocalDate.of(2020, 12, 11)).paga(entrada)); }
    @Test void edadSeCalculaALaFecha() { assertEquals(5, new Ocupante("Ana", LocalDate.of(2020, 12, 11)).edadA(entrada)); }
    @Test void nombreVacioSeRechaza() { assertThrows(ReglaNegocioException.class, () -> new Ocupante(" ", LocalDate.of(2000, 1, 1))); }
    @Test void fechaNulaSeRechaza() { assertThrows(ReglaNegocioException.class, () -> new Ocupante("Luis", null)); }
}
