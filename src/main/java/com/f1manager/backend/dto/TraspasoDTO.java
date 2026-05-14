package com.f1manager.backend.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class TraspasoDTO {
    private Integer id;
    private Integer partidaId;
    private PilotoMinDTO piloto;
    private EscuderiaMinDTO escuderiaOrigen;
    private EscuderiaMinDTO escuderiaDestino;
    private BigDecimal precio;
    private Integer temporada;
    private Boolean aceptada;
    private Boolean enCurso;
}
