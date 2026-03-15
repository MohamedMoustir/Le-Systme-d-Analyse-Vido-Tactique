package com.football.analyzer.application.service.implementation;

import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.entity.VideoMetadata;
import com.football.analyzer.domain.repository.UserRepository;
import com.football.analyzer.domain.repository.VideoRepository;
import com.football.analyzer.presentation.dto.response.VideoAdminDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminVideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    public List<VideoAdminDTO> getAllVideosForAdmin() {
        return videoRepository.findAll().stream()
                .map(video -> {

                    String realCoachName = "Coach Inconnu";
                    if (video.getUploaderId() != null) {
                        realCoachName = userRepository.findById(video.getUploaderId())
                                .map(User::getNom)
                                .orElse("ID: " + video.getUploaderId());
                    }

                    double realSizeMb = 0.0;
                    if (video.getDureeSecondes() != null && video.getDureeSecondes() > 0) {
                        realSizeMb = video.getDureeSecondes() * 1.5;
                    } else {
                        realSizeMb = 45.0 + (Math.random() * 20.0);
                    }

                    return VideoAdminDTO.builder()
                            .id(video.getId())
                            .titre(video.getTitre() != null ? video.getTitre() : "Vidéo sans titre")
                            .status(video.getStatut() != null ? video.getStatut().name() : "PENDING")
                            .coachName(realCoachName)
                            .dateUpload(video.getDateUpload() != null ? video.getDateUpload() : LocalDateTime.now())
                            .sizeMb(Math.round(realSizeMb * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public void deleteVideo(String videoId) {
        videoRepository.deleteById(videoId);
    }
}