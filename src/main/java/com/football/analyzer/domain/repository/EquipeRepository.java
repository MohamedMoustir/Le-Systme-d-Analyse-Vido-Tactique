package com.football.analyzer.domain.repository;



import com.football.analyzer.domain.entity.Equipe;

import java.util.List;
import java.util.Optional;

public interface EquipeRepository  {
        Equipe save(Equipe equipe);
        Optional<Equipe> findById(String id);
        List<Equipe> findAll();
        void deleteById(String id);
        long count();
        Optional<Equipe> findByUserId(String userId);

}


