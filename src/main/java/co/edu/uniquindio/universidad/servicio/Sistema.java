package co.edu.uniquindio.universidad.servicio;

import co.edu.uniquindio.universidad.modelo.Alojamiento;
import co.edu.uniquindio.universidad.modelo.DatosIniciales;
import co.edu.uniquindio.universidad.util.Reloj;
import co.edu.uniquindio.universidad.util.RelojSistema;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/** Fachada: ensambla el modelo y los servicios y gestiona la persistencia. */
public class Sistema {
    private final Alojamiento alojamiento;
    private final Reloj reloj;
    private final Path archivo;
    private final ServicioUsuarios usuarios;
    private final ServicioInventario inventario;
    private final ServicioReservas reservas;
    private final ServicioReportes reportes;
    private final SimuladorCanalExterno simulador;

    public Sistema(Alojamiento alojamiento, Reloj reloj, Path archivo) {
        this.alojamiento = alojamiento;
        this.reloj = reloj;
        this.archivo = archivo;
        this.usuarios = new ServicioUsuarios(alojamiento);
        this.inventario = new ServicioInventario(alojamiento, reloj);
        this.reservas = new ServicioReservas(alojamiento, inventario, reloj);
        this.reportes = new ServicioReportes(alojamiento);
        this.simulador = new SimuladorCanalExterno(usuarios, inventario, reservas, reloj, new Random());
    }

    /** Carga los datos guardados si existen; si no, arranca con los datos de demostración. */
    public static Sistema iniciar(Path archivo) {
        Alojamiento a;
        if (archivo != null && Files.exists(archivo)) {
            try { a = GestorPersistencia.cargar(archivo); }
            catch (RuntimeException e) { a = DatosIniciales.crear(); }
        } else {
            a = DatosIniciales.crear();
        }
        return new Sistema(a, new RelojSistema(), archivo);
    }

    public void guardar() { if (archivo != null) GestorPersistencia.guardar(alojamiento, archivo); }

    public Alojamiento getAlojamiento() { return alojamiento; }
    public Reloj getReloj() { return reloj; }
    public ServicioUsuarios usuarios() { return usuarios; }
    public ServicioInventario inventario() { return inventario; }
    public ServicioReservas reservas() { return reservas; }
    public ServicioReportes reportes() { return reportes; }
    public SimuladorCanalExterno simulador() { return simulador; }
}