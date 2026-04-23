package com.f1manager.backend.controller;

import com.f1manager.backend.entity.Estadistica;
import com.f1manager.backend.service.EstadisticaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/estadisticas")
@CrossOrigin(origins = "*")
public class EstadisticaController {
    
    private final EstadisticaService estadisticaService;
    
    public EstadisticaController(EstadisticaService estadisticaService) {
        this.estadisticaService = estadisticaService;
    }
    
    @GetMapping("/partida/{partidaId}")
    public ResponseEntity<List<Estadistica>> obtenerPorPartida(@PathVariable Integer partidaId) {
        return ResponseEntity.ok(estadisticaService.obtenerPorPartida(partidaId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Estadistica> obtenerPorId(@PathVariable Integer id) {
        return estadisticaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Estadistica> crear(@RequestBody Estadistica estadistica) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(estadisticaService.guardar(estadistica));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Estadistica> actualizar(@PathVariable Integer id, 
                                                   @RequestBody Estadistica estadistica) {
        return estadisticaService.obtenerPorId(id)
                .map(e -> {
                    estadistica.setId(id);
                    return ResponseEntity.ok(estadisticaService.guardar(estadistica));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (estadisticaService.obtenerPorId(id).isPresent()) {
            estadisticaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
