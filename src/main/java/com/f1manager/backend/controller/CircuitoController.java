package com.f1manager.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f1manager.backend.dto.CircuitoDTO;
import com.f1manager.backend.entity.Circuito;
import com.f1manager.backend.service.CircuitoService;

@RestController
@RequestMapping("/api/circuitos")
@CrossOrigin(origins = "*")
public class CircuitoController {
    
    private final CircuitoService circuitoService;
    
    public CircuitoController(CircuitoService circuitoService) {
        this.circuitoService = circuitoService;
    }
    
    @GetMapping
    public ResponseEntity<List<CircuitoDTO>> obtenerTodos() {
        return ResponseEntity.ok(circuitoService.obtenerTodos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CircuitoDTO> obtenerPorId(@PathVariable Integer id) {
        return circuitoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<CircuitoDTO> crear(@RequestBody Circuito circuito) {
        Circuito saved = circuitoService.guardar(circuito);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(circuitoService.toCircuitoDTO(saved));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CircuitoDTO> actualizar(@PathVariable Integer id,
                                                   @RequestBody Circuito circuito) {
        return circuitoService.obtenerPorId(id)
                .map(c -> {
                    circuito.setId(id);
                    Circuito updated = circuitoService.guardar(circuito);
                    return ResponseEntity.ok(circuitoService.toCircuitoDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (circuitoService.obtenerPorId(id).isPresent()) {
            circuitoService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
