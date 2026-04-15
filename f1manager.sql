DROP DATABASE f1_manager;
CREATE DATABASE IF NOT EXISTS f1_manager;
USE f1_manager;

-- 1. Tabla de Estadísticas (Se crea antes que Piloto)
CREATE TABLE Estadistica (
    id INT AUTO_INCREMENT PRIMARY KEY,
    valoracion INT CHECK (valoracion BETWEEN 1 AND 99),
    curva_rapida INT CHECK (curva_rapida BETWEEN 1 AND 99),
    curva_lenta INT CHECK (curva_lenta BETWEEN 1 AND 99),
    salidas INT CHECK (salidas BETWEEN 1 AND 99),
    consistencia INT CHECK (consistencia BETWEEN 1 AND 99)
) ENGINE=InnoDB;

-- 2. Tabla de Escudería
CREATE TABLE Escuderia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    imagen VARCHAR(255), -- Ruta de la imagen (ej: "images/redbull.png")
    presupuesto DECIMAL(15, 2) DEFAULT 0.00, -- Se formatea en Java como 6,97M
    aerodinamica INT DEFAULT 1 CHECK (aerodinamica BETWEEN 1 AND 100),
    motor INT DEFAULT 1 CHECK (motor BETWEEN 1 AND 100),
    durabilidad INT DEFAULT 1 CHECK (durabilidad BETWEEN 1 AND 20),
    tunel_viento INT DEFAULT 1 CHECK (tunel_viento BETWEEN 1 AND 5),
    banco_pruebas INT DEFAULT 1 CHECK (banco_pruebas BETWEEN 1 AND 5),
    escuela_pilotos INT DEFAULT 1 CHECK (escuela_pilotos BETWEEN 1 AND 5)
) ENGINE=InnoDB;

