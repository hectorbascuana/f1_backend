package com.f1manager.backend.service;

import com.f1manager.backend.dto.*;
import com.f1manager.backend.model.simulation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * CEREBRO del motor de carrera.
 * 
 * Gestiona múltiples carreras simultáneas en un ConcurrentHashMap<UUID,
 * EstadoCarrera>.
 * Contiene toda la lógica matemática determinista de simulación:
 * - Clasificación (vuelta rápida sin desgaste)
 * - Cálculo de tiempo por vuelta con penalizaciones
 * - Desgaste de neumáticos exponencial
 * - IA Top-Down de pit stops (sistema de utilidad)
 * - Probabilidad de DNF por durabilidad
 * 
 * SOSTENIBILIDAD: Todo en RAM, sin acceso a BD. Sin bibliotecas pesadas.
 * Solo aritmética básica y java.util.Random.
 */
@Service
public class CarreraMotorService {

    private static final Logger log = LoggerFactory.getLogger(CarreraMotorService.class);

    /** Map de carreras activas. Se elimina la entrada al finalizar (cleanup). */
    private final ConcurrentHashMap<UUID, EstadoCarrera> carrerasActivas = new ConcurrentHashMap<>();

    private final CarreraDataService carreraDataService;

    /**
     * Constante de desgaste: a 100% desgaste, penalización máxima de 5000ms (+5s)
     */
    private static final double CONSTANTE_DESGASTE = 5000.0;

    /** Coste fijo de un pit stop en ms (20 segundos) */
    private static final long COSTE_PIT_STOP_MS = 20_000;

    /** Probabilidad base de DNF por vuelta (0.1%) */
    private static final double PROBABILIDAD_DNF_BASE = 0.001;

    /** Random thread-safe para la simulación */
    private final Random random = new Random();

    public CarreraMotorService(CarreraDataService carreraDataService) {
        this.carreraDataService = carreraDataService;
    }

    // =========================================================================
    // INICIO DE CARRERA
    // =========================================================================

    /**
     * Inicia una carrera: carga datos y simula clasificación.
     * 
     * @param partidaId ID de la partida
     * @return DTO con UUID, circuito y parrilla de salida
     */
    public CarreraStartResponseDTO iniciarCarrera(Integer partidaId) {
        // 1. Cargar datos de BD → POJOs volátiles
        EstadoCarrera estado = carreraDataService.cargarDatosCarrera(partidaId);

        // 2. Simular clasificación (vuelta rápida sin desgaste con Blandos por defecto)
        simularClasificacion(estado);

        // 3. Registrar en el Map de carreras activas
        carrerasActivas.put(estado.getUuid(), estado);

        log.info("Carrera iniciada (Clasificación completada): UUID={}, Circuito={}",
                estado.getUuid(), estado.getCircuitoNombre());

        // 4. Construir respuesta (la parrilla ya está calculada)
        return construirStartResponse(estado);
    }

    /**
     * Asigna los compuestos iniciales antes de empezar la Vuelta 1.
     * Se llama desde procesarVuelta si es la primera vez.
     */
    private void asignarCompuestosIniciales(EstadoCarrera estado,
            TipoNeumatico compuestoPiloto1,
            TipoNeumatico compuestoPiloto2) {
        // Identificar los pilotos del jugador
        List<PilotoSimulacion> pilotosJugador = estado.getPilotos().stream()
                .filter(PilotoSimulacion::isEsJugador)
                .collect(Collectors.toList());

        // Asignar compuestos del jugador (ordenados por ID para consistencia)
        pilotosJugador.sort(Comparator.comparing(PilotoSimulacion::getPilotoId));
        if (pilotosJugador.size() >= 1 && compuestoPiloto1 != null) {
            pilotosJugador.get(0).setCompuestoActual(compuestoPiloto1);
        }
        if (pilotosJugador.size() >= 2 && compuestoPiloto2 != null) {
            pilotosJugador.get(1).setCompuestoActual(compuestoPiloto2);
        }

        // Asignar compuestos de la IA al azar (1/3 probabilidad cada compuesto)
        TipoNeumatico[] compuestos = TipoNeumatico.values();
        for (PilotoSimulacion ps : estado.getPilotos()) {
            if (!ps.isEsJugador()) {
                ps.setCompuestoActual(compuestos[random.nextInt(compuestos.length)]);
            }
        }
    }

