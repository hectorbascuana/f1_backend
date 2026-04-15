# Documentación Técnica - Arquitectura

## Arquitectura General

El proyecto sigue el patrón **MVC (Model-View-Controller)** con separación en capas:

```
┌─────────────────────────────────────────────────────────┐
│                     Cliente (Frontend)                  │
├─────────────────────────────────────────────────────────┤
│ REST API (HTTP/JSON)                                    │
├─────────────────────────────────────────────────────────┤
│ Controller Layer (Controladores REST)                   │
│ - Gestiona solicitudes HTTP                            │
│ - Mapea requests/responses                             │
│ - Validación básica                                    │
├─────────────────────────────────────────────────────────┤
│ Service Layer (Servicios)                              │
│ - Lógica de negocio                                    │
│ - Orquestación de operaciones                          │
│ - Validaciones complejas                               │
├─────────────────────────────────────────────────────────┤
│ Repository Layer (Repositorios JPA)                    │
│ - Acceso a datos                                       │
│ - Queries a BD                                         │
├─────────────────────────────────────────────────────────┤
│ Entity Layer (Entidades JPA/Hibernate)                 │
│ - Mapeo a tablas BD                                    │
│ - Relaciones entre entidades                           │
├─────────────────────────────────────────────────────────┤
│          MySQL Database (f1_manager)                    │
└─────────────────────────────────────────────────────────┘
```

## Capas Explicadas

### 1. Controller Layer (Presentación)
**Ubicación**: `com.f1manager.backend.controller`

**Responsabilidades**:
- Recibir solicitudes HTTP
- Validar entrada (validaciones básicas)
- Llamar a servicios
- Retornar respuestas JSON
- Manejar códigos de estado HTTP

**Ejemplo**:
```java
@RestController
@RequestMapping("/api/pilotos")
public class PilotoController {
    
    @GetMapping
    public ResponseEntity<List<Piloto>> obtenerTodos() {
        return ResponseEntity.ok(pilotoService.obtenerTodos());
    }
}
```

### 2. Service Layer (Lógica de Negocio)
**Ubicación**: `com.f1manager.backend.service`

**Responsabilidades**:
- Implementar reglas de negocio
- Orquestar múltiples operaciones
- Validaciones complejas
- Transacciones
- Cálculos y transformaciones

**Patrón**: Inyección de dependencias mediante constructores

```java
@Service
public class PilotoService {
    
    private final PilotoRepository pilotoRepository;
    
    // Constructor injection
    public PilotoService(PilotoRepository pilotoRepository) {
        this.pilotoRepository = pilotoRepository;
    }
    
    public List<Piloto> obtenerTodos() {
        return pilotoRepository.findAll();
    }
}
```

### 3. Repository Layer (Persistencia)
**Ubicación**: `com.f1manager.backend.repository`

**Responsabilidades**:
- Abstracción de acceso a datos
- Queries a BD
- Operaciones CRUD

**Características**:
- Extienden `JpaRepository<T, ID>`
- Generan automáticamente operaciones CRUD
- Permiten definir queries personalizadas

```java
@Repository
public interface PilotoRepository extends JpaRepository<Piloto, Integer> {
    // Las operaciones CRUD se generan automáticamente:
    // save(), saveAll(), findById(), findAll(), delete(), etc.
}
```

### 4. Entity Layer (Modelo de Datos)
**Ubicación**: `com.f1manager.backend.entity`

**Responsabilidades**:
- Mapear tablas de BD a clases Java
- Definir relaciones entre entidades
- Validaciones a nivel de entidad
- Restricciones de BD

**Anotaciones Principales**:
- `@Entity` - Marca como entidad JPA
- `@Table` - Especifica tabla en BD
- `@Column` - Configura columna
- `@OneToMany`, `@ManyToOne`, `@OneToOne` - Relaciones
- `@GeneratedValue` - Auto-increment
- `@Min`, `@Max` - Validaciones

## Patrones Utilizados

### 1. Inyección de Dependencias
Spring Boot inyecta dependencias automáticamente:

```java
@Service
public class MiServicio {
    private final MiRepository repository;
    
    // Constructor injection (recomendado)
    public MiServicio(MiRepository repository) {
        this.repository = repository;
    }
}
```

### 2. Patrón Repository
Abstrae la lógica de acceso a datos:

```java
// En lugar de acceso directo a BD
User user = repository.findById(1);
```

### 3. Patrón DTO (Data Transfer Object)
*Pendiente implementar* - Usar DTOs para las respuestas API

```java
// En lugar de retornar la entidad completa
public PilotoDTO obtenerPorId(Integer id) {
    return pilotoMapper.toDTO(pilotoRepository.findById(id));
}
```

## Relaciones entre Entidades

### Escuderia ↔ Piloto
```
One-to-Many: 1 Escuderia → N Pilotos
@OneToMany(mappedBy = "escuderia")
private List<Piloto> pilotos;

@ManyToOne
@JoinColumn(name = "escuderia_id")
private Escuderia escuderia;
```

### Piloto ↔ Estadistica
```
One-to-One: 1 Piloto ↔ 1 Estadistica
@OneToOne
@JoinColumn(name = "estadistica_id")
private Estadistica estadistica;
```

### Piloto ↔ Circuito (Many-to-Many)
```
Many-to-Many a través de PilotoCircuito
@OneToMany(mappedBy = "piloto")
private List<PilotoCircuito> pilotoCircuitos;

Clave compuesta:
- circuito_id
- piloto_id
- temporada
```

## Flujo de una Solicitud HTTP

