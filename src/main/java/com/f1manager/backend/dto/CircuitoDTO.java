package com.f1manager.backend.dto;

import java.time.LocalTime;

import lombok.Data;

@Data
public class CircuitoDTO {
    private Integer id;
    private String nombre;
    private String pais;
    private LocalTime tiempoBase;
    private Integer numVueltas;
    private Integer aerodinamicaReq;
    private Integer motorReq;
}