    /**
     * Simula la clasificación: cada piloto hace UNA vuelta rápida con Blandos,
     * sin desgaste, para determinar la parrilla de salida.
     * 
     * La clasificación usa la misma fórmula de tiempo pero con desgaste = 0
     * y sin penalización de salida (no es vuelta 1 de carrera).
     */
    private void simularClasificacion(EstadoCarrera estado) {
        for (PilotoSimulacion ps : estado.getPilotos()) {
            // Guardar compuesto original
            TipoNeumatico compuestoOriginal = ps.getCompuestoActual();

            // Clasificación siempre con Blandos y desgaste 0
            ps.setCompuestoActual(TipoNeumatico.BLANDO);
            ps.setDesgasteNeumatico(0.0);

            // Calcular tiempo de clasificación (vuelta 0 = sin penalización de salida)
            long tiempoClasificacion = calcularTiempoVuelta(ps, estado, 0);
            ps.setVueltaRapidaMs(tiempoClasificacion);

            // Restaurar compuesto elegido para la carrera
            ps.setCompuestoActual(compuestoOriginal);
            ps.setDesgasteNeumatico(0.0);
        }

        // Ordenar por tiempo de clasificación (menor = mejor)
        estado.getPilotos().sort(Comparator.comparingLong(PilotoSimulacion::getVueltaRapidaMs));

        // Asignar posiciones de parrilla
        for (int i = 0; i < estado.getPilotos().size(); i++) {
            estado.getPilotos().get(i).setPosicion(i + 1);
        }
    }

    // =========================================================================
    // PROCESAMIENTO DE VUELTA
    // =========================================================================

