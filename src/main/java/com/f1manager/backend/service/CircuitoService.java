package com.f1manager.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.f1manager.backend.dto.CircuitoDTO;
import com.f1manager.backend.entity.Circuito;
import com.f1manager.backend.repository.CircuitoRepository;

@Service
public class CircuitoService {
    
    private final CircuitoRepository circuitoRepository;
    
    public CircuitoService(CircuitoRepository circuitoRepository) {
        this.circuitoRepository = circuitoRepository;
    }
    
    public List<CircuitoDTO> obtenerTodos() {
        return circuitoRepository.findAll().stream()
                .map(this::toCircuitoDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<CircuitoDTO> obtenerPorId(Integer id) {
        return circuitoRepository.findById(id).map(this::toCircuitoDTO);
    }
    
    public Circuito guardar(Circuito circuito) {
        return circuitoRepository.save(circuito);
    }
    
    public void eliminar(Integer id) {
        circuitoRepository.deleteById(id);
    }

    public CircuitoDTO toCircuitoDTO(Circuito circuito) {
        CircuitoDTO dto = new CircuitoDTO();
        dto.setId(circuito.getId());
        dto.setNombre(circuito.getNombre());
        dto.setPais(circuito.getPais());
        dto.setTiempoBase(circuito.getTiempoBase());
        dto.setNumVueltas(circuito.getNumVueltas());
        dto.setAerodinamicaReq(circuito.getAerodinamicaReq());
        dto.setMotorReq(circuito.getMotorReq());
        return dto;
    }
}
