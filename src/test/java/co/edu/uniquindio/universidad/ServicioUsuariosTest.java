package co.edu.uniquindio.universidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.servicio.ServicioUsuarios;
import co.edu.uniquindio.universidad.util.RelojFijo;
import co.edu.uniquindio.universidad.servicio.Sistema;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicioUsuariosTest {
    private ServicioUsuarios us;

    @BeforeEach void preparar() {
        Sistema s = new Sistema(DatosIniciales.crear(), new RelojFijo(LocalDateTime.of(2026, 11, 16, 10, 0)), null);
        us = s.usuarios();
    }

    @Test void cadaRolIniciaSesionConSuUsuario() {
        assertEquals(Rol.ADMINISTRADOR, us.autenticar("admin", "admin123").getRol());
        assertEquals(Rol.RECEPCIONISTA, us.autenticar("recep", "recep123").getRol());
        assertEquals(Rol.HUESPED, us.autenticar("huesped", "huesped123").getRol());
    }
    @Test void claveIncorrectaSeRechaza() { assertThrows(ReglaNegocioException.class, () -> us.autenticar("admin", "mala")); }
    @Test void usuarioInexistenteSeRechaza() { assertThrows(ReglaNegocioException.class, () -> us.autenticar("nadie", "1234")); }
    @Test void registroDeHuespedYLogin() {
        us.registrarHuesped("Ana Ríos", "555", LocalDate.of(1995, 4, 4), "3011111111", "ana@mail.co", "ana", "clave1");
        assertEquals("Ana Ríos", ((Persona) us.autenticar("ana", "clave1")).getNombre());
    }
    @Test void documentoYUsuarioNoSeRepiten() {
        assertThrows(ReglaNegocioException.class, () -> us.registrarHuesped("X", "1000000003", LocalDate.of(1990, 1, 1), "3", "x@x.co", "otro", "clave1"));
        assertThrows(ReglaNegocioException.class, () -> us.registrarHuesped("Y", "777", LocalDate.of(1990, 1, 1), "3", "y@y.co", "admin", "clave1"));
    }
    @Test void correoInvalidoSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> us.registrarHuesped("Z", "888", LocalDate.of(1990, 1, 1), "3", "sin-arroba", "zeta", "clave1"));
    }
    @Test void contratarEditarYDesvincularRecepcionista() {
        Recepcionista r = us.contratarRecepcionista("Pedro", "321", LocalDate.of(1998, 2, 2), "3022222222", "pedro@mail.co", "pedro", "clave1");
        us.editarEmpleado(r, "Pedro Gómez", "3033333333", "pg@mail.co");
        assertEquals("Pedro Gómez", r.getNombre());
        us.desvincular(r);
        assertThrows(ReglaNegocioException.class, () -> us.autenticar("pedro", "clave1")); // ya no entra
    }
    @Test void elAdministradorNoSePuedeDesvincular() {
        Empleado admin = (Empleado) us.autenticar("admin", "admin123");
        assertThrows(ReglaNegocioException.class, () -> us.desvincular(admin));
    }
    @Test void huespedSinCredencialesNoPuedeIniciarSesion() {
        us.obtenerOCrearHuesped("Sin Acceso", "444", LocalDate.of(1990, 1, 1), "3", "s@s.co");
        assertThrows(ReglaNegocioException.class, () -> us.autenticar(null, "x"));
    }
}
