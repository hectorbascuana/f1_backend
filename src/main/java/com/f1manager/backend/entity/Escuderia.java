package com.f1manager.backend.entity;

import com.f1manager.backend.service.EscuderiaService;
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

    @Column(name = "puntos")
    private Integer puntos = 0;

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

    @ManyToOne
    @JoinColumn(name = "id_piloto_1")
    private Piloto piloto1;

    @ManyToOne
    @JoinColumn(name = "id_piloto_2")
    private Piloto piloto2;

    @OneToMany(mappedBy = "escuderia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("escuderia")
    private List<Piloto> pilotos;

    public Integer mejorarAerodinamica() {
        int nivel = this.getAerodinamica();
        float coste = EscuderiaService.costeMejoraBasica(nivel);
        BigDecimal presupuesto = this.getPresupuesto();
        restaPresupuesto(coste, presupuesto);
        
        int aumento = 0;
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
            this.aerodinamica = Math.min(nivel + aumento, 99);
        }
        return aumento;
    }

    public Integer mejorarMotor() {
        int nivel = this.getMotor();
        BigDecimal presupuesto = this.getPresupuesto();
        float coste = EscuderiaService.costeMejoraBasica(nivel);
        restaPresupuesto(coste, presupuesto);
        
        int aumento = 0;
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
            this.motor = Math.min(nivel + aumento, 99);
        }
        return aumento;
    }

    public Integer mejorarDurabilidad() {
        int nivel = this.getDurabilidad();
        BigDecimal presupuesto = this.getPresupuesto();
        float coste = EscuderiaService.costeMejoraDurabilidad(nivel);
        restaPresupuesto(coste, presupuesto);
        
        this.durabilidad = Math.min(nivel + 1, 20);
        return 1;
    }

    public Integer mejorarTunelViento() {
        int nivel = this.getTunelViento();
        BigDecimal presupuesto = this.getPresupuesto();
        float coste = EscuderiaService.costeMejoraInstalacion(nivel);
        restaPresupuesto(coste, presupuesto);
        
        this.tunelViento = Math.min(nivel + 1, 5);
        return 1;
    }

    public Integer mejorarBancoPruebas() {
        int nivel = this.getBancoPruebas();
        BigDecimal presupuesto = this.getPresupuesto();
        float coste = EscuderiaService.costeMejoraInstalacion(nivel);
        restaPresupuesto(coste, presupuesto);
        
        this.bancoPruebas = Math.min(nivel + 1, 5);
        return 1;
    }

    public Integer mejorarEscuelaPilotos() {
        int nivel = this.getEscuelaPilotos();
        BigDecimal presupuesto = this.getPresupuesto();
        float coste = EscuderiaService.costeMejoraEscuela(nivel);
        restaPresupuesto(coste, presupuesto);
        
        this.escuelaPilotos = Math.min(nivel + 1, 5);
        return 1;
    }

    public void restaPresupuesto(float coste, BigDecimal presupuesto) {
        if(presupuesto.compareTo(BigDecimal.valueOf(coste)) < 0){
            throw new IllegalArgumentException("Presupuesto insuficiente");
        }
        this.presupuesto = presupuesto.subtract(BigDecimal.valueOf(coste));
    }
}
