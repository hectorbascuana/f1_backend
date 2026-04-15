package com.f1manager.backend.dto;

public enum EscuderiaMejoraTipo {
    BASICA,
    DURABILIDAD,
    INSTALACIONES;

    public static EscuderiaMejoraTipo from(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de mejora es requerido");
        }
        return switch (tipo.toLowerCase()) {
            case "basica", "basicas" -> BASICA;
            case "durabilidad" -> DURABILIDAD;
            case "instalaciones" -> INSTALACIONES;
            default -> throw new IllegalArgumentException("Tipo de mejora inválido: " + tipo);
        };
    }
}
