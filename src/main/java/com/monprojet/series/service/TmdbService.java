// TmdbService.java
package com.monprojet.series.service;

import com.monprojet.series.dto.response.TmdbSerieResponse;
import com.monprojet.series.dto.tmdb.*;
import com.monprojet.series.entity.Episode;
import com.monprojet.series.entity.Saison;
import com.monprojet.series.entity.Serie;
import com.monprojet.series.entity.Utilisateur;
import com.monprojet.series.exception.BusinessException;
import com.monprojet.series.exception.ResourceNotFoundException;
import com.monprojet.series.exception.TmdbIndisponibleException;
import com.monprojet.series.repository.EpisodeRepository;
import com.monprojet.series.repository.SaisonRepository;
import com.monprojet.series.repository.SerieRepository;
import com.monprojet.series.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TmdbService {

    private final RestClient tmdbRestClient;
    private final SerieRepository serieRepository;
    private final SaisonRepository saisonRepository;
    private final EpisodeRepository episodeRepository;

    @Value("${tmdb.api.key}")
    private String apiKey;

    public List<TmdbSerieResponse> rechercherParTitre(String titre) {
        TmdbPageResponse page = appeler(uri -> uri.path("/search/tv")
                .queryParam("api_key", apiKey)
                .queryParam("language", "fr-FR")
                .queryParam("query", titre)
                .build(), TmdbPageResponse.class);
        return versReponses(page);
    }

    public List<TmdbSerieResponse> listerPopulaires() {
        TmdbPageResponse page = appeler(uri -> uri.path("/tv/popular")
                .queryParam("api_key", apiKey)
                .queryParam("language", "fr-FR")
                .build(), TmdbPageResponse.class);
        return versReponses(page);
    }

    public List<TmdbSerieResponse> listerSimilaires(Long tmdbId) {
        TmdbPageResponse page = appeler(uri -> uri.path("/tv/{id}/similar")
                .queryParam("api_key", apiKey)
                .queryParam("language", "fr-FR")
                .build(tmdbId), TmdbPageResponse.class);
        return versReponses(page);
    }

    /** RG7: refuses to import a series already present in the local database. */
    @Transactional
    public Serie importerSerie(Long tmdbId) {
        if (serieRepository.findByTmdbId(tmdbId).isPresent()) {
            throw new BusinessException("Cette série TMDB a déjà été importée (tmdbId=" + tmdbId + ").");
        }

        TmdbSerieDetailDto detail = appeler(uri -> uri.path("/tv/{id}")
                .queryParam("api_key", apiKey)
                .queryParam("language", "fr-FR")
                .build(tmdbId), TmdbSerieDetailDto.class);

        Serie serie = Serie.builder()
                .titre(detail.name())
                .description(detail.overview())
                .anneeSortie(extraireAnnee(detail.firstAirDate()))
                .note(detail.voteAverage())
                .imageUrl(construireUrlImage(detail.posterPath()))
                .tmdbId(detail.id())
                .build();
        serie = serieRepository.save(serie);

        for (TmdbSeasonDto saisonDto : detail.seasons()) {
            // Season 0 on TMDB is "Specials" — skipped to keep the import aligned
            // with the season numbering the rest of the app expects (RG2, RG3).
            if (saisonDto.numeroSaison() == 0) continue;
            importerSaison(serie, tmdbId, saisonDto);
        }

        return serie;
    }

    private void importerSaison(Serie serie, Long tmdbId, TmdbSeasonDto saisonDto) {
        Saison saison = Saison.builder()
                .numero(saisonDto.numeroSaison())
                .nbEpisodes(saisonDto.nombreEpisodes())
                .serie(serie)
                .build();
        saison = saisonRepository.save(saison);

        TmdbSeasonDetailDto saisonDetail = appeler(uri -> uri
                .path("/tv/{id}/season/{numero}")
                .queryParam("api_key", apiKey)
                .queryParam("language", "fr-FR")
                .build(tmdbId, saisonDto.numeroSaison()), TmdbSeasonDetailDto.class);

        Saison saisonFinale = saison;
        List<Episode> episodes = saisonDetail.episodes().stream()
                .map(ep -> Episode.builder()
                        .numero(ep.numeroEpisode())
                        .titre(ep.name())
                        .dureeMinutes(ep.runtime())
                        .saison(saisonFinale)
                        .build())
                .toList();
        episodeRepository.saveAll(episodes);
    }

    private List<TmdbSerieResponse> versReponses(TmdbPageResponse page) {
        return page.results().stream()
                .map(dto -> new TmdbSerieResponse(
                        dto.id(), dto.name(), dto.overview(),
                        dto.firstAirDate(), dto.voteAverage(), construireUrlImage(dto.posterPath())
                ))
                .toList();
    }

    private String construireUrlImage(String posterPath) {
        return posterPath == null ? null : "https://image.tmdb.org/t/p/w342" + posterPath;
    }

    private Integer extraireAnnee(String firstAirDate) {
        if (firstAirDate == null || firstAirDate.length() < 4) return null;
        return Integer.parseInt(firstAirDate.substring(0, 4));
    }

    /**
     * Single choke point for every TMDB call: turns a 404 into our own
     * ResourceNotFoundException and any other failure (network, 5xx, timeout)
     * into TmdbIndisponibleException — callers never touch RestClient directly.
     */
    private <T> T appeler(java.util.function.Function<UriBuilder, java.net.URI> uriFn, Class<T> type) {
        try {
            return tmdbRestClient.get()
                    .uri(uriFn::apply)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Série TMDB introuvable.");
            }
            throw new TmdbIndisponibleException("TMDB a renvoyé une erreur (" + ex.getStatusCode() + ").", ex);
        } catch (RestClientException ex) {
            throw new TmdbIndisponibleException("Impossible de contacter TMDB.", ex);
        }
    }

    // TmdbService.java — updated importerSerie signature and body (only the changed parts)

private final UtilisateurRepository utilisateurRepository; // add this field via @RequiredArgsConstructor

@Transactional
public Serie importerSerie(Long utilisateurId, Long tmdbId) {
    // RG7 is now scoped per user: the same TMDB series can be imported
    // independently by different users, but not twice by the same one.
    if (serieRepository.findByTmdbIdAndUtilisateurId(tmdbId, utilisateurId).isPresent()) {
        throw new BusinessException("Cette série TMDB a déjà été importée (tmdbId=" + tmdbId + ").");
    }

    Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : id=" + utilisateurId));

    TmdbSerieDetailDto detail = appeler(uri -> uri.path("/tv/{id}")
            .queryParam("api_key", apiKey)
            .queryParam("language", "fr-FR")
            .build(tmdbId), TmdbSerieDetailDto.class);

    Serie serie = Serie.builder()
            .titre(detail.name())
            .description(detail.overview())
            .anneeSortie(extraireAnnee(detail.firstAirDate()))
            .note(detail.voteAverage())
            .imageUrl(construireUrlImage(detail.posterPath()))
            .tmdbId(detail.id())
            .utilisateur(utilisateur)
            .build();
    serie = serieRepository.save(serie);

    for (TmdbSeasonDto saisonDto : detail.seasons()) {
        if (saisonDto.numeroSaison() == 0) continue;
        importerSaison(serie, tmdbId, saisonDto);
    }

    return serie;
}
}