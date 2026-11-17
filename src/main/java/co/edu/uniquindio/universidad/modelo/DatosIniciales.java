package co.edu.uniquindio.universidad.modelo;

import java.time.LocalDate;
import java.util.List;

/** Datos de demostración para el primer arranque. Usuarios: admin/admin123, recep/recep123, huesped/huesped123. */
public final class DatosIniciales {
    private DatosIniciales() {}

    public static Alojamiento crear() {
        Alojamiento a = new Alojamiento();

        Administrador admin = new Administrador("Administrador General", "1000000001", LocalDate.of(1985, 5, 10), "3100000001", "admin@apartasuitesuq.co");
        admin.definirCredenciales("admin", "admin123");
        a.agregarEmpleado(admin);
        Recepcionista rec = new Recepcionista("Laura Recepción", "1000000002", LocalDate.of(1995, 3, 2), "3100000002", "recepcion@apartasuitesuq.co");
        rec.definirCredenciales("recep", "recep123");
        a.agregarEmpleado(rec);
        Huesped h = new Huesped("Carlos Huésped", "1000000003", LocalDate.of(1990, 8, 21), "3100000003", "carlos@correo.co");
        h.definirCredenciales("huesped", "huesped123");
        a.agregarHuesped(h);

        Apartamento a101 = new Apartamento("A101", "Colibrí", 2, 4, 80_000, 110_000, 150_000);
        a101.setCaracteristicas(List.of("Balcón", "Vista a las montañas"));
        Apartamento a102 = new Apartamento("A102", "Palma de Cera", 1, 2, 60_000, 85_000, 120_000);
        a102.setCaracteristicas(List.of("Jacuzzi"));
        Apartamento a201 = new Apartamento("A201", "Cóndor", 3, 6, 90_000, 125_000, 170_000);
        a201.setCaracteristicas(List.of("Chimenea", "Balcón", "Vista a las montañas"));
        Apartamento a202 = new Apartamento("A202", "Cocora", 2, 5, 75_000, 100_000, 140_000);
        a202.setCaracteristicas(List.of("Terraza privada"));
        for (Apartamento ap : List.of(a101, a102, a201, a202)) a.agregarApartamento(ap);

        a.agregarPeriodo(new PeriodoTemporada(Temporada.MEDIA, LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 16), "Puente festivo"));
        a.agregarPeriodo(new PeriodoTemporada(Temporada.ALTA, LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 31), "Diciembre - Enero"));
        a.agregarPeriodo(new PeriodoTemporada(Temporada.ALTA, LocalDate.of(2027, 3, 21), LocalDate.of(2027, 3, 28), "Semana Santa"));
        a.agregarPeriodo(new PeriodoTemporada(Temporada.ALTA, LocalDate.of(2027, 6, 15), LocalDate.of(2027, 7, 15), "Vacaciones de mitad de año"));
        return a;
    }
}
