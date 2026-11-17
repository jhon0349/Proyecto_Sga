package co.edu.uniquindio.universidad.servicio;

import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.util.Reloj;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Simula una plataforma de terceros que envía reservas aleatorias (prueba la detección de conflictos de canal). */
public class SimuladorCanalExterno {
    private static final String[] NOMBRES = {"Ana Torres", "Luis Gómez", "Marta Ríos", "Pedro Salazar", "Sofía Vargas", "Juan Mejía"};
    private final ServicioUsuarios usuarios;
    private final ServicioInventario inventario;
    private final ServicioReservas reservas;
    private final Reloj reloj;
    private final Random azar;
    private int secuencia = 1000;

    public SimuladorCanalExterno(ServicioUsuarios u, ServicioInventario i, ServicioReservas r, Reloj reloj, Random azar) {
        this.usuarios = u; this.inventario = i; this.reservas = r; this.reloj = reloj; this.azar = azar;
    }

    /** @return descripción del resultado (aceptada o rechazada por conflicto). */
    public String generarReservaAleatoria() {
        List<Apartamento> activos = inventario.apartamentosActivos();
        if (activos.isEmpty()) return "No hay apartamentos para simular";
        Apartamento apto = activos.get(azar.nextInt(activos.size()));
        LocalDate entrada = reloj.hoy().plusDays(1 + azar.nextInt(30));
        LocalDate salida = entrada.plusDays(1 + azar.nextInt(4));
        String nombre = NOMBRES[azar.nextInt(NOMBRES.length)];
        String doc = "EXT" + (100000 + azar.nextInt(900000));
        Huesped titular = usuarios.obtenerOCrearHuesped(nombre, doc, LocalDate.of(1980 + azar.nextInt(20), 1 + azar.nextInt(12), 1 + azar.nextInt(28)),
                "3000000000", nombre.split(" ")[0].toLowerCase() + "@plataforma.co");
        List<Ocupante> extra = new ArrayList<>();
        int acompanantes = azar.nextInt(Math.min(3, apto.getCapacidad()));
        for (int k = 0; k < acompanantes; k++) extra.add(new Ocupante("Acompañante " + (k + 1), LocalDate.of(2000 + azar.nextInt(24), 6, 15)));
        String codigo = "EXT-" + (++secuencia);
        try {
            Reserva r = reservas.crearReservaExterna(codigo, titular, apto, entrada, salida, extra);
            return "ACEPTADA " + codigo + " → " + r.getId() + " (" + apto.getCodigo() + ", " + entrada + " a " + salida + ")";
        } catch (ReglaNegocioException e) {
            return "RECHAZADA " + codigo + " (" + apto.getCodigo() + ", " + entrada + " a " + salida + "): " + e.getMessage();
        }
    }
}
