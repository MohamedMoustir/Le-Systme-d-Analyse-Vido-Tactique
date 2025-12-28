package com.savt.backend.infrastructure.persistence;

import com.savt.backend.domain.entity.Joueur;
import com.savt.backend.domain.repository.JoueurRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class JoueurRepositoryImpl implements JoueurRepository {
    private final SpringDataJoueurRepository springDataJoueurRepository;
    @Override
    public List<Joueur> findByEquipeId(String equipeId) {
        return springDataJoueurRepository.findByEquipeId(equipeId);
    }

    @Override
    public Joueur save(Joueur joueur) {
        return springDataJoueurRepository.save(joueur);
    }

    @Override
    public Optional<Joueur> findById(String id) {
        return springDataJoueurRepository.findById(id);
    }

    @Override
    public List<Joueur> findAll() {
        return springDataJoueurRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        springDataJoueurRepository.deleteById(id);
    }
}
