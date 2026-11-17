package co.edu.uniquindio.universidad.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import sga.excepciones.ReglaNegocioException;
import sga.modelo.*;
import sga.servicio.Sistema;

import java.nio.file.Path;

/** Aplicación JavaFX del SGA: inicio de sesión y despacho según el rol. */
public class AppSGA extends Application {
    private Sistema sistema;
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        this.sistema = Sistema.iniciar(Path.of("sga-datos.dat"));
        Ui.alGuardar = sistema::guardar;
        // Cada 30 s se cancelan automáticamente las reservas Pendientes que superaron el plazo de 24 horas
        Timeline vigilante = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            if (!sistema.reservas().liberarVencidas().isEmpty()) sistema.guardar();
        }));
        vigilante.setCycleCount(Animation.INDEFINITE);
        vigilante.play();
        stage.setTitle("SGA - Aparta-Suites UQ");
        mostrarLogin();
        stage.show();
    }

    @Override
    public void stop() { if (sistema != null) sistema.guardar(); }

    private void mostrarLogin() {
        TextField usuario = new TextField();
        usuario.setPromptText("Usuario");
        PasswordField clave = new PasswordField();
        clave.setPromptText("Contraseña");
        Button ingresar = new Button("Ingresar");
        ingresar.setDefaultButton(true);
        ingresar.setOnAction(e -> {
            try {
                entrar(sistema.usuarios().autenticar(usuario.getText(), clave.getText()));
            } catch (ReglaNegocioException ex) {
                Ui.alerta(Alert.AlertType.WARNING, ex.getMessage());
            }
        });
        Button registro = new Button("Registrarme como huésped");
        registro.setOnAction(e -> FormHuesped.mostrar(sistema, true).ifPresent(h -> {
            sistema.guardar();
            Ui.info("Registro exitoso. Ya puede iniciar sesión con el usuario '" + h.getUsuario() + "'.");
        }));
        Label demo = new Label("Demo: admin/admin123 · recep/recep123 · huesped/huesped123");
        demo.setStyle("-fx-text-fill: #777;");
        VBox caja = new VBox(12, Ui.titulo("Aparta-Suites UQ · Sistema de Gestión de Alojamiento"), usuario, clave,
                new HBox(10, ingresar, registro), demo);
        caja.setAlignment(Pos.CENTER);
        caja.setMaxWidth(380);
        caja.setPadding(new Insets(30));
        BorderPane raiz = new BorderPane(caja);
        stage.setScene(new Scene(raiz, 560, 360));
    }

    private void entrar(Autenticable u) {
        Persona p = (Persona) u;
        Node vista = switch (u.getRol()) {
            case HUESPED -> new HuespedView(sistema, (Huesped) u);
            case RECEPCIONISTA -> new RecepcionView(sistema, (Empleado) u);
            case ADMINISTRADOR -> new AdminView(sistema, (Empleado) u);
        };
        Label quien = new Label("Aparta-Suites UQ  ·  " + p.getNombre() + " (" + u.getRol() + ")");
        Region relleno = new Region();
        HBox.setHgrow(relleno, Priority.ALWAYS);
        Button salir = new Button("Cerrar sesión");
        salir.setOnAction(e -> { sistema.guardar(); mostrarLogin(); });
        HBox barra = new HBox(10, quien, relleno, salir);
        barra.setPadding(new Insets(8, 12, 8, 12));
        barra.setStyle("-fx-background-color: #e8eef5;");
        BorderPane raiz = new BorderPane(vista);
        raiz.setTop(barra);
        stage.setScene(new Scene(raiz, 1150, 720));
    }

    public static void main(String[] args) { launch(args); }
}
