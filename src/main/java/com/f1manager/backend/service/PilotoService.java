package com.f1manager.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.f1manager.backend.dto.PilotoDetalleDTO;
import com.f1manager.backend.dto.PilotoResumenDTO;
import com.f1manager.backend.dto.EscuderiaMinDTO;
import com.f1manager.backend.dto.EstadisticaDTO;
import com.f1manager.backend.entity.Piloto;
import com.f1manager.backend.entity.Escuderia;
import com.f1manager.backend.entity.Estadistica;
import com.f1manager.backend.repository.PilotoRepository;

import java.util.stream.Collectors;

@Service
public class PilotoService {
    
    private final PilotoRepository pilotoRepository;
    
    public PilotoService(PilotoRepository pilotoRepository) {
        this.pilotoRepository = pilotoRepository;
    }
    
    public List<Piloto> obtenerTodos() {
        return pilotoRepository.findAll();
    }

    public List<PilotoResumenDTO> obtenerPorPartida(Integer partidaId) {
        return pilotoRepository.findByPartidaIdOrderByEstadisticaValoracionDesc(partidaId).stream()
                .map(this::toPilotoResumenDTO)
                .collect(Collectors.toList());
    }

    public List<PilotoResumenDTO> obtenerClasificacion(Integer partidaId) {
        return pilotoRepository.findByPartidaIdAndHaCorridoTrueOrderByPuntosDesc(partidaId).stream()
                .map(this::toPilotoResumenDTO)
                .collect(Collectors.toList());
    }

    public List<PilotoResumenDTO> obtenerPorEscuderia(Integer escuderiaId) {
        return pilotoRepository.findByEscuderiaId(escuderiaId).stream()
                .map(this::toPilotoResumenDTO)
                .collect(Collectors.toList());
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

    public PilotoResumenDTO toPilotoResumenDTO(Piloto piloto) {
        PilotoResumenDTO dto = new PilotoResumenDTO();
        dto.setId(piloto.getId());
        dto.setNombre(piloto.getNombre());
        dto.setPais(piloto.getPais());
        dto.setImagen(piloto.getImagen());
        dto.setEdad(piloto.getEdad());
        dto.setPuntos(piloto.getPuntos());
        dto.setValor(piloto.getValor());
        if (piloto.getEscuderia() != null) {
            dto.setEscuderia(toEscuderiaMinDTO(piloto.getEscuderia()));
            
            // Determinar asiento
            Escuderia esc = piloto.getEscuderia();
            if (esc.getPiloto1() != null && esc.getPiloto1().getId().equals(piloto.getId())) {
                dto.setAsiento(1);
            } else if (esc.getPiloto2() != null && esc.getPiloto2().getId().equals(piloto.getId())) {
                dto.setAsiento(2);
            } else {
                dto.setAsiento(null); // Reserva
            }
        }
        if (piloto.getEstadistica() != null) {
            dto.setEstadisticas(toEstadisticaDTO(piloto.getEstadistica()));
        }
        dto.setHaCorrido(piloto.getHaCorrido());
        return dto;
    }

    public EstadisticaDTO toEstadisticaDTO(Estadistica estadistica) {
        EstadisticaDTO dto = new EstadisticaDTO();
        dto.setId(estadistica.getId());
        dto.setValoracion(estadistica.getValoracion());
        dto.setCurvaRapida(estadistica.getCurvaRapida());
        dto.setCurvaLenta(estadistica.getCurvaLenta());
        dto.setSalidas(estadistica.getSalidas());
        dto.setConsistencia(estadistica.getConsistencia());
        return dto;
    }

    public EscuderiaMinDTO toEscuderiaMinDTO(Escuderia escuderia) {
        EscuderiaMinDTO dto = new EscuderiaMinDTO();
        dto.setId(escuderia.getId());
        dto.setNombre(escuderia.getNombre());
        dto.setImagen(escuderia.getImagen());
        return dto;
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
        dto.setHaCorrido(piloto.getHaCorrido());

        return dto;
    }
}
