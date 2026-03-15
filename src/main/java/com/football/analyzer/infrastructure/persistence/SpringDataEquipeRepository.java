package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.Equipe;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataEquipeRepository extends MongoRepository<Equipe,String> {
    Optional<Equipe> findByUserId(String userId);
}
