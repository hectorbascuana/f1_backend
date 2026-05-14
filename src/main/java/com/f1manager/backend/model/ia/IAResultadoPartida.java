package com.f1manager.backend.model.ia;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Contenedor de todas las decisiones IA calculadas para una partida.
 * Se almacena en el ConcurrentHashMap del IAService hasta que se haga flush.
 */
@Data
public class IAResultadoPartida {

    /** Lista de decisiones (múltiples por equipo posibles) */
    private List<IADecision> decisiones = new ArrayList<>();

    /** true cuando el hilo asíncrono ha terminado de calcular */
    private boolean completado = false;

    public void agregarDecision(IADecision decision) {
        this.decisiones.add(decision);
    }
}
