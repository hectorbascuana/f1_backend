package com.f1manager.backend.dto;

import lombok.Data;

@Data
public class PilotoMinDTO {
    private Integer id;
    private String nombre;
    private String pais;
    private String imagen;
    private Integer edad;
}
