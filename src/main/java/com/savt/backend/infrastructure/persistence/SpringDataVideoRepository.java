package com.savt.backend.infrastructure.persistence;

import com.savt.backend.domain.entity.VideoMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataVideoRepository  extends MongoRepository<VideoMetadata,String> {
}
