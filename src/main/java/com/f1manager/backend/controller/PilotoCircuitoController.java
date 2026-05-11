package com.f1manager.backend.controller;

import com.f1manager.backend.entity.PilotoCircuito;
import com.f1manager.backend.entity.PilotoCircuitoId;
import com.f1manager.backend.service.PilotoCircuitoService;
import com.f1manager.backend.dto.ResultadoCarreraDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/piloto-circuito")
@CrossOrigin(origins = "*")
public class PilotoCircuitoController {
    
    private final PilotoCircuitoService pilotoCircuitoService;
    
    public PilotoCircuitoController(PilotoCircuitoService pilotoCircuitoService) {
        this.pilotoCircuitoService = pilotoCircuitoService;
    }
    
    @GetMapping("/partida/{partidaId}")
    public ResponseEntity<List<PilotoCircuito>> obtenerPorPartida(@PathVariable Integer partidaId) {
        return ResponseEntity.ok(pilotoCircuitoService.obtenerPorPartida(partidaId));
    }
    
    @PostMapping
    public ResponseEntity<PilotoCircuito> crear(@RequestBody PilotoCircuito pilotoCircuito) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pilotoCircuitoService.guardar(pilotoCircuito));
    }
    
    @DeleteMapping("/{partidaId}/{circuitoId}/{pilotoId}/{temporada}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer partidaId,
                                         @PathVariable Integer circuitoId,
                                         @PathVariable Integer pilotoId,
                                         @PathVariable Integer temporada) {
        PilotoCircuitoId id = new PilotoCircuitoId(partidaId, circuitoId, pilotoId, temporada);
        if (pilotoCircuitoService.obtenerPorId(id).isPresent()) {
            pilotoCircuitoService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/resultado/{partidaId}/{temporada}/{circuitoId}")
    public ResponseEntity<List<ResultadoCarreraDTO>> obtenerResultadoCarrera(
            @PathVariable Integer partidaId,
            @PathVariable Integer temporada,
            @PathVariable Integer circuitoId) {
        return ResponseEntity.ok(pilotoCircuitoService.obtenerResultadoCarrera(partidaId, temporada, circuitoId));
    }
}
