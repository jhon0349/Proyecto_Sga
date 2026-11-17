package co.edu.uniquindio.universidad.servicio;

import co.edu.uniquindio.universidad.excepciones.*;
import co.edu.uniquindio.universidad.modelo.*;
import co.edu.uniquindio.universidad.util.Reloj;
import co.edu.uniquindio.universidad.util.Validar;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Núcleo del negocio: disponibilidad, cotización, creación, ciclo de vida, folio y cancelación. */
public class ServicioReservas {
    private final Alojamiento alojamiento;
    private final ServicioInventario inventario;
    private final Reloj reloj;

    public ServicioReservas(Alojamiento alojamiento, ServicioInventario inventario, Reloj reloj) {
        this.alojamiento = alojamiento;
        this.inventario = inventario;
        this.reloj = reloj;
    }

    // ================= Validaciones =================
    private void validarFechas(LocalDate entrada, LocalDate salida) {
        Validar.noNulo(entrada, "fecha de entrada");
        Validar.noNulo(salida, "fecha de salida");
        if (!salida.isAfter(entrada)) throw new ReglaNegocioException("La fecha de salida debe ser posterior a la de entrada (mínimo una noche)");
        if (entrada.isBefore(reloj.hoy())) throw new ReglaNegocioException("No se pueden crear reservas con fecha de entrada anterior a hoy");
    }
    private void validarCapacidad(Apartamento a, int personas) {
        if (personas > a.getCapacidad())
            throw new CapacidadExcedidaException("El apartamento " + a.getCodigo() + " admite máximo " + a.getCapacidad()
                    + " personas (incluidos menores) y se registraron " + personas);
    }

    // ================= Disponibilidad =================
    private Reserva reservaEnConflicto(Apartamento a, LocalDate entrada, LocalDate salida) {
        return alojamiento.getReservas().stream()
                .filter(r -> r.getApartamento().getCodigo().equals(a.getCodigo()) && r.getEstado().esActiva() && r.chocaCon(entrada, salida))
                .findFirst().orElse(null);
    }
    private Bloqueo bloqueoEnConflicto(Apartamento a, LocalDate entrada, LocalDate salida) {
        return alojamiento.getBloqueos().stream()
                .filter(b -> b.getCodigoApartamento().equals(a.getCodigo()) && b.chocaConEstancia(entrada, salida))
                .findFirst().orElse(null);
    }
    public synchronized boolean estaDisponible(Apartamento a, LocalDate entrada, LocalDate salida) {
        liberarVencidas();
        return a.isActivo() && reservaEnConflicto(a, entrada, salida) == null && bloqueoEnConflicto(a, entrada, salida) == null;
    }
    public List<Apartamento> disponibles(LocalDate entrada, LocalDate salida, int personas) {
        validarFechas(entrada, salida);
        List<Apartamento> res = new ArrayList<>();
        for (Apartamento a : inventario.apartamentosActivos())
            if (a.getCapacidad() >= personas && estaDisponible(a, entrada, salida)) res.add(a);
        return res;
    }
    /** Estado de una noche concreta (para el calendario de ocupación). */
    public EstadoNoche estadoNoche(Apartamento a, LocalDate noche) {
        boolean ocupada = alojamiento.getReservas().stream().anyMatch(r -> r.getApartamento() == a
                && (r.getEstado().esActiva() || r.getEstado() == EstadoReserva.FINALIZADA) && r.incluyeNoche(noche));
        if (ocupada) return EstadoNoche.OCUPADA;
        boolean bloqueada = alojamiento.getBloqueos().stream()
                .anyMatch(b -> b.getCodigoApartamento().equals(a.getCodigo()) && b.bloqueaNoche(noche));
        return bloqueada ? EstadoNoche.BLOQUEADA : EstadoNoche.LIBRE;
    }

