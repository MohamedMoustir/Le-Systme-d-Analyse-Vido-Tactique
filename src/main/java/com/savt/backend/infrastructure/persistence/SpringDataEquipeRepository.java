package com.savt.backend.infrastructure.persistence;

import com.savt.backend.domain.entity.Equipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataEquipeRepository extends MongoRepository<Equipe, String> {
    boolean existsByNom(String nom);
}