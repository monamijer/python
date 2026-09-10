// SerieRepository.java
package com.monprojet.series.repository;

import com.monprojet.series.entity.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {

    // Used by the TMDB import flow to avoid creating duplicates (RG7)
    Optional<Serie> findByTmdbId(Long tmdbId);
}