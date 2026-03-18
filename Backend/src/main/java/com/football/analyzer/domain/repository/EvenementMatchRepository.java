package com.football.analyzer.domain.repository;

import com.football.analyzer.domain.entity.EvenementMatch;

import java.util.List;
import java.util.Optional;

public interface EvenementMatchRepository {
    EvenementMatch save(EvenementMatch evenementMatch);
    Optional<EvenementMatch> findById(String id);
    List<EvenementMatch> findAll();
    void deleteById(String id);
}
