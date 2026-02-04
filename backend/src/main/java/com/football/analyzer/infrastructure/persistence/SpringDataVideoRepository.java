package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.VideoMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataVideoRepository  extends MongoRepository<VideoMetadata,String> {
}
