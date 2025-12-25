package com.savt.backend.application.service.implementation;

import com.savt.backend.application.mapper.UserMapper;
import com.savt.backend.application.mapper.VideoMapper;
import com.savt.backend.application.service.FileStorageService;
import com.savt.backend.application.service.VideoService;
import com.savt.backend.domain.entity.VideoMetadata;
import com.savt.backend.domain.enums.StatutAnalyse;
import com.savt.backend.domain.repository.UserRepository;
import com.savt.backend.domain.repository.VideoRepository;
import com.savt.backend.presentation.dto.Response.VideoResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final VideoMapper videoMapper;
    private final UserMapper userMapper;

    @Override
    public VideoResponse uploadAndSave(MultipartFile file, String titre, String uploaderId) {
        String fileName = fileStorageService.saveFile(file);

        VideoMetadata videoEntity = VideoMetadata.builder()
                .titre(titre)
                .urlFichier(fileName)
                .dateUpload(LocalDateTime.now())
                .statut(StatutAnalyse.EN_ATTENTE)
                .uploaderId(uploaderId)
                .build();

        VideoMetadata savedVideo = videoRepository.save(videoEntity);
        VideoResponse response = videoMapper.toResponse(savedVideo);
        userRepository.findByEmail(uploaderId).ifPresent(user -> {
            response.setUploader(userMapper.toResponse(user));
        });
        return response;
    }

    @Override
    public Optional<VideoMetadata> getVideoById(String id) {
        return videoRepository.findById(id);
    }

    @Override
    public List<VideoMetadata> getAllVideos() {
        return videoRepository.findAll();
    }

    @Override
    public void deleteVideo(String id) {
        videoRepository.deleteById(id);
    }
}
