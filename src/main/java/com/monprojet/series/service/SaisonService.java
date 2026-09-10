// SaisonService.java
package com.monprojet.series.service;

import com.monprojet.series.entity.Saison;
import com.monprojet.series.entity.Serie;
import com.monprojet.series.exception.ResourceNotFoundException;
import com.monprojet.series.repository.SaisonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SaisonService {

    private final SaisonRepository saisonRepository;
    private final SerieService serieService;

    @Transactional(readOnly = true)
    public List<Saison> listerParSerie(Long serieId) {
        serieService.obtenirParId(serieId); // 404 if the series doesn't exist
        return saisonRepository.findBySerieIdOrderByNumeroAsc(serieId);
    }

    @Transactional(readOnly = true)
    public Saison obtenirParId(Long id) {
        return saisonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Saison introuvable : id=" + id));
    }

    @Transactional(readOnly = true)
    public Optional<Saison> trouverParSerieEtNumero(Long serieId, Integer numero) {
        return saisonRepository.findBySerieIdAndNumero(serieId, numero);
    }

    public Saison creer(Long serieId, Saison saison) {
        Serie serie = serieService.obtenirParId(serieId);
        saison.setSerie(serie);
        return saisonRepository.save(saison);
    }
}