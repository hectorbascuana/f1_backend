package com.f1manager.backend.service;

import java.util.HashMap;
import java.util.Map;
import java.math.RoundingMode;
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

    public Optional<Map<String, Object>> procesarMejora(Integer id, EscuderiaMejoraTipo tipo) {
        return escuderiaRepository.findById(id).map(escuderia -> {
            Integer aumento = switch (tipo) {
                case AERODINAMICA -> escuderia.mejorarAerodinamica();
                case MOTOR -> escuderia.mejorarMotor();
                case DURABILIDAD -> escuderia.mejorarDurabilidad();
                case TUNEL_VIENTO -> escuderia.mejorarTunelViento();
                case BANCO_PRUEBAS -> escuderia.mejorarBancoPruebas();
                case ESCUELA_PILOTOS -> escuderia.mejorarEscuelaPilotos();
            };

            escuderiaRepository.save(escuderia);

            Map<String, Object> response = new HashMap<>();
            response.put("presupuesto", escuderia.getPresupuesto().setScale(2, RoundingMode.HALF_UP));
            response.put("aumento", aumento);

            switch (tipo) {
                case AERODINAMICA -> {
                    response.put("nivelActual", escuderia.getAerodinamica());
                    response.put("nuevoCoste", costeMejoraBasica(escuderia.getAerodinamica()));
                }
                case MOTOR -> {
                    response.put("nivelActual", escuderia.getMotor());
                    response.put("nuevoCoste", costeMejoraBasica(escuderia.getMotor()));
                }
                case DURABILIDAD -> {
                    response.put("nivelActual", escuderia.getDurabilidad());
                    response.put("nuevoCoste", costeMejoraDurabilidad(escuderia.getDurabilidad()));
                }
                case TUNEL_VIENTO -> {
                    response.put("nivelActual", escuderia.getTunelViento());
                    response.put("nuevoCoste", costeMejoraInstalacion(escuderia.getTunelViento()));
                }
                case BANCO_PRUEBAS -> {
                    response.put("nivelActual", escuderia.getBancoPruebas());
                    response.put("nuevoCoste", costeMejoraInstalacion(escuderia.getBancoPruebas()));
                }
                case ESCUELA_PILOTOS -> {
                    response.put("nivelActual", escuderia.getEscuelaPilotos());
                    response.put("nuevoCoste", costeMejoraEscuela(escuderia.getEscuelaPilotos()));
                }
            }

            return response;
        });
    }

    public Escuderia guardar(Escuderia escuderia) {
        return escuderiaRepository.save(escuderia);
    }

    public void eliminar(Integer id) {
        escuderiaRepository.deleteById(id);
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

    public static Float costeMejoraBasica(Integer nivel) {
        return redondear((float) (0.5 + Math.pow(nivel, 2) * 0.01));
    }

    public static Float costeMejoraDurabilidad(Integer nivel) {
        return redondear((float) (0.5 + Math.pow(nivel, 2) * 0.2));
    }

    public static Float costeMejoraInstalacion(Integer nivel) {
        return redondear((float) (15 + (15 * Math.pow(1.8, nivel))));
    }

    public static Float costeMejoraEscuela(Integer nivel) {
        return redondear((float) (15 + (12 * Math.pow(1.8, nivel))));
    }

    private static Float redondear(float valor) {
        return (float) (Math.round(valor * 100.0) / 100.0);
    }
}
