package com.football.analyzer.domain.repository;


import com.football.analyzer.domain.entity.PositionData;

import java.util.List;
import java.util.Optional;

public interface PositionRepository  {

    void deleteByVideoId(String videoId);
    List<PositionData> saveAll( List<PositionData> positionData);
    Optional<PositionData> findById(String id);
    List<PositionData> findAll();
}
