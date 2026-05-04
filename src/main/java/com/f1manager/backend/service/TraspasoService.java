package com.f1manager.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f1manager.backend.dto.TraspasoDTO;
import com.f1manager.backend.dto.PilotoMinDTO;
import com.f1manager.backend.dto.EscuderiaMinDTO;
import com.f1manager.backend.dto.TraspasoRequestDTO;
import com.f1manager.backend.entity.Traspaso;
import com.f1manager.backend.entity.Piloto;
import com.f1manager.backend.entity.Escuderia;
import com.f1manager.backend.entity.Partida;
import com.f1manager.backend.repository.TraspasoRepository;
import com.f1manager.backend.repository.PartidaRepository;
import com.f1manager.backend.repository.PilotoRepository;
import com.f1manager.backend.repository.EscuderiaRepository;

@Service
public class TraspasoService {

    private final TraspasoRepository traspasoRepository;
    private final PartidaRepository partidaRepository;
    private final PilotoRepository pilotoRepository;
    private final EscuderiaRepository escuderiaRepository;

    public TraspasoService(TraspasoRepository traspasoRepository,
                           PartidaRepository partidaRepository,
                           PilotoRepository pilotoRepository,
                           EscuderiaRepository escuderiaRepository) {
        this.traspasoRepository = traspasoRepository;
        this.partidaRepository = partidaRepository;
        this.pilotoRepository = pilotoRepository;
        this.escuderiaRepository = escuderiaRepository;
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

    public List<TraspasoDTO> obtenerActivosPorPiloto(Integer pilotoId) {
        return traspasoRepository.findByPilotoIdAndEnCursoTrue(pilotoId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<TraspasoDTO> obtenerPorId(Integer id) {
        return traspasoRepository.findById(id).map(this::toDTO);
    }

    public Traspaso guardar(Traspaso traspaso) {
        return traspasoRepository.save(traspaso);
    }

    public TraspasoDTO crearDesdeRequest(TraspasoRequestDTO request) {
        Traspaso traspaso = new Traspaso();
        
        Partida partida = partidaRepository.findById(request.getPartida())
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));
        Piloto piloto = pilotoRepository.findById(request.getPiloto())
                .orElseThrow(() -> new RuntimeException("Piloto no encontrado"));
        
        if (!piloto.getPartida().getId().equals(partida.getId())) {
            throw new RuntimeException("El piloto no pertenece a la partida indicada");
        }
        
        traspaso.setPartida(partida);
        traspaso.setPiloto(piloto);
        
        // La escudería de origen es automáticamente la del piloto
        traspaso.setEscuderiaOrigen(piloto.getEscuderia());
        
        if (request.getEscuderiaDestino() != null) {
            Escuderia destino = escuderiaRepository.findById(request.getEscuderiaDestino())
                    .orElse(null);
            if (destino != null) {
                if (!destino.getPartida().getId().equals(partida.getId())) {
                    throw new RuntimeException("La escudería destino no pertenece a la misma partida que el piloto");
                }
                if (piloto.getEscuderia() != null && destino.getId().equals(piloto.getEscuderia().getId())) {
                    throw new RuntimeException("El piloto ya pertenece a la escudería de destino");
                }
            }
            traspaso.setEscuderiaDestino(destino);
        }
        
        traspaso.setPrecio(request.getPrecio());
        traspaso.setTemporada(request.getTemporada());
        traspaso.setEnCurso(request.getEnCurso() != null ? request.getEnCurso() : true);
        traspaso.setAceptada(request.getAceptada() != null ? request.getAceptada() : false);
        
        return toDTO(traspasoRepository.save(traspaso));
    }

    @Transactional
    public TraspasoDTO aceptarTraspaso(Integer id) {
        Traspaso traspaso = traspasoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traspaso no encontrado"));

        if (!traspaso.getEnCurso()) {
            throw new RuntimeException("El traspaso ya no está en curso");
        }

        Piloto piloto = traspaso.getPiloto();
        Escuderia origen = traspaso.getEscuderiaOrigen();
        Escuderia destino = traspaso.getEscuderiaDestino();

        if (origen != null && (piloto.getEscuderia() == null || !piloto.getEscuderia().getId().equals(origen.getId()))) {
            throw new RuntimeException("El piloto ya no pertenece a la escudería de origen");
        }

        if (origen != null && origen.getPilotos().size() <= 2) {
            throw new RuntimeException("La escudería de origen no puede quedarse con menos de 2 pilotos");
        }

        if (destino.getPresupuesto().compareTo(traspaso.getPrecio()) < 0) {
            throw new RuntimeException("La escudería de destino no tiene suficiente presupuesto");
        }

        destino.setPresupuesto(destino.getPresupuesto().subtract(traspaso.getPrecio()));
        if (origen != null) {
            origen.setPresupuesto(origen.getPresupuesto().add(traspaso.getPrecio()));
        }

        piloto.setEscuderia(destino);

        traspaso.setEnCurso(false);
        traspaso.setAceptada(true);

        escuderiaRepository.save(destino);
        if (origen != null) {
            escuderiaRepository.save(origen);
        }
        pilotoRepository.save(piloto);
        
        return toDTO(traspasoRepository.save(traspaso));
    }

    @Transactional
    public TraspasoDTO rechazarTraspaso(Integer id) {
        Traspaso traspaso = traspasoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traspaso no encontrado"));

        if (!traspaso.getEnCurso()) {
            throw new RuntimeException("El traspaso ya no está en curso");
        }

        traspaso.setEnCurso(false);
        traspaso.setAceptada(false);

        return toDTO(traspasoRepository.save(traspaso));
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
