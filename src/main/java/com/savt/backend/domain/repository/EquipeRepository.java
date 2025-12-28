package com.savt.backend.domain.repository;

import com.savt.backend.domain.entity.Equipe;
import com.savt.backend.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface EquipeRepository {
    boolean existsByNom(String nom);
    boolean existsById(String id);
    Equipe save(Equipe equipe);
    Optional<Equipe> findById(String id);
    List<Equipe> findAll();
    void deleteById(String id);
}
