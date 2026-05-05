package com.f1manager.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.f1manager.backend.entity.Piloto;

import java.util.List;

@Repository
public interface PilotoRepository extends JpaRepository<Piloto, Integer> {
    List<Piloto> findByPartidaId(Integer partidaId);
    List<Piloto> findByPartidaIdOrderByEstadisticaValoracionDesc(Integer partidaId);
    List<Piloto> findByEscuderiaId(Integer escuderiaId);
    List<Piloto> findByPartidaIdAndEscuderiaId(Integer partidaId, Integer escuderiaId);
}