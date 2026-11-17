package co.edu.uniquindio.universidad.modelo;


import sga.excepciones.ReglaNegocioException;
import sga.excepciones.TransicionInvalidaException;
import sga.util.Validar;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase abstracta: abstrae lo común de los tres canales (Portal, Directa, Externa).
 * El valor y la política de cancelación quedan CONGELADOS al crear la reserva.
 */
public abstract class Reserva implements Serializable {
    private final String id;
    private final Huesped titular;
    private final Apartamento apartamento;
    private final LocalDate entrada;
    private final LocalDate salida;
    private final List<Ocupante> alojados; // incluye al titular
    private final Cotizacion cotizacion;   // valor congelado + desglose
    private final PoliticaCancelacion politica; // política congelada
    private final LocalDateTime creadaEn;
    private final Folio folio = new Folio();
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    private long retenido;
    private long devuelto;

    protected Reserva(String id, Huesped titular, Apartamento apartamento, LocalDate entrada, LocalDate salida,
                      List<Ocupante> alojados, Cotizacion cotizacion, PoliticaCancelacion politica, LocalDateTime creadaEn) {
        this.id = Validar.texto(id, "id");
        this.titular = Validar.noNulo(titular, "titular");
        this.apartamento = Validar.noNulo(apartamento, "apartamento");
        this.entrada = Validar.noNulo(entrada, "fecha de entrada");
        this.salida = Validar.noNulo(salida, "fecha de salida");
        if (!salida.isAfter(entrada)) throw new ReglaNegocioException("La fecha de salida debe ser posterior a la de entrada (mínimo una noche)");
        if (alojados == null || alojados.isEmpty()) throw new ReglaNegocioException("La reserva debe tener al menos un ocupante");
        this.alojados = new ArrayList<>(alojados);
        this.cotizacion = Validar.noNulo(cotizacion, "cotización");
        this.politica = Validar.noNulo(politica, "política de cancelación");
        this.creadaEn = Validar.noNulo(creadaEn, "fecha de creación");
        if (cotizacion.getTotal() > 0)
            folio.agregarCargo(new Cargo(TipoCargo.ALOJAMIENTO,
                    "Alojamiento " + apartamento.getCodigo() + " (" + noches() + " noches)", cotizacion.getTotal(), creadaEn));
    }

    // ---- Polimorfismo: cada canal define sus reglas económicas ----
    public abstract CanalReserva getCanal();
    /** Anticipo necesario para confirmar. */
    public abstract long anticipoRequerido();
    /** Comisión que el alojamiento paga al canal. */
    public abstract long comision();

    /** Cambia de estado solo por las transiciones permitidas; cualquier otra se rechaza. */
    public void cambiarEstado(EstadoReserva nuevo) {
        if (!estado.puedePasarA(nuevo))
            throw new TransicionInvalidaException("Transición no permitida: " + estado + " → " + nuevo);
        this.estado = nuevo;
    }
    public void cerrarEconomia(long retenido, long devuelto) { this.retenido = retenido; this.devuelto = devuelto; }

    public int noches() { return (int) ChronoUnit.DAYS.between(entrada, salida); }
    public boolean incluyeNoche(LocalDate noche) { return !noche.isBefore(entrada) && noche.isBefore(salida); }
    /** Dos estancias comparten al menos una noche si cada una empieza antes de que la otra termine. */
    public boolean chocaCon(LocalDate otraEntrada, LocalDate otraSalida) { return entrada.isBefore(otraSalida) && otraEntrada.isBefore(salida); }
    public boolean venceConfirmacion(LocalDateTime ahora) {
        return estado == EstadoReserva.PENDIENTE && !ahora.isBefore(creadaEn.plusHours(Reglas.HORAS_PLAZO_CONFIRMACION));
    }
    public LocalDateTime getFechaHoraEntrada() { return entrada.atTime(Reglas.HORA_ENTRADA); }
    public LocalDateTime getFechaHoraSalida() { return salida.atTime(Reglas.HORA_SALIDA); }
    public LocalDateTime getLimiteNoShow() { return entrada.atTime(Reglas.HORA_LIMITE_NO_SHOW); }
    public long getValorEstancia() { return cotizacion.getTotal(); }
    public int getPagantes() { return (int) alojados.stream().filter(o -> o.paga(entrada)).count(); }

    public String getId() { return id; }
    public Huesped getTitular() { return titular; }
    public Apartamento getApartamento() { return apartamento; }
    public LocalDate getEntrada() { return entrada; }
    public LocalDate getSalida() { return salida; }
    public List<Ocupante> getAlojados() { return Collections.unmodifiableList(alojados); }
    public Cotizacion getCotizacion() { return cotizacion; }
    public PoliticaCancelacion getPolitica() { return politica; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public Folio getFolio() { return folio; }
    public EstadoReserva getEstado() { return estado; }
    public long getRetenido() { return retenido; }
    public long getDevuelto() { return devuelto; }
}
