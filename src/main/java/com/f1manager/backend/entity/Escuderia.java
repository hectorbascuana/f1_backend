package com.f1manager.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "escuderia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Escuderia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "imagen", length = 255)
    private String imagen;

    @Column(name = "presupuesto", precision = 15, scale = 2)
    private BigDecimal presupuesto;

    @Min(1)
    @Max(100)
    @Column(name = "aerodinamica")
    private Integer aerodinamica;

    @Min(1)
    @Max(100)
    @Column(name = "motor")
    private Integer motor;

    @Min(1)
    @Max(20)
    @Column(name = "durabilidad")
    private Integer durabilidad;

    @Min(1)
    @Max(5)
    @Column(name = "tunel_viento")
    private Integer tunelViento;

    @Min(1)
    @Max(5)
    @Column(name = "banco_pruebas")
    private Integer bancoPruebas;

    @Min(1)
    @Max(5)
    @Column(name = "escuela_pilotos")
    private Integer escuelaPilotos;

    @OneToMany(mappedBy = "escuderia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("escuderia")
    private List<Piloto> pilotos;
}
