package com.f1manager.backend.dto;

import lombok.Data;

@Data
public class EstadisticaDTO {
    private Integer id;
    private Integer valoracion;
    private Integer curvaRapida;
    private Integer curvaLenta;
    private Integer salidas;
    private Integer consistencia;
}
