package com.f1manager.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.f1manager.backend.dto.PilotoDetalleDTO;
import com.f1manager.backend.entity.Piloto;
import com.f1manager.backend.repository.PilotoRepository;

@Service
public class PilotoService {
    
    private final PilotoRepository pilotoRepository;
    
    public PilotoService(PilotoRepository pilotoRepository) {
        this.pilotoRepository = pilotoRepository;
    }
    
    public List<Piloto> obtenerTodos() {
        return pilotoRepository.findAll();
    }
    
    public Optional<Piloto> obtenerPorId(Integer id) {
        return pilotoRepository.findById(id);
    }
    
    public Optional<PilotoDetalleDTO> obtenerDetallePorId(Integer id) {
        return pilotoRepository.findById(id).map(this::toPilotoDetalleDTO);
    }
    
    public Piloto guardar(Piloto piloto) {
        return pilotoRepository.save(piloto);
    }
    
    public void eliminar(Integer id) {
        pilotoRepository.deleteById(id);
    }

    public PilotoDetalleDTO toPilotoDetalleDTO(Piloto piloto) {
        PilotoDetalleDTO dto = new PilotoDetalleDTO();
        dto.setId(piloto.getId());
        dto.setNombre(piloto.getNombre());
        dto.setNacionalidad(piloto.getPais());
        dto.setEdad(piloto.getEdad());
        dto.setImagen(piloto.getImagen());
        dto.setPuntos(piloto.getPuntos());
        dto.setValor(piloto.getValor() != null ? piloto.getValor().toPlainString() : null);


        return dto;
    }
}
