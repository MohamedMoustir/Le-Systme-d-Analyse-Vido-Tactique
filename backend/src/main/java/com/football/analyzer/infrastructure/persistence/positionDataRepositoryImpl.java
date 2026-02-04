package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.PositionData;
import com.football.analyzer.domain.repository.PositionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class positionDataRepositoryImpl implements PositionRepository {

    private SpringDataPositionRepository repo;
    @Override
    public void deleteByVideoId(String videoId) {
        repo.deleteById(videoId);
    }

    @Override
    public List<PositionData> saveAll( List<PositionData> positionData) {
        return repo.saveAll(positionData);
    }



    @Override
    public Optional<PositionData> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public List<PositionData> findAll() {
        return repo.findAll();
    }




}
