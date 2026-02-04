package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.PositionData;
import com.football.analyzer.domain.entity.VideoMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataPositionRepository  extends MongoRepository<PositionData,String> {
}
