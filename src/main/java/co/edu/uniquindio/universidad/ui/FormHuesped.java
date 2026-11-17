package co.edu.uniquindio.universidad.ui;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import sga.excepciones.ReglaNegocioException;
import sga.modelo.Huesped;
import sga.servicio.Sistema;

import java.util.Optional;

/** Diálogo para registrar huéspedes: con usuario/contraseña (portal) o sin ellos (recepción / canal externo). */
public final class FormHuesped {
    private FormHuesped() {}

    public static Optional<Huesped> mostrar(Sistema s, boolean conCredenciales) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(conCredenciales ? "Registro de huésped" : "Nuevo huésped");
        TextField nombre = new TextField(), doc = new TextField(), tel = new TextField(), correo = new TextField(), usuario = new TextField();
        PasswordField clave = new PasswordField();
        DatePicker nac = new DatePicker();
        GridPane g = Ui.formulario();
        int f = 0;
        Ui.fila(g, f++, "Nombre completo", nombre);
        Ui.fila(g, f++, "Documento", doc);
        Ui.fila(g, f++, "Fecha de nacimiento", nac);
        Ui.fila(g, f++, "Teléfono", tel);
        Ui.fila(g, f++, "Correo", correo);
        if (conCredenciales) {
            Ui.fila(g, f++, "Usuario", usuario);
            Ui.fila(g, f++, "Contraseña", clave);
        }
        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Huesped[] resultado = new Huesped[1];
        d.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                var fecha = Ui.fecha(nac, "fecha de nacimiento");
                resultado[0] = conCredenciales
                        ? s.usuarios().registrarHuesped(nombre.getText(), doc.getText(), fecha, tel.getText(), correo.getText(), usuario.getText(), clave.getText())
                        : s.usuarios().obtenerOCrearHuesped(nombre.getText(), doc.getText(), fecha, tel.getText(), correo.getText());
            } catch (ReglaNegocioException ex) {
                Ui.alerta(Alert.AlertType.WARNING, ex.getMessage());
                ev.consume(); // mantiene abierto el diálogo
            }
        });
        d.showAndWait();
        return Optional.ofNullable(resultado[0]);
    }
}
