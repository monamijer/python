// UtilisateurService.java
package com.monprojet.series.service;

import com.monprojet.series.entity.Utilisateur;
import com.monprojet.series.exception.BusinessException;
import com.monprojet.series.exception.ResourceNotFoundException;
import com.monprojet.series.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    @Transactional(readOnly = true)
    public List<Utilisateur> listerTous() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Utilisateur obtenirParId(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : id=" + id));
    }

    public Utilisateur creer(Utilisateur utilisateur) {
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new BusinessException("Cet email est déjà utilisé : " + utilisateur.getEmail());
        }
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur obtenirParEmail(String email) {
    return utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : email=" + email));
}
}