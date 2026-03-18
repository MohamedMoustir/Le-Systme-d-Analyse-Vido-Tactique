package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.VideoService;
import com.football.analyzer.application.service.implementation.FootballAnalysisService;
import com.football.analyzer.application.utils.UserUtils;
import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.entity.VideoMetadata;
import com.football.analyzer.domain.enums.StatutAnalyse;
import com.football.analyzer.presentation.dto.Request.VideoUploadRequest;
import com.football.analyzer.presentation.dto.Response.VideoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private static final String BYTE_RANGE_PREFIX = "bytes";

    private final VideoService videoService;
    private final FootballAnalysisService footballAnalysisService; // The Runner
    private final UserUtils userUtils;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoResponse> uploadVideo(
            @RequestPart("file") MultipartFile file,
            @RequestPart("data") VideoUploadRequest request) {

        VideoResponse response = videoService.uploadAndSave(file, request, userUtils.getCurrentUser().getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<String> startAnalysis(@PathVariable String id) {
        footballAnalysisService.startAnalysis(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Analysis started");
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stopAnalysis() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-videos")
    public ResponseEntity<List<VideoMetadata>> getMyVideos() {
        User currentUser = userUtils.getCurrentUser();

        List<VideoMetadata> allVideos = videoService.findByUploaderId(currentUser.getId());

        List<VideoMetadata> readyVideos = allVideos.stream()
                .filter(video -> {
                    StatutAnalyse status = video.getStatut();
                    return status == StatutAnalyse.COMPLETED || status == StatutAnalyse.TERMINE;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(readyVideos);
    }

    @GetMapping("/play/{id}")
    public ResponseEntity<Resource> playVideo(@PathVariable String id) {
        try {
            VideoMetadata video = videoService.findById(id);
            Path originalPath = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(video.getUrlFichier()).normalize();
            Path analyzedPath = buildAnalyzedPath(originalPath);

            Path pathToServe = Files.exists(analyzedPath) ? analyzedPath : originalPath;
            if (!Files.exists(pathToServe)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(pathToServe.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                    .orElse(MediaType.parseMediaType("video/mp4"));

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);

        } catch (Exception e) {
            System.out.println("❌ [DEBUG] ERROR IN PLAY VIDEO:");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private Path buildAnalyzedPath(Path originalPath) {
        String originalName = originalPath.getFileName().toString();

        if (originalName.contains("_analyzed")) {
            return originalPath;
        }

        String analyzedName = originalName.toLowerCase().endsWith(".mp4")
                ? originalName.substring(0, originalName.length() - 4) + "_analyzed.mp4"
                : originalName + "_analyzed.mp4";
        return originalPath.resolveSibling(analyzedName);
    }
}