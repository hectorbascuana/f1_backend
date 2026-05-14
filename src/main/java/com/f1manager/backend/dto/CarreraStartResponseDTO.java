package com.f1manager.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Respuesta del endpoint POST /api/carrera/start/{partidaId}.
 * Contiene el UUID de la sesión de carrera, info del circuito y la parrilla de salida.
 */
@Data
public class CarreraStartResponseDTO {

    private UUID uuid;
    private CircuitoInfoDTO circuito;
    private List<ParrillaEntryDTO> parrilla;

    @Data
    public static class CircuitoInfoDTO {
        private Integer id;
        private String nombre;
        private String pais;
        private int numVueltas;
    }

    @Data
    public static class ParrillaEntryDTO {
        private int posicion;
        private Integer pilotoId;
        private String nombre;
        private String escuderia;
        private String escuderiaImagen;
        private long tiempoClasificacionMs;
        private String compuesto;
        private boolean esJugador;
    }
}
