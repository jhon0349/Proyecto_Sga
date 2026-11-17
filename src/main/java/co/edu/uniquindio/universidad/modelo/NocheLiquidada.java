package co.edu.uniquindio.universidad.modelo;

import java.io.Serializable;
import java.time.LocalDate;

public record NocheLiquidada(LocalDate noche, Temporada temporada, long tarifa, int pagantes) implements Serializable {
    public long subtotal() { return tarifa * pagantes; }
}
