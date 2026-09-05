package com.example.demo.service;

import Entites.EtudiantEntite;
import com.example.demo.repositories.EtudiantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EtudiantServiceImpl implements EtudiantService {

    private final EtudiantRepository etudiantRepository;

    public EtudiantServiceImpl(EtudiantRepository etudiantRepository) {
        this.etudiantRepository = etudiantRepository;
    }

    @Override
    public EtudiantEntite ajouterEtudiant(EtudiantEntite etudiant) {
        return etudiantRepository.save(etudiant);
    }

    @Override
    public List<EtudiantEntite> afficherTousLesEtudiants() {
        return etudiantRepository.findAll();
    }

    @Override
    public EtudiantEntite chercherEtudiantParId(Long id) {
        return etudiantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Etudiant introuvable avec l'id : " + id));
    }

    @Override
    public List<EtudiantEntite> chercherEtudiantsParNom(String nom) {
        return etudiantRepository.findByNomContainingIgnoreCase(nom);
    }

    @Override
    public List<EtudiantEntite> chercherEtudiantsParFormation(String formation) {
        return etudiantRepository.findByFormationContainingIgnoreCase(formation);
    }

    @Override
    public EtudiantEntite modifierEtudiant(Long id, EtudiantEntite etudiant) {

        EtudiantEntite etudiantExistant = chercherEtudiantParId(id);

        etudiantExistant.setNom(etudiant.getNom());
        etudiantExistant.setPrenom(etudiant.getPrenom());
        etudiantExistant.setAge(etudiant.getAge());
        etudiantExistant.setFormation(etudiant.getFormation());
        etudiantExistant.setEmail(etudiant.getEmail());
        etudiantExistant.setTelephone(etudiant.getTelephone());

        return etudiantRepository.save(etudiantExistant);
    }

    @Override
    public void supprimerEtudiant(Long id) {

        EtudiantEntite etudiant = chercherEtudiantParId(id);

        etudiantRepository.delete(etudiant);
    }
}

