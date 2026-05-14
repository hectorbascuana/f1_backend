.PHONY: help build run test clean docker-up docker-down docker-logs db-init

# Variables
MAVEN := ./mvnw
DOCKER_COMPOSE := docker-compose

help:
	@echo "F1 Manager Backend - Comandos Disponibles"
	@echo "=========================================="
	@echo ""
	@echo "Compilación:"
	@echo "  make build          - Compilar proyecto"
	@echo "  make build-clean    - Compilar limpiando primero (clean compile)"
	@echo "  make package        - Empaquetar JAR"
	@echo ""
	@echo "Ejecución Local:"
	@echo "  make run            - Ejecutar aplicación"
	@echo "  make run-dev        - Ejecutar con perfil dev"
	@echo "  make run-prod       - Ejecutar con perfil prod"
	@echo ""
	@echo "Testing:"
	@echo "  make test           - Ejecutar tests"
	@echo "  make test-watch     - Ejecutar tests en modo watch"
	@echo ""
	@echo "Docker:"
	@echo "  make docker-up      - Levantar contenedores (BD + Backend)"
	@echo "  make docker-down    - Detener contenedores"
	@echo "  make docker-logs    - Ver logs"
	@echo "  make docker-clean   - Eliminar volúmenes"
	@echo "  make docker-build   - Compilar imagen backend"
	@echo ""
	@echo "Base de Datos:"
	@echo "  make db-init        - Inicializar BD (local)"
	@echo "  make db-clean       - Limpiar BD"
	@echo ""
	@echo "Utilidades:"
	@echo "  make clean          - Limpiar proyecto"
	@echo "  make deps           - Mostrar árbol de dependencias"
	@echo "  make format         - Formatear código (si aplica)"
	@echo ""

# Compilación
build:
	$(MAVEN) compile

build-clean:
	$(MAVEN) clean compile

package:
	$(MAVEN) clean package

# Ejecución Local
run:
	$(MAVEN) spring-boot:run

run-dev:
	$(MAVEN) spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

run-prod:
	$(MAVEN) spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Testing
test:
	$(MAVEN) test

test-watch:
	$(MAVEN) test -Dsurefire.rerunFailingTestsCount=3

# Docker
docker-up:
	$(DOCKER_COMPOSE) up -d

docker-down:
	$(DOCKER_COMPOSE) down

docker-logs:
	$(DOCKER_COMPOSE) logs -f backend

docker-logs-db:
	$(DOCKER_COMPOSE) logs -f db

docker-build:
	$(DOCKER_COMPOSE) build

docker-clean:
	$(DOCKER_COMPOSE) down -v

# Base de Datos
db-init:
	mysql -u root -p < f1manager.sql

db-clean:
	mysql -u root -p -e "DROP DATABASE IF EXISTS f1_manager; source f1manager.sql;"

# Utilidades
clean:
	$(MAVEN) clean
	rm -rf target/

deps:
	$(MAVEN) dependency:tree

format:
	@echo "Formateando código..."
	$(MAVEN) spotless:apply

# Desarrollo
dev-setup: build db-init
	@echo "Ambiente de desarrollo configurado!"
	@echo "Para ejecutar: make run"

# Producción
prod-setup: package
	@echo "Ambiente de producción preparado!"
	@echo "JAR en: target/backend-0.0.1-SNAPSHOT.jar"

# Info
info:
	@echo "Información del Proyecto"
	@echo "========================"
	@echo "Nombre: F1 Manager Backend"
	@echo "Versión: 0.0.1-SNAPSHOT"
	@echo "Java: 17"
	@echo "Spring Boot: 4.0.5"
	@echo "Puerto: 8080"
	@echo "BD: MySQL f1_manager"
	@echo ""
	$(MAVEN) --version

# Puerto por defecto
status:
	@echo "Verificando estado..."
	@curl -s http://localhost:8080/actuator/health || echo "Aplicación no está ejecutándose"

stop-app:
	@echo "Deteniendo aplicación en puerto 8080..."
	@lsof -ti:8080 | xargs kill -9 2>/dev/null || echo "No hay proceso en puerto 8080"

# Ejemplo de curl
test-api:
	@echo "Probando GET /api/escuderias..."
	@curl -X GET http://localhost:8080/api/escuderias -H "Content-Type: application/json" | jq . || echo "Error en conexión"
