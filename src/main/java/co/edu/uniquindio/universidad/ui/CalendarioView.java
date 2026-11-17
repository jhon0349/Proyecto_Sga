package co.edu.uniquindio.universidad.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import co.edu.uniquindio.universidad.modelo.Apartamento;
import co.edu.uniquindio.universidad.modelo.EstadoNoche;
import co.edu.uniquindio.universidad.servicio.Sistema;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/** Calendario de ocupación mensual: noches ocupadas, bloqueadas y libres por apartamento. */
public class CalendarioView extends VBox {
    private final Sistema sistema;
    private YearMonth mes;
    private final Label etiquetaMes = Ui.titulo("");
    private final GridPane rejilla = new GridPane();

    public CalendarioView(Sistema sistema) {
        super(10);
        this.sistema = sistema;
        this.mes = YearMonth.from(sistema.getReloj().hoy());
        setPadding(new Insets(12));
        Button anterior = new Button("◀");
        Button siguiente = new Button("▶");
        anterior.setOnAction(e -> { mes = mes.minusMonths(1); dibujar(); });
        siguiente.setOnAction(e -> { mes = mes.plusMonths(1); dibujar(); });
        Button actualizar = new Button("Actualizar");
        actualizar.setOnAction(e -> dibujar());
        HBox leyendas = new HBox(12, leyenda("Libre", "#81c784"), leyenda("Ocupada", "#ef5350"), leyenda("Bloqueada", "#9e9e9e"));
        rejilla.setHgap(2);
        rejilla.setVgap(2);
        ScrollPane sp = new ScrollPane(rejilla);
        getChildren().addAll(new HBox(10, anterior, etiquetaMes, siguiente, actualizar), leyendas, sp);
        dibujar();
    }

    private Label leyenda(String texto, String color) {
        Label l = new Label("  " + texto + "  ");
        l.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        return l;
    }

    private static String color(EstadoNoche e) {
        return switch (e) {
            case LIBRE -> "#81c784";
            case OCUPADA -> "#ef5350";
            case BLOQUEADA -> "#9e9e9e";
        };
    }

    public void dibujar() {
        etiquetaMes.setText(mes.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")) + " " + mes.getYear());
        rejilla.getChildren().clear();
        for (int d = 1; d <= mes.lengthOfMonth(); d++) rejilla.add(new Label(String.valueOf(d)), d, 0);
        int fila = 1;
        for (Apartamento a : sistema.inventario().apartamentosActivos()) {
            rejilla.add(new Label(a.getCodigo() + " " + a.getNombre()), 0, fila);
            for (int d = 1; d <= mes.lengthOfMonth(); d++) {
                LocalDate noche = mes.atDay(d);
                EstadoNoche e = sistema.reservas().estadoNoche(a, noche);
                Label celda = new Label();
                celda.setMinSize(24, 22);
                celda.setStyle("-fx-background-color: " + color(e) + ";");
                Tooltip.install(celda, new Tooltip(a.getCodigo() + " · " + noche + " · " + e));
                rejilla.add(celda, d, fila);
            }
            fila++;
        }
    }
}