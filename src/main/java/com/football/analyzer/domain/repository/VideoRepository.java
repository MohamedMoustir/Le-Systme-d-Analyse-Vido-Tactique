package com.football.analyzer.domain.repository;


import com.football.analyzer.domain.entity.VideoMetadata;

import java.util.List;
import java.util.Optional;

public interface VideoRepository {
    VideoMetadata save(VideoMetadata video);
    Optional<VideoMetadata> findById(String id);
    List<VideoMetadata> findAll();
    void deleteById(String id);
    List<VideoMetadata> findByUploaderId(String userId);
    long count();
    long countByUploaderId(String uploaderId);
}