package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import Entites.EtudiantEntite;
import java.util.List;

public interface EtudiantRepository extends JpaRepository<EtudiantEntite, Long> {

    List<EtudiantEntite> findByNomContainingIgnoreCase(String nom);

    List<EtudiantEntite> findByFormationContainingIgnoreCase(String formation);
}
