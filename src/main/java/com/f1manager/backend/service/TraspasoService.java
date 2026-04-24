package com.f1manager.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.f1manager.backend.dto.TraspasoDTO;
import com.f1manager.backend.dto.PilotoMinDTO;
import com.f1manager.backend.dto.EscuderiaMinDTO;
import com.f1manager.backend.entity.Traspaso;
import com.f1manager.backend.entity.Piloto;
import com.f1manager.backend.entity.Escuderia;
import com.f1manager.backend.repository.TraspasoRepository;

@Service
public class TraspasoService {

    private final TraspasoRepository traspasoRepository;

    public TraspasoService(TraspasoRepository traspasoRepository) {
        this.traspasoRepository = traspasoRepository;
    }

    public List<TraspasoDTO> obtenerTodos() {
        return traspasoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TraspasoDTO> obtenerPorPartida(Integer partidaId) {
        return traspasoRepository.findByPartidaId(partidaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TraspasoDTO> obtenerActivosPorPartida(Integer partidaId) {
        return traspasoRepository.findByPartidaIdAndEnCursoTrue(partidaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<TraspasoDTO> obtenerPorId(Integer id) {
        return traspasoRepository.findById(id).map(this::toDTO);
    }

    public Traspaso guardar(Traspaso traspaso) {
        return traspasoRepository.save(traspaso);
    }

    public void eliminar(Integer id) {
        traspasoRepository.deleteById(id);
    }

    public TraspasoDTO toDTO(Traspaso traspaso) {
        TraspasoDTO dto = new TraspasoDTO();
        dto.setId(traspaso.getId());
        dto.setPartidaId(traspaso.getPartida().getId());
        dto.setPrecio(traspaso.getPrecio());
        dto.setTemporada(traspaso.getTemporada());
        dto.setAceptada(traspaso.getAceptada());
        dto.setEnCurso(traspaso.getEnCurso());
        
        if (traspaso.getPiloto() != null) {
            dto.setPiloto(toPilotoMinDTO(traspaso.getPiloto()));
        }
        
        if (traspaso.getEscuderiaOrigen() != null) {
            dto.setEscuderiaOrigen(toEscuderiaMinDTO(traspaso.getEscuderiaOrigen()));
        }
        
        if (traspaso.getEscuderiaDestino() != null) {
            dto.setEscuderiaDestino(toEscuderiaMinDTO(traspaso.getEscuderiaDestino()));
        }
        
        return dto;
    }

    private PilotoMinDTO toPilotoMinDTO(Piloto piloto) {
        PilotoMinDTO dto = new PilotoMinDTO();
        dto.setId(piloto.getId());
        dto.setNombre(piloto.getNombre());
        dto.setPais(piloto.getPais());
        dto.setImagen(piloto.getImagen());
        dto.setEdad(piloto.getEdad());
        return dto;
    }

    private EscuderiaMinDTO toEscuderiaMinDTO(Escuderia escuderia) {
        EscuderiaMinDTO dto = new EscuderiaMinDTO();
        dto.setId(escuderia.getId());
        dto.setNombre(escuderia.getNombre());
        dto.setImagen(escuderia.getImagen());
        return dto;
    }
}
