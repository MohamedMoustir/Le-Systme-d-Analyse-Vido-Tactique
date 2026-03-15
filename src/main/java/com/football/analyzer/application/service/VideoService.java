package com.football.analyzer.application.service;


import com.football.analyzer.domain.entity.VideoMetadata;
import com.football.analyzer.presentation.dto.request.VideoUploadRequest;
import com.football.analyzer.presentation.dto.response.FrameAnalysisDTO;
import com.football.analyzer.presentation.dto.response.VideoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {
    void processAnalysisMessage(String videoId, FrameAnalysisDTO dto);

    VideoResponse uploadAndSave(MultipartFile file, VideoUploadRequest request, String userId);

    List<VideoMetadata> findByUploaderId(String userId);

    VideoMetadata findById(String videoId);
}
