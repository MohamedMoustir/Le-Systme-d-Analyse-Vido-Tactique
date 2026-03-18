package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.Joueur;
import com.football.analyzer.domain.repository.JoueurRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class JoueurRepositoryImpl implements JoueurRepository {
    private final SpringDataJoueurRepository repo;
    @Override
    public Joueur save(Joueur joueur) {
        return repo.save(joueur);
    }

    @Override
    public List<Joueur> saveAll(List<Joueur> joueurs) {
        return repo.saveAll(joueurs);
    }

    @Override
    public Optional<Joueur> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public List<Joueur> findAll() {
        return repo.findAll();
    }

    @Override
    public void deleteById(String id) {
        repo.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repo.existsById(id);
    }
}
