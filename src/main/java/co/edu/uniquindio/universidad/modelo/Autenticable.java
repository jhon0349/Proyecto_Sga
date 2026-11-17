package co.edu.uniquindio.universidad.modelo;


public interface Autenticable {
    String getUsuario();
    boolean validarClave(String clave);
    Rol getRol();
}
