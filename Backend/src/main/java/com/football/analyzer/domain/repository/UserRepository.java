package com.football.analyzer.domain.repository;


import com.football.analyzer.domain.entity.Equipe;
import com.football.analyzer.domain.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    User save(User user);
    Optional<User> findById(String id);
    List<User> findAll();
    int count();
    void delete(User user);

}
