package com.f1manager.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f1manager.backend.dto.EscuderiaDTO;
import com.f1manager.backend.dto.PilotoResumenDTO;
import com.f1manager.backend.service.EscuderiaService;
import com.f1manager.backend.service.PilotoService;

@RestController
@RequestMapping("/api/clasificacion")
@CrossOrigin(origins = "*")
public class ClasificacionController {

    private final PilotoService pilotoService;
    private final EscuderiaService escuderiaService;

    public ClasificacionController(PilotoService pilotoService, EscuderiaService escuderiaService) {
        this.pilotoService = pilotoService;
        this.escuderiaService = escuderiaService;
    }

    @GetMapping("/{partidaId}/pilotos")
    public ResponseEntity<List<PilotoResumenDTO>> obtenerClasificacionPilotos(@PathVariable Integer partidaId) {
        return ResponseEntity.ok(pilotoService.obtenerClasificacion(partidaId));
    }

    @GetMapping("/{partidaId}/constructores")
    public ResponseEntity<List<EscuderiaDTO>> obtenerClasificacionConstructores(@PathVariable Integer partidaId) {
        return ResponseEntity.ok(escuderiaService.obtenerClasificacion(partidaId));
    }
}
