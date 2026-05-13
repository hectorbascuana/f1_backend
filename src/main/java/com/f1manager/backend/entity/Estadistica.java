package com.f1manager.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estadistica")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estadistica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @Min(1)
    @Max(99)
    @Column(name = "valoracion")
    private Integer valoracion;

    @Column(name = "valoracion_inicial")
    private Integer valoracionInicial;

    @Min(1)
    @Max(99)
    @Column(name = "curva_rapida")
    private Integer curvaRapida;

    @Min(1)
    @Max(99)
    @Column(name = "curva_lenta")
    private Integer curvaLenta;

    @Min(1)
    @Max(99)
    @Column(name = "salidas")
    private Integer salidas;

    @Min(1)
    @Max(99)
    @Column(name = "consistencia")
    private Integer consistencia;
}
