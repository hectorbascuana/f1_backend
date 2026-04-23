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
    private EscuderiaSeleccionadaDTO escuderiaSeleccionada;
    private Integer proximoCircuito;
    private LocalDateTime fechaCreacion;
    private Integer anio;


    @Data
    public static class EscuderiaSeleccionadaDTO {
        private Integer id;
        private String nombre;
        private String imagen;
        private Integer presupuesto;
    }
}