-- 3. Tabla de Piloto
CREATE TABLE Piloto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    pais VARCHAR(50),
    imagen VARCHAR(255),
    escuderia_id INT,
    edad INT,
    puntos INT DEFAULT 0,
    valor DECIMAL(15, 2), -- Igual que presupuesto
    estadistica_id INT,
    FOREIGN KEY (escuderia_id) REFERENCES Escuderia(id) ON DELETE SET NULL,
    FOREIGN KEY (estadistica_id) REFERENCES Estadistica(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Tabla de Circuito
CREATE TABLE Circuito (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    pais VARCHAR(50),
    tiempo_base TIME(3), -- Formato HH:MM:SS.mmm (ej: 00:01:15.964)
    num_vueltas INT NOT NULL,
    aerodinamica_req INT CHECK (aerodinamica_req BETWEEN 1 AND 10),
    motor_req INT CHECK (motor_req BETWEEN 1 AND 10)
) ENGINE=InnoDB;

-- 5. Tabla Piloto_Circuito (Clasificación de carrera)
CREATE TABLE Piloto_Circuito (
    circuito_id INT,
    piloto_id INT,
    temporada INT,
    posicion INT,
    tiempo_total TIME(3), -- Suma de todas las vueltas
    vuelta_rapida TIME(3),
    PRIMARY KEY (circuito_id, piloto_id, temporada),
    FOREIGN KEY (circuito_id) REFERENCES Circuito(id),
    FOREIGN KEY (piloto_id) REFERENCES Piloto(id)
) ENGINE=InnoDB;

-- ==========================================================
-- 1. ESTADÍSTICAS (Base para 22 pilotos)
-- ==========================================================
INSERT INTO Estadistica (id, valoracion, curva_rapida, curva_lenta, salidas, consistencia) VALUES
-- 1. Max Verstappen: El más completo, agresivo pero infalible.
(1, 97, 98, 96, 95, 98),
-- 2. Isack Hadjar: Talento joven Red Bull, rápido pero debe pulir consistencia.
(2, 75, 78, 74, 80, 70),
-- 3. George Russell: "Mr. Saturday", excelente en curvas rápidas.
(3, 90, 92, 88, 85, 87),
-- 4. Andrea Kimi Antonelli: El "prodigio", valoración alta por potencial, pero novato.
(4, 79, 82, 80, 78, 75),
-- 5. Lewis Hamilton: El rey de la gestión de neumáticos y curvas lentas.
(5, 95, 93, 97, 90, 96),
-- 6. Charles Leclerc: El mejor a una vuelta (calificación), pura velocidad.
(6, 94, 96, 95, 88, 85),
-- 7. Lando Norris: Muy equilibrado y en su pico de forma.
(7, 92, 91, 90, 86, 92),
-- 8. Oscar Piastri: Sangre fría, consistencia asombrosa para su edad.
(8, 89, 88, 89, 84, 94),
-- 9. Fernando Alonso: El "viejo zorro", las mejores salidas de la parrilla.
(9, 92, 89, 93, 99, 94),
-- 10. Lance Stroll: Irregular, pero con destellos en lluvia o circuitos lentos.
(10, 76, 75, 78, 82, 68),
-- 11. Pierre Gasly: Sólido, muy técnico en el paso por curva.
(11, 84, 85, 84, 83, 85),
-- 12. Franco Colapinto: Agresivo y con gran control del coche en curvas rápidas.
(12, 80, 83, 79, 82, 78),
-- 13. Carlos Sainz Jr.: El "Smooth Operator", análisis táctico y consistencia top.
(13, 91, 89, 92, 91, 95),
-- 14. Alex Albon: Exprime coches mediocres, muy bueno en curvas lentas.
(14, 85, 84, 87, 86, 88),
-- 15. Liam Lawson: Tenaz, muy bueno defendiendo posición.
(15, 81, 80, 82, 85, 83),
-- 16. Arvid Lindblad: La gran promesa británica, estadísticas de aprendizaje.
(16, 73, 75, 72, 76, 70),
-- 17. Nico Hülkenberg: Maestro de la precisión técnica y trazadas.
(17, 85, 86, 85, 80, 89),
-- 18. Gabriel Bortoleto: Campeón de F3/F2, muy inteligente en carrera.
(18, 78, 79, 80, 77, 82),
-- 19. Esteban Ocon: Muy difícil de adelantar, agresivo en la salida.
(19, 83, 82, 84, 89, 81),
-- 20. Oliver Bearman: Gran debutante, brilla en curvas rápidas.
(20, 81, 85, 78, 80, 79),
-- 21. Sergio Pérez: Especialista en circuitos urbanos y tracción (curvas lentas).
(21, 82, 78, 90, 88, 75),
-- 22. Valtteri Bottas: Muy limpio y rápido en curvas de alta velocidad.
(22, 84, 88, 82, 80, 88);

-- ==========================================================
-- 2. ESCUDERÍAS (2026 - 11 Equipos)
-- ==========================================================
-- TRUNCATE TABLE Escuderia; -- Descomenta si necesitas limpiar antes de insertar

INSERT INTO Escuderia (id, nombre, imagen, presupuesto, aerodinamica, motor, durabilidad, tunel_viento, banco_pruebas, escuela_pilotos) VALUES
-- 1. Red Bull: Gran aerodinámica, pero el nuevo motor es una incógnita (durabilidad baja).
(1, 'Oracle Red Bull Racing', 'assets/teams/redbull.png', 145.00, 76, 72, 12, 3, 2, 3),
-- 2. Mercedes: Motor potente de referencia, aerodinámica en recuperación.
(2, 'Mercedes-AMG PETRONAS', 'assets/teams/mercedes.png', 140.00, 72, 78, 16, 3, 3, 2),
-- 3. Ferrari: Motor muy fuerte y banco de pruebas top, pero suelen fallar en fiabilidad técnica.
(3, 'Scuderia Ferrari HP', 'assets/teams/ferrari.png', 142.00, 74, 79, 14, 3, 3, 3),
-- 4. McLaren: La mejor aerodinámica inicial, motor cliente Mercedes sólido.
(4, 'McLaren Formula 1', 'assets/teams/mclaren.png', 135.00, 80, 77, 17, 3, 2, 2),
-- 5. Aston Martin: Inversión masiva en túnel de viento, motor Honda (nuevo en 2026).
(5, 'Aston Martin Aramco', 'assets/teams/aston.png', 125.00, 73, 75, 13, 3, 2, 1),
-- 6. Alpine: Crisis técnica constante, motor justo y durabilidad sospechosa.
(6, 'Alpine F1 Team', 'assets/teams/alpine.png', 110.00, 65, 68, 10, 2, 2, 3),
-- 7. Williams: Muy rápidos en recta (poco drag), pero infraestructura antigua.
(7, 'Williams Racing', 'assets/teams/williams.png', 95.00, 62, 76, 15, 1, 2, 2),
-- 8. Racing Bulls: Dependencia de Red Bull, equilibrio medio.
(8, 'Racing Bulls (VCARB)', 'assets/teams/vcarb.png', 85.00, 68, 71, 12, 2, 1, 3),
-- 9. Audi: Proyecto nuevo, gran motor pero aerodinámica por desarrollar.
(9, 'Audi F1 Team', 'assets/teams/audi.png', 130.00, 66, 74, 11, 2, 3, 2),
-- 10. Haas: Recursos limitados, buena durabilidad por piezas Ferrari.
(10, 'MoneyGram Haas F1 Team', 'assets/teams/haas.png', 80.00, 64, 75, 16, 1, 1, 1),
-- 11. Cadillac: Equipo nuevo, mucho músculo americano pero todo por mejorar.
(11, 'Cadillac Andretti F1', 'assets/teams/cadillac.png', 120.00, 60, 70, 9, 2, 2, 1);
-- ==========================================================
-- 3. PILOTOS (Parrilla Actualizada 2026)
-- ==========================================================
INSERT INTO Piloto (nombre, pais, imagen, escuderia_id, edad, puntos, valor, estadistica_id) VALUES
-- Red Bull
('Max Verstappen', 'Países Bajos', 'assets/drivers/verstappen.png', 1, 28, 0, 55.00, 1),
('Isack Hadjar', 'Francia', 'assets/drivers/hadjar.png', 1, 21, 0, 12.00, 2),
-- Mercedes
('George Russell', 'Reino Unido', 'assets/drivers/russell.png', 2, 28, 0, 38.00, 3),
('Andrea Kimi Antonelli', 'Italia', 'assets/drivers/antonelli.png', 2, 19, 0, 20.00, 4),
-- Ferrari
('Lewis Hamilton', 'Reino Unido', 'assets/drivers/hamilton.png', 3, 41, 0, 50.00, 5),
('Charles Leclerc', 'Mónaco', 'assets/drivers/leclerc.png', 3, 28, 0, 48.00, 6),
-- McLaren
('Lando Norris', 'Reino Unido', 'assets/drivers/norris.png', 4, 26, 0, 42.00, 7),
('Oscar Piastri', 'Australia', 'assets/drivers/piastri.png', 4, 24, 0, 35.00, 8),
-- Aston Martin
('Fernando Alonso', 'España', 'assets/drivers/alonso.png', 5, 44, 0, 35.00, 9),
('Lance Stroll', 'Canadá', 'assets/drivers/stroll.png', 5, 27, 0, 15.00, 10),
-- Alpine
('Pierre Gasly', 'Francia', 'assets/drivers/gasly.png', 6, 30, 0, 22.00, 11),
('Franco Colapinto', 'Argentina', 'assets/drivers/colapinto.png', 6, 22, 0, 15.00, 12),
-- Williams
('Carlos Sainz Jr.', 'España', 'assets/drivers/sainz.png', 7, 31, 0, 34.00, 13),
('Alex Albon', 'Tailandia', 'assets/drivers/albon.png', 7, 30, 0, 20.00, 14),
-- Racing Bulls
('Liam Lawson', 'Nueva Zelanda', 'assets/drivers/lawson.png', 8, 24, 0, 14.00, 15),
('Arvid Lindblad', 'Reino Unido', 'assets/drivers/lindblad.png', 8, 18, 0, 10.00, 16),
-- Audi
('Nico Hülkenberg', 'Alemania', 'assets/drivers/hulkenberg.png', 9, 38, 0, 18.00, 17),
('Gabriel Bortoleto', 'Brasil', 'assets/drivers/bortoleto.png', 9, 21, 0, 15.00, 18),
-- Haas
('Esteban Ocon', 'Francia', 'assets/drivers/ocon.png', 10, 29, 0, 18.00, 19),
('Oliver Bearman', 'Reino Unido', 'assets/drivers/bearman.png', 10, 20, 0, 12.00, 20),
-- Cadillac
('Sergio Pérez', 'México', 'assets/drivers/perez.png', 11, 36, 0, 25.00, 21),
('Valtteri Bottas', 'Finlandia', 'assets/drivers/bottas.png', 11, 36, 0, 20.00, 22);

-- ==========================================================
-- 4. CIRCUITOS (Calendario 2026)
-- ==========================================================

INSERT INTO Circuito (nombre, pais, tiempo_base, num_vueltas, aerodinamica_req, motor_req) VALUES
('Albert Park Circuit', 'Australia', '00:01:19.800', 58, 7, 6),
('Shanghai International Circuit', 'China', '00:01:36.500', 54, 7, 8),
('Suzuka Circuit', 'Japón', '00:01:31.200', 53, 9, 7),
('Bahrain International Circuit', 'Bahréin', '00:01:32.100', 57, 6, 8),
('Jeddah Corniche Circuit', 'Arabia Saudí', '00:01:30.500', 50, 4, 10),
('Miami International Autodrome', 'EE. UU.', '00:01:29.800', 57, 5, 9),
('Circuit Gilles Villeneuve', 'Canadá', '00:01:14.800', 70, 5, 9),
('Circuit de Monaco', 'Mónaco', '00:01:14.500', 78, 10, 2),
('Circuit de Barcelona-Catalunya', 'España', '00:01:16.300', 66, 9, 6),
('Red Bull Ring', 'Austria', '00:01:07.500', 71, 6, 8),
('Silverstone Circuit', 'Reino Unido', '00:01:29.100', 52, 10, 7),
('Spa-Francorchamps', 'Bélgica', '00:01:46.200', 44, 5, 10),
('Hungaroring', 'Hungría', '00:01:19.500', 70, 9, 4),
('Circuit Zandvoort', 'Países Bajos', '00:01:13.200', 72, 9, 5),
('Autodromo Nazionale Monza', 'Italia', '00:01:21.000', 53, 2, 10),
('Circuito Urbano de Madrid', 'España', '00:01:32.400', 54, 8, 7),
('Baku City Circuit', 'Azerbaiyán', '00:01:43.000', 51, 4, 10),
('Marina Bay Street Circuit', 'Singapur', '00:01:45.000', 62, 10, 4),
('Circuit of the Americas', 'EE. UU. (Texas)', '00:01:37.500', 56, 8, 8),
('Autódromo Hermanos Rodríguez', 'México', '00:01:20.200', 71, 7, 9),
('Autódromo José Carlos Pace (Interlagos)', 'Brasil', '00:01:10.500', 71, 7, 7),
('Las Vegas Strip Circuit', 'EE. UU.', '00:01:33.200', 50, 3, 10),
('Losail International Circuit', 'Qatar', '00:01:24.500', 57, 8, 8),
('Yas Marina Circuit', 'Abu Dhabi', '00:01:26.100', 58, 6, 6);