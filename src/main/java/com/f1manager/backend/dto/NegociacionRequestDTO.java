package com.f1manager.backend.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class NegociacionRequestDTO {
    private Integer partidaId;
    private Integer pilotoId;
    private Integer escuderiaDestinoId;
    private BigDecimal precio;
}
