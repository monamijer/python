// VisionnageRepository.java
package com.monprojet.series.repository;

import com.monprojet.series.entity.Visionnage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisionnageRepository extends JpaRepository<Visionnage, Long> {

    // RG1: check before marking an episode as watched
    boolean existsByUtilisateur_IdAndEpisode_Id(Long utilisateurId, Long episodeId);

    // Used to delete/cancel a watch record (unmark episode)
    Optional<Visionnage> findByUtilisateur_IdAndEpisode_Id(Long utilisateurId, Long episodeId);

    // Number of watched episodes for a series (RG3)
    long countByUtilisateur_IdAndEpisode_Saison_Serie_Id(Long utilisateurId, Long serieId);

    // All watch records for a user on a given series, ordered — used to detect
    // "last episode of the season" (RG2) and to sum watched durations (RG4)
    List<Visionnage> findByUtilisateur_IdAndEpisode_Saison_Serie_Id(Long utilisateurId, Long serieId);

    @Query("""
        SELECT COALESCE(SUM(e.dureeMinutes), 0)
        FROM Visionnage v JOIN v.episode e
        WHERE v.utilisateur.id = :utilisateurId
        AND e.saison.serie.id = :serieId
        """)
    long sommeDureeVisionneeParSerie(
            @Param("utilisateurId") Long utilisateurId,
            @Param("serieId") Long serieId
    );

    // How many episodes of a specific season the user has already watched —
    // used to detect that a season is fully watched (RG2)
    long countByUtilisateur_IdAndEpisode_SaisonId(Long utilisateurId, Long saisonId);
}