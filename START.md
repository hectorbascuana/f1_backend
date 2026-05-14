# 🎉 ¡BACKEND COMPLETADO!

Tu proyecto F1 Manager Backend está **100% listo** para usar.

---

## ⚡ Inicio Rápido (Elije una opción)

### Opción 1️⃣ - RECOMENDADO: Ejecutar Localmente (más fácil)

```bash
# 1. Terminal 1: Iniciar MySQL
docker-compose up db

# 2. Terminal 2: Ejecutar backend
./mvnw spring-boot:run
```

La API estará en: **http://localhost:8080**

### Opción 2️⃣ - Ejecutar TODO con Docker

```bash
docker-compose up
```

### Opción 3️⃣ - Usar Make (más simple)

```bash
# Ver todos los comandos
make help

# Ejecutar
make run

# Con Docker
make docker-up
```

---

## 📋 Verificar que funciona

```bash
# En otra terminal:
curl http://localhost:8080/api/escuderias
```

Debe responder con una lista vacía (o 200 OK).

---

## 🧪 Probar API con ejemplos

```bash
# Hacer script ejecutable
chmod +x api-examples.sh

# Ejecutar ejemplos
./api-examples.sh
```

O manualmente:

```bash
# Crear Escudería
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

# Listar Escuderías
curl http://localhost:8080/api/escuderias | json_pp
```

---

## 📚 Documentación

- **README_BACKEND.md** - Documentación completa (requisitos, endpoints, etc.)
- **QUICK_START.md** - Guía de inicio rápido
- **ARCHITECTURE.md** - Documentación técnica de arquitectura
- **COMPLETION_SUMMARY.md** - Resumen de lo completado
- **api-examples.sh** - Script con ejemplos funcionales

---

## 🔍 Estructura del Proyecto

```
src/main/java/com/f1manager/backend/
├── controller/      → Endpoints REST (5 controladores)
├── service/         → Lógica de negocio (5 servicios)
├── repository/      → Acceso a datos (5 repositorios)
└── entity/          → Modelos de datos (6 entidades)
```

---

## 📈 Entidades disponibles

1. **Escuderia** - Equipos de F1
2. **Piloto** - Pilotos con estadísticas
3. **Estadistica** - Atributos de piloto
4. **Circuito** - Pistas/circuitos
5. **PilotoCircuito** - Resultados de carreras

---

## 🐳 Comandos Docker Útiles

```bash
# Ver estado
docker-compose ps

# Ver logs
docker-compose logs -f backend
docker-compose logs -f db

# Detener
docker-compose down

# Limpiar todo (incluyendo datos)
docker-compose down -v
```

---

## 🛠️ Comandos Makefile

```bash
make help              # Ver todos los comandos
make build            # Compilar
make run              # Ejecutar
make run-dev          # Ejecutar modo desarrollo
make docker-up        # Docker Compose up
make docker-down      # Docker Compose down
make test             # Ejecutar tests
make clean            # Limpiar
```

---

## 🔧 Compilación y Empaquetado

```bash
# Solo compilar
./mvnw compile

# Crear JAR
./mvnw clean package

# Ejecutar JAR
java -jar target/backend-0.0.1-SNAPSHOT.jar

# Ver dependencias
./mvnw dependency:tree
```

---

## ⚠️ Errors Comunes y Soluciones

| Problema | Solución |
|----------|----------|
| Connection refused (BD) | Asegúrate que MySQL está corriendo: `docker-compose up db` |
| Puerto 8080 en uso | Cambiar puerto en `application.properties`: `server.port=8081` |
| No se crea la BD | Ejecuta: `docker exec f1_manager_db mysql -uroot -proot < f1manager.sql` |
| Erro de compilación | Ejecuta: `./mvnw clean` antes de compilar |

---

## 📱 Endpoints Disponibles

| Recurso | GET | POST | PUT | DELETE |
|---------|-----|------|-----|--------|
| `/api/escuderias` | ✅ | ✅ | ✅ | ✅ |
| `/api/pilotos` | ✅ | ✅ | ✅ | ✅ |
| `/api/estadisticas` | ✅ | ✅ | ✅ | ✅ |
| `/api/circuitos` | ✅ | ✅ | ✅ | ✅ |
| `/api/piloto-circuito` | ✅ | ✅ | - | ✅ |

---

## 🎯 Próximos Pasos Sugeridos

1. **Probar API** ← EMPEZAR AQUÍ
   - Ejecuta `./api-examples.sh`
   - Prueba crear/editar/eliminar datos

2. **Integrar Frontend**
   - Nota: Todos los endpoints tienen CORS habilitado

3. **Agregar Tests**
   - Tests unitarios para servicios
   - Tests de integración para controladores

4. **Autenticación** (cuando sea necesario)
   - Agregar Spring Security
   - Implementar JWT

---

## 💾 Variables de Entorno (para producción)

```bash
# En Docker:
DB_HOST=your-host
DB_PORT=3306
DB_NAME=f1_manager
DB_USER=root
DB_PASSWORD=your-password
SERVER_PORT=8080
```

O en `.env`:
```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/f1_manager
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
```

---

## ✨ Lo que incluye este Backend

✅ **6 Entidades JPA** con relaciones complejas (1-N, M-N, etc.)

✅ **5 Repositorios** con acceso completo a datos

✅ **5 Servicios** con lógica de negocio

✅ **5 Controladores REST** con 18 endpoints

✅ **Validaciones** automáticas a nivel de entidad

✅ **Docker** para fácil deployment

✅ **Documentación** técnica completa

✅ **Ejemplos funcionales** de API

✅ **Configuración multi-ambiente** (dev/prod)

✅ **Makefile** con comandos útiles

---

## 🎓 Tecnologías Utilizadas

- **Spring Boot 4.0.5**
- **Spring Data JPA** (Hibernate)
- **MySQL 8.0**
- **Lombok** (reducción de código)
- **Jakarta Validation**
- **Docker & Docker Compose**
- **Maven** (build tool)
- **Java 17**

---

## 📞 Soporte

- Ver **README_BACKEND.md** para documentación completa
- Ver **ARCHITECTURE.md** para entender la arquitectura
- En terminal: `make help` para ver comandos disponibles

---

## 🚀 ¡A Empezar!

```bash
# Terminal 1
docker-compose up db &

# Terminal 2
./mvnw spring-boot:run

# Terminal 3 (después de 30 segundos)
curl http://localhost:8080/api/escuderias

# ¡Listo! Tu API está funcionando 🎉
```

---

**Compilación**: ✅ BUILD SUCCESS

**Estado**: 🟢 LISTO PARA USAR

**Próximas acciones**: Ejecuta `docker-compose up db` y luego `./mvnw spring-boot:run`

¡Disfruta desarrollando F1 Manager! 🏁
