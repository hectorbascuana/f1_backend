package com.f1manager.backend.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidaDTO {
    private Integer id;
    private String nombre;
    private Integer idEscuderiaSeleccionada;
    private Integer proximoCircuito;
    private LocalDateTime fechaCreacion;
    private Integer anio;
}
