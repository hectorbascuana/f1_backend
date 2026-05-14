package com.f1manager.backend.model.simulation;

/**
 * Perfil de estrategia asignado aleatoriamente a cada escudería IA al inicio de la carrera.
 * Determina cómo valoran el coste de desgaste vs el coste de una parada.
 * 
 * AGRESIVO: Aguanta más con neumáticos desgastados, para más tarde. Sale con Blandos.
 * CONSERVADOR: Para antes para preservar rendimiento. Sale con Medio o Duro.
 */
public enum PerfilIA {
    AGRESIVO,
    CONSERVADOR
}
