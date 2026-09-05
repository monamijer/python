package com.example.demo.service;

import Entites.EtudiantEntite;

import java.util.List;

public interface EtudiantService {

    EtudiantEntite ajouterEtudiant(EtudiantEntite etudiant);

    List<EtudiantEntite> afficherTousLesEtudiants();

    EtudiantEntite chercherEtudiantParId(Long id);

    List<EtudiantEntite> chercherEtudiantsParNom(String nom);

    List<EtudiantEntite> chercherEtudiantsParFormation(String formation);

    EtudiantEntite modifierEtudiant(Long id, EtudiantEntite etudiant);

    void supprimerEtudiant(Long id);
}

