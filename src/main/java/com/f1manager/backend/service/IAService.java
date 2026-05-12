package com.f1manager.backend.service;

import com.f1manager.backend.dto.EscuderiaMejoraTipo;
import com.f1manager.backend.dto.NegociacionRequestDTO;
import com.f1manager.backend.dto.NegociacionResponseDTO;
import com.f1manager.backend.dto.TraspasoRequestDTO;
import com.f1manager.backend.entity.Escuderia;
import com.f1manager.backend.entity.Partida;
import com.f1manager.backend.entity.Piloto;
import com.f1manager.backend.model.ia.IADecision;
import com.f1manager.backend.model.ia.IAResultadoPartida;
import com.f1manager.backend.repository.EscuderiaRepository;
import com.f1manager.backend.repository.PartidaRepository;
import com.f1manager.backend.repository.PilotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Servicio de IA para las decisiones de las escuderías rivales.
 *
 * Flujo:
 * 1. iniciarCarrera → procesarDecisionesIA (async, calcula en memoria)
 * 2. avanzarCarrera → aplicarDecisiones (sync, flush a BD)
 * 3. Si el usuario cierra la app → limpiarDecisiones (nada se persiste)
 */
@Service
public class IAService {

    private static final Logger log = LoggerFactory.getLogger(IAService.class);
    private static final int MAX_ACCIONES_POR_EQUIPO = 5;

    private final EscuderiaRepository escuderiaRepository;
    private final PilotoRepository pilotoRepository;
    private final EscuderiaService escuderiaService;
    private final TraspasoService traspasoService;
    private final PartidaRepository partidaRepository;

    /** Almacén en memoria: partidaId → decisiones calculadas */
    private final Map<Integer, IAResultadoPartida> decisiones = new ConcurrentHashMap<>();

    private final Random random = new Random();

    public IAService(EscuderiaRepository escuderiaRepository,
                     PilotoRepository pilotoRepository,
                     EscuderiaService escuderiaService,
                     TraspasoService traspasoService,
                     PartidaRepository partidaRepository) {
        this.escuderiaRepository = escuderiaRepository;
        this.pilotoRepository = pilotoRepository;
        this.escuderiaService = escuderiaService;
        this.traspasoService = traspasoService;
        this.partidaRepository = partidaRepository;
    }

    // =========================================================================
    // FASE 1: Cálculo asíncrono (se ejecuta durante la carrera)
    // =========================================================================

    @Async
    public void procesarDecisionesIA(Integer partidaId) {
        log.info("[IA] Iniciando procesamiento para partida {}", partidaId);
        IAResultadoPartida resultado = new IAResultadoPartida();

        try {
            Partida partida = partidaRepository.findById(partidaId)
                    .orElseThrow(() -> new RuntimeException("Partida no encontrada: " + partidaId));
            Integer escuderiaUsuarioId = partida.getEscuderiaSeleccionada() != null
                    ? partida.getEscuderiaSeleccionada().getId() : null;

            List<Escuderia> todasEscuderias = escuderiaRepository.findByPartidaId(partidaId);
            List<Piloto> todosPilotos = pilotoRepository.findByPartidaId(partidaId);

            for (Escuderia esc : todasEscuderias) {
                if (esc.getId().equals(escuderiaUsuarioId)) continue;

                List<IADecision> decisionesEquipo = calcularDecisionesEquipo(
                        esc, todasEscuderias, todosPilotos, escuderiaUsuarioId, partida);

                decisionesEquipo.forEach(resultado::agregarDecision);
            }

            log.info("[IA] Procesamiento completado para partida {}. Total decisiones: {}",
                    partidaId, resultado.getDecisiones().size());
        } catch (Exception e) {
            log.error("[IA] Error procesando partida {}: {}", partidaId, e.getMessage(), e);
        } finally {
            resultado.setCompletado(true);
            decisiones.put(partidaId, resultado);
        }
    }

    // =========================================================================
    // FASE 2: Flush (se ejecuta en avanzarCarrera, sincronamente)
    // =========================================================================

