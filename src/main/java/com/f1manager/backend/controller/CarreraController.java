package com.f1manager.backend.controller;

import com.f1manager.backend.dto.*;
import com.f1manager.backend.model.simulation.TipoNeumatico;
import com.f1manager.backend.service.CarreraMotorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller REST para el motor de carrera.
 * 
 * Endpoints:
 * - POST /api/carrera/start/{partidaId}  → Inicia carrera (clasificación + parrilla)
 * - POST /api/carrera/vuelta/{uuid}      → Procesa una vuelta
 * - GET  /api/carrera/estado/{uuid}      → Consulta estado actual (debug/reconexión)
 */
@RestController
@RequestMapping("/api/carrera")
@CrossOrigin(origins = "*")
public class CarreraController {

    private final CarreraMotorService carreraMotorService;

    public CarreraController(CarreraMotorService carreraMotorService) {
        this.carreraMotorService = carreraMotorService;
    }

    /**
     * POST /api/carrera/start/{partidaId}
     * 
     * Inicia una nueva sesión de carrera para la partida indicada.
     * Usa el circuito actual de la partida (partida.proximoCircuito).
     * 
     * El jugador elige los compuestos iniciales de sus 2 pilotos titulares.
     * La IA elige según su perfil (AGRESIVO → Blandos, CONSERVADOR → Medio/Duro).
     * 
     * Simula la clasificación y devuelve la parrilla de salida.
     * 
     * @param partidaId ID de la partida
     * @param request   Compuestos iniciales del jugador
     * @return UUID de la sesión + parrilla de salida
     */
    @PostMapping("/start/{partidaId}")
    public ResponseEntity<CarreraStartResponseDTO> iniciarCarrera(
            @PathVariable Integer partidaId) {
        
        CarreraStartResponseDTO response = carreraMotorService.iniciarCarrera(partidaId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/carrera/vuelta/{uuid}
     * 
     * Procesa UNA vuelta de la carrera identificada por el UUID.
     * En la Vuelta 1, los compuestos indicados se usan como neumáticos de salida.
     * 
     * En la última vuelta, los resultados se persisten automáticamente en BD
     * y la sesión se limpia de la memoria RAM.
     * 
     * @param uuid    UUID de la sesión de carrera
     * @param request Decisiones del jugador (pit stops + compuestos)
     * @return Ranking actualizado con tiempos, gaps, desgaste, etc.
     */
    @PostMapping("/vuelta/{uuid}")
    public ResponseEntity<CarreraVueltaResponseDTO> procesarVuelta(
            @PathVariable UUID uuid,
            @RequestBody CarreraVueltaRequestDTO request) {

        TipoNeumatico comp1 = null;
        TipoNeumatico comp2 = null;

        // Extraer compuestos. Si es la vuelta 1, se usan como iniciales.
        if (request.getNuevoCompuestoPiloto1() != null) {
            comp1 = TipoNeumatico.valueOf(request.getNuevoCompuestoPiloto1().toUpperCase());
        }
        if (request.getNuevoCompuestoPiloto2() != null) {
            comp2 = TipoNeumatico.valueOf(request.getNuevoCompuestoPiloto2().toUpperCase());
        }

        CarreraVueltaResponseDTO response = carreraMotorService.procesarVuelta(
                uuid,
                request.isPitStopPiloto1(), comp1,
                request.isPitStopPiloto2(), comp2
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/carrera/estado/{uuid}
     * 
     * Consulta el estado actual de la carrera sin procesar vuelta.
     * Útil para reconexión o debug.
     * 
     * @param uuid UUID de la sesión de carrera
     * @return Estado actual de la carrera
     */
    @GetMapping("/estado/{uuid}")
    public ResponseEntity<CarreraVueltaResponseDTO> obtenerEstado(@PathVariable UUID uuid) {
        CarreraVueltaResponseDTO response = carreraMotorService.obtenerEstado(uuid);
        return ResponseEntity.ok(response);
    }
}
