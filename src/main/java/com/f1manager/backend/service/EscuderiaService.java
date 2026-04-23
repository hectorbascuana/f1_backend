package com.f1manager.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.f1manager.backend.dto.EscuderiaDTO;
import com.f1manager.backend.dto.EscuderiaMejoraTipo;
import com.f1manager.backend.entity.Escuderia;
import com.f1manager.backend.repository.EscuderiaRepository;

@Service
public class EscuderiaService {

    private final EscuderiaRepository escuderiaRepository;

    public EscuderiaService(EscuderiaRepository escuderiaRepository) {
        this.escuderiaRepository = escuderiaRepository;
    }

    public List<EscuderiaDTO> obtenerTodas() {
        return escuderiaRepository.findAll().stream()
                .map(this::toEscuderiaDTO)
                .collect(Collectors.toList());
    }

    public List<EscuderiaDTO> obtenerPorPartida(Integer partidaId) {
        return escuderiaRepository.findByPartidaId(partidaId).stream()
                .map(this::toEscuderiaDTO)
                .collect(Collectors.toList());
    }

    public Optional<EscuderiaDTO> obtenerPorId(Integer id) {
        return escuderiaRepository.findById(id)
                .map(this::toEscuderiaDTO);
    }

    public Optional<EscuderiaDTO> procesarMejora(Integer id, EscuderiaMejoraTipo tipo) {
        return escuderiaRepository.findById(id).map(escuderia -> {
            switch (tipo) {
                case AERODINAMICA -> escuderia.mejorarAerodinamica();
                case MOTOR -> escuderia.mejorarMotor();
                case DURABILIDAD -> escuderia.mejorarDurabilidad();
                case TUNEL_VIENTO -> escuderia.mejorarTunelViento();
                case BANCO_PRUEBAS -> escuderia.mejorarBancoPruebas();
                case ESCUELA_PILOTOS -> escuderia.mejorarEscuelaPilotos();
            }
            return toEscuderiaDTO(escuderiaRepository.save(escuderia));
        });
    }

    public Escuderia guardar(Escuderia escuderia) {
        return escuderiaRepository.save(escuderia);
    }

    public void eliminar(Integer id) {
        escuderiaRepository.deleteById(id);
    }

    private int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public EscuderiaDTO toEscuderiaDTO(Escuderia escuderia) {
        EscuderiaDTO dto = new EscuderiaDTO();
        dto.setId(escuderia.getId());
        dto.setNombre(escuderia.getNombre());
        dto.setImagen(escuderia.getImagen());
        dto.setPresupuesto(escuderia.getPresupuesto());
        dto.setAerodinamica(escuderia.getAerodinamica());
        dto.setMotor(escuderia.getMotor());
        dto.setDurabilidad(escuderia.getDurabilidad());
        dto.setTunelViento(escuderia.getTunelViento());
        dto.setBancoPruebas(escuderia.getBancoPruebas());
        dto.setEscuelaPilotos(escuderia.getEscuelaPilotos());
        dto.setAerodinamicaCosto(costeMejoraBasica(escuderia.getAerodinamica()));
        dto.setMotorCosto(costeMejoraBasica(escuderia.getMotor()));
        dto.setDurabilidadCosto(costeMejoraDurabilidad(escuderia.getDurabilidad()));
        dto.setTunelVientoCosto(costeMejoraInstalacion(escuderia.getTunelViento()));
        dto.setBancoPruebasCosto(costeMejoraInstalacion(escuderia.getBancoPruebas()));
        dto.setEscuelaPilotosCosto(costeMejoraEscuela(escuderia.getEscuelaPilotos()));
        return dto;
    }

    private Float costeMejoraBasica(Integer nivel) {
        return redondear((float) (0.5 + Math.pow(nivel, 2) * 0.01));
    }

    private Float costeMejoraDurabilidad(Integer nivel) {
        return redondear((float) (0.5 + Math.pow(nivel, 2) * 0.2));
    }

    private Float costeMejoraInstalacion(Integer nivel) {
        return redondear((float) (15 + (15 * Math.pow(1.8, nivel))));
    }

    private Float costeMejoraEscuela(Integer nivel) {
        return redondear((float) (15 + (12 * Math.pow(1.8, nivel))));
    }

    private Float redondear(float valor) {
        return (float) (Math.round(valor * 100.0) / 100.0);
    }
}
