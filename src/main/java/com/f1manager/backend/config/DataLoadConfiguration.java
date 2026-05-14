package com.f1manager.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoadConfiguration {

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            // El backend ahora arranca limpio para soportar múltiples partidas.
            // Los datos iniciales se cargan al ejecutar el endpoint POST /api/partida/nueva
        };
    }
}
