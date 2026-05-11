package com.f1manager.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoCarreraDTO {
    private Integer posicion;
    private LocalTime tiempoTotal;
    private LocalTime vueltaRapida;
    
    // Piloto info
    private Integer pilotoId;
    private String pilotoNombre;
    private String pilotoImagen;
    private String pilotoPais;
    
    // Escuderia info
    private String escuderiaNombre;
    private String escuderiaImagen;
}
