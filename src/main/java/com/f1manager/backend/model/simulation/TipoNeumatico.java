package com.f1manager.backend.model.simulation;

/**
 * Enum que define los tres compuestos de neumáticos disponibles.
 * 
 * penalizacionBaseMs: tiempo extra por vuelta respecto al Blando (ms)
 * desgasteMinPorVuelta: tasa mínima de desgaste por vuelta (%)
 * desgasteMaxPorVuelta: tasa máxima de desgaste por vuelta (%)
 */
public enum TipoNeumatico {
    BLANDO(0, 8.0, 10.0),
    MEDIO(600, 4.0, 6.0),
    DURO(1200, 1.0, 3.0);

    private final long penalizacionBaseMs;
    private final double desgasteMinPorVuelta;
    private final double desgasteMaxPorVuelta;

    TipoNeumatico(long penalizacionBaseMs, double desgasteMin, double desgasteMax) {
        this.penalizacionBaseMs = penalizacionBaseMs;
        this.desgasteMinPorVuelta = desgasteMin;
        this.desgasteMaxPorVuelta = desgasteMax;
    }

    public long getPenalizacionBaseMs() {
        return penalizacionBaseMs;
    }

    public double getDesgasteMinPorVuelta() {
        return desgasteMinPorVuelta;
    }

    public double getDesgasteMaxPorVuelta() {
        return desgasteMaxPorVuelta;
    }
}