    /**
     * Procesa UNA vuelta para todos los pilotos de la carrera.
     * 
     * Secuencia por vuelta:
     * 1. Incrementar vuelta actual
     * 2. Para cada piloto activo (no DNF):
     * a. Evaluar pit stop (IA o jugador)
     * b. Calcular tiempo de vuelta
     * c. Aplicar desgaste de neumáticos
     * d. Evaluar DNF por durabilidad
     * 3. Recalcular ranking y gaps
     * 4. Si es última vuelta: persistir y cleanup
     * 
     * @param uuid             ID de la sesión de carrera
     * @param pitStopPiloto1   ¿El jugador quiere pit para su titular 1?
     * @param compuestoPiloto1 Nuevo compuesto si pit (puede ser null)
     * @param pitStopPiloto2   ¿El jugador quiere pit para su titular 2?
     * @param compuestoPiloto2 Nuevo compuesto si pit (puede ser null)
     * @return DTO con el estado actualizado
     */
    public CarreraVueltaResponseDTO procesarVuelta(UUID uuid,
            boolean pitStopPiloto1, TipoNeumatico compuestoPiloto1,
            boolean pitStopPiloto2, TipoNeumatico compuestoPiloto2) {
        EstadoCarrera estado = carrerasActivas.get(uuid);
        if (estado == null) {
            throw new RuntimeException("Carrera no encontrada: " + uuid);
        }
        if (estado.isFinalizada()) {
            throw new RuntimeException("La carrera ya ha finalizado");
        }

        // 1. Incrementar vuelta
        int vueltaAnterior = estado.getVueltaActual();
        estado.setVueltaActual(vueltaAnterior + 1);
        int vuelta = estado.getVueltaActual();

        // Si es la vuelta 1, asignamos los compuestos iniciales elegidos por el usuario
        if (vuelta == 1) {
            asignarCompuestosIniciales(estado, compuestoPiloto1, compuestoPiloto2);
        }

        // 2. Identificar pilotos del jugador para aplicar decisiones de pit
        List<PilotoSimulacion> pilotosJugador = estado.getPilotos().stream()
                .filter(PilotoSimulacion::isEsJugador)
                .sorted(Comparator.comparing(PilotoSimulacion::getPilotoId))
                .collect(Collectors.toList());

        // 3. Procesar cada piloto
        for (PilotoSimulacion ps : estado.getPilotos()) {
            if (ps.isDnf())
                continue; // Saltarse los retirados

            ps.setEnPitStop(false); // Reset flag de pit stop

            // === PIT STOP ===
            if (ps.isEsJugador()) {
                // Decisión del jugador
                boolean hacePit = false;
                TipoNeumatico nuevoCompuesto = null;

                if (pilotosJugador.size() >= 1 && ps.getPilotoId().equals(pilotosJugador.get(0).getPilotoId())) {
                    hacePit = pitStopPiloto1;
                    nuevoCompuesto = compuestoPiloto1;
                } else if (pilotosJugador.size() >= 2 && ps.getPilotoId().equals(pilotosJugador.get(1).getPilotoId())) {
                    hacePit = pitStopPiloto2;
                    nuevoCompuesto = compuestoPiloto2;
                }

                if (hacePit) {
                    ejecutarPitStop(ps, nuevoCompuesto != null ? nuevoCompuesto : TipoNeumatico.MEDIO);
                }
            } else {
                // Decisión de la IA
                evaluarPitStopIA(ps, estado);
            }

            // === CALCULAR TIEMPO DE VUELTA ===
            long tiempoVuelta = calcularTiempoVuelta(ps, estado, vuelta);

            // Si hizo pit stop, sumar el coste
            if (ps.isEnPitStop()) {
                tiempoVuelta += COSTE_PIT_STOP_MS;
            }

            ps.setTiempoUltimaVueltaMs(tiempoVuelta);
            ps.setTiempoTotalMs(ps.getTiempoTotalMs() + tiempoVuelta);
            ps.setVueltas(vuelta);

            // Actualizar vuelta rápida personal (sin contar pit stop)
            long tiempoSinPit = ps.isEnPitStop() ? tiempoVuelta - COSTE_PIT_STOP_MS : tiempoVuelta;
            if (tiempoSinPit < ps.getVueltaRapidaMs()) {
                ps.setVueltaRapidaMs(tiempoSinPit);
            }

            // === DESGASTE DE NEUMÁTICOS ===
            aplicarDesgaste(ps);

            // === EVALUAR DNF ===
            evaluarDNF(ps);
        }

        // 4. Recalcular ranking y gaps
        recalcularRanking(estado);

        // 5. ¿Es la última vuelta?
        if (vuelta >= estado.getTotalVueltas()) {
            estado.setFinalizada(true);

            // Persistir resultados en BD
            carreraDataService.persistirResultados(estado);

            // Cleanup: liberar RAM
            carrerasActivas.remove(uuid);

            log.info("Carrera finalizada y persistida: UUID={}", uuid);
        }

        return construirVueltaResponse(estado);
    }

    // =========================================================================
    // FÓRMULA DE TIEMPO POR VUELTA
    // =========================================================================

    /**
     * Calcula el tiempo de una vuelta para un piloto (en ms).
     * 
     * Fórmula:
     * tiempoVuelta = tiempoBase
     * + penalizaciónAerodinámica(aeroReq, aeroCoche, curvaLenta)
     * + penalizaciónMotor(motorReq, motorCoche, curvaRapida)
     * + penalizaciónNeumático(compuesto, desgaste)
     * + penalizaciónSalida(salidas, vuelta) // Solo vuelta 1
     * + ruidoAleatorio(consistencia)
     * 
     * NOTAS de diseño (feedback del usuario):
     * - curvaLenta es el proxy de habilidad aerodinámica del piloto
     * - curvaRapida es el proxy de habilidad en motor/rectas del piloto
     * - valoracion NO se usa en carrera (solo para precio y resumen)
     * 
     * @param ps     Piloto a calcular
     * @param estado Estado de la carrera (contiene parámetros del circuito)
     * @param vuelta Número de vuelta actual (0 = clasificación)
     * @return Tiempo en milisegundos
     */
    long calcularTiempoVuelta(PilotoSimulacion ps, EstadoCarrera estado, int vuelta) {
        long tiempo = estado.getTiempoBaseMs();

        // 1. Penalización aerodinámica
        // Cruza requisito aero del circuito [1-10] con aero del coche [1-99] y
        // curvaLenta del piloto [1-99]
        // A menor nivel vs requisito, mayor penalización
        tiempo += calcularPenalizacionAerodinamica(estado.getAerodinamicaReq(), ps.getAerodinamica(),
                ps.getCurvaLenta());

        // 2. Penalización motor
        // Cruza requisito motor del circuito [1-10] con motor del coche [1-99] y
        // curvaRapida del piloto [1-99]
        tiempo += calcularPenalizacionMotor(estado.getMotorReq(), ps.getMotor(), ps.getCurvaRapida());

        // 3. Penalización neumáticos (base + desgaste exponencial)
        tiempo += calcularPenalizacionNeumaticos(ps.getCompuestoActual(), ps.getDesgasteNeumatico());

        // 4. Penalización de salida (solo vuelta 1)
        if (vuelta == 1) {
            tiempo += calcularPenalizacionSalida(ps.getSalidas());
        }

        // 5. Ruido aleatorio (reducido por consistencia)
        tiempo += calcularRuidoAleatorio(ps.getConsistencia());

        return Math.max(tiempo, estado.getTiempoBaseMs()); // Nunca más rápido que el base teórico
    }

