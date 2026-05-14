package com.f1manager.backend.model.simulation;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * POJO volátil que contiene el estado global de una carrera en curso.
 * Se almacena en el ConcurrentHashMap del CarreraMotorService.
 * Vive solo en RAM — NO es una @Entity JPA.
 * 
 * Al finalizar la carrera, sus resultados se persisten y luego se elimina del Map.
 */
@Data
public class EstadoCarrera {

    // === Identificación ===
    private UUID uuid;
    private Integer partidaId;
    private Integer circuitoId;
    private String circuitoNombre;

    // === Parámetros del circuito ===
    private long tiempoBaseMs;      // Tiempo base del circuito convertido a ms
    private int totalVueltas;       // Número total de vueltas de la carrera
    private int aerodinamicaReq;    // Requisito aerodinámico del circuito [1-10]
    private int motorReq;           // Requisito de motor del circuito [1-10]

    // === Estado de la carrera ===
    private int vueltaActual;       // 0 = no empezada, 1..N = en curso
    private boolean finalizada;
    private int temporada;          // Año de la temporada

    // === Participantes ===
    private List<PilotoSimulacion> pilotos;  // Todos los pilotos (numEscuderias * 2)

    // === IA ===
    private Map<Integer, PerfilIA> perfilesIA;  // escuderiaId → AGRESIVO/CONSERVADOR

    // === ID de la escudería del jugador (para excluir de IA) ===
    private Integer escuderiaJugadorId;
}
