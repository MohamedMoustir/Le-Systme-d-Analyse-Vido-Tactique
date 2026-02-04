package com.football.analyzer.application.service;


import com.football.analyzer.domain.entity.VideoMetadata;
import com.football.analyzer.presentation.dto.Request.VideoUploadRequest;
import com.football.analyzer.presentation.dto.Response.FrameAnalysisDTO;
import com.football.analyzer.presentation.dto.Response.VideoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface VideoService {
    void processAnalysisMessage(String videoId, FrameAnalysisDTO dto) ;
    VideoResponse uploadAndSave(MultipartFile file, VideoUploadRequest request, String userId);
}
