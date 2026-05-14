package com.f1manager.backend.model.ia;

import com.f1manager.backend.dto.EscuderiaMejoraTipo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Representa UNA decisión calculada por la IA para una escudería.
 * Se almacena en memoria (nunca se persiste directamente).
 * Se ejecuta contra la BD solo cuando el usuario llama a avanzarCarrera.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IADecision {

    public enum TipoAccion {
        MEJORAR,
        FICHAR,
        AHORRAR
    }

    /** Escudería que toma la decisión */
    private Integer escuderiaId;

    /** Tipo de acción decidida */
    private TipoAccion tipo;

    // === Campos para MEJORAR ===
    /** Qué tipo de mejora aplicar (AERODINAMICA, MOTOR, etc.) */
    private EscuderiaMejoraTipo tipoMejora;

    // === Campos para FICHAR ===
    /** ID del piloto objetivo */
    private Integer pilotoObjetivoId;

    /** Precio de la oferta */
    private BigDecimal ofertaPrecio;

    /** true si el piloto pertenece al equipo del usuario (determina método de flush) */
    private boolean pilotoDelUsuario;

    // === Factory methods para claridad ===

    public static IADecision mejorar(Integer escuderiaId, EscuderiaMejoraTipo tipo) {
        IADecision d = new IADecision();
        d.setEscuderiaId(escuderiaId);
        d.setTipo(TipoAccion.MEJORAR);
        d.setTipoMejora(tipo);
        return d;
    }

    public static IADecision fichar(Integer escuderiaId, Integer pilotoId, BigDecimal precio, boolean esDelUsuario) {
        IADecision d = new IADecision();
        d.setEscuderiaId(escuderiaId);
        d.setTipo(TipoAccion.FICHAR);
        d.setPilotoObjetivoId(pilotoId);
        d.setOfertaPrecio(precio);
        d.setPilotoDelUsuario(esDelUsuario);
        return d;
    }

    public static IADecision ahorrar(Integer escuderiaId) {
        IADecision d = new IADecision();
        d.setEscuderiaId(escuderiaId);
        d.setTipo(TipoAccion.AHORRAR);
        return d;
    }
}
