package co.edu.uniquindio.universidad.ui;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.servicio.ServicioReportes;
import co.edu.uniquindio.universidad.servicio.Sistema;
import co.edu.uniquindio.universidad.util.Formato;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Panel del rol Administrador. */
public class AdminView extends TabPane {
    private final Sistema s;

    public AdminView(Sistema s, Empleado admin) {
        this.s = s;
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getTabs().addAll(new Tab("Empleados", empleados()), new Tab("Apartamentos y tarifas", apartamentos()),
                new Tab("Temporadas", temporadas()), new Tab("Política de cancelación", politica()),
                new Tab("Bloqueos", bloqueos()), new Tab("Reportes", reportes()),
                new Tab("Calendario", new CalendarioView(s)), new Tab("Simulador canal externo", simulador()));
    }

    // ---------------- Empleados ----------------
    private Node empleados() {
        TableView<Empleado> t = Ui.tabla();
        Ui.col(t, "Nombre", Empleado::getNombre);
        Ui.col(t, "Documento", Empleado::getDocumento);
        Ui.col(t, "Teléfono", Empleado::getTelefono);
        Ui.col(t, "Correo", Empleado::getCorreo);
        Ui.col(t, "Usuario", Empleado::getUsuario);
        Ui.col(t, "Rol", e -> e.getRol().toString());
        Ui.col(t, "Estado", e -> e.isActivo() ? "Activo" : "Desvinculado");
        Runnable refrescar = () -> Ui.refrescar(t, s.getAlojamiento().getEmpleados());
        refrescar.run();

        TextField nombre = new TextField(), doc = new TextField(), tel = new TextField(), correo = new TextField(), usuario = new TextField();
        PasswordField clave = new PasswordField();
        DatePicker nac = new DatePicker();
        t.getSelectionModel().selectedItemProperty().addListener((o, a, e) -> {
            if (e != null) { nombre.setText(e.getNombre()); doc.setText(e.getDocumento()); tel.setText(e.getTelefono()); correo.setText(e.getCorreo()); }
        });
        GridPane g = Ui.formulario();
        Ui.fila(g, 0, "Nombre", nombre);
        Ui.fila(g, 1, "Documento", doc);
        Ui.fila(g, 2, "Nacimiento", nac);
        Ui.fila(g, 3, "Teléfono", tel);
        Ui.fila(g, 4, "Correo", correo);
        Ui.fila(g, 5, "Usuario", usuario);
        Ui.fila(g, 6, "Contraseña", clave);
        Button contratar = Ui.boton("Contratar recepcionista", () -> {
            s.usuarios().contratarRecepcionista(nombre.getText(), doc.getText(), Ui.fecha(nac, "fecha de nacimiento"),
                    tel.getText(), correo.getText(), usuario.getText(), clave.getText());
            refrescar.run();
        });
        Button editar = Ui.boton("Editar datos del seleccionado", () -> {
            s.usuarios().editarEmpleado(Ui.seleccion(t), nombre.getText(), tel.getText(), correo.getText());
            refrescar.run();
        });
        Button desvincular = Ui.boton("Desvincular", () -> {
            Empleado e = Ui.seleccion(t);
            if (Ui.confirmar("¿Desvincular a " + e.getNombre() + "? Ya no podrá iniciar sesión.")) {
                s.usuarios().desvincular(e);
                refrescar.run();
            }
        });
        return Ui.caja(Ui.titulo("Gestión de empleados"), t, g, Ui.barra(contratar, editar, desvincular));
    }

