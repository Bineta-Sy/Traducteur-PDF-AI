package com.translator.metier;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fichier")
public class Fichier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nom_fichier")
    private String nomFichier;

    @Column(name = "nom_fichier_en")
    private String nomFichierEN;
    
    @ManyToOne
    @JoinColumn(name = "utilisateur")
    private Personne utilisateur;
    
    @CreationTimestamp
    @Column(name = "date_conversion", updatable = false)
    private LocalDateTime conversionDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public LocalDateTime getConversionDate() {
        return conversionDate;
    }

    public void setConversionDate(LocalDateTime conversionDate) {
        this.conversionDate = conversionDate;
    }

    public Personne getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Personne utilisateur) {
        this.utilisateur = utilisateur;
    }

	public String getNomFichierEN() {
		return nomFichierEN;
	}

	public void setNomFichierEN(String nomFichierEN) {
		this.nomFichierEN = nomFichierEN;
	}
    
}