package co.edu.uniquindio.universidad.servicio;

import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.util.Validar;

import java.time.LocalDate;

/** Autenticación, huéspedes y gestión de empleados. */
public class ServicioUsuarios {
    private final Alojamiento alojamiento;

    public ServicioUsuarios(Alojamiento alojamiento) { this.alojamiento = alojamiento; }

    public Autenticable autenticar(String usuario, String clave) {
        if (usuario != null) {
            for (Huesped h : alojamiento.getHuespedes())
                if (usuario.equals(h.getUsuario()) && h.validarClave(clave)) return h;
            for (Empleado e : alojamiento.getEmpleados())
                if (e.isActivo() && usuario.equals(e.getUsuario()) && e.validarClave(clave)) return e;
        }
        throw new ReglaNegocioException("Usuario o contraseña incorrectos");
    }

    public boolean usuarioExiste(String usuario) {
        return alojamiento.getHuespedes().stream().anyMatch(h -> usuario.equals(h.getUsuario()))
                || alojamiento.getEmpleados().stream().anyMatch(e -> usuario.equals(e.getUsuario()));
    }

    public Huesped buscarHuesped(String documento) {
        return alojamiento.getHuespedes().stream().filter(h -> h.getDocumento().equals(documento)).findFirst().orElse(null);
    }

    /** Auto-registro del huésped (con acceso al portal). */
    public Huesped registrarHuesped(String nombre, String documento, LocalDate nacimiento, String telefono,
                                    String correo, String usuario, String clave) {
        if (buscarHuesped(documento) != null) throw new ReglaNegocioException("Ya existe un huésped con ese documento");
        Validar.texto(usuario, "usuario");
        if (usuarioExiste(usuario.trim())) throw new ReglaNegocioException("El usuario ya está en uso");
        Huesped h = new Huesped(nombre, documento, nacimiento, telefono, correo);
        h.definirCredenciales(usuario, clave);
        alojamiento.agregarHuesped(h);
        return h;
    }

    /** Usado por recepción y por el canal externo: reutiliza el huésped si el documento ya existe. */
    public Huesped obtenerOCrearHuesped(String nombre, String documento, LocalDate nacimiento, String telefono, String correo) {
        Huesped existente = buscarHuesped(Validar.texto(documento, "documento"));
        if (existente != null) return existente;
        Huesped h = new Huesped(nombre, documento, nacimiento, telefono, correo);
        alojamiento.agregarHuesped(h);
        return h;
    }

    public Recepcionista contratarRecepcionista(String nombre, String documento, LocalDate nacimiento, String telefono,
                                                String correo, String usuario, String clave) {
        boolean docRepetido = alojamiento.getEmpleados().stream().anyMatch(e -> e.getDocumento().equals(documento));
        if (docRepetido) throw new ReglaNegocioException("Ya existe un empleado con ese documento");
        Validar.texto(usuario, "usuario");
        if (usuarioExiste(usuario.trim())) throw new ReglaNegocioException("El usuario ya está en uso");
        Recepcionista r = new Recepcionista(nombre, documento, nacimiento, telefono, correo);
        r.definirCredenciales(usuario, clave);
        alojamiento.agregarEmpleado(r);
        return r;
    }

    public void editarEmpleado(Empleado e, String nombre, String telefono, String correo) {
        e.setNombre(nombre);
        e.setTelefono(telefono);
        e.setCorreo(correo);
    }

    public void desvincular(Empleado e) {
        if (e instanceof Administrador) throw new ReglaNegocioException("No se puede desvincular al administrador");
        if (!e.isActivo()) throw new ReglaNegocioException("El empleado ya estaba desvinculado");
        e.desvincular();
    }
}