```
1. Cliente envía: GET /api/pilotos/1

2. Spring Router → PilotoController.obtenerPorId(1)

3. Controller llama → PilotoService.obtenerPorId(1)

4. Service llama → PilotoRepository.findById(1)

5. Repository → Hibernate genera SQL
   SELECT * FROM Piloto WHERE id = 1

6. Hibernate ejecuta query en MySQL

7. MySQL retorna resultado

8. Hibernate convierte a objeto Piloto

9. Repository retorna Optional<Piloto>

10. Service retorna Optional<Piloto>

11. Controller retorna ResponseEntity<Piloto>

12. Spring serializa a JSON y envía al cliente
```

## Ciclo de Vida de una Entidad

```
Estado: TRANSIENT (nueva, sin BD)
         ↓
Operación: repository.save(entity)
         ↓
Estado: MANAGED (en sesión de Hibernate)
         ↓
Spring commit automático en transacciones
         ↓
Estado: DETACHED (fuera de sesión)
         ↓
Acceso a propiedades lazy → LazyInitializationException
```

## Configuración Spring

### Escaneo de Componentes
`@SpringBootApplication` escanea automáticamente en:
- `@Controller`
- `@Service`
- `@Repository`
- `@Component`

### Inyección Automática
Spring busca en el contexto por tipo:
- Por tipo exacto
- Por nombre si hay múltiples implementaciones
- Por `@Qualifier`

## Validaciones

### A Nivel de Entidad (Jakarta Validation)
```java
@Min(1)
@Max(99)
private Integer valoracion;
```

### A Nivel de Controllers
```java
@PostMapping
public ResponseEntity<Piloto> crear(@Valid @RequestBody Piloto piloto) {
    // Si @Valid falla, retorna 400 Bad Request automáticamente
}
```

### A Nivel de Negocio (Programáticas)
```java
public void transferirPiloto(Integer pilotoId, Integer nuevoEquipoId) {
    if (piloto.esCampeon()) {
        throw new BusinessException("No se puede transferir al campeón");
    }
}
```

## Transacciones

Spring maneja automáticamente transacciones en métodos de servicios:

```java
@Service
@Transactional  // Una transacción por método
public class PilotoService {
    
    public void cambiarEquipo(Integer pilotoId, Integer equipoId) {
        // Todas estas operaciones son atómicas
        // Si una falla, se deshacen todas
        piloto.setEscuderia(equipo);
        piloto.setPuntos(0);
        escuderia.getPilotos().add(piloto);
    }
}
```

## Lazy Loading vs Eager Loading

```java
// LAZY (por defecto): Se carga solo cuando se accede
@ManyToOne(fetch = FetchType.LAZY)
private Escuderia escuderia;

// EAGER: Se carga inmediatamente con la entidad
@OneToMany(fetch = FetchType.EAGER, mappedBy = "escuderia")
private List<Piloto> pilotos;

// Cuidado: N+1 Query Problem
```

## Mejoras Futuras de Arquitectura

1. **Agregar DTOs**
   - Separar entidades de respuestas API
   - Mejor seguridad y control de qué exponer

2. **Excepciones Personalizadas**
   - `BusinessException`
   - `ResourceNotFoundException`
   - Manejador global con `@ExceptionHandler`

3. **Aspecto (AOP)**
   - Logging automático
   - Timing de operaciones
   - Caching

4. **Event-Driven**
   - Eventos cuando se crea un piloto
   - Listeners para operaciones asíncronas

5. **CQRS (Command Query Responsibility Segregation)**
   - Separar lecturas de escrituras
   - Optimizar queries

6. **Especificaciones JPA**
   - Filtros dinámicos
   - Búsquedas avanzadas

```java
// Ejemplo futuro con Specifications
List<Piloto> pilotos = pilotoRepository.findAll(
    Specification.where(tieneNombreLike("Max"))
        .and(tieneEdadMayorQue(20))
);
```

## Testing

### Tests Unitarios (Services)
```java
@ExtendWith(MockitoExtension.class)
class PilotoServiceTest {
    
    @Mock
    PilotoRepository repository;
    
    @InjectMocks
    PilotoService service;
    
    @Test
    void testObtenerPorId() {
        when(repository.findById(1)).thenReturn(Optional.of(piloto));
        Optional<Piloto> result = service.obtenerPorId(1);
        assertTrue(result.isPresent());
    }
}
```

### Tests de Integración (Controllers)
```java
@SpringBootTest
@AutoConfigureMockMvc
class PilotoControllerTest {
    
    @Autowired
    MockMvc mockMvc;
    
    @Test
    void testObtenerTodos() throws Exception {
        mockMvc.perform(get("/api/pilotos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }
}
```

## Referencia de Anotaciones Clave

| Anotación | Ubicación | Propósito |
|-----------|-----------|----------|
| `@SpringBootApplication` | Main class | Activa autoconfiguracion |
| `@RestController` | Controller | Define controlador REST |
| `@RequestMapping` | Controller/Method | Mapea rutas |
| `@Service` | Service | Define servicio |
| `@Repository` | Interface | Define repositorio |
| `@Entity` | Class | Mapea tabla BD |
| `@Table` | Class | Especifica tabla |
| `@Column` | Field | Especifica columna |
| `@Id` | Field | PK |
| `@GeneratedValue` | Field | Auto-increment |
| `@ManyToOne` | Field | Relación M-1 |
| `@OneToMany` | Field | Relación 1-M |
| `@JoinColumn` | Field | FK |
| `@Transactional` | Method/Class | Transacción |
| `@Valid` | Parameter | Valida objeto |
