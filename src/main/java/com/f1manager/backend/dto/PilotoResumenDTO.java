package com.f1manager.backend.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PilotoResumenDTO {
    private Integer id;
    private String nombre;
    private String pais;
    private String imagen;
    private Integer edad;
    private Integer puntos;
    private BigDecimal valor;
    private EscuderiaMinDTO escuderia;
    private EstadisticaDTO estadisticas;
    private Integer asiento;
}
