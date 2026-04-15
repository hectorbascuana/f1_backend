-- ==========================================================
-- 1. ESTADÍSTICAS (Base para 22 pilotos)
-- ==========================================================
INSERT INTO "Estadistica" (id, valoracion, curva_rapida, curva_lenta, salidas, consistencia) VALUES
(1, 50, 50, 50, 50, 50), (2, 50, 50, 50, 50, 50), (3, 50, 50, 50, 50, 50), (4, 50, 50, 50, 50, 50), (5, 50, 50, 50, 50, 50),
(6, 50, 50, 50, 50, 50), (7, 50, 50, 50, 50, 50), (8, 50, 50, 50, 50, 50), (9, 50, 50, 50, 50, 50), (10, 50, 50, 50, 50, 50),
(11, 50, 50, 50, 50, 50), (12, 50, 50, 50, 50, 50), (13, 50, 50, 50, 50, 50), (14, 50, 50, 50, 50, 50), (15, 50, 50, 50, 50, 50),
(16, 50, 50, 50, 50, 50), (17, 50, 50, 50, 50, 50), (18, 50, 50, 50, 50, 50), (19, 50, 50, 50, 50, 50), (20, 50, 50, 50, 50, 50),
(21, 50, 50, 50, 50, 50), (22, 50, 50, 50, 50, 50);

-- ==========================================================
-- 2. ESCUDERÍAS (2026 - 11 Equipos)
-- ==========================================================
INSERT INTO "Escuderia" (id, nombre, imagen, presupuesto, aerodinamica, motor, durabilidad, tunel_viento, banco_pruebas, escuela_pilotos) VALUES
(1, 'Oracle Red Bull Racing', 'assets/teams/redbull.png', 145.00, 1, 1, 1, 1, 1, 1),
(2, 'Mercedes-AMG PETRONAS', 'assets/teams/mercedes.png', 140.00, 1, 1, 1, 1, 1, 1),
(3, 'Scuderia Ferrari HP', 'assets/teams/ferrari.png', 142.00, 1, 1, 1, 1, 1, 1),
(4, 'McLaren Formula 1', 'assets/teams/mclaren.png', 135.00, 1, 1, 1, 1, 1, 1),
(5, 'Aston Martin Aramco', 'assets/teams/aston.png', 125.00, 1, 1, 1, 1, 1, 1),
(6, 'Alpine F1 Team', 'assets/teams/alpine.png', 110.00, 1, 1, 1, 1, 1, 1),
(7, 'Williams Racing', 'assets/teams/williams.png', 95.00, 1, 1, 1, 1, 1, 1),
(8, 'Racing Bulls (VCARB)', 'assets/teams/vcarb.png', 85.00, 1, 1, 1, 1, 1, 1),
(9, 'Audi F1 Team', 'assets/teams/audi.png', 130.00, 1, 1, 1, 1, 1, 1),
(10, 'MoneyGram Haas F1 Team', 'assets/teams/haas.png', 80.00, 1, 1, 1, 1, 1, 1),
(11, 'Cadillac Andretti F1', 'assets/teams/cadillac.png', 120.00, 1, 1, 1, 1, 1, 1);

-- ==========================================================
-- 3. PILOTOS (Parrilla Actualizada 2026)
-- ==========================================================
INSERT INTO "Piloto" (nombre, pais, imagen, escuderia_id, edad, puntos, valor, estadistica_id) VALUES
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
INSERT INTO "Circuito" (nombre, pais, tiempo_base, num_vueltas, aerodinamica_req, motor_req) VALUES
('Albert Park Circuit', 'Australia', '00:01:19.800', 58, 7, 6),
('Shanghai Audi Circuit', 'China', '00:01:36.500', 56, 7, 8),
('Suzuka Circuit', 'Japón', '00:01:31.200', 53, 9, 7),
('Bahrain International', 'Bahréin', '00:01:32.100', 57, 6, 8),
('Jeddah Corniche', 'Arabia Saudí', '00:01:30.500', 50, 4, 10),
('Miami International', 'EE. UU.', '00:01:29.800', 57, 5, 9),
('Circuit de Monaco', 'Mónaco', '00:01:14.500', 78, 10, 2),
('Circuit de Barcelona', 'España', '00:01:16.300', 66, 9, 6),
('Silverstone Circuit', 'Reino Unido', '00:01:29.100', 52, 10, 7),
('Spa-Francorchamps', 'Bélgica', '00:01:46.200', 44, 5, 10),
('Autodromo de Madrid', 'España', '00:01:32.400', 54, 8, 7),
('Las Vegas Strip', 'EE. UU.', '00:01:33.200', 50, 3, 10),
('Interlagos', 'Brasil', '00:01:10.500', 71, 7, 7),
('Yas Marina', 'Abu Dhabi', '00:01:26.100', 58, 6, 6);
