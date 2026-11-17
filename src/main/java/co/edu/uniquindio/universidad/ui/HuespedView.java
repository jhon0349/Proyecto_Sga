package co.edu.uniquindio.universidad.ui;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import sga.excepciones.ReglaNegocioException;
import sga.modelo.*;
import sga.servicio.Sistema;
import sga.util.Formato;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Panel del rol Huésped. */
public class HuespedView extends TabPane {
    private final Sistema s;
    private final Huesped h;

    public HuespedView(Sistema s, Huesped h) {
        this.s = s;
        this.h = h;
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getTabs().addAll(new Tab("Mi perfil", perfil()), new Tab("Disponibilidad y reserva", reservar()),
                new Tab("Mis reservas", misReservas()), new Tab("Historial de estancias", historial()));
    }

    // ---------------- Perfil ----------------
    private Node perfil() {
        TextField nombre = new TextField(h.getNombre()), doc = new TextField(h.getDocumento());
        TextField tel = new TextField(h.getTelefono()), correo = new TextField(h.getCorreo());
        doc.setEditable(false);
        DatePicker nac = new DatePicker(h.getFechaNacimiento());
        ImageView foto = new ImageView();
        Ui.mostrarFoto(foto, h.getFotoRuta());
        Button elegir = new Button("Adjuntar fotografía (verificación de identidad)");
        elegir.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
            File f = fc.showOpenDialog(getScene().getWindow());
            if (f != null) Ui.accion(() -> { h.setFotoRuta(f.getAbsolutePath()); Ui.mostrarFoto(foto, h.getFotoRuta()); });
        });
        Button guardar = Ui.boton("Guardar cambios", () -> {
            h.setNombre(nombre.getText());
            h.setFechaNacimiento(Ui.fecha(nac, "fecha de nacimiento"));
            h.setTelefono(tel.getText());
            h.setCorreo(correo.getText());
            Ui.info("Perfil actualizado");
        });
        GridPane g = Ui.formulario();
        Ui.fila(g, 0, "Nombre", nombre);
        Ui.fila(g, 1, "Documento", doc);
        Ui.fila(g, 2, "Fecha de nacimiento", nac);
        Ui.fila(g, 3, "Teléfono", tel);
        Ui.fila(g, 4, "Correo", correo);
        return Ui.caja(Ui.titulo("Mi perfil"), g, Ui.barra(elegir, guardar), foto);
    }

    // ---------------- Disponibilidad, cotización y reserva ----------------
    private Node reservar() {
        DatePicker entrada = new DatePicker(s.getReloj().hoy().plusDays(1));
        DatePicker salida = new DatePicker(s.getReloj().hoy().plusDays(3));
        Spinner<Integer> personas = new Spinner<>(1, 20, 2);
        TableView<Apartamento> aptos = Ui.tabla();
        Ui.col(aptos, "Código", Apartamento::getCodigo);
        Ui.col(aptos, "Nombre", Apartamento::getNombre);
        Ui.col(aptos, "Dorm.", a -> String.valueOf(a.getDormitorios()));
        Ui.col(aptos, "Capacidad", a -> String.valueOf(a.getCapacidad()));
        Ui.col(aptos, "Características", a -> String.join(", ", a.getCaracteristicas()));
        Ui.col(aptos, "Tarifa baja", a -> Formato.pesos(a.getTarifa(Temporada.BAJA)));
        Ui.col(aptos, "Media", a -> Formato.pesos(a.getTarifa(Temporada.MEDIA)));
        Ui.col(aptos, "Alta", a -> Formato.pesos(a.getTarifa(Temporada.ALTA)));
        aptos.setPrefHeight(170);

        OcupantesEditor ocupantes = new OcupantesEditor("Acompañantes (el titular se incluye automáticamente)");
        TableView<NocheLiquidada> desglose = Ui.tabla();
        Ui.col(desglose, "Noche", n -> n.noche().format(Formato.FECHA));
        Ui.col(desglose, "Temporada", n -> n.temporada().toString());
        Ui.col(desglose, "Tarifa/persona", n -> Formato.pesos(n.tarifa()));
        Ui.col(desglose, "Pagan", n -> String.valueOf(n.pagantes()));
        Ui.col(desglose, "Subtotal", n -> Formato.pesos(n.subtotal()));
        desglose.setPrefHeight(150);
        Label total = Ui.titulo("Total: -");

        Button buscar = Ui.boton("Buscar disponibles", () -> Ui.refrescar(aptos,
                s.reservas().disponibles(Ui.fecha(entrada, "fecha de entrada"), Ui.fecha(salida, "fecha de salida"), personas.getValue())));
        Button cotizar = Ui.boton("Cotizar estancia", () -> {
            Apartamento a = Ui.seleccion(aptos);
            Cotizacion c = s.reservas().cotizar(a, Ui.fecha(entrada, "fecha de entrada"), Ui.fecha(salida, "fecha de salida"), alojados(ocupantes));
            Ui.refrescar(desglose, c.getNoches());
            total.setText("Total: " + Formato.pesos(c.getTotal()) + "  (" + c.getNumeroNoches() + " noches)");
        });
        Button reservar = Ui.boton("Reservar (canal Portal)", () -> {
            Apartamento a = Ui.seleccion(aptos);
            Reserva r = s.reservas().crearReservaPortal(h, a, Ui.fecha(entrada, "fecha de entrada"), Ui.fecha(salida, "fecha de salida"), ocupantes.getOcupantes());
            Ui.info("Reserva " + r.getId() + " creada y PENDIENTE.\nValor: " + Formato.pesos(r.getValorEstancia())
                    + "\nPague el anticipo de " + Formato.pesos(r.anticipoRequerido()) + " en las próximas 24 horas desde 'Mis reservas'.");
            ocupantes.limpiar();
            Ui.refrescar(aptos, new ArrayList<>());
        });
        return Ui.caja(Ui.titulo("Consultar disponibilidad"),
                Ui.barra(new Label("Entrada"), entrada, new Label("Salida"), salida, new Label("Personas"), personas, buscar),
                aptos, ocupantes, Ui.barra(cotizar, reservar, total), desglose);
    }

    /** Titular + acompañantes: todos cuentan para la capacidad. */
    private List<Ocupante> alojados(OcupantesEditor ocupantes) {
        List<Ocupante> l = new ArrayList<>();
        l.add(Ocupante.de(h));
        l.addAll(ocupantes.getOcupantes());
        return l;
    }

    // ---------------- Mis reservas ----------------
    private Node misReservas() {
        TableView<Reserva> t = Ui.tabla();
        Ui.columnasReserva(t, false);
        Runnable refrescar = () -> { s.reservas().liberarVencidas(); Ui.refrescar(t, s.reservas().reservasDe(h)); };
        refrescar.run();
        Button actualizar = new Button("Actualizar");
        actualizar.setOnAction(e -> refrescar.run());
        Button pagar = Ui.boton("Pagar anticipo / abono", () -> {
            Reserva r = Ui.seleccion(t);
            long sugerido = r.getEstado() == EstadoReserva.PENDIENTE
                    ? Math.max(0, r.anticipoRequerido() - r.getFolio().totalPagos()) : r.getFolio().saldo();
            Ui.dialogoPago(sugerido).ifPresent(p -> {
                s.reservas().registrarPago(r, p.getKey(), p.getValue());
                refrescar.run();
                Ui.info("Pago registrado. Saldo actual: " + Formato.pesos(r.getFolio().saldo()));
            });
        });
        Button folio = Ui.boton("Ver folio", () -> Ui.verFolio(Ui.seleccion(t)));
        Button cancelar = Ui.boton("Cancelar reserva", () -> {
            Reserva r = Ui.seleccion(t);
            ResultadoCancelacion rc = s.reservas().calcularCancelacion(r); // el huésped ve antes cuánto se le devolvería
            String msg = "Retención: " + rc.porcentajeRetencion() + " % del valor de la estancia → " + Formato.pesos(rc.retenido())
                    + "\nSe le devolvería: " + Formato.pesos(rc.devolucion()) + "\n\n¿Desea cancelar la reserva " + r.getId() + "?";
            if (Ui.confirmar(msg)) {
                s.reservas().cancelar(r);
                refrescar.run();
            }
        });
        return Ui.caja(Ui.titulo("Mis reservas"), Ui.barra(actualizar, pagar, folio, cancelar), t);
    }

    // ---------------- Historial de estancias ----------------
    private Node historial() {
        TableView<Reserva> t = Ui.tabla();
        Ui.columnasReserva(t, false);
        Runnable refrescar = () -> Ui.refrescar(t, s.reportes().historialDe(h));
        refrescar.run();
        Button actualizar = new Button("Actualizar");
        actualizar.setOnAction(e -> refrescar.run());
        Button folio = Ui.boton("Ver folio", () -> Ui.verFolio(Ui.seleccion(t)));
        return Ui.caja(Ui.titulo("Historial de estancias (reservas finalizadas)"), Ui.barra(actualizar, folio), t);
    }
}
