// EpisodeRepository.java
package com.monprojet.series.repository;

import com.monprojet.series.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    List<Episode> findBySaisonIdOrderByNumeroAsc(Long saisonId);

    // All episodes of a series, across every season — needed for progress
    // calculation (RG3-RG5) without loading the Serie/Saison graph.
    List<Episode> findBySaison_Serie_IdOrderBySaison_NumeroAscNumeroAsc(Long serieId);

    long countBySaison_Serie_Id(Long serieId);

    long countBySaisonId(Long saisonId);

    @Query("""
    SELECT COALESCE(SUM(e.dureeMinutes), 0)
    FROM Episode e
    WHERE e.saison.serie.id = :serieId
    """)
long sommeDureeTotaleParSerie(@Param("serieId") Long serieId);
}