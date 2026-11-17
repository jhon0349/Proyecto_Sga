package co.edu.uniquindio.universidad.modelo;

import java.time.LocalDateTime;

public interface Movimiento {
    long getValor();
    LocalDateTime getFecha();
    String getDescripcion();
}

