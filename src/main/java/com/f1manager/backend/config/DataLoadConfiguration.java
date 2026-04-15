package com.f1manager.backend.config;

import com.f1manager.backend.entity.*;
import com.f1manager.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalTime;

@Configuration
public class DataLoadConfiguration {

    @Bean
    public CommandLineRunner loadData(
            EstadisticaRepository estadisticaRepo,
            EscuderiaRepository escuderiaRepo,
            PilotoRepository pilotoRepo,
            CircuitoRepository circuitoRepo) {
        
        return args -> {
            // Cargar solo si la BD está vacía
            if (escuderiaRepo.count() == 0) {
                cargarDatos(estadisticaRepo, escuderiaRepo, pilotoRepo, circuitoRepo);
            }
        };
    }

    private void cargarDatos(
            EstadisticaRepository estadisticaRepo,
            EscuderiaRepository escuderiaRepo,
            PilotoRepository pilotoRepo,
            CircuitoRepository circuitoRepo) {
        
        // 1. Estadísticas
        for (int i = 0; i < 22; i++) {
            Estadistica est = new Estadistica();
            est.setValoracion(50);
            est.setCurvaRapida(50);
            est.setCurvaLenta(50);
            est.setSalidas(50);
            est.setConsistencia(50);
            estadisticaRepo.save(est);
        }

        // 2. Escuderías
        String[][] escuderiasData = {
            {"Oracle Red Bull Racing", "assets/teams/redbull.png", "145.00"},
            {"Mercedes-AMG PETRONAS", "assets/teams/mercedes.png", "140.00"},
            {"Scuderia Ferrari HP", "assets/teams/ferrari.png", "142.00"},
            {"McLaren Formula 1", "assets/teams/mclaren.png", "135.00"},
            {"Aston Martin Aramco", "assets/teams/aston.png", "125.00"},
            {"Alpine F1 Team", "assets/teams/alpine.png", "110.00"},
            {"Williams Racing", "assets/teams/williams.png", "95.00"},
            {"Racing Bulls (VCARB)", "assets/teams/vcarb.png", "85.00"},
            {"Audi F1 Team", "assets/teams/audi.png", "130.00"},
            {"MoneyGram Haas F1 Team", "assets/teams/haas.png", "80.00"},
            {"Cadillac Andretti F1", "assets/teams/cadillac.png", "120.00"}
        };

        for (String[] data : escuderiasData) {
            Escuderia esc = new Escuderia();
            esc.setNombre(data[0]);
            esc.setImagen(data[1]);
            esc.setPresupuesto(new java.math.BigDecimal(data[2]));
            esc.setAerodinamica(1);
            esc.setMotor(1);
            esc.setDurabilidad(1);
            esc.setTunelViento(1);
            esc.setBancoPruebas(1);
            esc.setEscuelaPilotos(1);
            escuderiaRepo.save(esc);
        }

        // 3. Pilotos
        String[][] pilotosData = {
            {"Max Verstappen", "Países Bajos", "assets/drivers/verstappen.png", "1", "28", "55.00"},
            {"Isack Hadjar", "Francia", "assets/drivers/hadjar.png", "1", "21", "12.00"},
            {"George Russell", "Reino Unido", "assets/drivers/russell.png", "2", "28", "38.00"},
            {"Andrea Kimi Antonelli", "Italia", "assets/drivers/antonelli.png", "2", "19", "20.00"},
            {"Lewis Hamilton", "Reino Unido", "assets/drivers/hamilton.png", "3", "41", "50.00"},
            {"Charles Leclerc", "Mónaco", "assets/drivers/leclerc.png", "3", "28", "48.00"},
            {"Lando Norris", "Reino Unido", "assets/drivers/norris.png", "4", "26", "42.00"},
            {"Oscar Piastri", "Australia", "assets/drivers/piastri.png", "4", "24", "35.00"},
            {"Fernando Alonso", "España", "assets/drivers/alonso.png", "5", "44", "35.00"},
            {"Lance Stroll", "Canadá", "assets/drivers/stroll.png", "5", "27", "15.00"},
            {"Pierre Gasly", "Francia", "assets/drivers/gasly.png", "6", "30", "22.00"},
            {"Franco Colapinto", "Argentina", "assets/drivers/colapinto.png", "6", "22", "15.00"},
            {"Carlos Sainz Jr.", "España", "assets/drivers/sainz.png", "7", "31", "34.00"},
            {"Alex Albon", "Tailandia", "assets/drivers/albon.png", "7", "30", "20.00"},
            {"Liam Lawson", "Nueva Zelanda", "assets/drivers/lawson.png", "8", "24", "14.00"},
            {"Arvid Lindblad", "Reino Unido", "assets/drivers/lindblad.png", "8", "18", "10.00"},
            {"Nico Hülkenberg", "Alemania", "assets/drivers/hulkenberg.png", "9", "38", "18.00"},
            {"Gabriel Bortoleto", "Brasil", "assets/drivers/bortoleto.png", "9", "21", "15.00"},
            {"Esteban Ocon", "Francia", "assets/drivers/ocon.png", "10", "29", "18.00"},
            {"Oliver Bearman", "Reino Unido", "assets/drivers/bearman.png", "10", "20", "12.00"},
            {"Sergio Pérez", "México", "assets/drivers/perez.png", "11", "36", "25.00"},
            {"Valtteri Bottas", "Finlandia", "assets/drivers/bottas.png", "11", "36", "20.00"}
        };

        for (String[] data : pilotosData) {
            Piloto piloto = new Piloto();
            piloto.setNombre(data[0]);
            piloto.setPais(data[1]);
            piloto.setImagen(data[2]);
            Escuderia esc = escuderiaRepo.findById(Integer.parseInt(data[3])).orElse(null);
            if (esc != null) {
                piloto.setEscuderia(esc);
            }
            piloto.setEdad(Integer.parseInt(data[4]));
            piloto.setValor(new java.math.BigDecimal(data[5]));
            piloto.setPuntos(0);
            pilotoRepo.save(piloto);
        }

        // 4. Circuitos
        String[][] circuitosData = {
            {"Albert Park Circuit", "Australia", "00:01:19.800", "58", "7", "6"},
            {"Shanghai Audi Circuit", "China", "00:01:36.500", "56", "7", "8"},
            {"Suzuka Circuit", "Japón", "00:01:31.200", "53", "9", "7"},
            {"Bahrain International", "Bahréin", "00:01:32.100", "57", "6", "8"},
            {"Jeddah Corniche", "Arabia Saudí", "00:01:30.500", "50", "4", "10"},
            {"Miami International", "EE. UU.", "00:01:29.800", "57", "5", "9"},
            {"Circuit de Monaco", "Mónaco", "00:01:14.500", "78", "10", "2"},
            {"Circuit de Barcelona", "España", "00:01:16.300", "66", "9", "6"},
            {"Silverstone Circuit", "Reino Unido", "00:01:29.100", "52", "10", "7"},
            {"Spa-Francorchamps", "Bélgica", "00:01:46.200", "44", "5", "10"},
            {"Autodromo de Madrid", "España", "00:01:32.400", "54", "8", "7"},
            {"Las Vegas Strip", "EE. UU.", "00:01:33.200", "50", "3", "10"},
            {"Interlagos", "Brasil", "00:01:10.500", "71", "7", "7"},
            {"Yas Marina", "Abu Dhabi", "00:01:26.100", "58", "6", "6"}
        };

        for (String[] data : circuitosData) {
            Circuito circ = new Circuito();
            circ.setNombre(data[0]);
            circ.setPais(data[1]);
            circ.setTiempoBase(LocalTime.parse(data[2]));
            circ.setNumVueltas(Integer.parseInt(data[3]));
            circ.setAerodinamicaReq(Integer.parseInt(data[4]));
            circ.setMotorReq(Integer.parseInt(data[5]));
            circuitoRepo.save(circ);
        }
    }
}
