package com.f1manager.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.f1manager.backend.entity.Estadistica;
import com.f1manager.backend.repository.EstadisticaRepository;

@Service
public class EstadisticaService {
    
    private final EstadisticaRepository estadisticaRepository;
    
    public EstadisticaService(EstadisticaRepository estadisticaRepository) {
        this.estadisticaRepository = estadisticaRepository;
    }
    
    public List<Estadistica> obtenerTodas() {
        return estadisticaRepository.findAll();
    }
    
    public Optional<Estadistica> obtenerPorId(Integer id) {
        return estadisticaRepository.findById(id);
    }
    
    public Estadistica guardar(Estadistica estadistica) {
        return estadisticaRepository.save(estadistica);
    }
    
    public void eliminar(Integer id) {
        estadisticaRepository.deleteById(id);
    }
}
