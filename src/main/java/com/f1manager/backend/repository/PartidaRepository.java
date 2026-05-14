package com.f1manager.backend.repository;

import com.f1manager.backend.entity.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Integer> {
    
    @Modifying
    @Query(value = "DELETE FROM partida WHERE id = :id", nativeQuery = true)
    void deletePartidaById(@Param("id") Integer id);
}
