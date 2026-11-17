package co.edu.uniquindio.universidad.modelo;


import co.edu.uniquindio.universidad.excepciones.ReglaNegocioException;
import co.edu.uniquindio.universidad.util.Validar;

import java.io.Serializable;
import java.time.LocalDate;

/** Rango de fechas (ambas inclusive, como noches) en que un apartamento no se puede vender. */
public class Bloqueo implements Serializable {
    private final String codigoApartamento;
    private final LocalDate inicio;
    private final LocalDate fin;
    private final String motivo;

    public Bloqueo(String codigoApartamento, LocalDate inicio, LocalDate fin, String motivo) {
        this.codigoApartamento = Validar.texto(codigoApartamento, "apartamento");
        this.inicio = Validar.noNulo(inicio, "fecha de inicio");
        this.fin = Validar.noNulo(fin, "fecha de fin");
        if (fin.isBefore(inicio)) throw new ReglaNegocioException("La fecha final del bloqueo no puede ser anterior a la inicial");
        this.motivo = Validar.texto(motivo, "motivo");
    }
    public String getCodigoApartamento() { return codigoApartamento; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFin() { return fin; }
    public String getMotivo() { return motivo; }
    public boolean bloqueaNoche(LocalDate noche) { return !noche.isBefore(inicio) && !noche.isAfter(fin); }
    /** ¿Alguna noche de la estancia [entrada, salida) cae dentro del bloqueo? */
    public boolean chocaConEstancia(LocalDate entrada, LocalDate salida) { return inicio.isBefore(salida) && !fin.isBefore(entrada); }
}
