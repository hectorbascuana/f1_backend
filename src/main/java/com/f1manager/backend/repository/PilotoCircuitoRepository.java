package com.f1manager.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.f1manager.backend.entity.PilotoCircuito;
import com.f1manager.backend.entity.PilotoCircuitoId;

@Repository
public interface PilotoCircuitoRepository extends JpaRepository<PilotoCircuito, PilotoCircuitoId> {
}
