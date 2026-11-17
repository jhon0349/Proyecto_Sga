package co.edu.uniquindio.universidad.modelo;

import java.io.Serializable;
import java.util.*;

/**
 * Raíz del agregado: guarda toda la información de Aparta-Suites UQ (un único alojamiento).
 * Expone vistas de solo lectura; solo los servicios modifican las colecciones mediante estos métodos.
 */
public class Alojamiento implements Serializable {
    private final String nombre = "Aparta-Suites UQ";
    private final Map<String, Apartamento> apartamentos = new LinkedHashMap<>();
    private final List<Huesped> huespedes = new ArrayList<>();
    private final List<Empleado> empleados = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();
    private final List<Bloqueo> bloqueos = new ArrayList<>();
    private final List<PeriodoTemporada> periodos = new ArrayList<>();
    private final Set<String> codigosExternos = new HashSet<>();
    private final List<ConflictoCanal> conflictos = new ArrayList<>();
    private PoliticaCancelacion politica = PoliticaCancelacion.porDefecto();
    private int consecutivoReserva;

    public String getNombre() { return nombre; }
    public String siguienteIdReserva() { return String.format("R-%05d", ++consecutivoReserva); }

    public Collection<Apartamento> getApartamentos() { return Collections.unmodifiableCollection(apartamentos.values()); }
    public Apartamento buscarApartamento(String codigo) { return apartamentos.get(codigo); }
    public void agregarApartamento(Apartamento a) { apartamentos.put(a.getCodigo(), a); }

    public List<Huesped> getHuespedes() { return Collections.unmodifiableList(huespedes); }
    public void agregarHuesped(Huesped h) { huespedes.add(h); }
    public List<Empleado> getEmpleados() { return Collections.unmodifiableList(empleados); }
    public void agregarEmpleado(Empleado e) { empleados.add(e); }

    public List<Reserva> getReservas() { return Collections.unmodifiableList(reservas); }
    public void agregarReserva(Reserva r) { reservas.add(r); }

    public List<Bloqueo> getBloqueos() { return Collections.unmodifiableList(bloqueos); }
    public void agregarBloqueo(Bloqueo b) { bloqueos.add(b); }
    public void eliminarBloqueo(Bloqueo b) { bloqueos.remove(b); }

    public List<PeriodoTemporada> getPeriodos() { return Collections.unmodifiableList(periodos); }
    public void agregarPeriodo(PeriodoTemporada p) { periodos.add(p); }
    public void eliminarPeriodo(PeriodoTemporada p) { periodos.remove(p); }

    public boolean existeCodigoExterno(String c) { return codigosExternos.contains(c); }
    public void registrarCodigoExterno(String c) { codigosExternos.add(c); }
    public List<ConflictoCanal> getConflictos() { return Collections.unmodifiableList(conflictos); }
    public void agregarConflicto(ConflictoCanal c) { conflictos.add(c); }

    public PoliticaCancelacion getPolitica() { return politica; }
    public void setPolitica(PoliticaCancelacion p) { this.politica = p; }
}
