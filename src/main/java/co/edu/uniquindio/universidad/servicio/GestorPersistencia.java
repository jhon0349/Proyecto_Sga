package co.edu.uniquindio.universidad.servicio;

import co.edu.uniquindio.universidad.modelo.Alojamiento;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persistencia por serialización: la información no se pierde al cerrar el programa. */
public final class GestorPersistencia {
    private GestorPersistencia() {}

    public static void guardar(Alojamiento alojamiento, Path archivo) {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(archivo))) {
            out.writeObject(alojamiento);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la información", e);
        }
    }

    public static Alojamiento cargar(Path archivo) {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(archivo))) {
            return (Alojamiento) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo cargar la información guardada", e);
        }
    }
}