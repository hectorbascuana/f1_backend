package com.f1manager.backend.controller;

import com.f1manager.backend.model.ia.IAResultadoPartida;
import com.f1manager.backend.service.IAService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ia")
@CrossOrigin(origins = "*")
public class IAController {

    private final IAService iaService;

    public IAController(IAService iaService) {
        this.iaService = iaService;
    }

    @GetMapping("/estado/{partidaIdStr}")
    public ResponseEntity<Map<String, Object>> obtenerEstado(@PathVariable String partidaIdStr) {
        if ("undefined".equals(partidaIdStr) || partidaIdStr == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("completado", false);
            response.put("mensaje", "ID de partida no definido");
            return ResponseEntity.ok(response);
        }
        
        Integer partidaId;
        try {
            partidaId = Integer.parseInt(partidaIdStr);
        } catch (NumberFormatException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "ID de partida inválido");
            return ResponseEntity.badRequest().body(response);
        }

        IAResultadoPartida resultado = iaService.obtenerEstado(partidaId);
        Map<String, Object> response = new HashMap<>();
        if (resultado == null) {
            response.put("completado", false);
            response.put("numDecisiones", 0);
            response.put("mensaje", "No hay procesamiento IA en curso para esta partida");
        } else {
            response.put("completado", resultado.isCompletado());
            response.put("numDecisiones", resultado.getDecisiones().size());
            // Si está completado, incluimos la lista de decisiones para el HUD de depuración
            if (resultado.isCompletado()) {
                response.put("decisiones", resultado.getDecisiones());
            }
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/errores")
    public ResponseEntity<String> verErrores() {
        try {
            Path path = Paths.get("debug_carrera.log");
            if (!Files.exists(path)) {
                return ResponseEntity.ok("No hay registro de errores aún.");
            }
            String content = Files.readString(path);
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al leer los errores: " + e.getMessage());
        }
    }
}
