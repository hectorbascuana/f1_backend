package com.f1manager.backend.dto;

import lombok.Data;

@Data
public class EscuderiaMejoraDTO {
    private Integer aerodinamica;
    private Integer motor;
    private Integer durabilidad;
    private Integer tunelViento;
    private Integer bancoPruebas;
    private Integer escuelaPilotos;
    private String modo; // "incremento" o "set"
}
