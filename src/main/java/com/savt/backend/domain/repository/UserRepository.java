package com.savt.backend.domain.repository;

import com.savt.backend.domain.entity.User;
import com.savt.backend.domain.entity.VideoMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    User save(User user);
    Optional<User> findById(String id);
    List<User> findAll();
    void deleteById(String id);
}