    /**
     * Penalización aerodinámica (ms).
     * 
     * deficitAero = max(0, aeroReq * 10 - aeroCoche)
     * deficitPiloto = max(0, aeroReq * 10 - curvaLenta)
     * penalización = (deficitAero * 15) + (deficitPiloto * 10)
     * 
     * Ejemplo: aeroReq=10, aeroCoche=50, curvaLenta=60
     * deficit coche = max(0, 100-50) = 50 → 750ms
     * deficit piloto = max(0, 100-60) = 40 → 400ms
     * total = 1150ms (+1.15s)
     */
    private long calcularPenalizacionAerodinamica(int aeroReq, int aeroCoche, int curvaLenta) {
        int deficitCoche = Math.max(0, aeroReq * 10 - aeroCoche);
        int deficitPiloto = Math.max(0, aeroReq * 10 - curvaLenta);
        return (long) (deficitCoche * 15) + (long) (deficitPiloto * 10);
    }

    /**
     * Penalización motor (ms).
     * 
     * deficitMotor = max(0, motorReq * 10 - motorCoche)
     * deficitPiloto = max(0, motorReq * 10 - curvaRapida)
     * penalización = (deficitMotor * 15) + (deficitPiloto * 10)
     */
    private long calcularPenalizacionMotor(int motorReq, int motorCoche, int curvaRapida) {
        int deficitCoche = Math.max(0, motorReq * 10 - motorCoche);
        int deficitPiloto = Math.max(0, motorReq * 10 - curvaRapida);
        return (long) (deficitCoche * 15) + (long) (deficitPiloto * 10);
    }

    /**
     * Penalización de neumáticos (ms).
     * 
     * penalización = penalizaciónBase + CONSTANTE_DESGASTE × (desgaste / 100)²
     * 
     * El desgaste exponencial hace que el rendimiento caiga DRÁSTICAMENTE
     * al final de la vida útil:
     * 30% desgaste → +450ms
     * 60% desgaste → +1800ms
     * 90% desgaste → +4050ms
     * 100% desgaste → +5000ms
     */
    private long calcularPenalizacionNeumaticos(TipoNeumatico compuesto, double desgaste) {
        double factorDesgaste = desgaste / 100.0;
        long penalizacionDesgaste = (long) (CONSTANTE_DESGASTE * factorDesgaste * factorDesgaste);
        return compuesto.getPenalizacionBaseMs() + penalizacionDesgaste;
    }

    /**
     * Penalización de salida (ms). Solo se aplica en la vuelta 1.
     * 
     * penalización = max(0, (50 - salidas)) × 30
     * 
     * Piloto con salidas=99 → 0ms (perfecta)
     * Piloto con salidas=50 → 0ms
     * Piloto con salidas=20 → 900ms (+0.9s)
     * Piloto con salidas=1 → 1470ms (+1.47s)
     */
    private long calcularPenalizacionSalida(int salidas) {
        return Math.max(0, (50 - salidas)) * 30L;
    }

