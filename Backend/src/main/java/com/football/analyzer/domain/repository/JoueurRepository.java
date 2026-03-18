package com.football.analyzer.domain.repository;

import com.football.analyzer.domain.entity.Joueur;

import java.util.List;
import java.util.Optional;

public interface JoueurRepository {
    Joueur save(Joueur joueur);
    List<Joueur> saveAll(List<Joueur> joueurs);
    Optional<Joueur> findById(String id);
    List<Joueur> findAll();
    void deleteById(String id);

    boolean existsById(String id);
}