    // ================= Cotización =================
    /** @param alojados todas las personas que se alojan, titular incluido. */
    public Cotizacion cotizar(Apartamento a, LocalDate entrada, LocalDate salida, List<Ocupante> alojados) {
        validarFechas(entrada, salida);
        validarCapacidad(a, alojados.size());
        int pagantes = (int) alojados.stream().filter(o -> o.paga(entrada)).count(); // edad a la fecha de entrada
        List<NocheLiquidada> lineas = new ArrayList<>();
        for (LocalDate n = entrada; n.isBefore(salida); n = n.plusDays(1)) {
            Temporada t = inventario.temporadaDe(n);
            lineas.add(new NocheLiquidada(n, t, a.getTarifa(t), pagantes));
        }
        return new Cotizacion(lineas);
    }

    // ================= Creación =================
    public Reserva crearReservaPortal(Huesped titular, Apartamento a, LocalDate entrada, LocalDate salida, List<Ocupante> acompanantes) {
        return crear(CanalReserva.PORTAL, null, titular, a, entrada, salida, acompanantes);
    }
    public Reserva crearReservaDirecta(Huesped titular, Apartamento a, LocalDate entrada, LocalDate salida, List<Ocupante> acompanantes) {
        return crear(CanalReserva.DIRECTO, null, titular, a, entrada, salida, acompanantes);
    }
    public Reserva crearReservaExterna(String codigoExterno, Huesped titular, Apartamento a, LocalDate entrada, LocalDate salida, List<Ocupante> acompanantes) {
        return crear(CanalReserva.EXTERNO, codigoExterno, titular, a, entrada, salida, acompanantes);
    }

    /** Sincronizado: es la sección crítica que garantiza la regla "sin doble venta". */
    private synchronized Reserva crear(CanalReserva canal, String codigoExterno, Huesped titular, Apartamento a,
                                       LocalDate entrada, LocalDate salida, List<Ocupante> acompanantes) {
        liberarVencidas();
        Validar.noNulo(titular, "titular");
        Validar.noNulo(a, "apartamento");
        validarFechas(entrada, salida);
        if (!a.isActivo()) throw new ReglaNegocioException("El apartamento ya no está disponible en el catálogo");
        List<Ocupante> alojados = new ArrayList<>();
        alojados.add(Ocupante.de(titular));
        if (acompanantes != null) alojados.addAll(acompanantes);
        validarCapacidad(a, alojados.size());

        if (canal == CanalReserva.EXTERNO) {
            codigoExterno = Validar.texto(codigoExterno, "código externo");
            if (alojamiento.existeCodigoExterno(codigoExterno))
                throw new ReglaNegocioException("El código externo " + codigoExterno + " ya generó una reserva");
        }
        Reserva choque = reservaEnConflicto(a, entrada, salida);
        Bloqueo bloqueo = bloqueoEnConflicto(a, entrada, salida);
        if (choque != null || bloqueo != null) {
            String motivo = choque != null ? "Choca con la reserva vigente " + choque.getId()
                    : "El apartamento está bloqueado (" + bloqueo.getMotivo() + ")";
            if (canal == CanalReserva.EXTERNO) {
                alojamiento.agregarConflicto(new ConflictoCanal(codigoExterno, a.getCodigo(), entrada, salida, motivo, reloj.ahora()));
                throw new ConflictoCanalException("Conflicto de canal: " + motivo);
            }
            if (choque != null) throw new DobleVentaException("El apartamento ya está reservado en esas noches (" + choque.getId() + ")");
            throw new ReglaNegocioException(motivo);
        }

        Cotizacion cot = cotizar(a, entrada, salida, alojados);
        String id = alojamiento.siguienteIdReserva();
        LocalDateTime ahora = reloj.ahora();
        PoliticaCancelacion pol = alojamiento.getPolitica(); // inmutable -> queda congelada
        Reserva r = switch (canal) {
            case PORTAL -> new ReservaPortal(id, titular, a, entrada, salida, alojados, cot, pol, ahora);
            case DIRECTO -> new ReservaDirecta(id, titular, a, entrada, salida, alojados, cot, pol, ahora);
            case EXTERNO -> new ReservaExterna(codigoExterno, id, titular, a, entrada, salida, alojados, cot, pol, ahora);
        };
        alojamiento.agregarReserva(r);
        if (canal == CanalReserva.EXTERNO) {
            alojamiento.registrarCodigoExterno(codigoExterno);
            r.cambiarEstado(EstadoReserva.CONFIRMADA); // la plataforma ya garantiza la reserva
        }
        return r;
    }

