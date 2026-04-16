package com.f1manager.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1manager.backend.entity.Partida;
import com.f1manager.backend.entity.Escuderia;
import com.f1manager.backend.entity.Piloto;
import com.f1manager.backend.entity.Estadistica;
import com.f1manager.backend.repository.PartidaRepository;
import com.f1manager.backend.repository.EscuderiaRepository;
import com.f1manager.backend.repository.PilotoRepository;
import com.f1manager.backend.repository.EstadisticaRepository;
import com.f1manager.backend.repository.CircuitoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PartidaService {

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private EscuderiaRepository escuderiaRepository;

    @Autowired
    private PilotoRepository pilotoRepository;

    @Autowired
    private EstadisticaRepository estadisticaRepository;

    @Autowired
    private CircuitoRepository circuitoRepository;

    @Transactional
    public Partida crearNuevaPartida(String nombre) throws Exception {
        // Create new Partida
        Partida partida = new Partida();
        partida.setNombre(nombre);
        // Default circuit
        circuitoRepository.findById(1).ifPresent(c -> partida.setProximoCircuito(c));
        Partida partidaGuardada = partidaRepository.save(partida);

        // Load JSON
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = new ClassPathResource("initial_data.json").getInputStream();
        JsonNode root = mapper.readTree(is);

        // Parse Estadisticas
        Map<Integer, Estadistica> mapEstadisticas = new HashMap<>();
        JsonNode statsNode = root.get("estadisticas");
        if (statsNode != null && statsNode.isArray()) {
            for (JsonNode node : statsNode) {
                Estadistica est = new Estadistica();
                est.setPartida(partidaGuardada);
                est.setValoracion(node.path("valoracion").asInt());
                est.setCurvaRapida(node.path("curva_rapida").asInt());
                est.setCurvaLenta(node.path("curva_lenta").asInt());
                est.setSalidas(node.path("salidas").asInt());
                est.setConsistencia(node.path("consistencia").asInt());
                est = estadisticaRepository.save(est);
                mapEstadisticas.put(node.path("id").asInt(), est);
            }
        }

        // Parse Escuderias
        Map<Integer, Escuderia> mapEscuderias = new HashMap<>();
        JsonNode escNode = root.get("escuderias");
        if (escNode != null && escNode.isArray()) {
            for (JsonNode node : escNode) {
                Escuderia esc = new Escuderia();
                esc.setPartida(partidaGuardada);
                esc.setNombre(node.path("nombre").asText());
                esc.setImagen(node.path("imagen").asText());
                esc.setPresupuesto(BigDecimal.valueOf(node.path("presupuesto").asDouble()));
                esc.setAerodinamica(node.path("aerodinamica").asInt());
                esc.setMotor(node.path("motor").asInt());
                esc.setDurabilidad(node.path("durabilidad").asInt());
                esc.setTunelViento(node.path("tunel_viento").asInt());
                esc.setBancoPruebas(node.path("banco_pruebas").asInt());
                esc.setEscuelaPilotos(node.path("escuela_pilotos").asInt());
                esc = escuderiaRepository.save(esc);
                mapEscuderias.put(node.path("id").asInt(), esc);
            }
        }

        // Parse Pilotos
        JsonNode pilNode = root.get("pilotos");
        if (pilNode != null && pilNode.isArray()) {
            for (JsonNode node : pilNode) {
                Piloto p = new Piloto();
                p.setPartida(partidaGuardada);
                p.setNombre(node.path("nombre").asText());
                p.setPais(node.path("pais").asText());
                p.setImagen(node.path("imagen").asText());
                p.setEdad(node.path("edad").asInt());
                p.setPuntos(node.path("puntos").asInt());
                p.setValor(BigDecimal.valueOf(node.path("valor").asDouble()));
                
                if (node.has("escuderia_id")) {
                    p.setEscuderia(mapEscuderias.get(node.get("escuderia_id").asInt()));
                }
                if (node.has("estadistica_id")) {
                    p.setEstadistica(mapEstadisticas.get(node.get("estadistica_id").asInt()));
                }
                pilotoRepository.save(p);
            }
        }

        return partidaGuardada;
    }

    @Transactional
    public void eliminarPartida(Integer partidaId) {
        if(partidaRepository.existsById(partidaId)) {
            partidaRepository.deleteById(partidaId);
        } else {
            throw new RuntimeException("La partida no existe");
        }
    }
}
