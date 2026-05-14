package com.f1manager.backend.dto;

public enum EscuderiaMejoraTipo {
    AERODINAMICA,
    MOTOR,
    DURABILIDAD,
    TUNEL_VIENTO,
    BANCO_PRUEBAS,
    ESCUELA_PILOTOS;

    public static EscuderiaMejoraTipo from(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de mejora es requerido");
        }
        try {
            return EscuderiaMejoraTipo.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Manejar casos con guiones bajos o variaciones si es necesario
            String normalized = tipo.toUpperCase().replace("-", "_").replace(" ", "_");
            return EscuderiaMejoraTipo.valueOf(normalized);
        }
    }
}
