package com.f1manager.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.f1manager.backend.dto.TraspasoDTO;
import com.f1manager.backend.entity.Traspaso;
import com.f1manager.backend.service.TraspasoService;

@RestController
@RequestMapping("/api/traspasos")
@CrossOrigin(origins = "*")
public class TraspasoController {

    private final TraspasoService traspasoService;

    public TraspasoController(TraspasoService traspasoService) {
        this.traspasoService = traspasoService;
    }

    @GetMapping
    public List<TraspasoDTO> obtenerTodos() {
        return traspasoService.obtenerTodos();
    }

    @GetMapping("/partida/{partidaId}")
    public List<TraspasoDTO> obtenerPorPartida(@PathVariable Integer partidaId) {
        return traspasoService.obtenerPorPartida(partidaId);
    }

    @GetMapping("/partida/{partidaId}/activos")
    public List<TraspasoDTO> obtenerActivosPorPartida(@PathVariable Integer partidaId) {
        return traspasoService.obtenerActivosPorPartida(partidaId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TraspasoDTO> obtenerPorId(@PathVariable Integer id) {
        return traspasoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TraspasoDTO> crear(@RequestBody Traspaso traspaso) {
        Traspaso saved = traspasoService.guardar(traspaso);
        return ResponseEntity.status(HttpStatus.CREATED).body(traspasoService.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TraspasoDTO> actualizar(@PathVariable Integer id, @RequestBody Traspaso traspaso) {
        return traspasoService.obtenerPorId(id)
                .map(existing -> {
                    traspaso.setId(id);
                    Traspaso updated = traspasoService.guardar(traspaso);
                    return ResponseEntity.ok(traspasoService.toDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (traspasoService.obtenerPorId(id).isPresent()) {
            traspasoService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
