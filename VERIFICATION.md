# Verificación del Backend F1 Manager

## 🎉 Backend operacional

**Servidor ejecutándose**: http://localhost:8080  
**Base de datos**: H2 In-Memory Database (desarrollo)  
**Puerto**: 8080  

## Testing de Endpoints

### ✅ GET /api/escuderias
```bash
curl http://localhost:8080/api/escuderias
```
Respuesta: `[]` (vacío inicialmente)

### ✅ POST /api/escuderias
```bash
curl -X POST http://localhost:8080/api/escuderias \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Ferrari", "pais": "Italia"}'
```
Respuesta: Escudería creada con ID 1

### ✅ GET /api/escuderias/{id}
```bash
curl http://localhost:8080/api/escuderias/1
```
Respuesta: Objeto escudería con todos los campos

### ✅ PUT /api/escuderias/{id}
```bash
curl -X PUT http://localhost:8080/api/escuderias/1 \
  -H "Content-Type: application/json" \
  -d '{"id": 1, "nombre": "Ferrari Scuderia"}'
```
Respuesta: Escudería actualizada

### ✅ DELETE /api/escuderias/{id}
```bash
curl -X DELETE http://localhost:8080/api/escuderias/1
```
Respuesta: 200 OK (eliminada)

## Endpoints Disponibles (18 total)

### Escuderias (5 endpoints)
- `GET /api/escuderias` - Listar todas
- `POST /api/escuderias` - Crear
- `GET /api/escuderias/{id}` - Obtener por ID
- `PUT /api/escuderias/{id}` - Actualizar
- `DELETE /api/escuderias/{id}` - Eliminar

### Pilotos (5 endpoints)
- `GET /api/pilotos` - Listar todas
- `POST /api/pilotos` - Crear
- `GET /api/pilotos/{id}` - Obtener por ID
- `PUT /api/pilotos/{id}` - Actualizar
- `DELETE /api/pilotos/{id}` - Eliminar

### Circuitos (5 endpoints)
- Similar a Escuderías

### Estadísticas (3 endpoints)
- Similar a otros

### PilotoCircuito (3 endpoints)
- Similar a otros

## Configuración

### Entorno de Desarrollo (H2)
```bash
java -Dspring.profiles.active=dev -jar target/backend-0.0.1-SNAPSHOT.jar
```

**Archivo**: `application-dev.properties`
- Base de datos en memoria sin configuración externa
- DDL automático: `create-drop` (crea y elimina en cada inicio)
- Identifiers entrecomillados para evitar conflictos de mayúsculas

### Entorno de Producción (MySQL)
```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

**Archivo**: `application.properties`
- MySQL en `localhost:33006` (mapeado desde Docker)
- Usuario: `root` / Contraseña: `root`
- DDL: `update` (conserva datos)

## Compilación

```bash
./mvnw clean package -DskipTests
```

Resultado: JAR de 58MB en `target/backend-0.0.1-SNAPSHOT.jar`

## Arquitectura

- **Entidades JPA**: 6 (Estadistica, Escuderia, Piloto, Circuito, PilotoCircuito, PilotoCircuitoId)
- **Repositorios**: 5 (JpaRepository)
- **Servicios**: 5 (lógica CRUD)
- **Controladores**: 5 (@RestController con @CrossOrigin)
- **Base de datos**: H2 (desarrollo) / MySQL (producción)

## Próximos pasos

1. Crear más datos de prueba con POST
2. Probar relaciones entre entidades
3. Pasar a configuración MySQL en producción
4. Agregar autenticación/autorización si es necesario
5. Implementar validaciones avanzadas
