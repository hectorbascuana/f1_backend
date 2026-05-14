package com.f1manager.backend.entity;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "piloto_circuito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PilotoCircuito {

    @EmbeddedId
    private PilotoCircuitoId id;

    @Column(name = "posicion")
    private Integer posicion;

    @Column(name = "tiempo_total")
    private LocalTime tiempoTotal;

    @Column(name = "vuelta_rapida")
    private LocalTime vueltaRapida;

    @ManyToOne
    @MapsId("partidaId")
    @JoinColumn(name = "partida_id")
    private Partida partida;

    @ManyToOne
    @MapsId("circuitoId")
    @JoinColumn(name = "circuito_id")
    @JsonIgnoreProperties("pilotoCircuitos")
    private Circuito circuito;

    @ManyToOne
    @MapsId("pilotoId")
    @JoinColumn(name = "piloto_id")
    @JsonIgnoreProperties("pilotoCircuitos")
    private Piloto piloto;
}
