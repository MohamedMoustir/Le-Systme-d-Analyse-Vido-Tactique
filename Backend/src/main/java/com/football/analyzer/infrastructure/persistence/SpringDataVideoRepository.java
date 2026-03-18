package com.football.analyzer.infrastructure.persistence;

import com.football.analyzer.domain.entity.VideoMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataVideoRepository  extends MongoRepository<VideoMetadata,String> {
    List<VideoMetadata> findByUploaderId(String userId);
    long countByUploaderId(String uploaderId);
}