    @Transactional
    public void aplicarDecisiones(Integer partidaId) {
        IAResultadoPartida resultado = decisiones.remove(partidaId);
        if (resultado == null || resultado.getDecisiones().isEmpty()) {
            log.info("[IA] No hay decisiones pendientes para partida {}", partidaId);
            return;
        }

        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada: " + partidaId));
        log.info("[IA] Aplicando {} decisiones para partida {}", resultado.getDecisiones().size(), partidaId);

        for (IADecision decision : resultado.getDecisiones()) {
            try {
                switch (decision.getTipo()) {
                    case MEJORAR -> aplicarMejora(decision);
                    case FICHAR -> aplicarFichaje(decision, partida);
                    case AHORRAR -> log.debug("[IA] Escudería {} decide ahorrar", decision.getEscuderiaId());
                }
            } catch (Exception e) {
                log.warn("[IA] Error aplicando decisión {} para escudería {}: {}",
                        decision.getTipo(), decision.getEscuderiaId(), e.getMessage());
            }
        }
    }

    public void limpiarDecisiones(Integer partidaId) {
        decisiones.remove(partidaId);
    }

    public IAResultadoPartida obtenerEstado(Integer partidaId) {
        return decisiones.get(partidaId);
    }

    // =========================================================================
    // LÓGICA DE DECISIÓN POR EQUIPO
    // =========================================================================

    private List<IADecision> calcularDecisionesEquipo(
            Escuderia esc, List<Escuderia> todasEscuderias,
            List<Piloto> todosPilotos, Integer escuderiaUsuarioId, Partida partida) {

        List<IADecision> acciones = new ArrayList<>();
        // Trabajamos con una copia del presupuesto para simular el gasto
        BigDecimal presupuestoDisponible = esc.getPresupuesto();

        for (int iteracion = 0; iteracion < MAX_ACCIONES_POR_EQUIPO; iteracion++) {
            // Recalcular costes de instalaciones
            float costeTunel = EscuderiaService.costeMejoraInstalacion(esc.getTunelViento());
            float costeBanco = EscuderiaService.costeMejoraInstalacion(esc.getBancoPruebas());
            float costeEscuela = EscuderiaService.costeMejoraEscuela(esc.getEscuelaPilotos());

            // Puntuar necesidades
            int deficitAero = 99 - esc.getAerodinamica();
            int deficitMotor = 99 - esc.getMotor();
            int deficitDura = (20 - esc.getDurabilidad()) * 5; // Normalizado a escala ~100

            List<Piloto> pilotosEquipo = todosPilotos.stream()
                    .filter(p -> p.getEscuderia() != null && p.getEscuderia().getId().equals(esc.getId()))
                    .collect(Collectors.toList());
            int mediaValoracion = calcularMediaValoracionTitulares(esc, pilotosEquipo);
            int deficitPilotos = 99 - mediaValoracion;

            // PRIORIDAD 1: Instalaciones bajas (< 3)
            if (esc.getTunelViento() < 3 && presupuestoDisponible.floatValue() >= costeTunel) {
                acciones.add(IADecision.mejorar(esc.getId(), EscuderiaMejoraTipo.TUNEL_VIENTO));
                presupuestoDisponible = presupuestoDisponible.subtract(BigDecimal.valueOf(costeTunel));
                continue;
            }
            if (esc.getBancoPruebas() < 3 && presupuestoDisponible.floatValue() >= costeBanco) {
                acciones.add(IADecision.mejorar(esc.getId(), EscuderiaMejoraTipo.BANCO_PRUEBAS));
                presupuestoDisponible = presupuestoDisponible.subtract(BigDecimal.valueOf(costeBanco));
                continue;
            }
            if (esc.getEscuelaPilotos() < 3 && presupuestoDisponible.floatValue() >= costeEscuela) {
                acciones.add(IADecision.mejorar(esc.getId(), EscuderiaMejoraTipo.ESCUELA_PILOTOS));
                presupuestoDisponible = presupuestoDisponible.subtract(BigDecimal.valueOf(costeEscuela));
                continue;
            }

            // PRIORIDAD 2: ¿Mejorar coche o fichar?
            int puntajeCoche = deficitAero + deficitMotor + deficitDura;
            int puntajePilotos = deficitPilotos * 3; // Ponderar para equiparar escala

            if (puntajeCoche >= puntajePilotos) {
                // Intentar mejorar la parte más débil del coche
                EscuderiaMejoraTipo mejora = elegirMejoraCoche(esc, deficitAero, deficitMotor, deficitDura);
                float costeMejora = obtenerCosteMejora(esc, mejora);

                if (presupuestoDisponible.floatValue() >= costeMejora) {
                    acciones.add(IADecision.mejorar(esc.getId(), mejora));
                    presupuestoDisponible = presupuestoDisponible.subtract(BigDecimal.valueOf(costeMejora));
                    // Simular el aumento para la siguiente iteración
                    simularMejora(esc, mejora);
                    continue;
                }
            } else {
                // Intentar fichar
                IADecision fichaje = evaluarFichaje(esc, todosPilotos, presupuestoDisponible,
                        escuderiaUsuarioId, partida);
                if (fichaje != null) {
                    acciones.add(fichaje);
                    presupuestoDisponible = presupuestoDisponible.subtract(fichaje.getOfertaPrecio());
                    break; // Solo un fichaje por carrera para evitar caos
                }
            }

            // Si no puede hacer nada → ahorrar y salir del bucle
            break;
        }

        if (acciones.isEmpty()) {
            acciones.add(IADecision.ahorrar(esc.getId()));
        }

        return acciones;
    }