    /**
     * Ruido aleatorio (ms). Simula variabilidad humana.
     * 
     * rango = max(50, (100 - consistencia) × 12)
     * ruido = random(-rango, +rango)
     * 
     * Consistencia 99 → ±50ms (máquina)
     * Consistencia 50 → ±600ms
     * Consistencia 1 → ±1188ms (impredecible)
     */
    private long calcularRuidoAleatorio(int consistencia) {
        int rango = Math.max(50, (100 - consistencia) * 12);
        return (long) (random.nextDouble() * 2 * rango - rango);
    }

    // =========================================================================
    // DESGASTE DE NEUMÁTICOS
    // =========================================================================

    /**
     * Aplica desgaste al neumático actual del piloto.
     * 
     * La tasa de desgaste varía dentro del rango [min, max] del compuesto:
     * BLANDO: 8-10% por vuelta
     * MEDIO: 4-6% por vuelta
     * DURO: 1-3% por vuelta
     * 
     * Se añade un pequeño factor aleatorio dentro del rango para variabilidad.
     * El desgaste se limita a 100%.
     */
    private void aplicarDesgaste(PilotoSimulacion ps) {
        TipoNeumatico compuesto = ps.getCompuestoActual();
        double min = compuesto.getDesgasteMinPorVuelta();
        double max = compuesto.getDesgasteMaxPorVuelta();
        double desgasteVuelta = min + random.nextDouble() * (max - min);

        ps.setDesgasteNeumatico(Math.min(100.0, ps.getDesgasteNeumatico() + desgasteVuelta));
    }

    // =========================================================================
    // PIT STOPS
    // =========================================================================

    /**
     * Ejecuta un pit stop: reset de desgaste, cambio de compuesto, flags.
     */
    private void ejecutarPitStop(PilotoSimulacion ps, TipoNeumatico nuevoCompuesto) {
        ps.setCompuestoActual(nuevoCompuesto);
        ps.setDesgasteNeumatico(0.0);
        ps.setEnPitStop(true);
        ps.setNumParadas(ps.getNumParadas() + 1);
    }

    /**
     * IA Top-Down de pit stops (Sistema de Utilidad).
     * 
     * Evalúa si el coste proyectado de desgaste futuro supera el coste de una
     * parada.
     * 
     * Algoritmo:
     * 1. Proyectar la penalización de desgaste para las próximas 5 vueltas con
     * neumáticos actuales
     * 2. Calcular la penalización equivalente con neumáticos nuevos
     * 3. Si la diferencia > umbral (ajustado por perfil) + ruido → PIT STOP
     * 4. Seleccionar compuesto óptimo según vueltas restantes
     * 
     * Variabilidad: Se añade ruido de ±3000ms para evitar que todos paren en la
     * misma vuelta.
     */
    private void evaluarPitStopIA(PilotoSimulacion ps, EstadoCarrera estado) {
        PerfilIA perfil = estado.getPerfilesIA().get(ps.getEscuderiaId());
        if (perfil == null)
            return;

        int vueltasRestantes = estado.getTotalVueltas() - estado.getVueltaActual();
        if (vueltasRestantes <= 1)
            return; // No tiene sentido parar si queda 1 vuelta

        // Proyectar penalización de desgaste actual para las próximas N vueltas (max 5)
        int ventanaProyeccion = Math.min(5, vueltasRestantes);
        long penalizacionActualProyectada = 0;
        double desgasteProyectado = ps.getDesgasteNeumatico();
        double tasaDesgasteMedia = (ps.getCompuestoActual().getDesgasteMinPorVuelta()
                + ps.getCompuestoActual().getDesgasteMaxPorVuelta()) / 2.0;

        for (int i = 0; i < ventanaProyeccion; i++) {
            desgasteProyectado = Math.min(100.0, desgasteProyectado + tasaDesgasteMedia);
            penalizacionActualProyectada += calcularPenalizacionNeumaticos(ps.getCompuestoActual(), desgasteProyectado);
        }

        // Proyectar con neumáticos nuevos (Medio como referencia)
        long penalizacionNuevosProyectada = 0;
        TipoNeumatico compuestoNuevo = seleccionarCompuestoIA(vueltasRestantes);
        double desgasteNuevo = 0;
        double tasaNueva = (compuestoNuevo.getDesgasteMinPorVuelta()
                + compuestoNuevo.getDesgasteMaxPorVuelta()) / 2.0;

        for (int i = 0; i < ventanaProyeccion; i++) {
            desgasteNuevo = Math.min(100.0, desgasteNuevo + tasaNueva);
            penalizacionNuevosProyectada += calcularPenalizacionNeumaticos(compuestoNuevo, desgasteNuevo);
        }

        // Diferencia = lo que se ahorra cambiando neumáticos
        long ahorro = penalizacionActualProyectada - penalizacionNuevosProyectada;

        // Umbral ajustado por perfil del equipo
        double umbral;
        if (perfil == PerfilIA.AGRESIVO) {
            umbral = COSTE_PIT_STOP_MS * 1.3; // Aguanta más, requiere más ahorro para justificar parada
        } else {
            umbral = COSTE_PIT_STOP_MS * 0.7; // Para antes, menos exigente
        }

        // Ruido para evitar "efecto tren" (todos parando en la misma vuelta)
        long ruido = (long) (random.nextDouble() * 6000 - 3000); // ±3000ms

        if (ahorro > umbral + ruido) {
            ejecutarPitStop(ps, compuestoNuevo);
        }
    }

