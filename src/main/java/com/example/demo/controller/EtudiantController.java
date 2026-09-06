package com.example.demo.controller;

import com.example.demo.Entites.EtudiantEntite;
import com.example.demo.service.EtudiantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final EtudiantService etudiantService;

    public EtudiantController(EtudiantService etudiantService) {
        this.etudiantService = etudiantService;
    }

    // 1. Ajouter un étudiant
    @PostMapping
    public ResponseEntity<EtudiantEntite> ajouterEtudiant(
            @RequestBody EtudiantEntite etudiant) {

        return ResponseEntity.ok(
                etudiantService.ajouterEtudiant(etudiant)
        );
    }

    // 2. Afficher tous les étudiants
    @GetMapping
    public ResponseEntity<List<EtudiantEntite>> afficherTousLesEtudiants() {

        return ResponseEntity.ok(
                etudiantService.afficherTousLesEtudiants()
        );
    }

    // 3. Afficher un étudiant par son ID
    @GetMapping("/{id}")
    public ResponseEntity<EtudiantEntite> chercherEtudiantParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                etudiantService.chercherEtudiantParId(id)
        );
    }

    // 4. Rechercher par nom
    @GetMapping("/nom/{nom}")
    public ResponseEntity<List<EtudiantEntite>> chercherParNom(
            @PathVariable String nom) {

        return ResponseEntity.ok(
                etudiantService.chercherEtudiantsParNom(nom)
        );
    }

    // 5. Rechercher par formation
    @GetMapping("/formation/{formation}")
    public ResponseEntity<List<EtudiantEntite>> chercherParFormation(
            @PathVariable String formation) {

        return ResponseEntity.ok(
                etudiantService.chercherEtudiantsParFormation(formation)
        );
    }

    // 6. Modifier un étudiant
    @PutMapping("/{id}")
    public ResponseEntity<EtudiantEntite> modifierEtudiant(
            @PathVariable Long id,
            @RequestBody EtudiantEntite etudiant) {

        return ResponseEntity.ok(
                etudiantService.modifierEtudiant(id, etudiant)
        );
    }

    // 7. Supprimer un étudiant
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerEtudiant(
            @PathVariable Long id) {

        etudiantService.supprimerEtudiant(id);

        return ResponseEntity.ok(
                "Etudiant supprimé avec succès"
        );
    }
}
