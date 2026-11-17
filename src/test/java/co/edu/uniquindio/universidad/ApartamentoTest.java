package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.modelo.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApartamentoTest {
    private Apartamento nuevo() { return new Apartamento("A1", "Test", 2, 4, 80_000, 100_000, 150_000); }

    @Test void nuevoApartamentoEstaPreparadoYActivo() {
        Apartamento a = nuevo();
        assertEquals(EstadoApartamento.PREPARADO, a.getEstado());
        assertTrue(a.isActivo());
    }
    @Test void fueraDeServicioExigeMotivo() {
        assertThrows(ReglaNegocioException.class, () -> nuevo().cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO, " "));
    }
    @Test void fueraDeServicioGuardaMotivoYLoLimpiaAlSalir() {
        Apartamento a = nuevo();
        a.cambiarEstado(EstadoApartamento.FUERA_DE_SERVICIO, "Daño en la tubería");
        assertEquals("Daño en la tubería", a.getMotivoFueraServicio());
        a.cambiarEstado(EstadoApartamento.PREPARADO, null);
        assertNull(a.getMotivoFueraServicio());
    }
    @Test void tarifaPorTemporada() {
        Apartamento a = nuevo();
        assertEquals(80_000, a.getTarifa(Temporada.BAJA));
        assertEquals(150_000, a.getTarifa(Temporada.ALTA));
    }
    @Test void capacidadYTarifaInvalidasSeRechazan() {
        assertThrows(ReglaNegocioException.class, () -> new Apartamento("A", "x", 1, 0, 1, 1, 1));
        assertThrows(ReglaNegocioException.class, () -> nuevo().setTarifa(Temporada.BAJA, -5));
    }
    @Test void eliminacionLogicaConservaElObjeto() {
        Apartamento a = nuevo();
        a.eliminarLogicamente();
        assertFalse(a.isActivo());
    }
    @Test void novedadesSeAcumulanYLaListaEsDeSoloLectura() {
        Apartamento a = nuevo();
        a.registrarNovedad(new Novedad(LocalDateTime.now(), "Falta una toalla", "recep"));
        assertEquals(1, a.getNovedades().size());
        assertThrows(UnsupportedOperationException.class, () -> a.getNovedades().clear());
    }
    @Test void caracteristicasSeNormalizan() {
        Apartamento a = nuevo();
        a.setCaracteristicas(List.of(" Balcón ", "", "Jacuzzi"));
        assertEquals(List.of("Balcón", "Jacuzzi"), a.getCaracteristicas());
    }
}
