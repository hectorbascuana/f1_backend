package com.f1manager.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1manager.backend.entity.Partida;
import com.f1manager.backend.entity.Escuderia;
import com.f1manager.backend.entity.Piloto;
import com.f1manager.backend.entity.PilotoCircuito;
import com.f1manager.backend.entity.Estadistica;
import com.f1manager.backend.repository.PartidaRepository;
import com.f1manager.backend.repository.EscuderiaRepository;
import com.f1manager.backend.repository.PilotoRepository;
import com.f1manager.backend.repository.EstadisticaRepository;
import com.f1manager.backend.repository.CircuitoRepository;
import com.f1manager.backend.dto.PartidaDTO;
import com.f1manager.backend.dto.PartidaDTO.EscuderiaSeleccionadaDTO;
import com.f1manager.backend.dto.CircuitoDTO;
import com.f1manager.backend.entity.Circuito;
import com.f1manager.backend.repository.PilotoCircuitoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class PartidaService {

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private TraspasoService traspasoService;

    @Autowired
    private PilotoCircuitoRepository pilotoCircuitoRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private IAService iaService;

    @Autowired
    private ProgresoService progresoService;

    public List<PartidaDTO> obtenerTodas() {
        return partidaRepository.findAll().stream()
                .map(this::toPartidaDTO)
                .collect(Collectors.toList());
    }

    public Partida obtenerPorId(Integer id) {
        return partidaRepository.findById(id).orElseThrow(() -> new RuntimeException("Partida no encontrada"));
    }

    public EscuderiaSeleccionadaDTO toEscuderiaSeleccionadaDTO(Escuderia escuderia) {
        EscuderiaSeleccionadaDTO dto = new EscuderiaSeleccionadaDTO();
        dto.setId(escuderia.getId());
        dto.setNombre(escuderia.getNombre());
        dto.setImagen(escuderia.getImagen());
        dto.setPresupuesto(escuderia.getPresupuesto().intValue());
        return dto;
    }

    public PartidaDTO toPartidaDTO(Partida partida) {
        PartidaDTO dto = new PartidaDTO();
        dto.setId(partida.getId());
        dto.setNombre(partida.getNombre());
        dto.setEscuderiaSeleccionada(partida.getEscuderiaSeleccionada() != null
                ? toEscuderiaSeleccionadaDTO(partida.getEscuderiaSeleccionada())
                : null);

        if (partida.getProximoCircuito() != null) {
            Circuito c = partida.getProximoCircuito();
            CircuitoDTO cDto = new CircuitoDTO();
            cDto.setId(c.getId());
            cDto.setNombre(c.getNombre());
            cDto.setPais(c.getPais());
            cDto.setTiempoBase(c.getTiempoBase());
            cDto.setNumVueltas(c.getNumVueltas());
            cDto.setAerodinamicaReq(c.getAerodinamicaReq());
            cDto.setMotorReq(c.getMotorReq());
            dto.setProximoCircuito(cDto.getId());
        }

        dto.setFechaCreacion(partida.getFechaCreacion());
        dto.setAnio(partida.getAnio());

        return dto;
    }

    @Autowired
    private EscuderiaRepository escuderiaRepository;

    @Autowired
    private PilotoRepository pilotoRepository;

    @Autowired
    private EstadisticaRepository estadisticaRepository;

    @Autowired
    private CircuitoRepository circuitoRepository;

    @Transactional
    public Partida crearNuevaPartida(String nombre, Integer idEscuderiaJson) throws Exception {
        // Enforce limit of 3 games
        if (partidaRepository.count() >= 3) {
            throw new RuntimeException("No se pueden crear más de 3 partidas.");
        }

        // Create new Partida
        Partida partida = new Partida();
        partida.setNombre(nombre);
        // Default circuit
        circuitoRepository.findById(1).ifPresent(c -> partida.setProximoCircuito(c));
        Partida partidaGuardada = partidaRepository.save(partida);

        // Load JSON with explicit UTF-8 encoding
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = new ClassPathResource("initial_data.json").getInputStream();
        java.io.Reader reader = new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8);
        JsonNode root = mapper.readTree(reader);

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
                est.setValoracionInicial(est.getValoracion());
                est = estadisticaRepository.save(est);
                mapEstadisticas.put(node.path("id").asInt(), est);
            }
        }

        // Parse Escuderias
        Map<Integer, Escuderia> mapEscuderias = new HashMap<>();
        Map<Integer, Integer> mapEscuderiaPiloto1 = new HashMap<>();
        Map<Integer, Integer> mapEscuderiaPiloto2 = new HashMap<>();
        Escuderia escuderiaSeleccionada = null;
        JsonNode escNode = root.get("escuderias");
        if (escNode != null && escNode.isArray()) {
            for (JsonNode node : escNode) {
                int jsonId = node.path("id").asInt();
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
                mapEscuderias.put(jsonId, esc);

                if (node.has("id_piloto_1")) {
                    mapEscuderiaPiloto1.put(jsonId, node.get("id_piloto_1").asInt());
                }
                if (node.has("id_piloto_2")) {
                    mapEscuderiaPiloto2.put(jsonId, node.get("id_piloto_2").asInt());
                }

                // If this is the chosen team (by JSON ID), store it
                if (jsonId == idEscuderiaJson) {
                    escuderiaSeleccionada = esc;
                }
            }
        }

        // If team found, assign it to Partida
        if (escuderiaSeleccionada != null) {
            partidaGuardada.setEscuderiaSeleccionada(escuderiaSeleccionada);
            partidaRepository.save(partidaGuardada);
        } else {
            throw new RuntimeException(
                    "La escudería con ID '" + idEscuderiaJson + "' no existe en los datos iniciales.");
        }

        // Parse Pilotos
        Map<Integer, Piloto> mapPilotos = new HashMap<>();
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
                p = pilotoRepository.save(p);
                if (node.has("id")) {
                    mapPilotos.put(node.get("id").asInt(), p);
                }
            }
        }

        // Generar un piloto reserva aleatorio para cada equipo
        generarPilotosReserva(partidaGuardada, mapEscuderias);

        // Final pass: Link starting pilots to teams
        for (Map.Entry<Integer, Escuderia> entry : mapEscuderias.entrySet()) {
            Integer teamJsonId = entry.getKey();
            Escuderia esc = entry.getValue();
            boolean changed = false;

            if (mapEscuderiaPiloto1.containsKey(teamJsonId)) {
                esc.setPiloto1(mapPilotos.get(mapEscuderiaPiloto1.get(teamJsonId)));
                changed = true;
            }
            if (mapEscuderiaPiloto2.containsKey(teamJsonId)) {
                esc.setPiloto2(mapPilotos.get(mapEscuderiaPiloto2.get(teamJsonId)));
                changed = true;
            }

            if (changed) {
                escuderiaRepository.save(esc);
            }
        }

        return partidaGuardada;
    }

    @Transactional
    public void eliminarPartida(Integer partidaId) {
        Partida p = partidaRepository.findById(partidaId).orElse(null);
        if (p != null) {
            // 1. Desvincular la escudería seleccionada para evitar bloqueos por FK circular
            p.setEscuderiaSeleccionada(null);
            partidaRepository.saveAndFlush(p);

            // 2. Usar borrado nativo para que el ON DELETE CASCADE de la DB haga el trabajo
            // sucio
            // Esto evita que Hibernate intente hacer UPDATES (que fallan por NOT NULL)
            // antes de borrar
            partidaRepository.deletePartidaById(partidaId);
        } else {
            throw new RuntimeException("La partida no existe");
        }
    }

    @Transactional
    public Partida avanzarCarrera(Integer id) {
        Partida p = obtenerPorId(id);
        int carreraIdActual = p.getProximoCircuito().getId();
        int anioActual = p.getAnio();

        // 1. Repartir premios económicos basados en los resultados de la carrera que
        // acaba de terminar
        List<PilotoCircuito> resultados = pilotoCircuitoRepository
                .findByPartidaYearAndCircuitCustomOrder(id, anioActual, carreraIdActual);

        for (PilotoCircuito rc : resultados) {
            if (rc.getPiloto() != null && rc.getPiloto().getEscuderia() != null) {
                Escuderia esc = rc.getPiloto().getEscuderia();
                BigDecimal premio = calcularPremio(rc.getPosicion());
                esc.setPresupuesto(esc.getPresupuesto().add(premio));
                escuderiaRepository.save(esc);
            }
        }

        // 2. Aplicar decisiones de la IA (calculadas durante la carrera)
        iaService.aplicarDecisiones(id);

        // 3. Evolución de pilotos (mejoras y declive por edad)
        progresoService.procesarEvolucionPilotos(p);

        // 4. Avanzar al siguiente circuito o año
        if (carreraIdActual == 24) {
            p.setProximoCircuito(
                    circuitoRepository.findById(1).orElseThrow(() -> new RuntimeException("Circuito 1 no encontrado")));
            p.setAnio(anioActual + 1);
            
            // Al cambiar de año, actualizamos la valoración inicial de todos los pilotos de la partida
            List<Piloto> todosLosPilotos = pilotoRepository.findByPartidaId(id);
            for (Piloto pil : todosLosPilotos) {
                if (pil.getEstadistica() != null) {
                    pil.getEstadistica().setValoracionInicial(pil.getEstadistica().getValoracion());
                    // No hace falta guardarlo explícitamente si usamos transacciones, pero por seguridad:
                }
            }
        } else {
            p.setProximoCircuito(circuitoRepository.findById(carreraIdActual + 1)
                    .orElseThrow(() -> new RuntimeException("Siguiente circuito no encontrado")));
        }

        // Liberar bloqueos de negociaciones para esta partida al avanzar de carrera
        traspasoService.limpiarBloqueos(id);

        return partidaRepository.save(p);
    }

    /**
     * Calcula el premio económico basado en la posición final.
     * Escala equilibrada: 12M al primero, bajando hasta 0.2M al DNF.
     */
    private BigDecimal calcularPremio(Integer posicion) {
        if (posicion == null)
            return BigDecimal.valueOf(0.2); // DNF o sin posición

        return switch (posicion) {
            case 1 -> BigDecimal.valueOf(12.0);
            case 2 -> BigDecimal.valueOf(10.0);
            case 3 -> BigDecimal.valueOf(8.5);
            case 4 -> BigDecimal.valueOf(7.0);
            case 5 -> BigDecimal.valueOf(6.0);
            case 6 -> BigDecimal.valueOf(5.0);
            case 7 -> BigDecimal.valueOf(4.0);
            case 8 -> BigDecimal.valueOf(3.5);
            case 9 -> BigDecimal.valueOf(3.0);
            case 10 -> BigDecimal.valueOf(2.5);
            case 11 -> BigDecimal.valueOf(2.0);
            case 12 -> BigDecimal.valueOf(1.8);
            case 13 -> BigDecimal.valueOf(1.6);
            case 14 -> BigDecimal.valueOf(1.4);
            case 15 -> BigDecimal.valueOf(1.2);
            case 16 -> BigDecimal.valueOf(1.0);
            case 17 -> BigDecimal.valueOf(0.9);
            case 18 -> BigDecimal.valueOf(0.8);
            case 19 -> BigDecimal.valueOf(0.7);
            case 20 -> BigDecimal.valueOf(0.6);
            case 21 -> BigDecimal.valueOf(0.5);
            case 22 -> BigDecimal.valueOf(0.4);
            default -> BigDecimal.valueOf(0.2);
        };
    }

    public void guardar(Partida p) {
        partidaRepository.save(p);
    }

    private void generarPilotosReserva(Partida partida, Map<Integer, Escuderia> mapEscuderias) {
        String[] nombres = { "Lucas", "Mateo", "Liam", "Noah", "Leo", "Oliver", "Arthur", "Finn", "Hugo", "Arno",
                "Santi", "Pau", "Marc", "Erik", "Lars", "Timo", "Jan", "Klaus", "Ben", "Dan", "Iker", "Theo", "Jonas",
                "Felipe", "Alex" };
        String[] apellidos = { "Silva", "Müller", "Rossi", "García", "Smith", "Lefebvre", "Ivanov", "Sato", "Khan",
                "O'Connor", "Junior", "Santos", "Costa", "Popescu", "Varga", "Sørensen", "Bakker", "Novák", "Petrov",
                "Larsen", "Schmidt", "Dubois", "Moretti", "Vidal", "Becker" };
        String[] paises = { "Portugal", "Alemania", "Italia", "España", "Reino Unido", "Francia", "Rusia", "Japón",
                "India", "Irlanda", "Brasil", "Rumanía", "Hungría", "Dinamarca", "Países Bajos", "Chequia", "Noruega",
                "Argentina", "México", "EE. UU.", "Canadá", "Australia" };

        Random random = new Random();

        for (Escuderia escuderia : mapEscuderias.values()) {
            // Create Estadistica for the reserve
            Estadistica est = new Estadistica();
            est.setPartida(partida);

            // Stats range 60-75
            int valoracion = 60 + random.nextInt(16);
            est.setValoracion(valoracion);
            est.setCurvaRapida(55 + random.nextInt(valoracion - 50));
            est.setCurvaLenta(55 + random.nextInt(valoracion - 50));
            est.setSalidas(50 + random.nextInt(valoracion - 45));
            est.setConsistencia(45 + random.nextInt(valoracion - 40));
            est.setValoracionInicial(est.getValoracion());

            est = estadisticaRepository.save(est);

            // Create Piloto
            Piloto p = new Piloto();
            p.setPartida(partida);
            String nombreCompleto = nombres[random.nextInt(nombres.length)] + " "
                    + apellidos[random.nextInt(apellidos.length)];
            p.setNombre(nombreCompleto);
            p.setPais(paises[random.nextInt(paises.length)]);
            p.setImagen("assets/drivers/generic_reserve.png");
            p.setEdad(16 + random.nextInt(5)); // 16-20 years
            p.setPuntos(0);
            p.setValor(BigDecimal.valueOf(1 + random.nextInt(5))); // 1-5 million
            p.setEscuderia(escuderia);
            p.setEstadistica(est);

            pilotoRepository.save(p);
        }
    }
}
