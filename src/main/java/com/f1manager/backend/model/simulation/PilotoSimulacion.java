package com.f1manager.backend.model.simulation;

import lombok.Data;

/**
 * POJO volátil que representa a un piloto durante la simulación de carrera.
 * Vive solo en RAM — NO es una @Entity JPA.
 * 
 * Contiene tanto los atributos estáticos (copiados de BD al iniciar)
 * como el estado mutable que cambia vuelta a vuelta.
 */
@Data
public class PilotoSimulacion {

    // === Identidad (copiada de BD) ===
    private Integer pilotoId;
    private String nombre;
    private Integer escuderiaId;
    private String escuderiaNombre;
    private String escuderiaImagen;

    // === Estadísticas del piloto [1-99] ===
    private int valoracion;
    private int curvaRapida;    // Proxy de habilidad en rectas/motor
    private int curvaLenta;     // Proxy de habilidad aerodinámica/curvas lentas
    private int salidas;        // Habilidad en salida (vuelta 1)
    private int consistencia;   // Reduce el ruido aleatorio

    // === Estadísticas del coche [1-99] ===
    private int aerodinamica;
    private int motor;
    private int durabilidad;    // [1-20] Afecta probabilidad de DNF

    // === Estado de neumáticos (mutable) ===
    private TipoNeumatico compuestoActual;
    private double desgasteNeumatico;   // 0.0 – 100.0 (%)

    // === Estado de carrera (mutable) ===
    private long tiempoTotalMs;         // Tiempo acumulado en ms
    private long tiempoUltimaVueltaMs;  // Tiempo de la última vuelta procesada
    private long vueltaRapidaMs;        // Mejor vuelta personal en ms
    private int posicion;               // Posición actual en el ranking
    private long gapMs;                 // Gap con el coche inmediatamente delante
    private int vueltas;                // Vueltas completadas
    private boolean dnf;                // ¿Abandonó por fallo mecánico?
    private boolean enPitStop;          // ¿Hizo pit stop en esta vuelta?
    private int numParadas;             // Nº total de pit stops realizados
    private boolean esJugador;          // ¿Pertenece al equipo del usuario?
}
