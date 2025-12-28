package com.savt.backend.infrastructure.persistence;

import com.savt.backend.domain.entity.Equipe;
import com.savt.backend.domain.entity.User;
import com.savt.backend.domain.repository.EquipeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
@Component
@AllArgsConstructor
public class EquipeRepositoryImpl implements EquipeRepository {
    private final SpringDataEquipeRepository springDataEquipeRepository;
    @Override
    public boolean existsByNom(String nom) {
        return springDataEquipeRepository.existsByNom(nom);
    }

    @Override
    public boolean existsById(String id) {
        return springDataEquipeRepository.existsById(id);
    }

    @Override
    public Equipe save(Equipe equipe) {
        return springDataEquipeRepository.save(equipe);
    }

    @Override
    public Optional<Equipe> findById(String id) {
        return springDataEquipeRepository.findById(id);
    }

    @Override
    public List<Equipe> findAll() {
        return springDataEquipeRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        springDataEquipeRepository.deleteById(id);
    }
}
