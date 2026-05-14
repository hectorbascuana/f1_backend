package com.f1manager.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "circuito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Circuito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "pais", length = 50)
    private String pais;
    
    @Column(name = "tiempo_base")
    private LocalTime tiempoBase;
    
    @Column(name = "num_vueltas", nullable = false)
    private Integer numVueltas;
    
    @Min(1)
    @Max(10)
    @Column(name = "aerodinamica_req")
    private Integer aerodinamicaReq;
    
    @Min(1)
    @Max(10)
    @Column(name = "motor_req")
    private Integer motorReq;
    
    @OneToMany(mappedBy = "circuito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PilotoCircuito> pilotoCircuitos;
}
