package com.example.demo.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Etudiant {
    @PostMapping("/ajouter")
    public String ajouterEtudiant(){
        return "Etudiant ajouter avec succes";
    }
    @DeleteMapping("/supprimer")
    public String supprimerEtudiant(){
        return "Etudiant supprime avec succes";
    }
    @PutMapping("/modifier")
    public String modifierEtudiant(){
        return "Etudiant modifie avec succes";
    }
}