    // ---------------- Apartamentos y tarifas ----------------
    private Node apartamentos() {
        TableView<Apartamento> t = Ui.tabla();
        Ui.col(t, "Código", Apartamento::getCodigo);
        Ui.col(t, "Nombre", Apartamento::getNombre);
        Ui.col(t, "Dorm.", a -> String.valueOf(a.getDormitorios()));
        Ui.col(t, "Cap.", a -> String.valueOf(a.getCapacidad()));
        Ui.col(t, "Características", a -> String.join(", ", a.getCaracteristicas()));
        Ui.col(t, "Baja", a -> Formato.pesos(a.getTarifa(Temporada.BAJA)));
        Ui.col(t, "Media", a -> Formato.pesos(a.getTarifa(Temporada.MEDIA)));
        Ui.col(t, "Alta", a -> Formato.pesos(a.getTarifa(Temporada.ALTA)));
        Ui.col(t, "Estado", a -> a.getEstado().toString());
        Runnable refrescar = () -> Ui.refrescar(t, s.inventario().apartamentosActivos());
        refrescar.run();
        t.setPrefHeight(200);

        TextField codigo = new TextField(), nombre = new TextField(), dorm = new TextField(), cap = new TextField();
        TextField carac = new TextField(), tBaja = new TextField(), tMedia = new TextField(), tAlta = new TextField();
        carac.setPromptText("separadas por coma: Balcón, Jacuzzi");
        ImageView foto = new ImageView();
        t.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> {
            if (n == null) return;
            codigo.setText(n.getCodigo()); nombre.setText(n.getNombre());
            dorm.setText(String.valueOf(n.getDormitorios())); cap.setText(String.valueOf(n.getCapacidad()));
            carac.setText(String.join(", ", n.getCaracteristicas()));
            tBaja.setText(String.valueOf(n.getTarifa(Temporada.BAJA)));
            tMedia.setText(String.valueOf(n.getTarifa(Temporada.MEDIA)));
            tAlta.setText(String.valueOf(n.getTarifa(Temporada.ALTA)));
            Ui.mostrarFoto(foto, n.getFotos().isEmpty() ? null : n.getFotos().get(n.getFotos().size() - 1));
        });
        GridPane g = Ui.formulario();
        Ui.fila(g, 0, "Código", codigo);
        Ui.fila(g, 1, "Nombre", nombre);
        Ui.fila(g, 2, "Dormitorios", dorm);
        Ui.fila(g, 3, "Capacidad máx.", cap);
        Ui.fila(g, 4, "Características", carac);
        Ui.fila(g, 5, "Tarifa Baja (por persona/noche)", tBaja);
        Ui.fila(g, 6, "Tarifa Media", tMedia);
        Ui.fila(g, 7, "Tarifa Alta", tAlta);
        Button crear = Ui.boton("Crear", () -> {
            s.inventario().crearApartamento(codigo.getText(), nombre.getText(), (int) Ui.numero(dorm.getText(), "dormitorios"),
                    (int) Ui.numero(cap.getText(), "capacidad"), lista(carac.getText()),
                    Ui.numero(tBaja.getText(), "tarifa baja"), Ui.numero(tMedia.getText(), "tarifa media"), Ui.numero(tAlta.getText(), "tarifa alta"));
            refrescar.run();
        });
        Button modificar = Ui.boton("Modificar seleccionado", () -> {
            s.inventario().modificarApartamento(Ui.seleccion(t), nombre.getText(), (int) Ui.numero(dorm.getText(), "dormitorios"),
                    (int) Ui.numero(cap.getText(), "capacidad"), lista(carac.getText()),
                    Ui.numero(tBaja.getText(), "tarifa baja"), Ui.numero(tMedia.getText(), "tarifa media"), Ui.numero(tAlta.getText(), "tarifa alta"));
            refrescar.run();
        });
        Button eliminar = Ui.boton("Eliminar (lógico)", () -> {
            Apartamento a = Ui.seleccion(t);
            if (Ui.confirmar("¿Eliminar " + a + "? Se conserva su historial.")) { s.inventario().eliminarApartamento(a); refrescar.run(); }
        });
        Button fotoBtn = new Button("Adjuntar foto");
        fotoBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
            File f = fc.showOpenDialog(getScene().getWindow());
            if (f != null) Ui.accion(() -> { Apartamento a = Ui.seleccion(t); a.agregarFoto(f.getAbsolutePath()); Ui.mostrarFoto(foto, f.getAbsolutePath()); });
        });
        return Ui.caja(Ui.titulo("Apartamentos y tarifas por temporada"), t, g, Ui.barra(crear, modificar, eliminar, fotoBtn), foto);
    }
    private static List<String> lista(String csv) { return Arrays.asList(csv.split(",")); }

    // ---------------- Temporadas ----------------
    private Node temporadas() {
        TableView<PeriodoTemporada> t = Ui.tabla();
        Ui.col(t, "Temporada", p -> p.getTemporada().toString());
        Ui.col(t, "Desde", p -> p.getInicio().format(Formato.FECHA));
        Ui.col(t, "Hasta", p -> p.getFin().format(Formato.FECHA));
        Ui.col(t, "Descripción", PeriodoTemporada::getDescripcion);
        Runnable refrescar = () -> Ui.refrescar(t, s.getAlojamiento().getPeriodos());
        refrescar.run();
        ComboBox<Temporada> tipo = new ComboBox<>(FXCollections.observableArrayList(Temporada.MEDIA, Temporada.ALTA));
        tipo.setValue(Temporada.ALTA);
        DatePicker desde = new DatePicker(), hasta = new DatePicker();
        TextField desc = new TextField();
        desc.setPromptText("Ej.: Semana Santa");
        Button agregar = Ui.boton("Agregar periodo", () -> {
            s.inventario().definirPeriodo(tipo.getValue(), Ui.fecha(desde, "fecha de inicio"), Ui.fecha(hasta, "fecha de fin"), desc.getText());
            refrescar.run();
        });
        Button quitar = Ui.boton("Quitar seleccionado", () -> { s.inventario().eliminarPeriodo(Ui.seleccion(t)); refrescar.run(); });
        return Ui.caja(Ui.titulo("Temporadas (Baja = todas las fechas no cubiertas por Media o Alta)"), t,
                Ui.barra(tipo, new Label("Desde"), desde, new Label("Hasta"), hasta, desc), Ui.barra(agregar, quitar),
                new Label("Las tarifas por apartamento se definen en la pestaña 'Apartamentos y tarifas'."));
    }

    // ---------------- Política de cancelación ----------------
    private Node politica() {
        PoliticaCancelacion p = s.getAlojamiento().getPolitica();
        Spinner<Integer> dias = new Spinner<>(1, 90, p.getDiasDevolucionTotal());
        Spinner<Integer> pct = new Spinner<>(0, 100, p.getPorcentajeRetencionIntermedia());
        Spinner<Integer> horas = new Spinner<>(0, 2160, p.getHorasRetencionTotal());
        dias.setEditable(true); pct.setEditable(true); horas.setEditable(true);
        GridPane g = Ui.formulario();
        Ui.fila(g, 0, "Devolución total con más de (días)", dias);
        Ui.fila(g, 1, "Retención intermedia (%)", pct);
        Ui.fila(g, 2, "Retención del 100 % con menos de (horas)", horas);
        Button guardar = Ui.boton("Guardar política", () -> {
            s.inventario().configurarPolitica(dias.getValue(), pct.getValue(), horas.getValue());
            Ui.info("Política actualizada. Solo aplica a reservas nuevas: las existentes conservan la suya.");
        });
        return Ui.caja(Ui.titulo("Política de cancelación"), g, guardar,
                new Label("No-show: siempre se retiene el 100 %. El sistema calcula y registra la devolución, pero no mueve dinero real."));
    }

    // ---------------- Bloqueos ----------------
    private Node bloqueos() {
        TableView<Bloqueo> t = Ui.tabla();
        Ui.col(t, "Apartamento", Bloqueo::getCodigoApartamento);
        Ui.col(t, "Desde", b -> b.getInicio().format(Formato.FECHA));
        Ui.col(t, "Hasta", b -> b.getFin().format(Formato.FECHA));
        Ui.col(t, "Motivo", Bloqueo::getMotivo);
        Runnable refrescar = () -> Ui.refrescar(t, s.getAlojamiento().getBloqueos());
        refrescar.run();
        ComboBox<Apartamento> apto = new ComboBox<>(FXCollections.observableArrayList(s.inventario().apartamentosActivos()));
        apto.setOnShowing(e -> apto.getItems().setAll(s.inventario().apartamentosActivos()));
        DatePicker desde = new DatePicker(), hasta = new DatePicker();
        TextField motivo = new TextField();
        motivo.setPromptText("Motivo");
        Button agregar = Ui.boton("Registrar bloqueo", () -> {
            if (apto.getValue() == null) throw new sga.excepciones.ReglaNegocioException("Seleccione el apartamento");
            s.inventario().bloquear(apto.getValue(), Ui.fecha(desde, "fecha de inicio"), Ui.fecha(hasta, "fecha de fin"), motivo.getText());
            refrescar.run();
        });
        Button quitar = Ui.boton("Quitar bloqueo", () -> { s.inventario().quitarBloqueo(Ui.seleccion(t)); refrescar.run(); });
        return Ui.caja(Ui.titulo("Bloqueos de fechas"), t, Ui.barra(apto, new Label("Desde"), desde, new Label("Hasta"), hasta, motivo),
                Ui.barra(agregar, quitar));
    }

    // ---------------- Reportes ----------------
    private Node reportes() {
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList("Ocupación por apartamento", "Ingresos por periodo",
                "Reservas por canal (incluye canceladas y no-show)", "Saldos pendientes", "Apartamentos con más novedades",
                "Ocupación por temporada", "Comportamiento de cancelaciones"));
        tipo.setValue("Ocupación por apartamento");
        DatePicker desde = new DatePicker(s.getReloj().hoy().withDayOfMonth(1));
        DatePicker hasta = new DatePicker(s.getReloj().hoy().plusMonths(1).withDayOfMonth(1).minusDays(1));
        BorderPane salida = new BorderPane();
        Button generar = Ui.boton("Generar reporte", () -> {
            LocalDate d = Ui.fecha(desde, "fecha inicial"), h = Ui.fecha(hasta, "fecha final");
            ServicioReportes r = s.reportes();
            List<String[]> filas = new ArrayList<>();
            String[] enc;
            switch (tipo.getSelectionModel().getSelectedIndex()) {
                case 0 -> {
                    enc = new String[]{"Apartamento", "Noches ocupadas", "Noches del periodo", "% ocupación"};
                    for (var f : r.ocupacionPorApartamento(d, h))
                        filas.add(new String[]{f.apartamento().toString(), String.valueOf(f.nochesOcupadas()), String.valueOf(f.nochesPeriodo()), String.format("%.1f %%", f.porcentaje())});
                }
                case 1 -> {
                    enc = new String[]{"Concepto", "Valor"};
                    var i = r.ingresosPorPeriodo(d, h);
                    filas.add(new String[]{"Cobrado (pagos)", Formato.pesos(i.cobrado())});
                    filas.add(new String[]{"Devuelto", Formato.pesos(i.devuelto())});
                    filas.add(new String[]{"Comisiones canal externo", Formato.pesos(i.comisiones())});
                    filas.add(new String[]{"INGRESO NETO", Formato.pesos(i.neto())});
                }
                case 2 -> {
                    enc = new String[]{"Canal", "Total", "Pendientes", "Confirmadas", "En curso", "Finalizadas", "Canceladas", "No-show"};
                    for (var f : r.reservasPorCanal())
                        filas.add(new String[]{f.canal().toString(), String.valueOf(f.total()), String.valueOf(f.de(EstadoReserva.PENDIENTE)),
                                String.valueOf(f.de(EstadoReserva.CONFIRMADA)), String.valueOf(f.de(EstadoReserva.EN_CURSO)),
                                String.valueOf(f.de(EstadoReserva.FINALIZADA)), String.valueOf(f.de(EstadoReserva.CANCELADA)), String.valueOf(f.de(EstadoReserva.NO_SHOW))});
                }
                case 3 -> {
                    enc = new String[]{"Reserva", "Titular", "Apartamento", "Estado", "Saldo"};
                    for (Reserva x : r.saldosPendientes())
                        filas.add(new String[]{x.getId(), x.getTitular().getNombre(), x.getApartamento().getCodigo(), x.getEstado().toString(), Formato.pesos(x.getFolio().saldo())});
                }
                case 5 -> {
                    enc = new String[]{"Temporada", "Noches vendidas", "Ingresos liquidados"};
                    for (var f : r.ocupacionPorTemporada())
                        filas.add(new String[]{f.temporada().toString(), String.valueOf(f.nochesVendidas()), Formato.pesos(f.ingresos())});
                }
                case 6 -> {
                    enc = new String[]{"Indicador", "Valor"};
                    var c = r.estadisticasCancelaciones();
                    filas.add(new String[]{"Reservas totales", String.valueOf(c.totalReservas())});
                    filas.add(new String[]{"Canceladas", String.valueOf(c.canceladas())});
                    filas.add(new String[]{"No-show", String.valueOf(c.noShow())});
                    filas.add(new String[]{"% de cancelación (incluye no-show)", String.format("%.1f %%", c.porcentajeCancelacion())});
                    filas.add(new String[]{"Valor retenido", Formato.pesos(c.retenido())});
                    filas.add(new String[]{"Valor devuelto", Formato.pesos(c.devuelto())});
                }
                default -> {
                    enc = new String[]{"Apartamento", "Novedades"};
                    for (Apartamento a : r.apartamentosConMasNovedades())
                        filas.add(new String[]{a.toString(), String.valueOf(a.getNovedades().size())});
                }
            }
            salida.setCenter(Ui.tablaDinamica(enc, filas));
        });
        return Ui.caja(Ui.titulo("Reportes avanzados"),
                Ui.barra(tipo, new Label("Desde"), desde, new Label("Hasta"), hasta, generar), salida);
    }

    // ---------------- Simulador ----------------
    private Node simulador() {
        TextArea log = new TextArea();
        log.setEditable(false);
        Label conflictos = new Label();
        Runnable actualizar = () -> conflictos.setText("Conflictos de canal registrados: " + s.getAlojamiento().getConflictos().size());
        actualizar.run();
        Button uno = Ui.boton("Generar 1 reserva externa", () -> { log.appendText(s.simulador().generarReservaAleatoria() + "\n"); actualizar.run(); });
        Button diez = Ui.boton("Generar 10", () -> {
            for (int i = 0; i < 10; i++) log.appendText(s.simulador().generarReservaAleatoria() + "\n");
            actualizar.run();
        });
        Button ver = Ui.boton("Ver conflictos registrados", () -> {
            StringBuilder sb = new StringBuilder();
            for (ConflictoCanal c : s.getAlojamiento().getConflictos())
                sb.append(c.fecha().format(Formato.FECHA_HORA)).append(" · ").append(c.codigoExterno()).append(" · ").append(c.codigoApartamento())
                        .append(" · ").append(c.entrada()).append(" → ").append(c.salida()).append(" · ").append(c.motivo()).append("\n");
            Ui.textoLargo("Conflictos de canal", sb.length() == 0 ? "Sin conflictos" : sb.toString());
        });
        return Ui.caja(Ui.titulo("Simulador de plataforma externa"), Ui.barra(uno, diez, ver, conflictos), log);
    }
}