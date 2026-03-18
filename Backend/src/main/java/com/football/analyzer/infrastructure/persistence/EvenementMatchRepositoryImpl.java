package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.EvenementMatch;
import com.football.analyzer.domain.repository.EvenementMatchRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class EvenementMatchRepositoryImpl implements EvenementMatchRepository {
    private SpringDataEventMatchRepository repo;
    @Override
    public EvenementMatch save(EvenementMatch evenementMatch) {
        return repo.save(evenementMatch);
    }

    @Override
    public Optional<EvenementMatch> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public List<EvenementMatch> findAll() {
        return repo.findAll();
    }

    @Override
    public void deleteById(String id) {
   repo.deleteById(id);
    }
}
