package com.f1manager.backend.dto;

import lombok.Data;

@Data
public class PilotoDetalleDTO {
    private Integer id;
    private String nombre;
    private String nacionalidad;
    private Integer edad;
    private String imagen;
    private Integer puntos;
    private String valor;
}
