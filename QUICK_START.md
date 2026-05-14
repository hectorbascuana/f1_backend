# Quick Start - F1 Manager Backend

## Opción 1: Ejecución Local (Recomendado para desarrollo)

### Requisitos
- Java 17+
- MySQL 8.0+ instalado y ejecutándose
- Maven 3.6+ (incluido con `mvnw`)

### Pasos

1. **Crear la base de datos**:
```bash
mysql -u root -p < f1manager.sql
```
Introduce la contraseña: `root`

2. **Compilar el proyecto**:
```bash
./mvnw clean compile
```

3. **Ejecutar la aplicación**:
```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

---

## Opción 2: Con Docker Compose (Recomendado para producción)

### Requisitos
- Docker instalado
- Docker Compose instalado
- ~2 GB de espacio disponible

### Pasos

1. **Iniciar los contenedores**:
```bash
docker-compose up -d
```

2. **Ver logs** (opcional):
```bash
docker-compose logs -f backend
docker-compose logs -f db
```

3. **Detener los contenedores**:
```bash
docker-compose down
```

4. **Limpiar datos** (eliminar volumen):
```bash
docker-compose down -v
```

---

## Opción 3: Solo MySQL en Docker (Backend local)

### Pasos

1. **Ejecutar solo la BD**:
```bash
docker-compose up -d db
```

2. **Compilar y ejecutar backend localmente**:
```bash
./mvnw spring-boot:run
```

---

## Probar la API

### Crear Escudería
```bash
curl -X POST http://localhost:8080/api/escuderias \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Red Bull Racing",
    "presupuesto": 500000000,
    "aerodinamica": 85,
    "motor": 90,
    "durabilidad": 15,
    "tunelViento": 5,
    "bancoPruebas": 5,
    "escuelaPilotos": 5
  }'
```

### Listar Escuderías
```bash
curl http://localhost:8080/api/escuderias | json_pp
```

### Crear Piloto
```bash
curl -X POST http://localhost:8080/api/pilotos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Max Verstappen",
    "pais": "Holanda",
    "edad": 26,
    "puntos": 0,
    "valor": 50000000,
    "escuderia": {"id": 1}
  }'
```

---

## Problemas Comunes

### Error: "Connection refused" al conectar BD
- Asegúrate de que MySQL está corriendo: `mysql --version`
- Verifica credenciales en `application.properties`
- Si usas Docker: `docker-compose up -d db` y espera 20 segundos

### Error: "ddl-auto=update" modifica tablas
- Es normal en desarrollo, regenerará las tablas automáticamente
- En producción, cambia a `ddl-auto=validate`

### Puerto 8080 ya en uso
- Cambia el puerto en `application.properties`: `server.port=8081`
- O detén la otra aplicación usando ese puerto

### Error de compilación Java
- Verifica tener Java 17+: `java --version`
- Limpia el cache: `./mvnw clean`

---

## Variables de Entorno (Docker)

Para Docker, puedes sobrescribir configuraciones con variables:

```bash
docker-compose -e "SPRING_DATASOURCE_PASSWORD=nuevacontraseña" up
```

O agregar en `.env`:
```
DB_HOST=custom-host
DB_USER=usuario
DB_PASSWORD=contraseña
```

---

## Perfiles de Spring Boot

Ejecutar con perfil específico:

```bash
# Desarrollo (logs detallados)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Producción (logs mínimos)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

---

## Comandos Útiles

```bash
# Compilar sin ejecutar tests
./mvnw clean compile -DskipTests

# Ejecutar solo tests
./mvnw test

# Crear JAR empaquetado
./mvnw clean package

# Ver dependencias
./mvnw dependency:tree

# Ejecutar en background
./mvnw spring-boot:run &

# Ver procesos Java
jps -l

# Matar proceso en puerto 8080
sudo lsof -ti:8080 | xargs kill -9
```

---

## Siguientes Pasos

1. Implementar DTOs para las respuestas
2. Agregar excepciones personalizadas
3. Crear tests unitarios
4. Agregar autenticación (JWT)
5. Documentar con Swagger
6. Optimizar queries con JPA Specifications

¡Bienvenido al desarrollo de F1 Manager!
