package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.Equipe;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataEquipeRepository extends MongoRepository<Equipe,String> {
}
