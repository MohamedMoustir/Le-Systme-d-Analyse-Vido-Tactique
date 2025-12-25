package com.savt.backend.application.service;

import com.savt.backend.domain.entity.VideoMetadata;
import com.savt.backend.presentation.dto.Response.VideoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface VideoService {
    VideoResponse uploadAndSave(MultipartFile file, String titre, String uploaderId);
    Optional<VideoMetadata> getVideoById(String id);
    List<VideoMetadata> getAllVideos();
    void deleteVideo(String id);
}
