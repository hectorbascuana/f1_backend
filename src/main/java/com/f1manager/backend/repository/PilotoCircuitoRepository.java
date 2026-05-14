package com.f1manager.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.f1manager.backend.entity.PilotoCircuito;
import com.f1manager.backend.entity.PilotoCircuitoId;

import java.util.List;

@Repository
public interface PilotoCircuitoRepository extends JpaRepository<PilotoCircuito, PilotoCircuitoId> {
    List<PilotoCircuito> findByIdPartidaId(Integer partidaId);

    @Query("SELECT pc FROM PilotoCircuito pc WHERE pc.id.partidaId = :partidaId AND pc.id.temporada = :temporada AND pc.id.circuitoId = :circuitoId ORDER BY CASE WHEN pc.posicion IS NULL THEN 1 ELSE 0 END ASC, pc.posicion ASC")
    List<PilotoCircuito> findByPartidaYearAndCircuitCustomOrder(
            @Param("partidaId") Integer partidaId,
            @Param("temporada") Integer temporada,
            @Param("circuitoId") Integer circuitoId);
}
