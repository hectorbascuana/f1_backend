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
import java.util.Random;

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

    public void mejorarAerodinamica() {
        int nivel = this.getAerodinamica();
        int aumento;
        if(nivel < 99){
            int nivelTunelViento = this.getTunelViento();
            int ale = new Random().nextInt(100);
            switch (nivelTunelViento) {
                case 0:
                    if(ale < 50){
                        aumento = 0;
                    }else if(ale < 85){
                        aumento = 1;
                    }else if(ale < 95){
                        aumento = 2;
                    }else{
                        aumento = 3;
                    }
                    break;
                case 1:
                    if(ale < 40){
                        aumento = 0;
                    }else if(ale < 70){
                        aumento = 1;
                    }else if(ale < 90){
                        aumento = 2;
                    }else{
                        aumento = 3;
                    }
                    break;
                case 2:
                    if(ale < 20){
                        aumento = 0;
                    }else if(ale < 60){
                        aumento = 1;
                    }else if(ale < 85){
                        aumento = 2;
                    }else{
                        aumento = 3;
                    }
                    break;
                case 3:
                    if(ale < 10){
                        aumento = 0;
                    }else if(ale < 55){
                        aumento = 1;
                    }else if(ale < 80){
                        aumento = 2;
                    }else{
                        aumento = 3;
                    }
                    break;
                case 4:
                    if(ale < 45){
                        aumento = 1;
                    }else if(ale < 70){
                        aumento = 2;
                    }else{ 
                        aumento = 3;
                    }
                    break;
                case 5:
                    if(ale < 30){
                        aumento = 1;
                    }else if(ale < 60){
                        aumento = 2;
                    }else{
                        aumento = 3;
                    }
                    break;
                default:
                    aumento = 0;
                    break;
            }
            this.aerodinamica += aumento;
        }
        
            
    }

    public void mejorarMotor() {
        int nivel = this.getMotor();
        int aumento;
        if(nivel < 100){
            int nivelBancoPruebas = this.getBancoPruebas();
            int ale = new Random().nextInt(100);
            switch (nivelBancoPruebas) {
                case 0:
                    if(ale < 50){
                        aumento = 0;
                    }else if(ale < 85){
                        aumento = 1;
                    }else if(ale < 95){
                        aumento = 2;
                    }else{
                        aumento = 3;
                    }
                    break;
                case 1:
                    if(ale < 40){
                        aumento = 0;
                    }else if(ale < 70){
                        aumento = 1;
                    }else if(ale < 90){
                        aumento = 2;
                    }else{
                        aumento = 3;
                    }
                    break;
                case 2:
                    if(ale < 20){
                        aumento = 0;
                    }else if(ale < 60){
                        aumento = 1;
                    }else if(ale < 85){
                        aumento = 2;
                    }else{
                        aumento = 3;
                    }
                    break;
                case 3:
                    if(ale < 10){
                        aumento = 0;
                    }else if(ale < 55){
                        aumento = 1;
                    }else if(ale < 80){
                        aumento = 2;
                    }else{
                        aumento = 3;
                    }
                    break;
                case 4:
                    if(ale < 45){
                        aumento = 1;
                    }else if(ale < 70){
                        aumento = 2;
                    }else{ 
                        aumento = 3;
                    }
                    break;
                case 5:
                    if(ale < 30){
                        aumento = 1;
                    }else if(ale < 60){
                        aumento = 2;
                    }else{
                        aumento = 3;
                    }
                    break;
                default:
                    aumento = 0;
                    break;
            }
            this.motor += aumento;
        }
    }

    public void mejorarDurabilidad() {
        this.durabilidad = Math.min(this.durabilidad + 1, 20);
    }

    public void mejorarTunelViento() {
        this.tunelViento = Math.min(this.tunelViento + 1, 5);
    }

    public void mejorarBancoPruebas() {
        this.bancoPruebas = Math.min(this.bancoPruebas + 1, 5);
    }

    public void mejorarEscuelaPilotos() {
        this.escuelaPilotos = Math.min(this.escuelaPilotos + 1, 5);
    }
}
