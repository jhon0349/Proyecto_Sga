package co.edu.uniquindio.universidad.ui;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.servicio.Sistema;
import co.edu.uniquindio.universidad.util.Formato;

import java.util.ArrayList;
import java.util.List;

/** Panel del rol Recepcionista. */
public class RecepcionView extends TabPane {
    private final Sistema s;
    private final Empleado empleado;

    public RecepcionView(Sistema s, Empleado empleado) {
        this.s = s;
        this.empleado = empleado;
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getTabs().addAll(new Tab("Reservas", reservas()), new Tab("Nueva reserva", nuevaReserva()),
                new Tab("Apartamentos y novedades", apartamentos()), new Tab("Calendario", new CalendarioView(s)));
    }

    // ---------------- Gestión de reservas ----------------
    private Node reservas() {
        TableView<Reserva> t = Ui.tabla();
        Ui.columnasReserva(t, true);
        Runnable refrescar = () -> { s.reservas().liberarVencidas(); Ui.refrescar(t, s.reservas().todas()); };
        refrescar.run();
        Button actualizar = new Button("Actualizar");
        actualizar.setOnAction(e -> refrescar.run());
        Button confirmar = Ui.boton("Confirmar", () -> { s.reservas().confirmar(Ui.seleccion(t)); refrescar.run(); });
        Button checkIn = Ui.boton("Check-in", () -> { s.reservas().checkIn(Ui.seleccion(t)); refrescar.run(); });
        Button checkOut = Ui.boton("Check-out", () -> {
            Reserva r = Ui.seleccion(t);
            if (r.getFolio().saldo() > 0 && !Ui.confirmar("El folio tiene un saldo de " + Formato.pesos(r.getFolio().saldo()) + ". ¿Hacer la salida de todos modos?")) return;
            s.reservas().checkOut(r);
            refrescar.run();
        });
        Button noShow = Ui.boton("No-show", () -> {
            Reserva r = Ui.seleccion(t);
            if (Ui.confirmar("¿Declarar no-show la reserva " + r.getId() + "? Se retiene el 100 % de lo pagado.")) {
                s.reservas().declararNoShow(r);
                refrescar.run();
            }
        });
        Button cancelar = Ui.boton("Cancelar", () -> {
            Reserva r = Ui.seleccion(t);
            ResultadoCancelacion rc = s.reservas().calcularCancelacion(r);
            if (Ui.confirmar("Retención " + rc.porcentajeRetencion() + " % → " + Formato.pesos(rc.retenido()) + "\nDevolución: "
                    + Formato.pesos(rc.devolucion()) + "\n¿Cancelar " + r.getId() + "?")) {
                s.reservas().cancelar(r);
                refrescar.run();
            }
        });
        Button pago = Ui.boton("Registrar pago", () -> {
            Reserva r = Ui.seleccion(t);
            Ui.dialogoPago(r.getFolio().saldo()).ifPresent(p -> {
                s.reservas().registrarPago(r, p.getKey(), p.getValue());
                refrescar.run();
            });
        });
        Button servicio = Ui.boton("Cargar servicio", () -> {
            Reserva r = Ui.seleccion(t);
            TextInputDialog d1 = new TextInputDialog("Desayuno");
            d1.setHeaderText("Descripción del servicio (desayuno, lavandería...)");
            var desc = d1.showAndWait();
            if (desc.isEmpty()) return;
            TextInputDialog d2 = new TextInputDialog("20000");
            d2.setHeaderText("Valor en pesos");
            var val = d2.showAndWait();
            if (val.isEmpty()) return;
            s.reservas().registrarCargoServicio(r, desc.get(), Ui.numero(val.get(), "valor"));
            refrescar.run();
        });
        Button corregir = Ui.boton("Reversar movimiento", () -> {
            Reserva r = Ui.seleccion(t);
            List<String> opciones = new ArrayList<>();
            for (int i = 0; i < r.getFolio().getCargos().size(); i++)
                opciones.add("C" + i + " · " + r.getFolio().getCargos().get(i).getDescripcion() + " · " + Formato.pesos(r.getFolio().getCargos().get(i).getValor()));
            for (int i = 0; i < r.getFolio().getPagos().size(); i++)
                opciones.add("P" + i + " · " + r.getFolio().getPagos().get(i).getDescripcion() + " · " + Formato.pesos(r.getFolio().getPagos().get(i).getValor()));
            if (opciones.isEmpty()) throw new ReglaNegocioException("El folio no tiene movimientos");
            ChoiceDialog<String> d = new ChoiceDialog<>(opciones.get(0), opciones);
            d.setHeaderText("Los movimientos no se editan ni se borran: se registra un movimiento contrario");
            var sel = d.showAndWait();
            if (sel.isEmpty()) return;
            int idx = Integer.parseInt(sel.get().substring(1, sel.get().indexOf(' ')));
            if (sel.get().startsWith("C")) s.reservas().corregirCargo(r, idx, "corrección de recepción");
            else s.reservas().corregirPago(r, idx, "corrección de recepción");
            refrescar.run();
        });
        Button folio = Ui.boton("Ver folio", () -> Ui.verFolio(Ui.seleccion(t)));
        return Ui.caja(Ui.titulo("Reservas del alojamiento"), Ui.barra(actualizar, confirmar, checkIn, checkOut, noShow, cancelar),
                Ui.barra(pago, servicio, corregir, folio), t);
    }

