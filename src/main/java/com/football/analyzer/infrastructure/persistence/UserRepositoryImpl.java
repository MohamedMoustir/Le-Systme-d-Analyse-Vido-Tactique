package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final SpringDataUserRepository repo;
    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return repo.save(user);
    }

    @Override
    public Optional<User> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public List<User> findAll() {
        return repo.findAll();
    }

    @Override
    public int count() {
        return Math.toIntExact(repo.count());
    }

    @Override
    public void delete(User user) {
      repo.delete(user);
    }
}
