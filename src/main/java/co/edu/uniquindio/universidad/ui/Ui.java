package co.edu.uniquindio.universidad.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Pair;
import sga.excepciones.ReglaNegocioException;
import sga.modelo.*;
import sga.util.Formato;

import java.io.File;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Utilidades comunes de la interfaz gráfica. */
public final class Ui {
    private Ui() {}

    /** Se invoca tras cada acción exitosa para persistir los datos. */
    public static Runnable alGuardar = () -> { };

    /** Ejecuta una acción mostrando al usuario cualquier error de regla de negocio. */
    public static void accion(Runnable r) {
        try {
            r.run();
            alGuardar.run();
        } catch (ReglaNegocioException e) {
            alerta(Alert.AlertType.WARNING, e.getMessage());
        } catch (RuntimeException e) {
            alerta(Alert.AlertType.ERROR, "Error inesperado: " + e);
        }
    }
    public static Button boton(String texto, Runnable r) {
        Button b = new Button(texto);
        b.setOnAction(e -> accion(r));
        return b;
    }
    public static void alerta(Alert.AlertType tipo, String mensaje) {
        Alert a = new Alert(tipo, mensaje);
        a.setHeaderText(null);
        a.showAndWait();
    }
    public static void info(String mensaje) { alerta(Alert.AlertType.INFORMATION, mensaje); }
    public static boolean confirmar(String mensaje) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }
    public static void textoLargo(String titulo, String texto) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        TextArea ta = new TextArea(texto);
        ta.setEditable(false);
        ta.setFont(Font.font("Monospaced", 12));
        ta.setPrefSize(640, 380);
        a.getDialogPane().setContent(ta);
        a.showAndWait();
    }

    // ---------- Tablas ----------
    public static <T> TableView<T> tabla() {
        TableView<T> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("Sin datos"));
        return t;
    }
    public static <T> void col(TableView<T> t, String titulo, Function<T, String> f) {
        TableColumn<T, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(x -> new SimpleStringProperty(f.apply(x.getValue())));
        t.getColumns().add(c);
    }
    public static <T> void refrescar(TableView<T> t, Collection<T> datos) {
        t.setItems(FXCollections.observableArrayList(datos));
        t.refresh();
    }
    public static TableView<String[]> tablaDinamica(String[] encabezados, List<String[]> filas) {
        TableView<String[]> t = tabla();
        for (int i = 0; i < encabezados.length; i++) {
            final int k = i;
            col(t, encabezados[i], fila -> fila[k]);
        }
        t.setItems(FXCollections.observableArrayList(filas));
        return t;
    }
    public static <T> T seleccion(TableView<T> t) {
        T s = t.getSelectionModel().getSelectedItem();
        if (s == null) throw new ReglaNegocioException("Seleccione un elemento de la tabla");
        return s;
    }
    public static void columnasReserva(TableView<Reserva> t, boolean conTitular) {
        col(t, "Reserva", Reserva::getId);
        col(t, "Apto.", r -> r.getApartamento().getCodigo());
        if (conTitular) col(t, "Titular", r -> r.getTitular().getNombre());
        col(t, "Entrada", r -> r.getEntrada().format(Formato.FECHA));
        col(t, "Salida", r -> r.getSalida().format(Formato.FECHA));
        col(t, "Canal", r -> r.getCanal().toString());
        col(t, "Estado", r -> r.getEstado().toString());
        col(t, "Valor", r -> Formato.pesos(r.getValorEstancia()));
        col(t, "Pagado", r -> Formato.pesos(r.getFolio().totalPagos()));
        col(t, "Saldo", r -> Formato.pesos(r.getFolio().saldo()));
    }

    // ---------- Formularios ----------
    public static GridPane formulario() {
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(8);
        g.setPadding(new Insets(10));
        return g;
    }
    public static void fila(GridPane g, int fila, String etiqueta, Node campo) {
        g.add(new Label(etiqueta), 0, fila);
        g.add(campo, 1, fila);
    }
    public static LocalDate fecha(DatePicker d, String campo) {
        if (d.getValue() == null) throw new ReglaNegocioException("Seleccione la " + campo);
        return d.getValue();
    }
    public static long numero(String texto, String campo) {
        try {
            return Long.parseLong(texto.trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException | NullPointerException e) {
            throw new ReglaNegocioException("El campo '" + campo + "' debe ser un número entero");
        }
    }
    public static HBox barra(Node... nodos) {
        HBox b = new HBox(10, nodos);
        b.setPadding(new Insets(8, 0, 8, 0));
        return b;
    }
    public static VBox caja(Node... nodos) {
        VBox v = new VBox(10, nodos);
        v.setPadding(new Insets(12));
        return v;
    }
    public static Label titulo(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 15));
        return l;
    }

    // ---------- Diálogos específicos ----------
    public static Optional<Pair<MedioPago, Long>> dialogoPago(long sugerido) {
        ChoiceDialog<MedioPago> d1 = new ChoiceDialog<>(MedioPago.EFECTIVO, MedioPago.values());
        d1.setTitle("Registrar pago");
        d1.setHeaderText("Medio de pago");
        Optional<MedioPago> medio = d1.showAndWait();
        if (medio.isEmpty()) return Optional.empty();
        TextInputDialog d2 = new TextInputDialog(String.valueOf(Math.max(0, sugerido)));
        d2.setTitle("Registrar pago");
        d2.setHeaderText("Valor en pesos (sin decimales)");
        Optional<String> valor = d2.showAndWait();
        if (valor.isEmpty()) return Optional.empty();
        return Optional.of(new Pair<>(medio.get(), numero(valor.get(), "valor")));
    }

    public static void verFolio(Reserva r) {
        StringBuilder sb = new StringBuilder();
        sb.append("FOLIO ").append(r.getId()).append(" - ").append(r.getTitular().getNombre())
                .append(" - ").append(r.getEstado()).append("\n\nCARGOS\n");
        for (Cargo c : r.getFolio().getCargos())
            sb.append(String.format("  %-52s %14s%n", c.getDescripcion(), Formato.pesos(c.getValor())));
        sb.append("\nPAGOS\n");
        for (Pago p : r.getFolio().getPagos())
            sb.append(String.format("  %-16s %-13s %-22s %14s%n", p.getFecha().format(Formato.FECHA_HORA), p.getMedio(), p.getDescripcion(), Formato.pesos(p.getValor())));
        sb.append(String.format("%nTotal cargos: %s%nTotal pagos:  %s%nSALDO:        %s%n",
                Formato.pesos(r.getFolio().totalCargos()), Formato.pesos(r.getFolio().totalPagos()), Formato.pesos(r.getFolio().saldo())));
        textoLargo("Folio " + r.getId(), sb.toString());
    }

    public static void mostrarFoto(ImageView vista, String ruta) {
        vista.setImage(null);
        if (ruta == null) return;
        File f = new File(ruta);
        if (f.exists()) vista.setImage(new Image(f.toURI().toString(), 150, 0, true, true));
    }
}