    // ---------------- Reservas Directa / Externa ----------------
    private Node nuevaReserva() {
        ComboBox<CanalReserva> canal = new ComboBox<>(FXCollections.observableArrayList(CanalReserva.DIRECTO, CanalReserva.EXTERNO));
        canal.setValue(CanalReserva.DIRECTO);
        TextField codigoExterno = new TextField();
        codigoExterno.setPromptText("Código de la plataforma");
        codigoExterno.disableProperty().bind(canal.valueProperty().isNotEqualTo(CanalReserva.EXTERNO));
        ComboBox<Huesped> huesped = new ComboBox<>(FXCollections.observableArrayList(s.getAlojamiento().getHuespedes()));
        huesped.setOnShowing(e -> huesped.getItems().setAll(s.getAlojamiento().getHuespedes()));
        Button nuevoHuesped = Ui.boton("Nuevo huésped", () -> FormHuesped.mostrar(s, false).ifPresent(h -> {
            huesped.getItems().setAll(s.getAlojamiento().getHuespedes());
            huesped.setValue(h);
        }));
        DatePicker entrada = new DatePicker(s.getReloj().hoy());
        DatePicker salida = new DatePicker(s.getReloj().hoy().plusDays(2));
        ComboBox<Apartamento> apto = new ComboBox<>();
        Button buscar = Ui.boton("Buscar apartamentos disponibles", () -> {
            apto.getItems().setAll(s.reservas().disponibles(Ui.fecha(entrada, "fecha de entrada"), Ui.fecha(salida, "fecha de salida"), 1));
            if (apto.getItems().isEmpty()) Ui.info("No hay apartamentos disponibles para esas fechas");
        });
        OcupantesEditor ocupantes = new OcupantesEditor("Ocupantes que acompañan al titular");
        Label cotizacion = Ui.titulo("Cotización: -");

        Button cotizar = Ui.boton("Cotizar", () -> {
            Huesped h = titular(huesped);
            List<Ocupante> todos = new ArrayList<>();
            todos.add(Ocupante.de(h));
            todos.addAll(ocupantes.getOcupantes());
            Cotizacion c = s.reservas().cotizar(seleccionApto(apto), Ui.fecha(entrada, "fecha de entrada"), Ui.fecha(salida, "fecha de salida"), todos);
            cotizacion.setText("Cotización: " + Formato.pesos(c.getTotal()) + " (" + c.getNumeroNoches() + " noches)");
        });
        Button crear = Ui.boton("Crear reserva", () -> {
            Huesped h = titular(huesped);
            Apartamento a = seleccionApto(apto);
            var in = Ui.fecha(entrada, "fecha de entrada");
            var out = Ui.fecha(salida, "fecha de salida");
            Reserva r = canal.getValue() == CanalReserva.EXTERNO
                    ? s.reservas().crearReservaExterna(codigoExterno.getText(), h, a, in, out, ocupantes.getOcupantes())
                    : s.reservas().crearReservaDirecta(h, a, in, out, ocupantes.getOcupantes());
            Ui.info("Reserva " + r.getId() + " creada (" + r.getEstado() + "). Valor: " + Formato.pesos(r.getValorEstancia()));
            ocupantes.limpiar();
            apto.getItems().clear();
        });
        return Ui.caja(Ui.titulo("Registrar reserva"),
                Ui.barra(new Label("Canal"), canal, codigoExterno),
                Ui.barra(new Label("Titular"), huesped, nuevoHuesped),
                Ui.barra(new Label("Entrada"), entrada, new Label("Salida"), salida, buscar),
                Ui.barra(new Label("Apartamento"), apto), ocupantes, Ui.barra(cotizar, crear, cotizacion));
    }
    private Huesped titular(ComboBox<Huesped> c) {
        if (c.getValue() == null) throw new ReglaNegocioException("Seleccione el huésped titular");
        return c.getValue();
    }
    private Apartamento seleccionApto(ComboBox<Apartamento> c) {
        if (c.getValue() == null) throw new ReglaNegocioException("Busque y seleccione un apartamento disponible");
        return c.getValue();
    }

