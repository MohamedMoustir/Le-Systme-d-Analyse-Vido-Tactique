package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.EvenementMatch;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataEventMatchRepository extends MongoRepository<EvenementMatch,String> {
}