    // ================= Vencimiento (24 h) =================
    public synchronized List<Reserva> liberarVencidas() {
        List<Reserva> vencidas = new ArrayList<>();
        LocalDateTime ahora = reloj.ahora();
        for (Reserva r : alojamiento.getReservas()) {
            if (r.venceConfirmacion(ahora)) {
                cerrarConRetencion(r, EstadoReserva.CANCELADA, 0); // vencimiento: se devuelve todo lo pagado
                vencidas.add(r);
            }
        }
        return vencidas;
    }

    // ================= Ciclo de vida =================
    public synchronized void confirmar(Reserva r) {
        liberarVencidas();
        if (r.getEstado() != EstadoReserva.PENDIENTE) throw new TransicionInvalidaException("Solo se confirman reservas Pendientes");
        if (r.getFolio().totalPagos() < r.anticipoRequerido())
            throw new ReglaNegocioException("Falta el anticipo de " + r.anticipoRequerido() + " para confirmar la reserva");
        r.cambiarEstado(EstadoReserva.CONFIRMADA);
    }

    public synchronized void checkIn(Reserva r) {
        liberarVencidas();
        if (r.getEstado() != EstadoReserva.CONFIRMADA) throw new TransicionInvalidaException("El check-in exige una reserva Confirmada");
        if (reloj.hoy().isBefore(r.getEntrada())) throw new ReglaNegocioException("Aún no llega la fecha de entrada (" + r.getEntrada() + ")");
        Apartamento a = r.getApartamento();
        if (a.getEstado() != EstadoApartamento.PREPARADO)
            throw new ReglaNegocioException("El apartamento no está Preparado (estado actual: " + a.getEstado() + ")");
        r.cambiarEstado(EstadoReserva.EN_CURSO);
        a.cambiarEstado(EstadoApartamento.OCUPADO, null);
    }

