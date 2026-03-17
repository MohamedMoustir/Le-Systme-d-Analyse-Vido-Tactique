package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.Equipe;
import com.football.analyzer.domain.repository.EquipeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class EquipeRepositoryImpl implements EquipeRepository {
    private final SpringDataEquipeRepository repo ;

    @Override
    public Equipe save(Equipe equipe) {
        return repo.save(equipe);
    }

    @Override
    public Optional<Equipe> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public List<Equipe> findAll() {
        return repo.findAll();
    }

    @Override
    public void deleteById(String id) {
     repo.deleteById(id);
    }

    @Override
    public long count() {
        return repo.count();
    }

    @Override
    public Optional<Equipe> findByUserId(String userId) {
        return repo.findByUserId(userId);
    }
}