    /**
     * Selecciona el compuesto óptimo para la IA según vueltas restantes.
     * 
     * ≤15 vueltas: Blando (rinde bien en stint corto)
     * ≤30 vueltas: Medio (equilibrio)
     * >30 vueltas: Duro (maximiza duración)
     */
    private TipoNeumatico seleccionarCompuestoIA(int vueltasRestantes) {
        if (vueltasRestantes <= 15)
            return TipoNeumatico.BLANDO;
        if (vueltasRestantes <= 30)
            return TipoNeumatico.MEDIO;
        return TipoNeumatico.DURO;
    }

    // =========================================================================
    // DNF (Did Not Finish)
    // =========================================================================

    /**
     * Evalúa si un piloto abandona por fallo mecánico.
     * 
     * probabilidadBase = 0.001 (0.1% por vuelta)
     * factor = 1 + (20 - durabilidad) × 0.15
     * durabilidad 20 → factor 1.0 → 0.1% por vuelta (~5.6% en 58 vueltas)
     * durabilidad 10 → factor 2.5 → 0.25% por vuelta (~13.4% en 58 vueltas)
     * durabilidad 1 → factor 3.85 → 0.385% por vuelta (~20% en 58 vueltas)
     */
    private void evaluarDNF(PilotoSimulacion ps) {
        double factor = 1.0 + (20 - ps.getDurabilidad()) * 0.15;
        double probabilidad = PROBABILIDAD_DNF_BASE * factor;

        if (random.nextDouble() < probabilidad) {
            ps.setDnf(true);
            ps.setTiempoTotalMs(0); // Tiempo 00:00:00
            log.info("DNF: {} (escudería: {})", ps.getNombre(), ps.getEscuderiaNombre());
        }
    }

    // =========================================================================
    // RANKING Y GAPS
    // =========================================================================

    /**
     * Recalcula el ranking ordenando por:
     * 1. DNFs al final
     * 2. Menor tiempo total primero
     * 
     * Luego calcula el gap con el coche inmediatamente delante.
     */
    private void recalcularRanking(EstadoCarrera estado) {
        List<PilotoSimulacion> pilotos = estado.getPilotos();

        // Separar activos y DNFs
        List<PilotoSimulacion> activos = pilotos.stream()
                .filter(p -> !p.isDnf())
                .sorted(Comparator.comparingLong(PilotoSimulacion::getTiempoTotalMs))
                .collect(Collectors.toList());

        List<PilotoSimulacion> dnfs = pilotos.stream()
                .filter(PilotoSimulacion::isDnf)
                .collect(Collectors.toList());

        // Asignar posiciones a activos
        for (int i = 0; i < activos.size(); i++) {
            activos.get(i).setPosicion(i + 1);
            if (i == 0) {
                activos.get(i).setGapMs(0);
            } else {
                activos.get(i).setGapMs(
                        activos.get(i).getTiempoTotalMs() - activos.get(i - 1).getTiempoTotalMs());
            }
        }

        // DNFs al final sin posición útil
        int posicionDnf = activos.size() + 1;
        for (PilotoSimulacion dnf : dnfs) {
            dnf.setPosicion(posicionDnf++);
            dnf.setGapMs(0);
        }
    }

