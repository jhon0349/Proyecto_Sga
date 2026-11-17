package co.edu.uniquindio.universidad.modelo;


import sga.excepciones.ReglaNegocioException;
import sga.util.Seguridad;
import sga.util.Validar;

import java.io.Serializable;

/** Usuario y contraseña (almacenada como hash). Encapsula la seguridad de acceso. */
public class Credencial implements Serializable {
    private final String usuario;
    private String hashClave;

    public Credencial(String usuario, String clave) {
        String u = Validar.texto(usuario, "usuario");
        if (u.length() < 3) throw new ReglaNegocioException("El usuario debe tener al menos 3 caracteres");
        this.usuario = u;
        this.hashClave = hashValida(clave);
    }
    private static String hashValida(String clave) {
        if (clave == null || clave.length() < 4) throw new ReglaNegocioException("La contraseña debe tener al menos 4 caracteres");
        return Seguridad.hash(clave);
    }
    public String getUsuario() { return usuario; }
    public boolean verificar(String clave) { return clave != null && hashClave.equals(Seguridad.hash(clave)); }
    public void cambiarClave(String actual, String nueva) {
        if (!verificar(actual)) throw new ReglaNegocioException("La contraseña actual no coincide");
        this.hashClave = hashValida(nueva);
    }
}
