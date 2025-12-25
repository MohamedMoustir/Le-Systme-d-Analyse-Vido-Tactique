package com.savt.backend.domain.repository;

import com.savt.backend.domain.entity.VideoMetadata;
import java.util.List;
import java.util.Optional;

public interface VideoRepository {
    VideoMetadata save(VideoMetadata video);
    Optional<VideoMetadata> findById(String id);
    List<VideoMetadata> findAll();
    void deleteById(String id);
}