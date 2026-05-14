package com.f1manager.backend.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f1manager.backend.dto.NegociacionRequestDTO;
import com.f1manager.backend.dto.NegociacionResponseDTO;
import com.f1manager.backend.dto.TraspasoDTO;
import com.f1manager.backend.dto.PilotoMinDTO;
import com.f1manager.backend.dto.EscuderiaMinDTO;
import com.f1manager.backend.dto.TraspasoRequestDTO;
import com.f1manager.backend.entity.Traspaso;
import com.f1manager.backend.entity.Piloto;
import com.f1manager.backend.entity.Escuderia;
import com.f1manager.backend.entity.Partida;
import com.f1manager.backend.repository.TraspasoRepository;
import com.f1manager.backend.repository.PartidaRepository;
import com.f1manager.backend.repository.PilotoRepository;
import com.f1manager.backend.repository.EscuderiaRepository;

@Service
public class TraspasoService {

    private final TraspasoRepository traspasoRepository;
    private final PartidaRepository partidaRepository;
    private final PilotoRepository pilotoRepository;
    private final EscuderiaRepository escuderiaRepository;

    // =========================================================================
    // CACHÉ EN MEMORIA: Bloqueo por GP (no persiste en BD — decisión de diseño)
    // Map<partidaId, Set<pilotoId>>: indica qué pilotos han rechazado negociar en el GP actual.
    // Se limpia al avanzar de carrera en la partida correspondiente.
    // =========================================================================
    private final Map<Integer, Set<Integer>> bloqueosNegociacion = new ConcurrentHashMap<>();

    public TraspasoService(TraspasoRepository traspasoRepository,
                           PartidaRepository partidaRepository,
                           PilotoRepository pilotoRepository,
                           EscuderiaRepository escuderiaRepository) {
        this.traspasoRepository = traspasoRepository;
        this.partidaRepository = partidaRepository;
        this.pilotoRepository = pilotoRepository;
        this.escuderiaRepository = escuderiaRepository;
    }

    // =========================================================================
    // CRUD BÁSICO
    // =========================================================================

