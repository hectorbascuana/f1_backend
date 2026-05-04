package com.f1manager.backend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TraspasoRequestDTO {
    private Integer partida;
    private Integer piloto;
    private Integer escuderiaDestino;
    private BigDecimal precio;
    private Integer temporada;
    private Boolean enCurso;
    private Boolean aceptada;
}
