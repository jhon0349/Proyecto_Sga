package co.edu.uniquindio.universidad.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import co.edu.uniquindio.universidad.modelo.Ocupante;

import java.util.ArrayList;
import java.util.List;

/** Componente reutilizable para registrar ocupantes con su fecha de nacimiento (nunca la edad). */
public class OcupantesEditor extends VBox {
    private final ObservableList<Ocupante> lista = FXCollections.observableArrayList();

    public OcupantesEditor(String titulo) {
        super(6);
        TextField nombre = new TextField();
        nombre.setPromptText("Nombre");
        DatePicker nacimiento = new DatePicker();
        nacimiento.setPromptText("Fecha de nacimiento");
        ListView<Ocupante> vista = new ListView<>(lista);
        vista.setPrefHeight(110);
        Button agregar = Ui.boton("Agregar", () -> {
            lista.add(new Ocupante(nombre.getText(), Ui.fecha(nacimiento, "fecha de nacimiento del ocupante")));
            nombre.clear();
            nacimiento.setValue(null);
        });
        Button quitar = new Button("Quitar seleccionado");
        quitar.setOnAction(e -> {
            Ocupante o = vista.getSelectionModel().getSelectedItem();
            if (o != null) lista.remove(o);
        });
        getChildren().addAll(new Label(titulo), new HBox(8, nombre, nacimiento, agregar, quitar), vista);
    }
    public List<Ocupante> getOcupantes() { return new ArrayList<>(lista); }
    public void limpiar() { lista.clear(); }
}
