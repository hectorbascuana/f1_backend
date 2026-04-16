package com.f1manager.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "partida")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Partida {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @ManyToOne
    @JoinColumn(name = "id_escuderia_seleccionada")
    private Escuderia escuderiaSeleccionada;
    
    @ManyToOne
    @JoinColumn(name = "id_proximo_circuito")
    private Circuito proximoCircuito;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
