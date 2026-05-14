package com.f1manager.backend.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.f1manager.backend.dto.NegociacionRequestDTO;
import com.f1manager.backend.dto.NegociacionResponseDTO;
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

    @GetMapping("/piloto/{pilotoId}/activos")
    public List<TraspasoDTO> obtenerActivosPorPiloto(@PathVariable Integer pilotoId) {
        return traspasoService.obtenerActivosPorPiloto(pilotoId);
    }

    @GetMapping("/{partidaId}/bloqueados")
    public Set<Integer> obtenerBloqueados(@PathVariable Integer partidaId) {
        return traspasoService.obtenerPilotosBloqueados(partidaId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TraspasoDTO> obtenerPorId(@PathVariable Integer id) {
        return traspasoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/aceptadas/{partidaId}")
    public List<TraspasoDTO> obtenerAceptadasPorPartida(@PathVariable Integer partidaId) {
        return traspasoService.obtenerAceptadasPorPartida(partidaId);
    }


    

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody com.f1manager.backend.dto.TraspasoRequestDTO request) {
        try {
            TraspasoDTO savedDto = traspasoService.crearDesdeRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al crear el traspaso");
        }
    }

    @PostMapping("/{id}/aceptar")
    public ResponseEntity<?> aceptarTraspaso(@PathVariable Integer id) {
        try {
            TraspasoDTO dto = traspasoService.aceptarTraspaso(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al aceptar el traspaso");
        }
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazarTraspaso(@PathVariable Integer id) {
        try {
            TraspasoDTO dto = traspasoService.rechazarTraspaso(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al rechazar el traspaso");
        }
    }

    @PostMapping("/negociar")
    public ResponseEntity<?> negociarTraspaso(@RequestBody NegociacionRequestDTO request) {
        try {
            NegociacionResponseDTO response = traspasoService.negociarTraspaso(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al procesar la negociación");
        }
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
