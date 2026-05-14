package com.f1manager.backend.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NegociacionResponseDTO {
    
    public enum Resultado { ACEPTADO, RECHAZADO }

    private Resultado resultado;
    private String mensaje;
    private TraspasoDTO traspaso;
    private BigDecimal presupuestoRestante;
}
