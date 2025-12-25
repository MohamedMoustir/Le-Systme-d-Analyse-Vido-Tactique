package com.savt.backend.infrastructure.persistence;

import com.savt.backend.domain.entity.VideoMetadata;
import com.savt.backend.domain.repository.VideoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class VideoRepositoryImpl implements VideoRepository {

    private final SpringDataVideoRepository springDataVideoRepository;
    @Override
    public VideoMetadata save(VideoMetadata video) {
        return springDataVideoRepository.save(video);
    }

    @Override
    public Optional<VideoMetadata> findById(String id) {
        return springDataVideoRepository.findById(id);
    }

    @Override
    public List<VideoMetadata> findAll() {
        return springDataVideoRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        springDataVideoRepository.deleteById(id);
    }
}
