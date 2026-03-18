package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.implementation.AdminVideoService;
import com.football.analyzer.presentation.dto.Response.VideoAdminDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/videos")
@AllArgsConstructor
public class AdminVideoController {

    private final AdminVideoService adminVideoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VideoAdminDTO>> getAllVideos() {
        return ResponseEntity.ok(adminVideoService.getAllVideosForAdmin());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteVideo(@PathVariable String id) {
        adminVideoService.deleteVideo(id);
        return ResponseEntity.ok("Vidéo supprimée avec succès et espace libéré");
    }


}