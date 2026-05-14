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

import com.f1manager.backend.dto.EscuderiaDTO;
import com.f1manager.backend.dto.MejoraRequestDTO;

import com.f1manager.backend.dto.EscuderiaMejoraTipo;
import com.f1manager.backend.dto.LineupRequestDTO;
import com.f1manager.backend.entity.Escuderia;
import com.f1manager.backend.service.EscuderiaService;

@RestController
@RequestMapping("/api/escuderias")
@CrossOrigin(origins = "*")
public class EscuderiaController {
    
    private final EscuderiaService escuderiaService;
    
    public EscuderiaController(EscuderiaService escuderiaService) {
        this.escuderiaService = escuderiaService;
    }
    
    @GetMapping("/partida/{partidaId}")
    public ResponseEntity<List<EscuderiaDTO>> obtenerPorPartida(@PathVariable Integer partidaId) {
        return ResponseEntity.ok(escuderiaService.obtenerPorPartida(partidaId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EscuderiaDTO> obtenerPorId(@PathVariable Integer id) {
        return escuderiaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EscuderiaDTO> crear(@RequestBody Escuderia escuderia) {
        Escuderia saved = escuderiaService.guardar(escuderia);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(escuderiaService.toEscuderiaDTO(saved));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EscuderiaDTO> actualizar(@PathVariable Integer id,
                                                    @RequestBody Escuderia escuderia) {
        return escuderiaService.obtenerPorId(id)
                .map(existing -> {
                    escuderia.setId(id);
                    Escuderia updated = escuderiaService.guardar(escuderia);
                    return ResponseEntity.ok(escuderiaService.toEscuderiaDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/mejorar")
    public ResponseEntity<?> mejorar(@RequestBody MejoraRequestDTO request) {
        try {
            EscuderiaMejoraTipo mejoraTipo = EscuderiaMejoraTipo.from(request.getTipoMejora());
            return escuderiaService.procesarMejora(request.getEscuderiaId(), mejoraTipo)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage());
        }
    }

    @PutMapping("/alineacion")
    public ResponseEntity<?> gestionarAlineacion(@RequestBody LineupRequestDTO request) {
        try {
            return escuderiaService.gestionarAsiento(request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (escuderiaService.obtenerPorId(id).isPresent()) {
            escuderiaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    
}
