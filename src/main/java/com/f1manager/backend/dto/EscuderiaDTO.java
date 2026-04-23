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
    private Float aerodinamicaCosto;
    private Integer motor;
    private Float motorCosto;
    private Integer durabilidad;
    private Float durabilidadCosto;
    private Integer tunelViento;
    private Float tunelVientoCosto;
    private Integer bancoPruebas;
    private Float bancoPruebasCosto;
    private Integer escuelaPilotos;
    private Float escuelaPilotosCosto;
}