    // ---------------- Estado operativo y novedades ----------------
    private Node apartamentos() {
        TableView<Apartamento> t = Ui.tabla();
        Ui.col(t, "Código", Apartamento::getCodigo);
        Ui.col(t, "Nombre", Apartamento::getNombre);
        Ui.col(t, "Estado operativo", a -> a.getEstado().toString());
        Ui.col(t, "Motivo (fuera de servicio)", a -> a.getMotivoFueraServicio() == null ? "" : a.getMotivoFueraServicio());
        Ui.col(t, "Novedades", a -> String.valueOf(a.getNovedades().size()));
        Runnable refrescar = () -> Ui.refrescar(t, s.inventario().apartamentosActivos());
        refrescar.run();

        ComboBox<EstadoApartamento> estado = new ComboBox<>(FXCollections.observableArrayList(EstadoApartamento.values()));
        estado.setValue(EstadoApartamento.PREPARADO);
        TextField motivo = new TextField();
        motivo.setPromptText("Motivo (obligatorio si queda fuera de servicio)");
        Button aplicar = Ui.boton("Cambiar estado", () -> {
            s.inventario().cambiarEstado(Ui.seleccion(t), estado.getValue(), motivo.getText());
            motivo.clear();
            refrescar.run();
        });
        TextField novedad = new TextField();
        novedad.setPromptText("Describa el daño, faltante o situación");
        novedad.setPrefWidth(380);
        Button registrar = Ui.boton("Registrar novedad", () -> {
            s.inventario().registrarNovedad(Ui.seleccion(t), novedad.getText(), empleado.getNombre());
            novedad.clear();
            refrescar.run();
        });
        Button ver = Ui.boton("Ver novedades", () -> {
            Apartamento a = Ui.seleccion(t);
            StringBuilder sb = new StringBuilder();
            for (Novedad n : a.getNovedades())
                sb.append(n.getFecha().format(Formato.FECHA_HORA)).append(" · ").append(n.getAutor()).append(": ").append(n.getDescripcion()).append("\n");
            Ui.textoLargo("Novedades de " + a, sb.length() == 0 ? "Sin novedades" : sb.toString());
        });
        return Ui.caja(Ui.titulo("Estado operativo de los apartamentos"), t, Ui.barra(estado, motivo, aplicar),
                Ui.barra(novedad, registrar, ver));
    }
}