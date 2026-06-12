package com.f1manager.backend.service;

import com.f1manager.backend.entity.*;
import com.f1manager.backend.model.simulation.*;
import com.f1manager.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

/**
 * Puente entre la simulación in-memory y la capa de persistencia JPA.
 * 
 * Responsabilidades:
 * 1. Cargar datos de MySQL y construir los POJOs volátiles (EstadoCarrera, PilotoSimulacion)
 * 2. Persistir los resultados al finalizar la carrera (PilotoCircuito, puntos, ha_corrido)
 * 3. Auto-alinear pilotos de escuderías IA (los 2 con más valoración)
 */
@Service
public class CarreraDataService {


    private final CircuitoRepository circuitoRepository;
    private final EscuderiaRepository escuderiaRepository;
    private final PilotoRepository pilotoRepository;
    private final PilotoCircuitoRepository pilotoCircuitoRepository;
    private final PartidaRepository partidaRepository;

    /** Puntos F1 estándar: posiciones 1-10 */
    private static final int[] PUNTOS_F1 = {25, 18, 15, 12, 10, 8, 6, 4, 2, 1};

    public CarreraDataService(CircuitoRepository circuitoRepository,
                              EscuderiaRepository escuderiaRepository,
                              PilotoRepository pilotoRepository,
                              PilotoCircuitoRepository pilotoCircuitoRepository,
                              PartidaRepository partidaRepository) {
        this.circuitoRepository = circuitoRepository;
        this.escuderiaRepository = escuderiaRepository;
        this.pilotoRepository = pilotoRepository;
        this.pilotoCircuitoRepository = pilotoCircuitoRepository;
        this.partidaRepository = partidaRepository;
    }

    /**
     * Carga todos los datos necesarios de MySQL y construye un EstadoCarrera listo para simular.
     * 
     * - Carga el circuito y convierte tiempo_base a milisegundos
     * - Carga todas las escuderías de la partida
     * - Auto-alinea pilotos de escuderías IA (2 mejores valorados en asientos)
     * - Crea PilotoSimulacion para cada titular
     * - Asigna perfiles IA aleatorios (AGRESIVO/CONSERVADOR)
     */
    @Transactional
    public EstadoCarrera cargarDatosCarrera(Integer partidaId) {
        // 1. Cargar la partida
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada: " + partidaId));

        Circuito circuito = partida.getProximoCircuito();
        if (circuito == null) {
            throw new RuntimeException("La partida no tiene un circuito próximo asignado");
        }

        Integer escuderiaJugadorId = partida.getEscuderiaSeleccionada() != null
                ? partida.getEscuderiaSeleccionada().getId()
                : null;

        // 2. Construir EstadoCarrera
        EstadoCarrera estado = new EstadoCarrera();
        estado.setUuid(UUID.randomUUID());
        estado.setPartidaId(partidaId);
        estado.setCircuitoId(circuito.getId());
        estado.setCircuitoNombre(circuito.getNombre());
        estado.setTiempoBaseMs(localTimeToMs(circuito.getTiempoBase()));
        estado.setTotalVueltas(circuito.getNumVueltas());
        estado.setAerodinamicaReq(circuito.getAerodinamicaReq());
        estado.setMotorReq(circuito.getMotorReq());
        estado.setVueltaActual(0);
        estado.setFinalizada(false);
        estado.setTemporada(partida.getAnio());
        estado.setEscuderiaJugadorId(escuderiaJugadorId);

        // 3. Cargar escuderías y pilotos
        List<Escuderia> escuderias = escuderiaRepository.findByPartidaId(partidaId);
        List<PilotoSimulacion> pilotosSimulacion = new ArrayList<>();
        Map<Integer, PerfilIA> perfilesIA = new HashMap<>();
        Random random = new Random();

        for (Escuderia esc : escuderias) {
            // Auto-alinear pilotos de la IA (no los del jugador)
            if (escuderiaJugadorId == null || !esc.getId().equals(escuderiaJugadorId)) {
                autoAlinearPilotos(esc);
            }

            // Asignar perfil IA aleatorio a escuderías rivales
            if (escuderiaJugadorId == null || !esc.getId().equals(escuderiaJugadorId)) {
                perfilesIA.put(esc.getId(), random.nextBoolean() ? PerfilIA.AGRESIVO : PerfilIA.CONSERVADOR);
            }

            // Crear PilotoSimulacion para cada titular
            boolean esEquipoJugador = escuderiaJugadorId != null && esc.getId().equals(escuderiaJugadorId);

            if (esc.getPiloto1() != null) {
                pilotosSimulacion.add(crearPilotoSimulacion(esc.getPiloto1(), esc, esEquipoJugador));
            }
            if (esc.getPiloto2() != null) {
                pilotosSimulacion.add(crearPilotoSimulacion(esc.getPiloto2(), esc, esEquipoJugador));
            }
        }

        estado.setPilotos(pilotosSimulacion);
        estado.setPerfilesIA(perfilesIA);

        return estado;
    }

