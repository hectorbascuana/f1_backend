package com.f1manager.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.f1manager.backend.dto.EscuderiaDTO;
import com.f1manager.backend.dto.EscuderiaMejoraDTO;
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
    
    public Optional<EscuderiaDTO> mejorarEscuderia(Integer id, EscuderiaMejoraTipo tipo, EscuderiaMejoraDTO mejora) {
        return escuderiaRepository.findById(id).map(escuderia -> {
            switch (tipo) {
                case BASICA -> aplicarMejoraBasica(escuderia, mejora);
                case DURABILIDAD -> aplicarMejoraDurabilidad(escuderia, mejora);
                case INSTALACIONES -> aplicarMejoraInstalaciones(escuderia, mejora);
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

    private void aplicarMejoraBasica(Escuderia escuderia, EscuderiaMejoraDTO mejora) {
        boolean incremento = mejora.getModo() == null || mejora.getModo().equalsIgnoreCase("incremento");

        if (mejora.getAerodinamica() != null) {
            int valor = incremento ? escuderia.getAerodinamica() + mejora.getAerodinamica() : mejora.getAerodinamica();
            escuderia.setAerodinamica(clamp(valor, 1, 100));
        }
        if (mejora.getMotor() != null) {
            int valor = incremento ? escuderia.getMotor() + mejora.getMotor() : mejora.getMotor();
            escuderia.setMotor(clamp(valor, 1, 100));
        }
    }

    private void aplicarMejoraDurabilidad(Escuderia escuderia, EscuderiaMejoraDTO mejora) {
        if (mejora.getDurabilidad() != null) {
            boolean incremento = mejora.getModo() == null || mejora.getModo().equalsIgnoreCase("incremento");
            int valor = incremento ? escuderia.getDurabilidad() + mejora.getDurabilidad() : mejora.getDurabilidad();
            escuderia.setDurabilidad(clamp(valor, 1, 20));
        }
    }

    private void aplicarMejoraInstalaciones(Escuderia escuderia, EscuderiaMejoraDTO mejora) {
        boolean incremento = mejora.getModo() == null || mejora.getModo().equalsIgnoreCase("incremento");

        if (mejora.getTunelViento() != null) {
            int valor = incremento ? escuderia.getTunelViento() + mejora.getTunelViento() : mejora.getTunelViento();
            escuderia.setTunelViento(clamp(valor, 1, 5));
        }
        if (mejora.getBancoPruebas() != null) {
            int valor = incremento ? escuderia.getBancoPruebas() + mejora.getBancoPruebas() : mejora.getBancoPruebas();
            escuderia.setBancoPruebas(clamp(valor, 1, 5));
        }
        if (mejora.getEscuelaPilotos() != null) {
            int valor = incremento ? escuderia.getEscuelaPilotos() + mejora.getEscuelaPilotos() : mejora.getEscuelaPilotos();
            escuderia.setEscuelaPilotos(clamp(valor, 1, 5));
        }
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
        return dto;
    }
}
