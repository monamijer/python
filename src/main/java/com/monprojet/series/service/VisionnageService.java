// VisionnageService.java
package com.monprojet.series.service;

import com.monprojet.series.dto.response.MarquerVisionnageResponse;
import com.monprojet.series.dto.response.ProgressionResponse;
import com.monprojet.series.dto.response.ProgressionResponse.StatutProgression;
import com.monprojet.series.entity.Episode;
import com.monprojet.series.entity.Saison;
import com.monprojet.series.entity.Utilisateur;
import com.monprojet.series.entity.Visionnage;
import com.monprojet.series.exception.BusinessException;
import com.monprojet.series.exception.ResourceNotFoundException;
import com.monprojet.series.repository.EpisodeRepository;
import com.monprojet.series.repository.SaisonRepository;
import com.monprojet.series.repository.VisionnageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VisionnageService {

    private final VisionnageRepository visionnageRepository;
    private final EpisodeRepository episodeRepository;
    private final SaisonRepository saisonRepository;
    private final UtilisateurService utilisateurService;
    private final EpisodeService episodeService;

    /**
     * RG1: refuses a duplicate watch record.
     * RG2: detects season completion and resolves the next season, if any.
     */
    public MarquerVisionnageResponse marquerCommeVu(Long utilisateurId, Long episodeId) {
        Utilisateur utilisateur = utilisateurService.obtenirParId(utilisateurId);
        Episode episode = episodeService.obtenirParId(episodeId);

        if (visionnageRepository.existsByUtilisateur_IdAndEpisode_Id(utilisateurId, episodeId)) {
            throw new BusinessException("Cet épisode a déjà été marqué comme vu par cet utilisateur.");
        }

        Visionnage visionnage = Visionnage.builder()
                .utilisateur(utilisateur)
                .episode(episode)
                .dateVisionnage(LocalDateTime.now())
                .build();
        visionnageRepository.save(visionnage);

        Saison saison = episode.getSaison();
        long episodesVusSaison = visionnageRepository
                .countByUtilisateur_IdAndEpisode_SaisonId(utilisateurId, saison.getId());
        long episodesTotalSaison = episodeRepository.countBySaisonId(saison.getId());

        boolean saisonTerminee = episodesVusSaison >= episodesTotalSaison;
        Integer prochaineSaisonNumero = null;

        if (saisonTerminee) {
            prochaineSaisonNumero = saisonRepository
                    .findBySerieIdAndNumero(saison.getSerie().getId(), saison.getNumero() + 1)
                    .map(Saison::getNumero)
                    .orElse(null);
        }

        return new MarquerVisionnageResponse(episodeId, saisonTerminee, prochaineSaisonNumero);
    }

    public void annulerVisionnage(Long utilisateurId, Long episodeId) {
        Visionnage visionnage = visionnageRepository
                .findByUtilisateur_IdAndEpisode_Id(utilisateurId, episodeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun visionnage trouvé pour cet utilisateur et cet épisode."));
        visionnageRepository.delete(visionnage);
    }

    /** RG3, RG4, RG5, RG6 combined for a single series. */
    @Transactional(readOnly = true)
    public ProgressionResponse calculerProgression(Long utilisateurId, Long serieId) {
        utilisateurService.obtenirParId(utilisateurId);

        long episodesVus = visionnageRepository
                .countByUtilisateur_IdAndEpisode_Saison_Serie_Id(utilisateurId, serieId);
        long episodesTotal = episodeRepository.countBySaison_Serie_Id(serieId);

        // Pas d'épisodes enregistrés → réponse neutre au lieu d'un 404.
        if (episodesTotal == 0) {
            return new ProgressionResponse(
                    serieId, "", 0, 0, 0.0, 0L, 0L, StatutProgression.A_COMMENCER
            );
        }

        double pourcentage = (episodesVus * 100.0) / episodesTotal;
        long tempsVisionne = visionnageRepository.sommeDureeVisionneeParSerie(utilisateurId, serieId);
        long tempsTotal = episodeRepository.sommeDureeTotaleParSerie(serieId);
        long tempsRestant = tempsTotal - tempsVisionne;

        StatutProgression statut = resoudreStatut(pourcentage);

        String titre = episodeRepository.findBySaison_Serie_IdOrderBySaison_NumeroAscNumeroAsc(serieId)
                .stream().findFirst()
                .map(e -> e.getSaison().getSerie().getTitre())
                .orElse("");

        return new ProgressionResponse(
                serieId, titre, episodesVus, episodesTotal,
                Math.round(pourcentage * 10) / 10.0,
                tempsVisionne, tempsRestant, statut
        );
    }

    /** RG6: classifies every series the catalog holds by watch status for this user. */
    @Transactional(readOnly = true)
    public List<ProgressionResponse> listerParStatut(Long utilisateurId, StatutProgression statut) {
        return episodeRepository.findAll().stream()
                .map(e -> e.getSaison().getSerie().getId())
                .distinct()
                .map(serieId -> calculerProgression(utilisateurId, serieId))
                .filter(p -> p.statut() == statut)
                .toList();
    }

    private StatutProgression resoudreStatut(double pourcentage) {
        if (pourcentage <= 0.0) return StatutProgression.A_COMMENCER;
        if (pourcentage >= 100.0) return StatutProgression.TERMINEE;
        return StatutProgression.EN_COURS;
    }
}