    /**
     * Auto-alinea los 2 pilotos con mayor valoración de una escudería en los asientos titulares.
     * Solo se aplica a escuderías controladas por la IA (no la del jugador).
     */
    private void autoAlinearPilotos(Escuderia escuderia) {
        List<Piloto> pilotos = pilotoRepository.findByEscuderiaId(escuderia.getId());

        if (pilotos.isEmpty()) return;

        // Ordenar por valoración descendente
        pilotos.sort((a, b) -> {
            int valA = a.getEstadistica() != null ? a.getEstadistica().getValoracion() : 0;
            int valB = b.getEstadistica() != null ? b.getEstadistica().getValoracion() : 0;
            return Integer.compare(valB, valA);
        });

        // Asignar los 2 mejores a los asientos
        Piloto mejor1 = pilotos.get(0);
        Piloto mejor2 = pilotos.size() > 1 ? pilotos.get(1) : null;

        // Solo actualizar si es necesario (si no coincide con los ya asignados)
        boolean necesitaActualizar = false;
        if (escuderia.getPiloto1() == null || !escuderia.getPiloto1().getId().equals(mejor1.getId())) {
            escuderia.setPiloto1(mejor1);
            necesitaActualizar = true;
        }
        if (mejor2 != null && (escuderia.getPiloto2() == null || !escuderia.getPiloto2().getId().equals(mejor2.getId()))) {
            escuderia.setPiloto2(mejor2);
            necesitaActualizar = true;
        }

        if (necesitaActualizar) {
            escuderiaRepository.save(escuderia);
        }
    }

    /**
     * Crea un PilotoSimulacion a partir de un Piloto JPA y su Escudería.
     * Copia todos los atributos estáticos necesarios para la simulación.
     */
    private PilotoSimulacion crearPilotoSimulacion(Piloto piloto, Escuderia escuderia, boolean esEquipoJugador) {
        PilotoSimulacion ps = new PilotoSimulacion();

        // Identidad
        ps.setPilotoId(piloto.getId());
        ps.setNombre(piloto.getNombre());
        ps.setEscuderiaId(escuderia.getId());
        ps.setEscuderiaNombre(escuderia.getNombre());
        ps.setEscuderiaImagen(escuderia.getImagen());

        // Estadísticas del piloto
        Estadistica est = piloto.getEstadistica();
        if (est != null) {
            ps.setValoracion(est.getValoracion() != null ? est.getValoracion() : 50);
            ps.setCurvaRapida(est.getCurvaRapida() != null ? est.getCurvaRapida() : 50);
            ps.setCurvaLenta(est.getCurvaLenta() != null ? est.getCurvaLenta() : 50);
            ps.setSalidas(est.getSalidas() != null ? est.getSalidas() : 50);
            ps.setConsistencia(est.getConsistencia() != null ? est.getConsistencia() : 50);
        } else {
            ps.setValoracion(50);
            ps.setCurvaRapida(50);
            ps.setCurvaLenta(50);
            ps.setSalidas(50);
            ps.setConsistencia(50);
        }

        // Estadísticas del coche
        ps.setAerodinamica(escuderia.getAerodinamica() != null ? escuderia.getAerodinamica() : 50);
        ps.setMotor(escuderia.getMotor() != null ? escuderia.getMotor() : 50);
        ps.setDurabilidad(escuderia.getDurabilidad() != null ? escuderia.getDurabilidad() : 10);

        // Estado inicial de carrera
        ps.setCompuestoActual(TipoNeumatico.BLANDO); // Se sobreescribe después
        ps.setDesgasteNeumatico(0.0);
        ps.setTiempoTotalMs(0);
        ps.setTiempoUltimaVueltaMs(0);
        ps.setVueltaRapidaMs(Long.MAX_VALUE);
        ps.setPosicion(0);
        ps.setGapMs(0);
        ps.setVueltas(0);
        ps.setDnf(false);
        ps.setEnPitStop(false);
        ps.setNumParadas(0);
        ps.setEsJugador(esEquipoJugador);

        return ps;
    }

