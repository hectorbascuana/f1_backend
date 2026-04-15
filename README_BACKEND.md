# F1 Manager - Backend

## Descripción
Backend desarrollado con Spring Boot 4.0.5 para gestionar un videojuego de Fórmula 1. La aplicación maneja la información de pilotos, escuderías, circuitos y estadísticas de carreras.

## Estructura del Proyecto

```
src/main/java/com/f1manager/backend/
├── BackendApplication.java          # Clase principal de Spring Boot
├── controller/                      # Controladores REST
│   ├── EstadisticaController.java
│   ├── EscuderiaController.java
│   ├── PilotoController.java
│   ├── CircuitoController.java
│   └── PilotoCircuitoController.java
├── service/                         # Servicios (Lógica de negocio)
│   ├── EstadisticaService.java
│   ├── EscuderiaService.java
│   ├── PilotoService.java
│   ├── CircuitoService.java
│   └── PilotoCircuitoService.java
├── repository/                      # Repositorios JPA
│   ├── EstadisticaRepository.java
│   ├── EscuderiaRepository.java
│   ├── PilotoRepository.java
│   ├── CircuitoRepository.java
│   └── PilotoCircuitoRepository.java
└── entity/                          # Entidades JPA/Hibernate
    ├── Estadistica.java
    ├── Escuderia.java
    ├── Piloto.java
    ├── Circuito.java
    ├── PilotoCircuito.java
    └── PilotoCircuitoId.java
```

## Requisitos Previos

- Java 17 o superior
- MySQL 8.0 o superior
- Maven 3.6+ (o usar Maven Wrapper: `./mvnw`)

## Configuración Base de Datos

1. **Crear la base de datos** (si no existe):
```bash
mysql -u root -p < f1manager.sql
```

2. **Credenciales por defecto** (configuradas en `application.properties`):
   - Host: `localhost:3306`
   - Database: `f1_manager`
   - Username: `root`
   - Password: `root`

## Compilar el Proyecto

```bash
./mvnw clean compile
```

## Ejecutar la Aplicación

```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## Empaquetado JAR

```bash
./mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Endpoints API

### Estadísticas
- `GET /api/estadisticas` - Obtener todas
- `GET /api/estadisticas/{id}` - Obtener por ID
- `POST /api/estadisticas` - Crear
- `PUT /api/estadisticas/{id}` - Actualizar
- `DELETE /api/estadisticas/{id}` - Eliminar

### Escuderías
- `GET /api/escuderias` - Obtener todas
- `GET /api/escuderias/{id}` - Obtener por ID
- `POST /api/escuderias` - Crear
- `PUT /api/escuderias/{id}` - Actualizar
- `DELETE /api/escuderias/{id}` - Eliminar

### Pilotos
- `GET /api/pilotos` - Obtener todos
- `GET /api/pilotos/{id}` - Obtener por ID
- `POST /api/pilotos` - Crear
- `PUT /api/pilotos/{id}` - Actualizar
- `DELETE /api/pilotos/{id}` - Eliminar

### Circuitos
- `GET /api/circuitos` - Obtener todos
- `GET /api/circuitos/{id}` - Obtener por ID
- `POST /api/circuitos` - Crear
- `PUT /api/circuitos/{id}` - Actualizar
- `DELETE /api/circuitos/{id}` - Eliminar

### Piloto-Circuito (Resultados de carreras)
- `GET /api/piloto-circuito` - Obtener todos
- `POST /api/piloto-circuito` - Crear resultado
- `DELETE /api/piloto-circuito/{circuitoId}/{pilotoId}/{temporada}` - Eliminar

## Tecnologías Utilizadas

- **Spring Boot 4.0.5**
  - Spring Data JPA
  - Spring Web (MVC)
  - Spring Validation
- **MySQL** - Base de datos relacional
- **Hibernate** - ORM (Object-Relational Mapping)
- **Lombok** - Reducción de código boilerplate
- **Maven** - Gestor de dependencias

## Configuración Adicional (application.properties)

```properties
# Base de datos MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/f1_manager
spring.datasource.username=root
spring.datasource.password=root

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.org.hibernate.SQL=DEBUG
```

## Modelo de Datos

### Entidad: Estadistica
Almacena las estadísticas de un piloto.
- `valoracion` (1-99)
- `curva_rapida` (1-99)
- `curva_lenta` (1-99)
- `salidas` (1-99)
- `consistencia` (1-99)

### Entidad: Escuderia
Información de los equipos/escuderías.
- `nombre` - Nombre del equipo
- `presupuesto` - Presupuesto disponible
- `aerodinamica` (1-100)
- `motor` (1-100)
- `durabilidad` (1-20)
- `tunel_viento` (1-5)
- `banco_pruebas` (1-5)
- `escuela_pilotos` (1-5)

### Entidad: Piloto
Información de los pilotos.
- `nombre`, `pais`, `imagen`
- `edad`, `puntos`, `valor`
- Relación: pertenece a una `Escuderia`
- Relación: tiene una `Estadistica`

### Entidad: Circuito
Información de los circuitos/pistas.
- `nombre`, `pais`
- `tiempo_base` - Tiempo de referencia
- `num_vueltas` - Número de vueltas
- `aerodinamica_req` (1-10) - Requisito de aerodinámica
- `motor_req` (1-10) - Requisito de motor

### Entidad: PilotoCircuito
Resultados de carreras (relación muchos-a-muchos).
- `circuito_id` + `piloto_id` + `temporada` = Clave compuesta
- `posicion` - Posición final
- `tiempo_total` - Tiempo total de la carrera
- `vuelta_rapida` - Tiempo de vuelta rápida

## Notas de Desarrollo

- La base de datos se crea/actualiza automáticamente con `ddl-auto=update`
- Todos los controladores tienen CORS habilitado: `@CrossOrigin(origins = "*")`
- Las validaciones se realizan mediante anotaciones de Jakarta Validation
- Se usa inyección de dependencias mediante constructores
- Los servicios manejan la lógica de negocio
- Los repositorios son interfaces que extienden `JpaRepository`

## Versiones de Tecnologías

- Java: 17
- Spring Boot: 4.0.5
- MySQL Connector: versión incluida en Spring Boot parent
- Lombok: versión incluida en Spring Boot parent

## Pruebas

Ejecutar pruebas:
```bash
./mvnw test
```

Las pruebas actualmente son placeholder. Se recomienda agregar tests unitarios para servicios y tests de integración para controladores.

## Próximos Pasos

- [ ] Implementar DTOs (Data Transfer Objects) para las respuestas API
- [ ] Agregar excepciones personalizadas y manejo de errores global
- [ ] Crear tests unitarios e integración
- [ ] Agregar autenticación/autorización
- [ ] Implementar paginación en endpoints GET
- [ ] Agregar filtros y búsquedas avanzadas
- [ ] Documentar API con Swagger/OpenAPI
- [ ] Optimizar queries con uso de projections y especifications

## Licencia

Este proyecto es un ejemplo educativo para aprender Spring Boot.
