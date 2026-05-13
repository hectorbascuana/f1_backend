package com.f1manager.backend.service;

import com.f1manager.backend.entity.Escuderia;
import com.f1manager.backend.entity.Estadistica;
import com.f1manager.backend.entity.Partida;
import com.f1manager.backend.entity.Piloto;
import com.f1manager.backend.repository.PilotoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ProgresoService {

    private final PilotoRepository pilotoRepository;
    private final Random random = new Random();

    // Multiplicador base para el precio (Rating^2 * MULTIPLIER)
    private static final double PRICE_MULTIPLIER = 35000.0;
    
    // Configuración de declive
    private static final int BASE_DECLINE_AGE = 32;

    // Almacén temporal de los últimos cambios por partida (para el endpoint de reporte)
    private final Map<Integer, List<EvolucionReport>> reportesUltimaCarrera = new ConcurrentHashMap<>();

    public ProgresoService(PilotoRepository pilotoRepository) {
        this.pilotoRepository = pilotoRepository;
    }

    @Transactional
    public void procesarEvolucionPilotos(Partida partida) {
        List<Piloto> pilotos = pilotoRepository.findByPartida(partida);
        List<EvolucionReport> cambios = new ArrayList<>();
        log.info("[PROGRESO] Procesando evolución para {} pilotos en partida {}", pilotos.size(), partida.getId());

        for (Piloto piloto : pilotos) {
            EvolucionReport report = actualizarEstadisticas(piloto);
            actualizarPrecioMercado(piloto);
            pilotoRepository.save(piloto);
            
            if (report != null) {
                cambios.add(report);
            }
        }
        
        reportesUltimaCarrera.put(partida.getId(), cambios);
    }

    private EvolucionReport actualizarEstadisticas(Piloto piloto) {
        Estadistica stats = piloto.getEstadistica();
        if (stats == null) return null;

        String estadisticaCambiada = null;
        int cambio = 0;

        Escuderia esc = piloto.getEscuderia();
        int nivelAcademia = (esc != null) ? esc.getEscuelaPilotos() : 0;
        int edad = piloto.getEdad();
        int umbralDeclive = BASE_DECLINE_AGE + nivelAcademia;

        if (edad < umbralDeclive) {
            estadisticaCambiada = simularCrecimiento(stats, edad, nivelAcademia);
            if (estadisticaCambiada != null) cambio = 1;
        } else {
            estadisticaCambiada = simularDeclive(stats, edad, umbralDeclive, nivelAcademia);
            if (estadisticaCambiada != null) cambio = -1;
        }

        if (estadisticaCambiada != null) {
            recalcularMedia(stats);
            return new EvolucionReport(
                piloto.getId(),
                piloto.getNombre(),
                cambio > 0 ? "MEJORA" : "DECLIVE",
                estadisticaCambiada,
                cambio,
                stats.getValoracion()
            );
        }
        return null;
    }

    private String simularCrecimiento(Estadistica stats, int edad, int nivelAcademia) {
        double baseProb = 5.0 + (nivelAcademia * 3.0); 
        double ageFactor = Math.max(0.5, 2.5 - (edad / 15.0));
        double ratingFactor = Math.max(0.1, (100.0 - stats.getValoracion()) / 50.0);
        
        double chance = baseProb * ageFactor * ratingFactor;

        if (random.nextDouble() * 100 < chance) {
            return incrementarEstadisticaAleatoria(stats);
        }
        return null;
    }

    private String simularDeclive(Estadistica stats, int edad, int umbral, int nivelAcademia) {
        int añosPasados = edad - umbral;
        double baseDeclineProb = (añosPasados * añosPasados) * 0.8;
        double academyFactor = Math.max(0.3, 1.0 - (nivelAcademia * 0.12));
        
        double chance = baseDeclineProb * academyFactor;

        if (random.nextDouble() * 100 < chance) {
            return decrementarEstadisticaAleatoria(stats);
        }
        return null;
    }

    private String incrementarEstadisticaAleatoria(Estadistica stats) {
        int r = random.nextInt(4);
        switch (r) {
            case 0 -> { stats.setCurvaRapida(Math.min(99, stats.getCurvaRapida() + 1)); return "Curva Rápida"; }
            case 1 -> { stats.setCurvaLenta(Math.min(99, stats.getCurvaLenta() + 1)); return "Curva Lenta"; }
            case 2 -> { stats.setSalidas(Math.min(99, stats.getSalidas() + 1)); return "Salidas"; }
            case 3 -> { stats.setConsistencia(Math.min(99, stats.getConsistencia() + 1)); return "Consistencia"; }
        }
        return null;
    }

    private String decrementarEstadisticaAleatoria(Estadistica stats) {
        int r = random.nextInt(4);
        switch (r) {
            case 0 -> { stats.setCurvaRapida(Math.max(10, stats.getCurvaRapida() - 1)); return "Curva Rápida"; }
            case 1 -> { stats.setCurvaLenta(Math.max(10, stats.getCurvaLenta() - 1)); return "Curva Lenta"; }
            case 2 -> { stats.setSalidas(Math.max(10, stats.getSalidas() - 1)); return "Salidas"; }
            case 3 -> { stats.setConsistencia(Math.max(10, stats.getConsistencia() - 1)); return "Consistencia"; }
        }
        return null;
    }

    private void recalcularMedia(Estadistica stats) {
        int media = (stats.getCurvaRapida() + stats.getCurvaLenta() + stats.getSalidas() + stats.getConsistencia()) / 4;
        stats.setValoracion(media);
    }

    private void actualizarPrecioMercado(Piloto piloto) {
        if (piloto.getEstadistica() == null) return;
        
        int valoracion = piloto.getEstadistica().getValoracion();
        int edad = piloto.getEdad();

        double precioBase = (valoracion * valoracion) * PRICE_MULTIPLIER;

        double factorEdad = 1.0;
        if (edad > 30) {
            factorEdad = Math.max(0.1, 1.0 - (edad - 30) * 0.05);
        }

        long precioFinal = Math.round(precioBase * factorEdad);
        piloto.setValor(BigDecimal.valueOf(precioFinal));
    }

    public List<EvolucionReport> obtenerReporte(Integer partidaId) {
        return reportesUltimaCarrera.getOrDefault(partidaId, new ArrayList<>());
    }

    public static record EvolucionReport(
        Integer pilotoId,
        String nombre,
        String tipo,
        String estadistica,
        int cambio,
        int nuevaValoracion
    ){} 
}
