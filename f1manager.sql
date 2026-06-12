-- Active: 1775658415552@@127.0.0.1@3306@f1_manager
SET NAMES 'utf8mb4';

DROP DATABASE IF EXISTS f1_manager;

CREATE DATABASE IF NOT EXISTS f1_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE f1_manager;

-- 1. Tabla de Circuito (Se crea primero porque no tiene dependencias)
CREATE TABLE circuito (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    pais VARCHAR(50),
    tiempo_base TIME(3), -- Formato HH:MM:SS.mmm (ej: 00:01:15.964)
    num_vueltas INT NOT NULL,
    aerodinamica_req INT CHECK (
        aerodinamica_req BETWEEN 1 AND 10
    ),
    motor_req INT CHECK (motor_req BETWEEN 1 AND 10)
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Tabla de Partida (Representa una partida guardada)
CREATE TABLE partida (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_escuderia_seleccionada INT NULL, -- Se asignará cuando el jugador elija
    id_proximo_circuito INT DEFAULT 1,  -- Primera vez que me ha creado una partida con proximoCircuito = null. Si no encuenttra el circuto 1 se quedará el valor por defecto
    anio INT DEFAULT 2026,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_proximo_circuito) REFERENCES circuito (id) ON DELETE SET NULL
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. Tabla de Estadísticas
CREATE TABLE estadistica (
    id INT AUTO_INCREMENT PRIMARY KEY,
    partida_id INT NOT NULL,
    valoracion INT CHECK (valoracion BETWEEN 1 AND 99),
    valoracion_inicial INT,
    curva_rapida INT CHECK (curva_rapida BETWEEN 1 AND 99),
    curva_lenta INT CHECK (curva_lenta BETWEEN 1 AND 99),
    salidas INT CHECK (salidas BETWEEN 1 AND 99),
    consistencia INT CHECK (consistencia BETWEEN 1 AND 99),
    FOREIGN KEY (partida_id) REFERENCES partida (id) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. Tabla de Escudería
CREATE TABLE escuderia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    partida_id INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    imagen VARCHAR(255),
    presupuesto DECIMAL(15, 2) DEFAULT 0.00,
    puntos INT DEFAULT 0,
    id_piloto_1 INT NULL,
    id_piloto_2 INT NULL,
    aerodinamica INT DEFAULT 1 CHECK (aerodinamica BETWEEN 1 AND 99),
    motor INT DEFAULT 1 CHECK (motor BETWEEN 1 AND 99),
    durabilidad INT DEFAULT 1 CHECK (durabilidad BETWEEN 1 AND 20),
    tunel_viento INT DEFAULT 1 CHECK (tunel_viento BETWEEN 1 AND 5),
    banco_pruebas INT DEFAULT 1 CHECK (banco_pruebas BETWEEN 1 AND 5),
    escuela_pilotos INT DEFAULT 1 CHECK (escuela_pilotos BETWEEN 1 AND 5),
    FOREIGN KEY (partida_id) REFERENCES partida (id) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. Tabla de Piloto

--Añadido campo boleano de is rokie. Por defecto será false para que no sea necesario modificar el archivo initial_data.json
CREATE TABLE piloto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    partida_id INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    pais VARCHAR(50),
    imagen VARCHAR(255),
    escuderia_id INT,
    edad INT,
    puntos INT DEFAULT 0,
    valor DECIMAL(15, 2),
    estadistica_id INT,
    ha_corrido BOOLEAN DEFAULT FALSE,
    is_rokie BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (partida_id) REFERENCES partida (id) ON DELETE CASCADE,
    FOREIGN KEY (escuderia_id) REFERENCES escuderia (id) ON DELETE SET NULL,
    FOREIGN KEY (estadistica_id) REFERENCES estadistica (id) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 6. Tabla Piloto_Circuito (Clasificación de carrera)
CREATE TABLE piloto_circuito (
    partida_id INT NOT NULL,
    circuito_id INT NOT NULL,
    piloto_id INT NOT NULL,
    temporada INT,
    posicion INT,
    tiempo_total TIME(3),
    vuelta_rapida TIME(3),
    PRIMARY KEY (
        partida_id,
        circuito_id,
        piloto_id,
        temporada
    ),
    FOREIGN KEY (partida_id) REFERENCES partida (id) ON DELETE CASCADE,
    FOREIGN KEY (circuito_id) REFERENCES circuito (id),
    FOREIGN KEY (piloto_id) REFERENCES piloto (id) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 7 Tabla de transpasos
CREATE TABLE traspaso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    partida_id INT NOT NULL,
    piloto_id INT NOT NULL,
    escuderia_origen_id INT,
    escuderia_destino_id INT,
    precio DECIMAL(15, 2),
    temporada INT,
    aceptada BOOLEAN DEFAULT FALSE,
    en_curso BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (partida_id) REFERENCES partida (id) ON DELETE CASCADE,
    FOREIGN KEY (piloto_id) REFERENCES piloto (id) ON DELETE CASCADE,
    FOREIGN KEY (escuderia_origen_id) REFERENCES escuderia (id) ON DELETE SET NULL,
    FOREIGN KEY (escuderia_destino_id) REFERENCES escuderia (id) ON DELETE SET NULL
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; 

-- Alterar Partida para referenciar a Escuderia, solucionando la dependencia circular
ALTER TABLE partida
ADD FOREIGN KEY (id_escuderia_seleccionada) REFERENCES escuderia (id) ON DELETE SET NULL;

-- Alterar Escuderia para referenciar a Pilotos, solucionando la dependencia circular
ALTER TABLE escuderia
ADD CONSTRAINT fk_escuderia_piloto1 FOREIGN KEY (id_piloto_1) REFERENCES piloto (id) ON DELETE SET NULL,
ADD CONSTRAINT fk_escuderia_piloto2 FOREIGN KEY (id_piloto_2) REFERENCES piloto (id) ON DELETE SET NULL;

-- ==========================================================
-- CIRCUITOS (Calendario 2026 - Se mantienen porque son estáticos y globales)
-- ==========================================================

INSERT INTO
    circuito (
        nombre,
        pais,
        tiempo_base,
        num_vueltas,
        aerodinamica_req,
        motor_req
    )
VALUES (
        'Albert Park Circuit',
        'Australia',
        '00:01:19.800',
        58,
        7,
        6
    ),
    (
        'Shanghai International Circuit',
        'China',
        '00:01:36.500',
        54,
        7,
        8
    ),
    (
        'Suzuka Circuit',
        'Japón',
        '00:01:31.200',
        53,
        9,
        7
    ),
    (
        'Bahrain International Circuit',
        'Bahréin',
        '00:01:32.100',
        57,
        6,
        8
    ),
    (
        'Jeddah Corniche Circuit',
        'Arabia Saudí',
        '00:01:30.500',
        50,
        4,
        10
    ),
    (
        'Miami International Autodrome',
        'EE. UU.',
        '00:01:29.800',
        57,
        5,
        9
    ),
    (
        'Circuit Gilles Villeneuve',
        'Canadá',
        '00:01:14.800',
        70,
        5,
        9
    ),
    (
        'Circuit de Monaco',
        'Mónaco',
        '00:01:14.500',
        78,
        10,
        2
    ),
    (
        'Circuit de Barcelona-Catalunya',
        'España',
        '00:01:16.300',
        66,
        9,
        6
    ),
    (
        'Red Bull Ring',
        'Austria',
        '00:01:07.500',
        71,
        6,
        8
    ),
    (
        'Silverstone Circuit',
        'Reino Unido',
        '00:01:29.100',
        52,
        10,
        7
    ),
    (
        'Spa-Francorchamps',
        'Bélgica',
        '00:01:46.200',
        44,
        5,
        10
    ),
    (
        'Hungaroring',
        'Hungría',
        '00:01:19.500',
        70,
        9,
        4
    ),
    (
        'Circuit Zandvoort',
        'Países Bajos',
        '00:01:13.200',
        72,
        9,
        5
    ),
    (
        'Autodromo Nazionale Monza',
        'Italia',
        '00:01:21.000',
        53,
        2,
        10
    ),
    (
        'Circuito Urbano de Madrid',
        'España',
        '00:01:32.400',
        54,
        8,
        7
    ),
    (
        'Baku City Circuit',
        'Azerbaiyán',
        '00:01:43.000',
        51,
        4,
        10
    ),
    (
        'Marina Bay Street Circuit',
        'Singapur',
        '00:01:45.000',
        62,
        10,
        4
    ),
    (
        'Circuit of the Americas',
        'EE. UU. (Texas)',
        '00:01:37.500',
        56,
        8,
        8
    ),
    (
        'Autódromo Hermanos Rodríguez',
        'México',
        '00:01:20.200',
        71,
        7,
        9
    ),
    (
        'Autódromo José Carlos Pace (Interlagos)',
        'Brasil',
        '00:01:10.500',
        71,
        7,
        7
    ),
    (
        'Las Vegas Strip Circuit',
        'EE. UU.',
        '00:01:33.200',
        50,
        3,
        10
    ),
    (
        'Losail International Circuit',
        'Qatar',
        '00:01:24.500',
        57,
        8,
        8
    ),
    (
        'Yas Marina Circuit',
        'Abu Dhabi',
        '00:01:26.100',
        58,
        6,
        6
    );