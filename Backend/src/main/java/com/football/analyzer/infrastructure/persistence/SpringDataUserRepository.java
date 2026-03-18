package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataUserRepository  extends MongoRepository<User,String> {
    Optional<User> findByEmail(String email);
}
