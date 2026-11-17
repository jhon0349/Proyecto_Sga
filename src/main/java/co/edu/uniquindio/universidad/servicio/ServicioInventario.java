package co.edu.uniquindio.universidad.servicio;

import sga.excepciones.ReglaNegocioException;
import sga.modelo.*;
import sga.util.Reloj;
import sga.util.Validar;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/** Apartamentos, temporadas, tarifas, bloqueos, novedades y política de cancelación. */
public class ServicioInventario {
    private final Alojamiento alojamiento;
    private final Reloj reloj;

    public ServicioInventario(Alojamiento alojamiento, Reloj reloj) {
        this.alojamiento = alojamiento;
        this.reloj = reloj;
    }

    // ---------- Apartamentos ----------
    public Apartamento crearApartamento(String codigo, String nombre, int dormitorios, int capacidad,
                                        Collection<String> caracteristicas, long tarifaBaja, long tarifaMedia, long tarifaAlta) {
        if (alojamiento.buscarApartamento(Validar.texto(codigo, "código")) != null)
            throw new ReglaNegocioException("Ya existe un apartamento con el código " + codigo);
        Apartamento a = new Apartamento(codigo, nombre, dormitorios, capacidad, tarifaBaja, tarifaMedia, tarifaAlta);
        a.setCaracteristicas(caracteristicas);
        alojamiento.agregarApartamento(a);
        return a;
    }

    /** Un cambio de tarifas no afecta reservas existentes (valor congelado en cada reserva). */
    public void modificarApartamento(Apartamento a, String nombre, int dormitorios, int capacidad,
                                     Collection<String> caracteristicas, long tarifaBaja, long tarifaMedia, long tarifaAlta) {
        a.setNombre(nombre);
        a.setDormitorios(dormitorios);
        a.setCapacidad(capacidad);
        a.setCaracteristicas(caracteristicas);
        a.setTarifa(Temporada.BAJA, tarifaBaja);
        a.setTarifa(Temporada.MEDIA, tarifaMedia);
        a.setTarifa(Temporada.ALTA, tarifaAlta);
    }

    /** Eliminación lógica: se conserva el historial. */
    public void eliminarApartamento(Apartamento a) {
        boolean conReservas = alojamiento.getReservas().stream()
                .anyMatch(r -> r.getApartamento() == a && r.getEstado().esActiva());
        if (conReservas) throw new ReglaNegocioException("El apartamento tiene reservas activas; no puede eliminarse");
        a.eliminarLogicamente();
    }

    public List<Apartamento> apartamentosActivos() {
        return alojamiento.getApartamentos().stream().filter(Apartamento::isActivo).toList();
    }

    /** Recepción cambia el estado operativo. OCUPADO solo lo gestionan el check-in y el check-out. */
    public void cambiarEstado(Apartamento a, EstadoApartamento nuevo, String motivo) {
        if (a.getEstado() == EstadoApartamento.OCUPADO)
            throw new ReglaNegocioException("El apartamento está ocupado: su estado cambia con el check-out");
        if (nuevo == EstadoApartamento.OCUPADO)
            throw new ReglaNegocioException("El estado Ocupado solo se asigna con el check-in de una reserva");
        a.cambiarEstado(nuevo, motivo);
    }

    public Novedad registrarNovedad(Apartamento a, String descripcion, String autor) {
        Novedad n = new Novedad(reloj.ahora(), descripcion, autor);
        a.registrarNovedad(n);
        return n;
    }

    // ---------- Temporadas ----------
    public PeriodoTemporada definirPeriodo(Temporada t, LocalDate inicio, LocalDate fin, String descripcion) {
        PeriodoTemporada p = new PeriodoTemporada(t, inicio, fin, descripcion);
        alojamiento.agregarPeriodo(p);
        return p;
    }
    public void eliminarPeriodo(PeriodoTemporada p) { alojamiento.eliminarPeriodo(p); }

    /** Alta prevalece sobre Media; todo lo no cubierto es Baja (temporada base). */
    public Temporada temporadaDe(LocalDate fecha) {
        Temporada res = Temporada.BAJA;
        for (PeriodoTemporada p : alojamiento.getPeriodos()) {
            if (!p.contiene(fecha)) continue;
            if (p.getTemporada() == Temporada.ALTA) return Temporada.ALTA;
            res = Temporada.MEDIA;
        }
        return res;
    }

    public void definirTarifa(Apartamento a, Temporada t, long valor) { a.setTarifa(t, valor); }

    // ---------- Política ----------
    public void configurarPolitica(int diasDevolucionTotal, int porcentajeIntermedio, int horasRetencionTotal) {
        alojamiento.setPolitica(new PoliticaCancelacion(diasDevolucionTotal, porcentajeIntermedio, horasRetencionTotal));
    }

    // ---------- Bloqueos ----------
    public Bloqueo bloquear(Apartamento a, LocalDate inicio, LocalDate fin, String motivo) {
        Bloqueo b = new Bloqueo(a.getCodigo(), inicio, fin, motivo);
        for (Reserva r : alojamiento.getReservas()) {
            if (r.getApartamento() == a && r.getEstado().esActiva() && b.chocaConEstancia(r.getEntrada(), r.getSalida()))
                throw new ReglaNegocioException("El bloqueo choca con la reserva activa " + r.getId());
        }
        alojamiento.agregarBloqueo(b);
        return b;
    }
    public void quitarBloqueo(Bloqueo b) { alojamiento.eliminarBloqueo(b); }
}
