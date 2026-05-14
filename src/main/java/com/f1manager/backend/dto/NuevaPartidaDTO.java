package com.f1manager.backend.dto;

public class NuevaPartidaDTO {
    private String nombre;
    private Integer idEscuderiaJson;

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getIdEscuderiaJson() {
        return idEscuderiaJson;
    }

    public void setIdEscuderiaJson(Integer idEscuderiaJson) {
        this.idEscuderiaJson = idEscuderiaJson;
    }
}
