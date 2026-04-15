package com.f1manager.backend.dto;

import java.time.LocalTime;

import lombok.Data;

@Data
public class PilotoCircuitoDTO {
    private PilotoCircuitoIdDTO id;
    private Integer posicion;
    private LocalTime tiempoTotal;
    private LocalTime vueltaRapida;

    @Data
    public static class PilotoCircuitoIdDTO {
        private Integer circuitoId;
        private Integer pilotoId;
        private Integer temporada;
    }
}
