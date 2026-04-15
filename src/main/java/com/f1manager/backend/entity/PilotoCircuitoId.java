package com.f1manager.backend.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PilotoCircuitoId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer circuitoId;
    private Integer pilotoId;
    private Integer temporada;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PilotoCircuitoId that = (PilotoCircuitoId) o;
        return Objects.equals(circuitoId, that.circuitoId) &&
               Objects.equals(pilotoId, that.pilotoId) &&
               Objects.equals(temporada, that.temporada);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(circuitoId, pilotoId, temporada);
    }
}
