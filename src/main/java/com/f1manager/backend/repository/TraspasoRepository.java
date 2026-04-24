package com.f1manager.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.f1manager.backend.entity.Traspaso;
import java.util.List;

@Repository
public interface TraspasoRepository extends JpaRepository<Traspaso, Integer> {
    List<Traspaso> findByPartidaId(Integer partidaId);
    List<Traspaso> findByPilotoId(Integer pilotoId);
    List<Traspaso> findByEnCursoTrue();
    List<Traspaso> findByPartidaIdAndEnCursoTrue(Integer partidaId);
}
