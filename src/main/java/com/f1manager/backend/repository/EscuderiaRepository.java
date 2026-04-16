package com.f1manager.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.f1manager.backend.entity.Escuderia;

import java.util.List;

@Repository
public interface EscuderiaRepository extends JpaRepository<Escuderia, Integer> {
    List<Escuderia> findByPartidaId(Integer partidaId);
}
