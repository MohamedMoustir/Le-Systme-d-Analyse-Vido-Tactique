package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.VideoService;
import com.football.analyzer.application.service.implementation.FootballAnalysisService;
import com.football.analyzer.presentation.dto.Request.VideoUploadRequest;
import com.football.analyzer.presentation.dto.Response.VideoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final FootballAnalysisService footballAnalysisService; // The Runner

    // 1. Upload Video
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoResponse> uploadVideo(
            @RequestPart("file") MultipartFile file,
            @RequestPart("data") VideoUploadRequest request) {

        VideoResponse response = videoService.uploadAndSave(file, request, "user@email.com");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<String> startAnalysis(@PathVariable String id) {
//        footballAnalysisService.startAnalysis(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Stream trigger ready");
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stopAnalysis() {
        // Implementation logic
        return ResponseEntity.ok().build();
    }
}