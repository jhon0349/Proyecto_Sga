package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.servicio.Sistema;
import co.edu.uniquindio.universidad.util.RelojFijo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersistenciaYSimuladorTest {
    @Test void guardarYCargarConservaReservasFoliosYUsuarios() throws IOException {
        Path f = Files.createTempFile("sga", ".dat");
        try {
            Sistema s = new Sistema(DatosIniciales.crear(), new RelojFijo(LocalDateTime.of(2026, 11, 16, 10, 0)), f);
            Huesped h = (Huesped) s.usuarios().autenticar("huesped", "huesped123");
            Apartamento a = s.inventario().apartamentosActivos().get(0);
            Reserva r = s.reservas().crearReservaPortal(h, a, s.getReloj().hoy().plusDays(5), s.getReloj().hoy().plusDays(7), List.of());
            s.reservas().registrarPago(r, MedioPago.TARJETA, 10_000);
            s.guardar();

            Sistema s2 = Sistema.iniciar(f);
            assertEquals(1, s2.getAlojamiento().getReservas().size());
            Reserva r2 = s2.getAlojamiento().getReservas().get(0);
            assertEquals(r.getValorEstancia(), r2.getValorEstancia());
            assertEquals(10_000, r2.getFolio().totalPagos());
            assertEquals(Rol.ADMINISTRADOR, s2.usuarios().autenticar("admin", "admin123").getRol());
        } finally { Files.deleteIfExists(f); }
    }
    @Test void sinArchivoArrancaConDatosDeDemostracion() {
        Sistema s = Sistema.iniciar(Path.of("no-existe-" + System.nanoTime() + ".dat"));
        assertEquals(4, s.inventario().apartamentosActivos().size());
    }
    @Test void simuladorGeneraReservasSinRomperLaRegla() {
        Sistema s = new Sistema(DatosIniciales.crear(), new RelojFijo(LocalDateTime.of(2026, 11, 16, 10, 0)), null);
        int aceptadas = 0, rechazadas = 0;
        for (int i = 0; i < 80; i++) {
            String res = s.simulador().generarReservaAleatoria();
            if (res.startsWith("ACEPTADA")) aceptadas++; else rechazadas++;
        }
        assertTrue(aceptadas > 0);
        assertTrue(rechazadas >= s.getAlojamiento().getConflictos().size());
        // invariante: ninguna pareja de reservas activas del mismo apartamento comparte noches
        List<Reserva> rs = s.getAlojamiento().getReservas();
        for (int i = 0; i < rs.size(); i++) for (int j = i + 1; j < rs.size(); j++)
            if (rs.get(i).getApartamento() == rs.get(j).getApartamento())
                assertFalse(rs.get(i).chocaCon(rs.get(j).getEntrada(), rs.get(j).getSalida()));
    }
}