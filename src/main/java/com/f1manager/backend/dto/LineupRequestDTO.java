package com.f1manager.backend.dto;

import lombok.Data;

@Data
public class LineupRequestDTO {
    private Integer escuderiaId;
    private Integer pilotoId; // Opcional si es para bajar al piloto
    private Integer asiento;  // 1 o 2
}
