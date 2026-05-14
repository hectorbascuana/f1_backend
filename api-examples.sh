#!/bin/bash

# Script con ejemplos de cURL para probar la API F1 Manager
# Uso: bash api-examples.sh o ./api-examples.sh

BASE_URL="http://localhost:8080/api"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}F1 Manager API - Ejemplos de Uso${NC}"
echo -e "${BLUE}========================================${NC}\n"

# 1. ESTADÍSTICAS
echo -e "${YELLOW}1. ESTADÍSTICAS${NC}\n"

echo -e "${GREEN}POST - Crear Estadística${NC}"
curl -X POST "$BASE_URL/estadisticas" \
  -H "Content-Type: application/json" \
  -d '{
    "valoracion": 85,
    "curvaRapida": 90,
    "curvaLenta": 80,
    "salidas": 88,
    "consistencia": 92
  }' | jq .
echo -e "\n"

echo -e "${GREEN}GET - Listar todas las Estadísticas${NC}"
curl -X GET "$BASE_URL/estadisticas" | jq .
echo -e "\n"

echo -e "${GREEN}GET - Obtener Estadística por ID${NC}"
curl -X GET "$BASE_URL/estadisticas/1" | jq .
echo -e "\n"

# 2. ESCUDERÍAS
echo -e "${YELLOW}2. ESCUDERÍAS${NC}\n"

echo -e "${GREEN}POST - Crear Escudería${NC}"
curl -X POST "$BASE_URL/escuderias" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Red Bull Racing",
    "imagen": "images/redbull.png",
    "presupuesto": 500000000,
    "aerodinamica": 85,
    "motor": 90,
    "durabilidad": 15,
    "tunelViento": 5,
    "bancoPruebas": 5,
    "escuelaPilotos": 5
  }' | jq .
echo -e "\n"

echo -e "${GREEN}POST - Crear otra Escudería${NC}"
curl -X POST "$BASE_URL/escuderias" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Mercedes-AMG",
    "imagen": "images/mercedes.png",
    "presupuesto": 480000000,
    "aerodinamica": 88,
    "motor": 88,
    "durabilidad": 16,
    "tunelViento": 5,
    "bancoPruebas": 5,
    "escuelaPilotos": 4
  }' | jq .
echo -e "\n"

echo -e "${GREEN}GET - Listar todas las Escuderías${NC}"
curl -X GET "$BASE_URL/escuderias" | jq .
echo -e "\n"

echo -e "${GREEN}GET - Obtener Escudería por ID${NC}"
curl -X GET "$BASE_URL/escuderias/1" | jq .
echo -e "\n"

echo -e "${GREEN}PUT - Actualizar Escudería${NC}"
curl -X PUT "$BASE_URL/escuderias/1" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Red Bull Racing Updated",
    "presupuesto": 510000000,
    "aerodinamica": 87,
    "motor": 92
  }' | jq .
echo -e "\n"

# 3. PILOTOS
echo -e "${YELLOW}3. PILOTOS${NC}\n"

echo -e "${GREEN}POST - Crear Piloto${NC}"
curl -X POST "$BASE_URL/pilotos" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Max Verstappen",
    "pais": "Países Bajos",
    "imagen": "images/max.png",
    "edad": 26,
    "puntos": 0,
    "valor": 50000000,
    "escuderia": {
      "id": 1
    },
    "estadistica": {
      "id": 1
    }
  }' | jq .
echo -e "\n"

echo -e "${GREEN}POST - Crear otro Piloto${NC}"
curl -X POST "$BASE_URL/pilotos" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Lewis Hamilton",
    "pais": "Reino Unido",
    "imagen": "images/lewis.png",
    "edad": 39,
    "puntos": 0,
    "valor": 45000000,
    "escuderia": {
      "id": 2
    }
  }' | jq .
echo -e "\n"

echo -e "${GREEN}GET - Listar todos los Pilotos${NC}"
curl -X GET "$BASE_URL/pilotos" | jq .
echo -e "\n"

echo -e "${GREEN}GET - Obtener Piloto por ID${NC}"
curl -X GET "$BASE_URL/pilotos/1" | jq .
echo -e "\n"

# 4. CIRCUITOS
echo -e "${YELLOW}4. CIRCUITOS${NC}\n"

