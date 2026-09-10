// SerieService.java
package com.monprojet.series.service;

import com.monprojet.series.entity.Serie;
import com.monprojet.series.exception.ResourceNotFoundException;
import com.monprojet.series.repository.SerieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SerieService {

    private final SerieRepository serieRepository;

    @Transactional(readOnly = true)
    public List<Serie> listerToutes() {
        return serieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Serie obtenirParId(Long id) {
        return serieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Série introuvable : id=" + id));
    }

    public Serie creer(Serie serie) {
        return serieRepository.save(serie);
    }

    public Serie modifier(Long id, Serie donnees) {
        Serie existante = obtenirParId(id);
        existante.setTitre(donnees.getTitre());
        existante.setGenre(donnees.getGenre());
        existante.setDescription(donnees.getDescription());
        existante.setAnneeSortie(donnees.getAnneeSortie());
        existante.setNote(donnees.getNote());
        existante.setImageUrl(donnees.getImageUrl());
        return serieRepository.save(existante);
    }

    public void supprimer(Long id) {
        Serie serie = obtenirParId(id);
        serieRepository.delete(serie);
    }
}