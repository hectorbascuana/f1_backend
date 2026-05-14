package com.f1manager.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.f1manager.backend.entity.PilotoCircuito;
import com.f1manager.backend.entity.PilotoCircuitoId;
import com.f1manager.backend.entity.Partida;
import com.f1manager.backend.repository.PilotoCircuitoRepository;
import com.f1manager.backend.repository.PartidaRepository;
import com.f1manager.backend.dto.ResultadoCarreraDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.stream.Collectors;

@Service
public class PilotoCircuitoService {

    private final PilotoCircuitoRepository pilotoCircuitoRepository;
    private final PartidaRepository partidaRepository;

    public PilotoCircuitoService(PilotoCircuitoRepository pilotoCircuitoRepository,
            PartidaRepository partidaRepository) {
        this.pilotoCircuitoRepository = pilotoCircuitoRepository;
        this.partidaRepository = partidaRepository;
    }

    public List<PilotoCircuito> obtenerTodos() {
        return pilotoCircuitoRepository.findAll();
    }

    public List<PilotoCircuito> obtenerPorPartida(Integer partidaId) {
        return pilotoCircuitoRepository.findByIdPartidaId(partidaId);
    }

    public Optional<PilotoCircuito> obtenerPorId(PilotoCircuitoId id) {
        return pilotoCircuitoRepository.findById(id);
    }

    public PilotoCircuito guardar(PilotoCircuito pilotoCircuito) {
        return pilotoCircuitoRepository.save(pilotoCircuito);
    }

    public void eliminar(PilotoCircuitoId id) {
        pilotoCircuitoRepository.deleteById(id);
    }

    public List<ResultadoCarreraDTO> obtenerResultadoCarrera(Integer partidaId, Integer temporada, Integer circuitoId) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

        // Validar año
        if (temporada > partida.getAnio()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Año no alcanzado");
        }

        // Validar circuito si es el año actual
        if (temporada.equals(partida.getAnio())) {
            if (partida.getProximoCircuito() != null && circuitoId > partida.getProximoCircuito().getId()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La carrera aún no se ha disputado");
            }
        }

        List<PilotoCircuito> resultados = pilotoCircuitoRepository.findByPartidaYearAndCircuitCustomOrder(partidaId, temporada, circuitoId);

        if (resultados.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay resultados para esta carrera");
        }

        return resultados.stream().map(rc -> {
            ResultadoCarreraDTO dto = new ResultadoCarreraDTO();
            dto.setPosicion(rc.getPosicion());
            dto.setTiempoTotal(rc.getTiempoTotal());
            dto.setVueltaRapida(rc.getVueltaRapida());

            if (rc.getPiloto() != null) {
                dto.setPilotoId(rc.getPiloto().getId());
                dto.setPilotoNombre(rc.getPiloto().getNombre());
                dto.setPilotoImagen(rc.getPiloto().getImagen());
                dto.setPilotoPais(rc.getPiloto().getPais());

                if (rc.getPiloto().getEscuderia() != null) {
                    dto.setEscuderiaNombre(rc.getPiloto().getEscuderia().getNombre());
                    dto.setEscuderiaImagen(rc.getPiloto().getEscuderia().getImagen());
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }
}
