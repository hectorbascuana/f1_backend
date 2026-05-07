package com.f1manager.backend.dto;

import lombok.Data;

/**
 * Request para procesar una vuelta de carrera.
 * El jugador indica si quiere hacer pit stop para cada uno de sus pilotos.
 */
@Data
public class CarreraVueltaRequestDTO {
    private boolean pitStopPiloto1;             // ¿Pit stop para titular 1?
    private String nuevoCompuestoPiloto1;        // "BLANDO", "MEDIO", "DURO" (solo si pitStop=true)
    private boolean pitStopPiloto2;             // ¿Pit stop para titular 2?
    private String nuevoCompuestoPiloto2;        // "BLANDO", "MEDIO", "DURO" (solo si pitStop=true)
}
