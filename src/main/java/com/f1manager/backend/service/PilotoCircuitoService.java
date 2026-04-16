package com.f1manager.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.f1manager.backend.entity.PilotoCircuito;
import com.f1manager.backend.entity.PilotoCircuitoId;
import com.f1manager.backend.repository.PilotoCircuitoRepository;

@Service
public class PilotoCircuitoService {
    
    private final PilotoCircuitoRepository pilotoCircuitoRepository;
    
    public PilotoCircuitoService(PilotoCircuitoRepository pilotoCircuitoRepository) {
        this.pilotoCircuitoRepository = pilotoCircuitoRepository;
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
}
