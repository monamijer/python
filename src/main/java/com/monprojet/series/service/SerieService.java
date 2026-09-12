// SerieService.java
package com.monprojet.series.service;

import com.monprojet.series.entity.Serie;
import com.monprojet.series.entity.Utilisateur;
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
    private final UtilisateurService utilisateurService;

    @Transactional(readOnly = true)
    public List<Serie> listerToutes(Long utilisateurId) {
        return serieRepository.findByUtilisateurId(utilisateurId);
    }

    // Returns 404 whether the series doesn't exist OR belongs to another user —
    // this indistinguishability is intentional, it prevents leaking existence
    // of another user's data through error message differences.
    @Transactional(readOnly = true)
    public Serie obtenirParId(Long id, Long utilisateurId) {
        return serieRepository.findByIdAndUtilisateurId(id, utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Série introuvable : id=" + id));
    }

    public Serie creer(Long utilisateurId, Serie serie) {
        Utilisateur utilisateur = utilisateurService.obtenirParId(utilisateurId);
        serie.setUtilisateur(utilisateur);
        return serieRepository.save(serie);
    }

    public Serie modifier(Long id, Long utilisateurId, Serie donnees) {
        Serie existante = obtenirParId(id, utilisateurId);
        existante.setTitre(donnees.getTitre());
        existante.setGenre(donnees.getGenre());
        existante.setDescription(donnees.getDescription());
        existante.setAnneeSortie(donnees.getAnneeSortie());
        existante.setNote(donnees.getNote());
        existante.setImageUrl(donnees.getImageUrl());
        return serieRepository.save(existante);
    }

    public void supprimer(Long id, Long utilisateurId) {
        Serie serie = obtenirParId(id, utilisateurId);
        serieRepository.delete(serie);
    }

    // Used by AdminController — deliberately bypasses ownership filtering.
    // Kept in this service (not duplicated in AdminController) so the admin
    // path stays a thin wrapper rather than a second implementation to maintain.
    @Transactional(readOnly = true)
    public List<Serie> listerToutesAdmin() {
        return serieRepository.findAll();
    }
}