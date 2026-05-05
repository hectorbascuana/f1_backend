package com.f1manager.backend.entity;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "piloto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Piloto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "pais", length = 50)
    private String pais;

    @Column(name = "imagen", length = 255)
    private String imagen;

    @Column(name = "edad")
    private Integer edad;

    @Column(name = "puntos")
    private Integer puntos = 0;

    @Column(name = "valor", precision = 15, scale = 2)
    private BigDecimal valor;

    @ManyToOne
    @JoinColumn(name = "escuderia_id", referencedColumnName = "id")
    @JsonIgnoreProperties("pilotos")
    private Escuderia escuderia;

    @OneToOne
    @JoinColumn(name = "estadistica_id", referencedColumnName = "id")
    private Estadistica estadistica;

    @Column(name = "ha_corrido", nullable = false)
    private Boolean haCorrido = false;

    @OneToMany(mappedBy = "piloto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PilotoCircuito> pilotoCircuitos;
}
