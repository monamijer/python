// TmdbController.java
package com.monprojet.series.controller;

import com.monprojet.series.dto.response.SerieResponse;
import com.monprojet.series.dto.response.TmdbSerieResponse;
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
    public List<TmdbSerieResponse> rechercher(@RequestParam String titre) {
        return tmdbService.rechercherParTitre(titre);
    }

    @GetMapping("/populaires")
    public List<TmdbSerieResponse> populaires() {
        return tmdbService.listerPopulaires();
    }

    @GetMapping("/{tmdbId}/similaires")
    public List<TmdbSerieResponse> similaires(@PathVariable Long tmdbId) {
        return tmdbService.listerSimilaires(tmdbId);
    }

    @PostMapping("/importer/{tmdbId}")
    public ResponseEntity<SerieResponse> importer(@PathVariable Long tmdbId) {
        var serieImportee = tmdbService.importerSerie(tmdbId);
        return ResponseEntity.status(HttpStatus.CREATED).body(SerieMapper.toResponse(serieImportee));
    }

    @PostMapping("/api/utilisateurs/{userId}/tmdb/importer/{tmdbId}")
    public ResponseEntity<SerieResponse> importer(@PathVariable Long userId, @PathVariable Long tmdbId) {
        var serieImportee = tmdbService.importerSerie(userId, tmdbId);
        return ResponseEntity.status(HttpStatus.CREATED).body(SerieMapper.toResponse(serieImportee));
    }
}