    public synchronized void checkOut(Reserva r) {
        r.cambiarEstado(EstadoReserva.FINALIZADA); // valida que esté En curso
        r.getApartamento().cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION, null);
    }

    public synchronized void declararNoShow(Reserva r) {
        liberarVencidas();
        if (r.getEstado() != EstadoReserva.CONFIRMADA) throw new TransicionInvalidaException("Solo una reserva Confirmada puede ser no-show");
        if (reloj.ahora().isBefore(r.getLimiteNoShow()))
            throw new ReglaNegocioException("El no-show solo se declara después de las 10:00 p. m. del día de entrada");
        long ret = r.getPolitica().retencion(r.getValorEstancia(), Duration.ZERO, true);
        cerrarConRetencion(r, EstadoReserva.NO_SHOW, ret);
    }

    // ================= Cancelación =================
    /** Qué pasaría si se cancelara ahora (el huésped lo ve antes de confirmar). */
    public ResultadoCancelacion calcularCancelacion(Reserva r) {
        if (!r.getEstado().puedePasarA(EstadoReserva.CANCELADA))
            throw new TransicionInvalidaException("Una reserva " + r.getEstado() + " no se puede cancelar");
        Duration antelacion = Duration.between(reloj.ahora(), r.getFechaHoraEntrada());
        int pct = r.getPolitica().porcentajeRetencion(antelacion, false);
        long retencion = r.getPolitica().retencion(r.getValorEstancia(), antelacion, false);
        return resultado(r, pct, retencion);
    }
    private ResultadoCancelacion resultado(Reserva r, int pct, long retencion) {
        long pagado = Math.max(0, r.getFolio().totalPagos());
        long retenido = Math.min(retencion, pagado);   // solo se retiene lo efectivamente pagado
        return new ResultadoCancelacion(pct, retenido, pagado - retenido);
    }

    public synchronized ResultadoCancelacion cancelar(Reserva r) {
        liberarVencidas();
        ResultadoCancelacion res = calcularCancelacion(r);
        long retencion = r.getPolitica().retencion(r.getValorEstancia(),
                Duration.between(reloj.ahora(), r.getFechaHoraEntrada()), false);
        cerrarConRetencion(r, EstadoReserva.CANCELADA, retencion);
        return res;
    }

    /** Registra la devolución en el folio (no mueve dinero real) y libera las noches al dejar de estar activa. */
    private void cerrarConRetencion(Reserva r, EstadoReserva destino, long retencionPolitica) {
        r.cambiarEstado(destino);
        LocalDateTime ahora = reloj.ahora();
        ResultadoCancelacion res = resultado(r, 0, Math.max(0, retencionPolitica));
        Folio f = r.getFolio();
        f.revertirAlojamiento(ahora, destino.getEtiqueta());
        if (res.retenido() > 0)
            f.agregarCargo(new Cargo(TipoCargo.PENALIDAD, "Retención por " + destino.getEtiqueta().toLowerCase(), res.retenido(), ahora));
        if (res.devolucion() > 0) {
            MedioPago medio = f.getPagos().isEmpty() ? MedioPago.TRANSFERENCIA : f.getPagos().get(f.getPagos().size() - 1).getMedio();
            f.agregarPago(new Pago(medio, -res.devolucion(), ahora, "Devolución por " + destino.getEtiqueta().toLowerCase()));
        }
        r.cerrarEconomia(res.retenido(), res.devolucion());
    }

    // ================= Folio =================
    public synchronized Pago registrarPago(Reserva r, MedioPago medio, long valor) {
        liberarVencidas();
        if (!r.getEstado().esActiva()) throw new ReglaNegocioException("Solo se registran pagos en reservas activas (estado: " + r.getEstado() + ")");
        Validar.positivo(valor, "valor del pago");
        if (valor > r.getFolio().saldo()) throw new ReglaNegocioException("El pago supera el saldo del folio (" + r.getFolio().saldo() + ")");
        Pago p = new Pago(medio, valor, reloj.ahora(), "Pago " + medio);
        r.getFolio().agregarPago(p);
        // Portal: la reserva se confirma sola al completarse el anticipo del 30 %
        if (r.getCanal() == CanalReserva.PORTAL && r.getEstado() == EstadoReserva.PENDIENTE
                && r.getFolio().totalPagos() >= r.anticipoRequerido())
            r.cambiarEstado(EstadoReserva.CONFIRMADA);
        return p;
    }

    public synchronized Cargo registrarCargoServicio(Reserva r, String descripcion, long valor) {
        if (r.getEstado() != EstadoReserva.EN_CURSO) throw new ReglaNegocioException("Los servicios se cargan mientras la reserva está En curso");
        Validar.positivo(valor, "valor del servicio");
        Cargo c = new Cargo(TipoCargo.SERVICIO, Validar.texto(descripcion, "descripción"), valor, reloj.ahora());
        r.getFolio().agregarCargo(c);
        return c;
    }
    public void corregirCargo(Reserva r, int indice, String motivo) { r.getFolio().revertirCargo(indice, reloj.ahora(), motivo); }
    public void corregirPago(Reserva r, int indice, String motivo) { r.getFolio().revertirPago(indice, reloj.ahora(), motivo); }

    // ================= Consultas =================
    public List<Reserva> todas() { return alojamiento.getReservas(); }
    public List<Reserva> reservasDe(Huesped h) {
        return alojamiento.getReservas().stream().filter(r -> r.getTitular().getDocumento().equals(h.getDocumento())).toList();
    }
    public Reserva buscar(String id) {
        return alojamiento.getReservas().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }
}
