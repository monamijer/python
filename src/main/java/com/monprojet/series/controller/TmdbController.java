// TmdbController.java
package com.monprojet.series.controller;

import com.monprojet.series.dto.response.*;
import com.monprojet.series.mapper.SerieMapper;
import com.monprojet.series.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tmdb")
@RequiredArgsConstructor
public class TmdbController {

    private final TmdbService tmdbService;

    @GetMapping("/recherche")
    public List<TmdbSerieResponse> rechercher(
            @RequestParam String titre,
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) String langue) {
        return tmdbService.rechercherParTitre(titre, annee, langue);
    }

    @GetMapping("/populaires")
    public List<TmdbSerieResponse> populaires(@RequestParam(defaultValue = "1") int page) {
        return tmdbService.listerPopulaires(page);
    }

    @GetMapping("/tendances")
    public List<TmdbSerieResponse> tendances() {
        return tmdbService.listerTendances();
    }

    @GetMapping("/mieux-notees")
    public List<TmdbSerieResponse> mieuxNotees() {
        return tmdbService.listerMieuxNotees();
    }

    @GetMapping("/diffusees-bientot")
    public List<TmdbSerieResponse> diffuseesBientot() {
        return tmdbService.listerDiffuseesBientot();
    }

    @GetMapping("/genres")
    public List<GenreResponse> genres() {
        return tmdbService.listerGenres();
    }

    @GetMapping("/decouvrir")
    public List<TmdbSerieResponse> decouvrir(
            @RequestParam(required = false) Long genre,
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) Double noteMin) {
        return tmdbService.decouvrir(genre, annee, noteMin);
    }

    @GetMapping("/serie/{tmdbId}")
    public TmdbSerieResponse detailSerie(@PathVariable Long tmdbId) {
        return tmdbService.obtenirDetail(tmdbId);
    }

    @GetMapping("/serie/{tmdbId}/similaires")
    public List<TmdbSerieResponse> similaires(@PathVariable Long tmdbId) {
        return tmdbService.listerSimilaires(tmdbId);
    }

    @GetMapping("/serie/{tmdbId}/recommandations")
    public List<TmdbSerieResponse> recommandations(@PathVariable Long tmdbId) {
        return tmdbService.listerRecommandations(tmdbId);
    }

    @GetMapping("/serie/{tmdbId}/credits")
    public List<MembreCastingResponse> credits(@PathVariable Long tmdbId) {
        return tmdbService.listerCredits(tmdbId);
    }

    @GetMapping("/acteur/{acteurId}")
    public ActeurResponse acteur(@PathVariable Long acteurId) {
        return tmdbService.obtenirActeur(acteurId);
    }

    @GetMapping("/recherche-acteur")
    public List<ActeurResponse> rechercherActeur(@RequestParam String nom) {
        return tmdbService.rechercherActeur(nom);
    }

    @GetMapping("/acteur/{acteurId}/series")
    public List<TmdbSerieResponse> seriesActeur(@PathVariable Long acteurId) {
        return tmdbService.listerSeriesActeur(acteurId);
    }

    // The only endpoint that mutates local data and requires ownership scoping
    @PostMapping("/../utilisateurs/{userId}/tmdb/importer/{tmdbId}")
    public ResponseEntity<SerieResponse> importer(@PathVariable Long userId, @PathVariable Long tmdbId) {
        var serieImportee = tmdbService.importerSerie(userId, tmdbId);
        return ResponseEntity.status(HttpStatus.CREATED).body(SerieMapper.toResponse(serieImportee));
    }
}