    public List<TraspasoDTO> obtenerTodos() {
        return traspasoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TraspasoDTO> obtenerPorPartida(Integer partidaId) {
        return traspasoRepository.findByPartidaId(partidaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TraspasoDTO> obtenerActivosPorPartida(Integer partidaId) {
        return traspasoRepository.findByPartidaIdAndEnCursoTrue(partidaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TraspasoDTO> obtenerActivosPorPiloto(Integer pilotoId) {
        return traspasoRepository.findByPilotoIdAndEnCursoTrue(pilotoId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<TraspasoDTO> obtenerPorId(Integer id) {
        return traspasoRepository.findById(id).map(this::toDTO);
    }

    public Traspaso guardar(Traspaso traspaso) {
        return traspasoRepository.save(traspaso);
    }

    public TraspasoDTO crearDesdeRequest(TraspasoRequestDTO request) {
        Traspaso traspaso = new Traspaso();

        Partida partida = partidaRepository.findById(request.getPartida())
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));
        Piloto piloto = pilotoRepository.findById(request.getPiloto())
                .orElseThrow(() -> new RuntimeException("Piloto no encontrado"));

        if (!piloto.getPartida().getId().equals(partida.getId())) {
            throw new RuntimeException("El piloto no pertenece a la partida indicada");
        }

        traspaso.setPartida(partida);
        traspaso.setPiloto(piloto);
        traspaso.setEscuderiaOrigen(piloto.getEscuderia());

        if (request.getEscuderiaDestino() != null) {
            Escuderia destino = escuderiaRepository.findById(request.getEscuderiaDestino()).orElse(null);
            if (destino != null) {
                if (!destino.getPartida().getId().equals(partida.getId())) {
                    throw new RuntimeException("La escudería destino no pertenece a la misma partida");
                }
                if (piloto.getEscuderia() != null && destino.getId().equals(piloto.getEscuderia().getId())) {
                    throw new RuntimeException("El piloto ya pertenece a la escudería de destino");
                }
            }
            traspaso.setEscuderiaDestino(destino);
        }

        traspaso.setPrecio(request.getPrecio());
        traspaso.setTemporada(request.getTemporada());
        traspaso.setEnCurso(request.getEnCurso() != null ? request.getEnCurso() : true);
        traspaso.setAceptada(request.getAceptada() != null ? request.getAceptada() : false);

        return toDTO(traspasoRepository.save(traspaso));
    }

    public void eliminar(Integer id) {
        traspasoRepository.deleteById(id);
    }

    // =========================================================================
    // GESTIÓN DE BLOQUEOS POR PARTIDA
    // =========================================================================

    public Set<Integer> obtenerPilotosBloqueados(Integer partidaId) {
        return bloqueosNegociacion.getOrDefault(partidaId, Collections.emptySet());
    }

    public void limpiarBloqueos(Integer partidaId) {
        bloqueosNegociacion.remove(partidaId);
    }

    // =========================================================================
    // ACEPTAR / RECHAZAR (reutilizados desde negociarTraspaso y expuestos en el Controller)
    // =========================================================================

    @Transactional
    public TraspasoDTO aceptarTraspaso(Integer id) {
        Traspaso traspaso = traspasoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traspaso no encontrado"));

        if (!traspaso.getEnCurso()) {
            throw new RuntimeException("El traspaso ya no está en curso");
        }

        Piloto piloto = traspaso.getPiloto();
        Escuderia origen = traspaso.getEscuderiaOrigen();
        Escuderia destino = traspaso.getEscuderiaDestino();

        if (origen != null && (piloto.getEscuderia() == null
                || !piloto.getEscuderia().getId().equals(origen.getId()))) {
            throw new RuntimeException("El piloto ya no pertenece a la escudería de origen");
        }

        if (origen != null && origen.getPilotos().size() <= 2) {
            throw new RuntimeException("La escudería de origen no puede quedarse con menos de 2 pilotos");
        }

        if (destino.getPresupuesto().compareTo(traspaso.getPrecio()) < 0) {
            throw new RuntimeException("La escudería de destino no tiene suficiente presupuesto");
        }

        // Transferencia de dinero
        destino.setPresupuesto(destino.getPresupuesto().subtract(traspaso.getPrecio()));
        if (origen != null) {
            origen.setPresupuesto(origen.getPresupuesto().add(traspaso.getPrecio()));
        }

        // Cambio de escudería del piloto
        piloto.setEscuderia(destino);

        traspaso.setEnCurso(false);
        traspaso.setAceptada(true);

        escuderiaRepository.save(destino);
        if (origen != null) {
            escuderiaRepository.save(origen);
        }
        pilotoRepository.save(piloto);

        return toDTO(traspasoRepository.save(traspaso));
    }

    @Transactional
    public TraspasoDTO rechazarTraspaso(Integer id) {
        Traspaso traspaso = traspasoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traspaso no encontrado"));

        if (!traspaso.getEnCurso()) {
            throw new RuntimeException("El traspaso ya no está en curso");
        }

        traspaso.setEnCurso(false);
        traspaso.setAceptada(false);

        return toDTO(traspasoRepository.save(traspaso));
    }

    // =========================================================================
    // LÓGICA IA: Sistema de puntuación para decidir si el equipo acepta la oferta
    //
    // PILAR 1 (hasta +50 pts): Beneficio económico — ratio oferta/valor del piloto
    // PILAR 2 (hasta +30 pts): Necesidad del equipo — si tienen poco presupuesto, venden más fácil
    // PILAR 3 (hasta -25 pts): Importancia del piloto — si es su estrella, el equipo lo retiene
    //
    // Umbral de aceptación: score >= 40
    // Ajustar los valores numéricos para balancear durante las pruebas
    // =========================================================================
    private boolean evaluarDecisionEquipo(Piloto piloto, Escuderia origen, BigDecimal oferta) {
        double score = 0;

        // PILAR 1: Beneficio económico
        if (piloto.getValor() != null && piloto.getValor().compareTo(BigDecimal.ZERO) > 0) {
            double ratio = oferta.doubleValue() / piloto.getValor().doubleValue();
            if (ratio >= 1.5) {
                score += 50;   // Oferta irrechazable: pagan un 50% más del valor
            } else if (ratio >= 1.2) {
                score += 35;   // Buena oferta
            } else if (ratio >= 1.0) {
                score += 20;   // Oferta justa, el equipo lo considera
            } else {
                score -= 30;   // Oferta por debajo del valor: penalización fuerte
            }
        } else {
            // Sin valor definido: comparar contra 5M de referencia base
            double ratioBase = oferta.doubleValue() / 5_000_000.0;
            score += Math.min(ratioBase * 20, 40);
        }

        // PILAR 2: Necesidad económica del equipo de origen
        if (origen != null && origen.getPresupuesto() != null) {
            double presupuesto = origen.getPresupuesto().doubleValue();
            if (presupuesto < 10_000_000) {
                score += 30;   // Equipo en quiebra: vende lo que sea
            } else if (presupuesto < 50_000_000) {
                score += 20;   // Equipo con dificultades financieras
            } else if (presupuesto < 120_000_000) {
                score += 5;    // Presupuesto normal
            }
            // Equipo rico (>120M): no necesita el dinero, no suma
        }

        // PILAR 3: Importancia del piloto en su equipo actual
        if (piloto.getPuntos() != null) {
            int puntos = piloto.getEstadistica().getValoracion();
            if (puntos > 90) {
                score -= 25;   // Estrella indiscutible
            } else if (puntos > 82) {
                score -= 15;   // Piloto importante
            } else if (puntos > 75) {
                score -= 5;    // Rendimiento medio
            }
            // Pocos puntos: no penaliza, piloto prescindible
        }

        // PILAR 4: Estado del coche y las instalaciones
        if (origen != null) {
            int nivelCoche = (origen.getAerodinamica()) 
                           + (origen.getMotor())
                           + (origen.getDurabilidad())
                           ;
            int nivelInstalaciones = (origen.getTunelViento())
                                   + (origen.getBancoPruebas())
                                   + (origen.getEscuelaPilotos());
            
            // Coche máximo = 200, Instalaciones máximo = 15
            if (nivelCoche < 150 || nivelInstalaciones < 8) {
                score += 15; // Coche/Instalaciones muy pobres: venden para reinvertir
            } else if (nivelCoche > 210 && nivelInstalaciones > 12) {
                score -= 15; // Coche top e instalaciones top: prioridad deportiva, no económica
            }
        }

        return score >= 40;
    }

    // =========================================================================
    // ENDPOINT PRINCIPAL: "Soccer Champs" — Acción-Reacción inmediata
    // Crea la oferta, decide al instante y cierra el ciclo en una sola transacción
    // =========================================================================
    @Transactional
    public NegociacionResponseDTO negociarTraspaso(NegociacionRequestDTO request) {

        // 1. Cargar entidades
        Partida partida = partidaRepository.findById(request.getPartidaId())
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));
        Piloto piloto = pilotoRepository.findById(request.getPilotoId())
                .orElseThrow(() -> new RuntimeException("Piloto no encontrado"));
        Escuderia destino = escuderiaRepository.findById(request.getEscuderiaDestinoId())
                .orElseThrow(() -> new RuntimeException("Escudería de destino no encontrada"));

        // 2. Validaciones básicas
        if (!piloto.getPartida().getId().equals(partida.getId())) {
            throw new RuntimeException("El piloto no pertenece a esta partida");
        }
        if (!destino.getPartida().getId().equals(partida.getId())) {
            throw new RuntimeException("La escudería destino no pertenece a esta partida");
        }
        if (piloto.getEscuderia() != null && piloto.getEscuderia().getId().equals(destino.getId())) {
            throw new RuntimeException("El piloto ya pertenece a la escudería de destino");
        }
        if (request.getPrecio() == null || request.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio de la oferta debe ser mayor que cero");
        }

        // 3. Comprobar bloqueo por GP (caché en memoria por partidaId)
        Set<Integer> bloqueados = obtenerPilotosBloqueados(partida.getId());
        if (bloqueados.contains(piloto.getId())) {
            throw new RuntimeException(
                "Este piloto ya rechazó negociar en este Gran Premio. Inténtalo en el siguiente GP."
            );
        }

        // 4. Validar presupuesto de la escudería del jugador (destino)
        if (destino.getPresupuesto().compareTo(request.getPrecio()) < 0) {
            throw new RuntimeException("Tu escudería no tiene presupuesto suficiente para esta oferta");
        }

        Escuderia origen = piloto.getEscuderia();

        // 5. Crear el registro del traspaso con en_curso = true
        Traspaso traspaso = new Traspaso();
        traspaso.setPartida(partida);
        traspaso.setPiloto(piloto);
        traspaso.setEscuderiaOrigen(origen);
        traspaso.setEscuderiaDestino(destino);
        traspaso.setPrecio(request.getPrecio());
        traspaso.setTemporada(partida.getAnio());
        traspaso.setEnCurso(true);
        traspaso.setAceptada(false);
        Traspaso traspasoGuardado = traspasoRepository.save(traspaso);

        // 6. La IA decide: ¿acepta o rechaza el equipo de origen?
        boolean acepta = evaluarDecisionEquipo(piloto, origen, request.getPrecio());

        // 7. Ejecutar consecuencias reutilizando los métodos existentes
        if (acepta) {
            TraspasoDTO traspasoResultado = aceptarTraspaso(traspasoGuardado.getId());
            Escuderia destinoActualizado = escuderiaRepository.findById(destino.getId()).orElse(destino);

            return new NegociacionResponseDTO(
                NegociacionResponseDTO.Resultado.ACEPTADO,
                "¡Trato hecho! " + piloto.getNombre() + " se une a tu escudería.",
                traspasoResultado,
                destinoActualizado.getPresupuesto()
            );
        } else {
            // Registrar el intento fallido en el caché de la partida
            bloqueosNegociacion.computeIfAbsent(partida.getId(), k -> ConcurrentHashMap.newKeySet())
                               .add(piloto.getId());

            TraspasoDTO traspasoResultado = rechazarTraspaso(traspasoGuardado.getId());

            return new NegociacionResponseDTO(
                NegociacionResponseDTO.Resultado.RECHAZADO,
                piloto.getNombre() + " ha rechazado la oferta. No escuchará más propuestas hasta el próximo GP.",
                traspasoResultado,
                destino.getPresupuesto()
            );
        }
    }

    // =========================================================================
    // MAPEO DTO
    // =========================================================================

    public TraspasoDTO toDTO(Traspaso traspaso) {
        TraspasoDTO dto = new TraspasoDTO();
        dto.setId(traspaso.getId());
        dto.setPartidaId(traspaso.getPartida().getId());
        dto.setPrecio(traspaso.getPrecio());
        dto.setTemporada(traspaso.getTemporada());
        dto.setAceptada(traspaso.getAceptada());
        dto.setEnCurso(traspaso.getEnCurso());

        if (traspaso.getPiloto() != null) {
            dto.setPiloto(toPilotoMinDTO(traspaso.getPiloto()));
        }
        if (traspaso.getEscuderiaOrigen() != null) {
            dto.setEscuderiaOrigen(toEscuderiaMinDTO(traspaso.getEscuderiaOrigen()));
        }
        if (traspaso.getEscuderiaDestino() != null) {
            dto.setEscuderiaDestino(toEscuderiaMinDTO(traspaso.getEscuderiaDestino()));
        }

        return dto;
    }

    private PilotoMinDTO toPilotoMinDTO(Piloto piloto) {
        PilotoMinDTO dto = new PilotoMinDTO();
        dto.setId(piloto.getId());
        dto.setNombre(piloto.getNombre());
        dto.setPais(piloto.getPais());
        dto.setImagen(piloto.getImagen());
        dto.setEdad(piloto.getEdad());
        return dto;
    }

    private EscuderiaMinDTO toEscuderiaMinDTO(Escuderia escuderia) {
        EscuderiaMinDTO dto = new EscuderiaMinDTO();
        dto.setId(escuderia.getId());
        dto.setNombre(escuderia.getNombre());
        dto.setImagen(escuderia.getImagen());
        return dto;
    }

    public List<TraspasoDTO> obtenerAceptadasPorPartida(Integer partidaId) {
        return traspasoRepository.findByPartidaIdAndAceptadaTrue(partidaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
    