package co.edu.uniquindio.universidad;

import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.servicio.Sistema;
import co.edu.uniquindio.universidad.util.RelojFijo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Escenario base compartido por las pruebas: hoy = 16/11/2026 10:00, dos apartamentos y un huésped. */
class Fixture {
    final RelojFijo reloj = new RelojFijo(LocalDateTime.of(2026, 11, 16, 10, 0));
    final Alojamiento alojamiento = new Alojamiento();
    final Sistema sistema = new Sistema(alojamiento, reloj, null);
    final Apartamento a101;   // capacidad 4: 80.000 / 110.000 / 150.000
    final Apartamento a102;   // capacidad 2: 60.000 /  85.000 / 120.000
    final Huesped titular;

    Fixture() {
        a101 = sistema.inventario().crearApartamento("A101", "Colibrí", 2, 4, List.of("Balcón"), 80_000, 110_000, 150_000);
        a102 = sistema.inventario().crearApartamento("A102", "Palma", 1, 2, List.of("Jacuzzi"), 60_000, 85_000, 120_000);
        titular = sistema.usuarios().obtenerOCrearHuesped("Carlos Pérez", "123", LocalDate.of(1990, 1, 1), "3001112222", "carlos@mail.co");
    }
    LocalDate hoy() { return reloj.hoy(); }
    Reserva directa(Apartamento a, int diasDesdeHoy, int noches) {
        return sistema.reservas().crearReservaDirecta(titular, a, hoy().plusDays(diasDesdeHoy), hoy().plusDays(diasDesdeHoy + noches), List.of());
    }
    Reserva portal(Apartamento a, int diasDesdeHoy, int noches) {
        return sistema.reservas().crearReservaPortal(titular, a, hoy().plusDays(diasDesdeHoy), hoy().plusDays(diasDesdeHoy + noches), List.of());
    }
}