    /**
     * Persiste los resultados de la carrera finalizada en la base de datos.
     * 
     * 1. Crea registros PilotoCircuito con posición, tiempo total y vuelta rápida
     * 2. Asigna puntos F1 (25, 18, 15, 12, 10, 8, 6, 4, 2, 1) a los top 10
     * 3. Actualiza Piloto.puntos y Escuderia.puntos
     * 4. Marca Piloto.haCorrido = true
     */
    @Transactional
    public void persistirResultados(EstadoCarrera estado) {
        Partida partida = partidaRepository.findById(estado.getPartidaId())
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));
        Circuito circuito = circuitoRepository.findById(estado.getCircuitoId())
                .orElseThrow(() -> new RuntimeException("Circuito no encontrado"));

        // Map para acumular puntos por escudería
        Map<Integer, Integer> puntosEscuderia = new HashMap<>();

        for (PilotoSimulacion ps : estado.getPilotos()) {
            Piloto piloto = pilotoRepository.findById(ps.getPilotoId())
                    .orElseThrow(() -> new RuntimeException("Piloto no encontrado: " + ps.getPilotoId()));

            // Crear registro PilotoCircuito
            PilotoCircuitoId pcId = new PilotoCircuitoId(
                    estado.getPartidaId(),
                    estado.getCircuitoId(),
                    ps.getPilotoId(),
                    estado.getTemporada()
            );

            PilotoCircuito pc = new PilotoCircuito();
            pc.setId(pcId);
            pc.setPartida(partida);
            pc.setCircuito(circuito);
            pc.setPiloto(piloto);
            pc.setPosicion(ps.isDnf() ? null : ps.getPosicion());

            // Convertir ms a LocalTime
            if (!ps.isDnf()) {
                pc.setTiempoTotal(msToLocalTime(ps.getTiempoTotalMs()));
                pc.setVueltaRapida(msToLocalTime(ps.getVueltaRapidaMs()));
            }
            // DNF → tiempos null

            pilotoCircuitoRepository.save(pc);

            // Asignar puntos F1 si no es DNF y está en top 10
            if (!ps.isDnf() && ps.getPosicion() >= 1 && ps.getPosicion() <= 10) {
                int puntosGanados = PUNTOS_F1[ps.getPosicion() - 1];

                // Actualizar puntos del piloto
                piloto.setPuntos((piloto.getPuntos() != null ? piloto.getPuntos() : 0) + puntosGanados);

                // Acumular puntos para la escudería
                puntosEscuderia.merge(ps.getEscuderiaId(), puntosGanados, Integer::sum);
            }

            // Marcar que ha corrido
            piloto.setHaCorrido(true);
            pilotoRepository.save(piloto);
        }

        // Actualizar puntos de escuderías
        for (Map.Entry<Integer, Integer> entry : puntosEscuderia.entrySet()) {
            Escuderia esc = escuderiaRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Escudería no encontrada: " + entry.getKey()));
            esc.setPuntos((esc.getPuntos() != null ? esc.getPuntos() : 0) + entry.getValue());
            escuderiaRepository.save(esc);
        }
    }

    // === Utilidades de conversión de tiempo ===

    /** Convierte un LocalTime (HH:MM:SS.mmm) a milisegundos */
    public static long localTimeToMs(LocalTime time) {
        if (time == null) return 0;
        return time.getHour() * 3600_000L
                + time.getMinute() * 60_000L
                + time.getSecond() * 1_000L
                + time.getNano() / 1_000_000L;
    }

    /** Convierte milisegundos a LocalTime (HH:MM:SS.mmm) */
    public static LocalTime msToLocalTime(long ms) {
        long totalSeconds = ms / 1000;
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);
        int nanos = (int) ((ms % 1000) * 1_000_000);
        return LocalTime.of(hours, minutes, seconds, nanos);
    }
}
