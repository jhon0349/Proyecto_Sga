package co.edu.uniquindio.universidad.modelo;


import sga.excepciones.ReglaNegocioException;
import sga.util.Validar;

import java.io.Serializable;
import java.util.*;

/** Unidad vendible. Se entrega completa a un solo grupo; los dormitorios son solo descriptivos. */
public class Apartamento implements Serializable {
    private final String codigo;
    private String nombre;
    private int dormitorios;
    private int capacidad;
    private final List<String> caracteristicas = new ArrayList<>();
    private final Map<Temporada, Long> tarifas = new EnumMap<>(Temporada.class); // por persona por noche
    private EstadoApartamento estado = EstadoApartamento.PREPARADO;
    private String motivoFueraServicio;
    private boolean activo = true; // eliminación lógica
    private final List<Novedad> novedades = new ArrayList<>();
    private final List<String> fotos = new ArrayList<>();

    public Apartamento(String codigo, String nombre, int dormitorios, int capacidad,
                       long tarifaBaja, long tarifaMedia, long tarifaAlta) {
        this.codigo = Validar.texto(codigo, "código");
        setNombre(nombre);
        setDormitorios(dormitorios);
        setCapacidad(capacidad);
        setTarifa(Temporada.BAJA, tarifaBaja);
        setTarifa(Temporada.MEDIA, tarifaMedia);
        setTarifa(Temporada.ALTA, tarifaAlta);
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = Validar.texto(v, "nombre"); }
    public int getDormitorios() { return dormitorios; }
    public void setDormitorios(int v) { this.dormitorios = (int) Validar.positivo(v, "dormitorios"); }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int v) { this.capacidad = (int) Validar.positivo(v, "capacidad"); }
    public List<String> getCaracteristicas() { return Collections.unmodifiableList(caracteristicas); }
    public void setCaracteristicas(Collection<String> c) {
        caracteristicas.clear();
        for (String s : c) if (s != null && !s.isBlank()) caracteristicas.add(s.trim());
    }
    public long getTarifa(Temporada t) { return tarifas.get(t); }
    public void setTarifa(Temporada t, long valor) { tarifas.put(Validar.noNulo(t, "temporada"), Validar.positivo(valor, "tarifa")); }
    public EstadoApartamento getEstado() { return estado; }
    public String getMotivoFueraServicio() { return motivoFueraServicio; }
    public boolean isActivo() { return activo; }
    public void eliminarLogicamente() { this.activo = false; }
    public List<Novedad> getNovedades() { return Collections.unmodifiableList(novedades); }
    public void registrarNovedad(Novedad n) { novedades.add(Validar.noNulo(n, "novedad")); }
    public List<String> getFotos() { return Collections.unmodifiableList(fotos); }
    public void agregarFoto(String ruta) { fotos.add(Validar.texto(ruta, "foto")); }

    /** Cambia el estado operativo; Fuera de servicio exige motivo. */
    public void cambiarEstado(EstadoApartamento nuevo, String motivo) {
        Validar.noNulo(nuevo, "estado");
        if (nuevo == EstadoApartamento.FUERA_DE_SERVICIO) {
            if (motivo == null || motivo.isBlank())
                throw new ReglaNegocioException("Debe registrar el motivo para dejar el apartamento fuera de servicio");
            this.motivoFueraServicio = motivo.trim();
        } else {
            this.motivoFueraServicio = null;
        }
        this.estado = nuevo;
    }

    @Override public String toString() { return codigo + " - " + nombre; }
}
