package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.VideoService;
import com.football.analyzer.application.service.implementation.FootballAnalysisService;
import com.football.analyzer.presentation.dto.Request.VideoUploadRequest;
import com.football.analyzer.presentation.dto.Response.VideoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final VideoService videoService;
    private final FootballAnalysisService analysisService;

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }


    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> uploadVideo(@RequestParam("video") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "Please select a video file");
            return response;
        }

        try {
            VideoUploadRequest uploadRequest = new VideoUploadRequest();

            VideoResponse savedVideo = videoService.uploadAndSave(file, uploadRequest, "web-user");

            response.put("success", true);
            response.put("videoId", savedVideo.getId());
            response.put("filename", savedVideo.getTitre());
            response.put("url", "/uploads/" + savedVideo.getUrlFichier());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to upload: " + e.getMessage());
        }

        return response;
    }


    @PostMapping("/start-analysis")
    @ResponseBody
    public Map<String, Object> startAnalysis(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        String videoId = request.get("videoId");

        if (videoId == null || videoId.isEmpty()) {
            response.put("success", false);
            response.put("message", "Video ID is required");
            return response;
        }

        analysisService.startAnalysis(videoId);

        response.put("success", true);
        response.put("message", "Analysis started for ID: " + videoId);
        return response;
    }
}