package com.f1manager.backend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EscuderiaDTO {
    private Integer id;
    private String nombre;
    private String imagen;
    private BigDecimal presupuesto;
    private Integer aerodinamica;
    private Integer motor;
    private Integer durabilidad;
    private Integer tunelViento;
    private Integer bancoPruebas;
    private Integer escuelaPilotos;
}
