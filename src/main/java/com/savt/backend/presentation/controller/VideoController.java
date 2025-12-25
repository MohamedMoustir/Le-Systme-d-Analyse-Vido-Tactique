package com.savt.backend.presentation.controller;

import com.savt.backend.application.service.VideoService;
import com.savt.backend.presentation.dto.Response.VideoResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/videos")
@AllArgsConstructor
public class VideoController {
    private final VideoService videoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('COACH')")
    public ResponseEntity<VideoResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("titre") String titre,
            @RequestParam("uploaderId") String uploaderId
    ) {
        return ResponseEntity.ok(videoService.uploadAndSave(file, titre, uploaderId));
    }
}
