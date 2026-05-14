package com.f1manager.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "traspaso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Traspaso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @ManyToOne
    @JoinColumn(name = "piloto_id", nullable = false)
    private Piloto piloto;

    @ManyToOne
    @JoinColumn(name = "escuderia_origen_id")
    private Escuderia escuderiaOrigen;

    @ManyToOne
    @JoinColumn(name = "escuderia_destino_id")
    private Escuderia escuderiaDestino;

    @Column(name = "precio", precision = 15, scale = 2)
    private BigDecimal precio;

    @Column(name = "temporada")
    private Integer temporada;

    @Column(name = "aceptada")
    private Boolean aceptada = false;

    @Column(name = "en_curso")
    private Boolean enCurso = true;
}
