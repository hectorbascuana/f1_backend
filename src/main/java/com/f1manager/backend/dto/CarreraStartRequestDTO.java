package com.f1manager.backend.dto;

import com.f1manager.backend.model.simulation.TipoNeumatico;
import lombok.Data;

/**
 * Request para iniciar una carrera.
 * El jugador elige el compuesto inicial de sus 2 pilotos titulares.
 */
@Data
public class CarreraStartRequestDTO {
    private TipoNeumatico compuestoPiloto1;  // Compuesto inicial para el titular 1
    private TipoNeumatico compuestoPiloto2;  // Compuesto inicial para el titular 2
}