    // =========================================================================
    // HELPERS DE DECISIÓN
    // =========================================================================

    private int calcularMediaValoracionTitulares(Escuderia esc, List<Piloto> pilotosEquipo) {
        int sum = 0;
        int count = 0;
        if (esc.getPiloto1() != null && esc.getPiloto1().getEstadistica() != null) {
            sum += esc.getPiloto1().getEstadistica().getValoracion();
            count++;
        }
        if (esc.getPiloto2() != null && esc.getPiloto2().getEstadistica() != null) {
            sum += esc.getPiloto2().getEstadistica().getValoracion();
            count++;
        }
        return count > 0 ? sum / count : 50;
    }

    private EscuderiaMejoraTipo elegirMejoraCoche(Escuderia esc, int defAero, int defMotor, int defDura) {
        // Elegir la parte más débil con algo de aleatoriedad
        Map<EscuderiaMejoraTipo, Integer> candidatos = new LinkedHashMap<>();
        if (esc.getAerodinamica() < 99) candidatos.put(EscuderiaMejoraTipo.AERODINAMICA, defAero);
        if (esc.getMotor() < 99) candidatos.put(EscuderiaMejoraTipo.MOTOR, defMotor);
        if (esc.getDurabilidad() < 20) candidatos.put(EscuderiaMejoraTipo.DURABILIDAD, defDura);

        if (candidatos.isEmpty()) return EscuderiaMejoraTipo.AERODINAMICA;

        // Ordenar por déficit descendente y elegir top con algo de ruido
        List<Map.Entry<EscuderiaMejoraTipo, Integer>> sorted = candidatos.entrySet().stream()
                .sorted(Map.Entry.<EscuderiaMejoraTipo, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        // 70% elige la más necesaria, 30% elige la segunda
        if (sorted.size() > 1 && random.nextDouble() < 0.3) {
            return sorted.get(1).getKey();
        }
        return sorted.get(0).getKey();
    }

    private float obtenerCosteMejora(Escuderia esc, EscuderiaMejoraTipo tipo) {
        return switch (tipo) {
            case AERODINAMICA -> EscuderiaService.costeMejoraBasica(esc.getAerodinamica());
            case MOTOR -> EscuderiaService.costeMejoraBasica(esc.getMotor());
            case DURABILIDAD -> EscuderiaService.costeMejoraDurabilidad(esc.getDurabilidad());
            case TUNEL_VIENTO -> EscuderiaService.costeMejoraInstalacion(esc.getTunelViento());
            case BANCO_PRUEBAS -> EscuderiaService.costeMejoraInstalacion(esc.getBancoPruebas());
            case ESCUELA_PILOTOS -> EscuderiaService.costeMejoraEscuela(esc.getEscuelaPilotos());
        };
    }

    /** Simula el efecto de la mejora en la copia local (para iteraciones siguientes) */
    private void simularMejora(Escuderia esc, EscuderiaMejoraTipo tipo) {
        switch (tipo) {
            case AERODINAMICA -> esc.setAerodinamica(Math.min(esc.getAerodinamica() + 1, 99));
            case MOTOR -> esc.setMotor(Math.min(esc.getMotor() + 1, 99));
            case DURABILIDAD -> esc.setDurabilidad(Math.min(esc.getDurabilidad() + 1, 20));
            case TUNEL_VIENTO -> esc.setTunelViento(Math.min(esc.getTunelViento() + 1, 5));
            case BANCO_PRUEBAS -> esc.setBancoPruebas(Math.min(esc.getBancoPruebas() + 1, 5));
            case ESCUELA_PILOTOS -> esc.setEscuelaPilotos(Math.min(esc.getEscuelaPilotos() + 1, 5));
        }
    }

    // =========================================================================
    // LÓGICA DE FICHAJE
    // =========================================================================

    private IADecision evaluarFichaje(Escuderia esc, List<Piloto> todosPilotos,
                                      BigDecimal presupuesto, Integer escuderiaUsuarioId, Partida partida) {
        // Valoración mínima de los titulares actuales
        int peorTitular = getPeorValoracionTitular(esc);

        // Filtrar candidatos: no del mismo equipo, con estadísticas, mejor que el peor titular
        List<Piloto> candidatos = todosPilotos.stream()
                .filter(p -> p.getEscuderia() != null && !p.getEscuderia().getId().equals(esc.getId()))
                .filter(p -> p.getEstadistica() != null)
                .filter(p -> p.getEstadistica().getValoracion() > peorTitular)
                .filter(p -> p.getValor() != null && p.getValor().compareTo(presupuesto) <= 0)
                .collect(Collectors.toList());

        if (candidatos.isEmpty()) return null;

        // Puntuar cada candidato
        Piloto mejorCandidato = null;
        double mejorPuntuacion = -1;

        for (Piloto p : candidatos) {
            double puntuacion = calcularPuntuacionFichaje(p, presupuesto);
            if (puntuacion > mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                mejorCandidato = p;
            }
        }

        if (mejorCandidato == null) return null;

        // Generar precio de oferta: valor × factor (0.9 a 1.3)
        double factor = 0.9 + random.nextDouble() * 0.4;
        BigDecimal oferta = mejorCandidato.getValor()
                .multiply(BigDecimal.valueOf(factor))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // No ofertar más de lo que tienen
        if (oferta.compareTo(presupuesto) > 0) {
            oferta = presupuesto.multiply(BigDecimal.valueOf(0.8))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }

        boolean esDelUsuario = mejorCandidato.getEscuderia().getId().equals(escuderiaUsuarioId);

        return IADecision.fichar(esc.getId(), mejorCandidato.getId(), oferta, esDelUsuario);
    }

    private double calcularPuntuacionFichaje(Piloto p, BigDecimal presupuesto) {
        int valoracion = p.getEstadistica().getValoracion();
        int edad = p.getEdad() != null ? p.getEdad() : 25;
        double valor = p.getValor() != null ? p.getValor().doubleValue() : 10.0;
        double pres = presupuesto.doubleValue();

        // Valoración (0-99 → 0-1) × peso 0.4
        double scoreValoracion = (valoracion / 99.0) * 0.4;
        // Juventud (menor edad = mejor) × peso 0.3
        double scoreJuventud = Math.max(0, (35 - edad) / 20.0) * 0.3;
        // Asequibilidad (menor ratio valor/presupuesto = mejor) × peso 0.3
        double scorePrecio = Math.max(0, 1.0 - (valor / pres)) * 0.3;

        return scoreValoracion + scoreJuventud + scorePrecio;
    }

    private int getPeorValoracionTitular(Escuderia esc) {
        int val1 = (esc.getPiloto1() != null && esc.getPiloto1().getEstadistica() != null)
                ? esc.getPiloto1().getEstadistica().getValoracion() : 0;
        int val2 = (esc.getPiloto2() != null && esc.getPiloto2().getEstadistica() != null)
                ? esc.getPiloto2().getEstadistica().getValoracion() : 0;
        return Math.min(val1, val2);
    }

    // =========================================================================
    // APLICACIÓN DE DECISIONES (FLUSH)
    // =========================================================================

    private void aplicarMejora(IADecision decision) {
        escuderiaService.procesarMejora(decision.getEscuderiaId(), decision.getTipoMejora());
        log.info("[IA] Escudería {} mejoró {}", decision.getEscuderiaId(), decision.getTipoMejora());
    }

    private void aplicarFichaje(IADecision decision, Partida partida) {
        if (decision.isPilotoDelUsuario()) {
            // Crear oferta pendiente para que el usuario la vea
            TraspasoRequestDTO request = new TraspasoRequestDTO();
            request.setPartida(partida.getId());
            request.setPiloto(decision.getPilotoObjetivoId());
            request.setEscuderiaDestino(decision.getEscuderiaId());
            request.setPrecio(decision.getOfertaPrecio());
            request.setTemporada(partida.getAnio());
            request.setEnCurso(true);
            request.setAceptada(false);

            traspasoService.crearDesdeRequest(request);
            log.info("[IA] Escudería {} creó oferta por piloto {} (del usuario) - Precio: {}",
                    decision.getEscuderiaId(), decision.getPilotoObjetivoId(), decision.getOfertaPrecio());
        } else {
            // Negociación IA vs IA (resolución instantánea)
            NegociacionRequestDTO request = new NegociacionRequestDTO();
            request.setPartidaId(partida.getId());
            request.setPilotoId(decision.getPilotoObjetivoId());
            request.setEscuderiaDestinoId(decision.getEscuderiaId());
            request.setPrecio(decision.getOfertaPrecio());

            NegociacionResponseDTO response = traspasoService.negociarTraspaso(request);
            log.info("[IA] Escudería {} negoció por piloto {} → {}",
                    decision.getEscuderiaId(), decision.getPilotoObjetivoId(), response.getResultado());

            // Si fue aceptado, gestionar alineación
            if (response.getResultado() == NegociacionResponseDTO.Resultado.ACEPTADO) {
                gestionarAlineacionPostFichaje(decision.getEscuderiaId(), decision.getPilotoObjetivoId());
            }
        }
    }

    /** Tras un fichaje exitoso, sustituye al peor titular si el nuevo es mejor */
    private void gestionarAlineacionPostFichaje(Integer escuderiaId, Integer nuevoPilotoId) {
        Optional<Escuderia> optEsc = escuderiaRepository.findById(escuderiaId);
        Optional<Piloto> optPiloto = pilotoRepository.findById(nuevoPilotoId);

        if (optEsc.isEmpty() || optPiloto.isEmpty()) return;

        Escuderia esc = optEsc.get();
        Piloto nuevoPiloto = optPiloto.get();
        if (nuevoPiloto.getEstadistica() == null) return;

        int valorNuevo = nuevoPiloto.getEstadistica().getValoracion();

        int val1 = (esc.getPiloto1() != null && esc.getPiloto1().getEstadistica() != null)
                ? esc.getPiloto1().getEstadistica().getValoracion() : 0;
        int val2 = (esc.getPiloto2() != null && esc.getPiloto2().getEstadistica() != null)
                ? esc.getPiloto2().getEstadistica().getValoracion() : 0;

        // Sustituir al peor titular si el nuevo es mejor
        if (valorNuevo > val1 && val1 <= val2) {
            esc.setPiloto1(nuevoPiloto);
            escuderiaRepository.save(esc);
            log.info("[IA] {} reemplaza al titular 1 en escudería {}", nuevoPiloto.getNombre(), escuderiaId);
        } else if (valorNuevo > val2) {
            esc.setPiloto2(nuevoPiloto);
            escuderiaRepository.save(esc);
            log.info("[IA] {} reemplaza al titular 2 en escudería {}", nuevoPiloto.getNombre(), escuderiaId);
        }
    }
}
