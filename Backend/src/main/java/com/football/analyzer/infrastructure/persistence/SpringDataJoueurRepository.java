package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.Joueur;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataJoueurRepository extends MongoRepository<Joueur, String> {
}
