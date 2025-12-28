package com.savt.backend.domain.repository;

import com.savt.backend.domain.entity.Joueur;

import java.util.List;
import java.util.Optional;

public interface JoueurRepository {
    List<Joueur> findByEquipeId(String equipeId);
    Joueur save(Joueur joueur);
    Optional<Joueur> findById(String id);
    List<Joueur> findAll();
    void deleteById(String id);
}