echo -e "${GREEN}POST - Crear Circuito${NC}"
curl -X POST "$BASE_URL/circuitos" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Circuit de Spa-Francorchamps",
    "pais": "Bélgica",
    "tiempoBase": "00:01:43.969",
    "numVueltas": 44,
    "aerodinamicaReq": 3,
    "motorReq": 9
  }' | jq .
echo -e "\n"

echo -e "${GREEN}POST - Crear otro Circuito${NC}"
curl -X POST "$BASE_URL/circuitos" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Circuit de Monaco",
    "pais": "Mónaco",
    "tiempoBase": "00:01:12.909",
    "numVueltas": 78,
    "aerodinamicaReq": 8,
    "motorReq": 3
  }' | jq .
echo -e "\n"

echo -e "${GREEN}GET - Listar todos los Circuitos${NC}"
curl -X GET "$BASE_URL/circuitos" | jq .
echo -e "\n"

# 5. PILOTO-CIRCUITO (Resultados de carreras)
echo -e "${YELLOW}5. RESULTADOS DE CARRERAS (Piloto-Circuito)${NC}\n"

echo -e "${GREEN}POST - Crear Resultado de Carrera${NC}"
curl -X POST "$BASE_URL/piloto-circuito" \
  -H "Content-Type: application/json" \
  -d '{
    "id": {
      "circuitoId": 1,
      "pilotoId": 1,
      "temporada": 2024
    },
    "posicion": 1,
    "tiempoTotal": "01:15:30.123",
    "vueltaRapida": "00:01:43.000",
    "circuito": {
      "id": 1
    },
    "piloto": {
      "id": 1
    }
  }' | jq .
echo -e "\n"

echo -e "${GREEN}GET - Listar todos los Resultados${NC}"
curl -X GET "$BASE_URL/piloto-circuito" | jq .
echo -e "\n"

# EJEMPLOS CON VALIDACIÓN
echo -e "${YELLOW}6. EJEMPLOS DE ERROR (Validación)${NC}\n"

echo -e "${RED}POST - Intentar crear Estadística con valor inválido${NC}"
echo "(Debe fallar: valoracion debe estar entre 1 y 99)"
curl -X POST "$BASE_URL/estadisticas" \
  -H "Content-Type: application/json" \
  -d '{
    "valoracion": 150,
    "curvaRapida": 90
  }' | jq .
echo -e "\n"

echo -e "${RED}GET - Intentar obtener recurso no existente${NC}"
curl -X GET "$BASE_URL/pilotos/99999" | jq .
echo -e "\n"

# RESUMEN
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Resumen de Endpoints${NC}"
echo -e "${BLUE}========================================${NC}\n"

echo -e "${GREEN}Estadísticas:${NC}"
echo "  GET    /api/estadisticas"
echo "  GET    /api/estadisticas/{id}"
echo "  POST   /api/estadisticas"
echo "  PUT    /api/estadisticas/{id}"
echo "  DELETE /api/estadisticas/{id}"
echo ""

echo -e "${GREEN}Escuderías:${NC}"
echo "  GET    /api/escuderias"
echo "  GET    /api/escuderias/{id}"
echo "  POST   /api/escuderias"
echo "  PUT    /api/escuderias/{id}"
echo "  DELETE /api/escuderias/{id}"
echo ""

echo -e "${GREEN}Pilotos:${NC}"
echo "  GET    /api/pilotos"
echo "  GET    /api/pilotos/{id}"
echo "  POST   /api/pilotos"
echo "  PUT    /api/pilotos/{id}"
echo "  DELETE /api/pilotos/{id}"
echo ""

echo -e "${GREEN}Circuitos:${NC}"
echo "  GET    /api/circuitos"
echo "  GET    /api/circuitos/{id}"
echo "  POST   /api/circuitos"
echo "  PUT    /api/circuitos/{id}"
echo "  DELETE /api/circuitos/{id}"
echo ""

echo -e "${GREEN}Piloto-Circuito:${NC}"
echo "  GET    /api/piloto-circuito"
echo "  POST   /api/piloto-circuito"
echo "  DELETE /api/piloto-circuito/{circuitoId}/{pilotoId}/{temporada}"
echo ""

echo -e "${BLUE}✓ Pruebas completadas${NC}\n"
