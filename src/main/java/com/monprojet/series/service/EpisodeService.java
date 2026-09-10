// EpisodeService.java
package com.monprojet.series.service;

import com.monprojet.series.entity.Episode;
import com.monprojet.series.entity.Saison;
import com.monprojet.series.exception.ResourceNotFoundException;
import com.monprojet.series.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final SaisonService saisonService;

    @Transactional(readOnly = true)
    public List<Episode> listerParSaison(Long saisonId) {
        saisonService.obtenirParId(saisonId);
        return episodeRepository.findBySaisonIdOrderByNumeroAsc(saisonId);
    }

    @Transactional(readOnly = true)
    public Episode obtenirParId(Long id) {
        return episodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Épisode introuvable : id=" + id));
    }

    public Episode creer(Long saisonId, Episode episode) {
        Saison saison = saisonService.obtenirParId(saisonId);
        episode.setSaison(saison);
        return episodeRepository.save(episode);
    }
}