    // =========================================================================
    // CONSULTA DE ESTADO (debug / reconexión)
    // =========================================================================

    /**
     * Devuelve el estado actual de una carrera sin procesar vuelta.
     */
    public CarreraVueltaResponseDTO obtenerEstado(UUID uuid) {
        EstadoCarrera estado = carrerasActivas.get(uuid);
        if (estado == null) {
            throw new RuntimeException("Carrera no encontrada: " + uuid);
        }
        return construirVueltaResponse(estado);
    }

    // =========================================================================
    // CONSTRUCTORES DE DTOs DE RESPUESTA
    // =========================================================================

    private CarreraStartResponseDTO construirStartResponse(EstadoCarrera estado) {
        CarreraStartResponseDTO response = new CarreraStartResponseDTO();
        response.setUuid(estado.getUuid());

        // Info del circuito
        CarreraStartResponseDTO.CircuitoInfoDTO circuitoDTO = new CarreraStartResponseDTO.CircuitoInfoDTO();
        circuitoDTO.setId(estado.getCircuitoId());
        circuitoDTO.setNombre(estado.getCircuitoNombre());
        circuitoDTO.setNumVueltas(estado.getTotalVueltas());
        response.setCircuito(circuitoDTO);

        // Parrilla
        List<CarreraStartResponseDTO.ParrillaEntryDTO> parrilla = new ArrayList<>();
        for (PilotoSimulacion ps : estado.getPilotos()) {
            CarreraStartResponseDTO.ParrillaEntryDTO entry = new CarreraStartResponseDTO.ParrillaEntryDTO();
            entry.setPosicion(ps.getPosicion());
            entry.setPilotoId(ps.getPilotoId());
            entry.setNombre(ps.getNombre());
            entry.setEscuderia(ps.getEscuderiaNombre());
            entry.setEscuderiaImagen(ps.getEscuderiaImagen());
            entry.setTiempoClasificacionMs(ps.getVueltaRapidaMs());
            entry.setCompuesto(ps.getCompuestoActual().name());
            entry.setEsJugador(ps.isEsJugador());
            parrilla.add(entry);
        }
        parrilla.sort(Comparator.comparingInt(CarreraStartResponseDTO.ParrillaEntryDTO::getPosicion));
        response.setParrilla(parrilla);

        return response;
    }

    private CarreraVueltaResponseDTO construirVueltaResponse(EstadoCarrera estado) {
        CarreraVueltaResponseDTO response = new CarreraVueltaResponseDTO();
        response.setVueltaActual(estado.getVueltaActual());
        response.setTotalVueltas(estado.getTotalVueltas());
        response.setFinalizada(estado.isFinalizada());

        List<CarreraVueltaResponseDTO.PilotoCarreraDTO> ranking = new ArrayList<>();
        for (PilotoSimulacion ps : estado.getPilotos()) {
            CarreraVueltaResponseDTO.PilotoCarreraDTO dto = new CarreraVueltaResponseDTO.PilotoCarreraDTO();
            dto.setPosicion(ps.getPosicion());
            dto.setPilotoId(ps.getPilotoId());
            dto.setNombre(ps.getNombre());
            dto.setEscuderia(ps.getEscuderiaNombre());
            dto.setEscuderiaImagen(ps.getEscuderiaImagen());
            dto.setTiempoTotalMs(ps.getTiempoTotalMs());
            dto.setTiempoVueltaMs(ps.getTiempoUltimaVueltaMs());
            dto.setVueltaRapidaMs(ps.getVueltaRapidaMs());
            dto.setGapMs(ps.getGapMs());
            dto.setCompuesto(ps.getCompuestoActual().name());
            dto.setDesgaste(Math.round(ps.getDesgasteNeumatico() * 10.0) / 10.0);
            dto.setNumParadas(ps.getNumParadas());
            dto.setEnPitStop(ps.isEnPitStop());
            dto.setDnf(ps.isDnf());
            dto.setEsJugador(ps.isEsJugador());
            ranking.add(dto);
        }
        ranking.sort(Comparator.comparingInt(CarreraVueltaResponseDTO.PilotoCarreraDTO::getPosicion));
        response.setRanking(ranking);

        return response;
    }
}
