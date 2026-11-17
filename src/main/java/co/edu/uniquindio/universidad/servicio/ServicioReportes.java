package co.edu.uniquindio.universidad.servicio;

import sga.modelo.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Reportes avanzados del administrador. */
public class ServicioReportes {
    private final Alojamiento alojamiento;

    public ServicioReportes(Alojamiento alojamiento) { this.alojamiento = alojamiento; }

    public record FilaOcupacion(Apartamento apartamento, long nochesOcupadas, long nochesPeriodo) {
        public double porcentaje() { return nochesPeriodo == 0 ? 0 : 100.0 * nochesOcupadas / nochesPeriodo; }
    }
    public record IngresosPeriodo(long cobrado, long devuelto, long comisiones) {
        public long neto() { return cobrado - devuelto - comisiones; }
    }
    public record FilaCanal(CanalReserva canal, Map<EstadoReserva, Long> porEstado) {
        public long total() { return porEstado.values().stream().mapToLong(Long::longValue).sum(); }
        public long de(EstadoReserva e) { return porEstado.getOrDefault(e, 0L); }
    }

    /** Ocupación por apartamento en [desde, hasta] (ambas fechas inclusive, contadas como noches). */
    public List<FilaOcupacion> ocupacionPorApartamento(LocalDate desde, LocalDate hasta) {
        LocalDate finExclusivo = hasta.plusDays(1);
        long nochesPeriodo = Math.max(0, ChronoUnit.DAYS.between(desde, finExclusivo));
        List<FilaOcupacion> filas = new ArrayList<>();
        for (Apartamento a : alojamiento.getApartamentos()) {
            if (!a.isActivo()) continue;
            long ocupadas = 0;
            for (Reserva r : alojamiento.getReservas()) {
                boolean cuenta = r.getApartamento() == a && (r.getEstado() == EstadoReserva.CONFIRMADA
                        || r.getEstado() == EstadoReserva.EN_CURSO || r.getEstado() == EstadoReserva.FINALIZADA);
                if (!cuenta) continue;
                LocalDate ini = r.getEntrada().isAfter(desde) ? r.getEntrada() : desde;
                LocalDate fin = r.getSalida().isBefore(finExclusivo) ? r.getSalida() : finExclusivo;
                ocupadas += Math.max(0, ChronoUnit.DAYS.between(ini, fin));
            }
            filas.add(new FilaOcupacion(a, ocupadas, nochesPeriodo));
        }
        return filas;
    }

    /** Ingresos por periodo: pagos recibidos, devoluciones y comisiones de reservas externas con entrada en el rango. */
    public IngresosPeriodo ingresosPorPeriodo(LocalDate desde, LocalDate hasta) {
        long cobrado = 0, devuelto = 0, comisiones = 0;
        for (Reserva r : alojamiento.getReservas()) {
            for (Pago p : r.getFolio().getPagos()) {
                LocalDate f = p.getFecha().toLocalDate();
                if (f.isBefore(desde) || f.isAfter(hasta)) continue;
                if (p.getValor() > 0) cobrado += p.getValor(); else devuelto += -p.getValor();
            }
            boolean vigente = r.getEstado() != EstadoReserva.CANCELADA && r.getEstado() != EstadoReserva.NO_SHOW;
            if (vigente && !r.getEntrada().isBefore(desde) && !r.getEntrada().isAfter(hasta)) comisiones += r.comision();
        }
        return new IngresosPeriodo(cobrado, devuelto, comisiones);
    }

    /** Reservas por canal, incluidas canceladas y no-show. */
    public List<FilaCanal> reservasPorCanal() {
        List<FilaCanal> filas = new ArrayList<>();
        for (CanalReserva c : CanalReserva.values()) {
            Map<EstadoReserva, Long> m = new EnumMap<>(EstadoReserva.class);
            for (Reserva r : alojamiento.getReservas()) if (r.getCanal() == c) m.merge(r.getEstado(), 1L, Long::sum);
            filas.add(new FilaCanal(c, m));
        }
        return filas;
    }

    public List<Reserva> saldosPendientes() {
        return alojamiento.getReservas().stream().filter(r -> r.getFolio().saldo() > 0).toList();
    }

    public List<Apartamento> apartamentosConMasNovedades() {
        return alojamiento.getApartamentos().stream()
                .filter(a -> !a.getNovedades().isEmpty())
                .sorted(Comparator.comparingInt((Apartamento a) -> a.getNovedades().size()).reversed())
                .toList();
    }

    // ---------------- Funcionalidad extendida ----------------
    public record FilaTemporada(Temporada temporada, long nochesVendidas, long ingresos) {}
    public record ResumenCancelaciones(long totalReservas, long canceladas, long noShow, long retenido, long devuelto) {
        public double porcentajeCancelacion() { return totalReservas == 0 ? 0 : 100.0 * (canceladas + noShow) / totalReservas; }
    }

    /** Ocupación por temporada: noches vendidas e ingresos liquidados (valor congelado) de las reservas que ocupan o ocuparon el apartamento. */
    public List<FilaTemporada> ocupacionPorTemporada() {
        Map<Temporada, long[]> acumulado = new EnumMap<>(Temporada.class);
        for (Temporada t : Temporada.values()) acumulado.put(t, new long[2]);
        for (Reserva r : alojamiento.getReservas()) {
            boolean cuenta = r.getEstado() == EstadoReserva.CONFIRMADA || r.getEstado() == EstadoReserva.EN_CURSO
                    || r.getEstado() == EstadoReserva.FINALIZADA;
            if (!cuenta) continue;
            for (NocheLiquidada n : r.getCotizacion().getNoches()) {
                long[] fila = acumulado.get(n.temporada());
                fila[0]++;
                fila[1] += n.subtotal();
            }
        }
        List<FilaTemporada> filas = new ArrayList<>();
        for (Temporada t : Temporada.values()) filas.add(new FilaTemporada(t, acumulado.get(t)[0], acumulado.get(t)[1]));
        return filas;
    }

    /** Comportamiento de las cancelaciones: cuántas hubo, cuántos no-show y cuánto se retuvo y se devolvió. */
    public ResumenCancelaciones estadisticasCancelaciones() {
        long canceladas = 0, noShow = 0, retenido = 0, devuelto = 0;
        for (Reserva r : alojamiento.getReservas()) {
            if (r.getEstado() == EstadoReserva.CANCELADA) canceladas++;
            if (r.getEstado() == EstadoReserva.NO_SHOW) noShow++;
            retenido += r.getRetenido();
            devuelto += r.getDevuelto();
        }
        return new ResumenCancelaciones(alojamiento.getReservas().size(), canceladas, noShow, retenido, devuelto);
    }

    /** Historial de estancias de un huésped: sus reservas Finalizadas, de la más reciente a la más antigua. */
    public List<Reserva> historialDe(Huesped huesped) {
        return alojamiento.getReservas().stream()
                .filter(r -> r.getTitular().getDocumento().equals(huesped.getDocumento()) && r.getEstado() == EstadoReserva.FINALIZADA)
                .sorted(Comparator.comparing(Reserva::getEntrada).reversed())
                .toList();
    }
}
