package com.savt.backend.domain.repository;

import com.savt.backend.domain.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}
