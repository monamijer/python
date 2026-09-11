// AdminController.java
package com.monprojet.series.controller;

import com.monprojet.series.dto.response.UtilisateurResponse;
import com.monprojet.series.mapper.UtilisateurMapper;
import com.monprojet.series.repository.UtilisateurRepository;
import com.monprojet.series.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs")
@RequiredArgsConstructor
public class AdminController {

    private final UtilisateurRepository utilisateurRepository;

    // Sécurisé au niveau SecurityConfig : hasRole("ADMIN") sur /api/admin/**

    @GetMapping
    public List<UtilisateurResponse> listerTous() {
        return utilisateurRepository.findAll().stream().map(UtilisateurMapper::toResponse).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        var utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : id=" + id));
        utilisateurRepository.delete(utilisateur);
        return ResponseEntity.noContent().build();
    }
}   