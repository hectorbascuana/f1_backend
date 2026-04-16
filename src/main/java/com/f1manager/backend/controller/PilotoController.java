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

import com.f1manager.backend.dto.PilotoDetalleDTO;
import com.f1manager.backend.entity.Piloto;
import com.f1manager.backend.service.PilotoService;

@RestController
@RequestMapping("/api/partida/{partidaId}/pilotos")
@CrossOrigin(origins = "*")
public class PilotoController {
    
    private final PilotoService pilotoService;
    
    public PilotoController(PilotoService pilotoService) {
        this.pilotoService = pilotoService;
    }
    
    @GetMapping
    public ResponseEntity<List<Piloto>> obtenerPorPartida(@PathVariable Integer partidaId) {
        return ResponseEntity.ok(pilotoService.obtenerPorPartida(partidaId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Piloto> obtenerPorId(@PathVariable Integer id) {
        return pilotoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/detalle")
    public ResponseEntity<PilotoDetalleDTO> obtenerDetallePorId(@PathVariable Integer id) {
        return pilotoService.obtenerDetallePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Piloto> crear(@RequestBody Piloto piloto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pilotoService.guardar(piloto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Piloto> actualizar(@PathVariable Integer id,
                                             @RequestBody Piloto piloto) {
        return pilotoService.obtenerPorId(id)
                .map(p -> {
                    piloto.setId(id);
                    return ResponseEntity.ok(pilotoService.guardar(piloto));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (pilotoService.obtenerPorId(id).isPresent()) {
            pilotoService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
