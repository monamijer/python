package com.example.demo.Entites;

import jakarta.persistence.*;

@Entity
public class EtudiantEntite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String prenom;
    private int age;

    public EtudiantEntite(Long id, String nom, String prenom, int age){
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
    }
    public EtudiantEntite(){

    }
    public Long getId(){
        return id;
    }
    public String getNom(){
        return nom;
    }
    public String getPrenom(){
        return prenom;
    }
    public int getAge(){
        return age;
    }
    public void setId(Long id){
        this.id = id;
    }
    public void setNom(String nom){
        this.nom = nom;
    }
    public void setPrenom(String prenom){
        this.prenom = prenom;
    }
    public void setAge(int age){
        this.age = age;
    }
}