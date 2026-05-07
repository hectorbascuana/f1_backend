package com.f1manager.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * Respuesta del endpoint POST /api/carrera/vuelta/{uuid}.
 * Contiene el estado actualizado de la carrera tras procesar una vuelta.
 */
@Data
public class CarreraVueltaResponseDTO {

    private int vueltaActual;
    private int totalVueltas;
    private boolean finalizada;
    private List<PilotoCarreraDTO> ranking;

    @Data
    public static class PilotoCarreraDTO {
        private int posicion;
        private Integer pilotoId;
        private String nombre;
        private String escuderia;
        private String escuderiaImagen;
        private long tiempoTotalMs;
        private long tiempoVueltaMs;
        private long vueltaRapidaMs;
        private long gapMs;
        private String compuesto;
        private double desgaste;
        private int numParadas;
        private boolean enPitStop;
        private boolean dnf;
        private boolean esJugador;
    }
}
