package com.f1manager.backend.controller;

import com.f1manager.backend.entity.Partida;
import com.f1manager.backend.service.PartidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/partida")
@CrossOrigin(origins = "*")
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    @PostMapping("/nueva")
    public ResponseEntity<?> crearNuevaPartida(@RequestParam(defaultValue = "Partida Nueva") String nombre) {
        try {
            Partida p = partidaService.crearNuevaPartida(nombre);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("partidaId", p.getId());
            response.put("mensaje", "Partida generada existosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPartida(@PathVariable Integer id) {
        try {
            partidaService.eliminarPartida(id);
            Map<String, String> response = new HashMap<>();
            response.put("status", "ok");
            response.put("mensaje", "Partida eliminada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
