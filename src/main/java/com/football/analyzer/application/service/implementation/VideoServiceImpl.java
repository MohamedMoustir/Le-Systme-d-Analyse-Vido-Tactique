package com.football.analyzer.application.service.implementation;

import com.football.analyzer.application.service.VideoService;
import com.football.analyzer.domain.entity.EvenementMatch;
import com.football.analyzer.domain.entity.MatchStatistics;
import com.football.analyzer.domain.entity.PositionData;
import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.entity.VideoMetadata;
import com.football.analyzer.domain.enums.StatutAnalyse;
import com.football.analyzer.domain.enums.SubscriptionPlan;
import com.football.analyzer.domain.exception.PaymentRequiredException;
import com.football.analyzer.domain.exception.ResourceNotFoundException;
import com.football.analyzer.domain.repository.EvenementMatchRepository;
import com.football.analyzer.domain.repository.PositionRepository;
import com.football.analyzer.domain.repository.UserRepository;
import com.football.analyzer.domain.repository.VideoRepository;
import com.football.analyzer.presentation.dto.Response.FrameAnalysisDTO;
import com.football.analyzer.presentation.dto.Request.VideoUploadRequest;
import com.football.analyzer.presentation.dto.Response.PlayerDataDTO;
import com.football.analyzer.presentation.dto.Response.VideoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoRepository metadataRepository;
    private final PositionRepository positionRepository;
    private final EvenementMatchRepository evenementMatchRepository;
    private final UserRepository userRepository;
    @Value("${upload.path:uploads}")
    private String uploadPath;
    private final List<PositionData> positionBuffer = new ArrayList<>();
    private static final int BATCH_SIZE = 200;
    private final java.util.Map<String, Long> lastGoalTimeMap = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long GOAL_COOLDOWN_MS = 10000;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    private final java.util.Map<String, Integer> lastGoalFrameMap = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int GOAL_FRAME_COOLDOWN = 300;

    public void processAnalysisMessage(String videoId, FrameAnalysisDTO dto) {
        if (dto == null || dto.getType() == null) return;

        switch (dto.getType()) {
            case "video_info":
                updateVideoInfo(videoId, dto);
                break;
            case "frame_analysis":
                handleFrameAnalysis(videoId, dto);
                break;
            case "jersey_discovery":
                handleJerseyDiscovery(videoId, dto);
            case "analysis_complete":
                finalizeAnalysis(videoId, dto);
                break;
            case "error":
                markAsError(videoId, dto.getMessage());
                break;
        }
    }

    @Override
    public VideoResponse uploadAndSave(MultipartFile file, VideoUploadRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));

        SubscriptionPlan userPlan = user.getPlan() != null ? user.getPlan() : SubscriptionPlan.FREE;
        long currentVideoCount = metadataRepository.countByUploaderId(userId);

        if (currentVideoCount >= userPlan.getMaxVideos()) {
            String message = userPlan == SubscriptionPlan.FREE
                ? "Vous avez atteint la limite de vidéos pour le plan GRATUIT (1 vidéo). Passez au plan PREMIUM pour télécharger plus de vidéos."
                : "Vous avez atteint la limite de vidéos pour votre plan (" + userPlan.getMaxVideos() + " vidéos).";

            log.warn("Upload denied for user {}: {} (Current: {}, Max: {})",
                     userId, message, currentVideoCount, userPlan.getMaxVideos());
            throw new PaymentRequiredException(message);
        }

        try {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".mp4";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            VideoMetadata metadata = VideoMetadata.builder()
                    .titre(request.getTitre() != null ? request.getTitre() : originalFilename)
                    .urlFichier(uniqueFilename)
                    .uploaderId(userId)
                    .dateUpload(LocalDateTime.now())
                    .statut(StatutAnalyse.EN_ATTENTE)
                    .build();

            VideoMetadata saved = metadataRepository.save(metadata);

            log.info("Video uploaded and saved: ID={} Path={} User={} Plan={} Videos={}/{}",
                     saved.getId(), filePath, userId, userPlan, currentVideoCount + 1, userPlan.getMaxVideos());

            return VideoResponse.builder()
                    .id(saved.getId())
                    .titre(saved.getTitre())
                    .urlFichier("/api/uploads/" + saved.getUrlFichier())
                    .statut(saved.getStatut().name())
                    .build();

        } catch (IOException e) {
            log.error("upload failed", e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public List<VideoMetadata> findByUploaderId(String userId) {
        return metadataRepository.findByUploaderId(userId);
    }

    @Override
    public VideoMetadata findById(String videoId) {
        return metadataRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with ID: " + videoId));
    }


    private void handleFrameAnalysis(String videoId, FrameAnalysisDTO dto) {
        if ("GOAL".equalsIgnoreCase(dto.getEvent())) {
            int currentFrame = dto.getFrameNum();
            int lastGoalFrame = lastGoalFrameMap.getOrDefault(videoId, -GOAL_FRAME_COOLDOWN);

            if (currentFrame - lastGoalFrame > GOAL_FRAME_COOLDOWN) {
                lastGoalFrameMap.put(videoId, currentFrame);

                CompletableFuture.runAsync(() -> {
                    log.info("Goal VERIFIED (Frame-based): {} for Video: {}", currentFrame, videoId);
                    saveEvent(videoId, dto);

                    if (messagingTemplate != null) {

                        java.util.Map<String, Object> goalMessage = new java.util.HashMap<>();
                        goalMessage.put("type", "goal");
                        goalMessage.put("is_goal", true);
                        goalMessage.put("frame_num", currentFrame);
                        goalMessage.put("timestamp", currentFrame / 25.0);
                        goalMessage.put("description", "BUT ! MARQUÉ");
                        goalMessage.put("team_id", 0);

                        messagingTemplate.convertAndSend("/topic/analysis/" + videoId, goalMessage);
                    }
                });
            }
        }

        if (dto.getPlayers() != null && !dto.getPlayers().isEmpty()) {
            List<PositionData> currentPositions = dto.getPlayers().stream()
                    .map(playerDto -> mapToPositionEntity(videoId, dto.getFrameNum(), playerDto))
                    .collect(Collectors.toList());

            synchronized (positionBuffer) {
                positionBuffer.addAll(currentPositions);
                if (positionBuffer.size() >= BATCH_SIZE) {
                    final List<PositionData> toSave = new ArrayList<>(positionBuffer);
                    positionBuffer.clear();

                    CompletableFuture.runAsync(() -> {
                        try {
                            positionRepository.saveAll(toSave);
                            log.debug(" Saved batch of {} positions", toSave.size());
                        } catch (Exception e) {
                            log.error(" Error saving batch: {}", e.getMessage());
                        }
                    });
                }
            }
        }
    }
    @Transactional
    public void saveEvent(String videoId, FrameAnalysisDTO dto) {

        EvenementMatch event = EvenementMatch.builder()
                .videoId(videoId)
                .type("BUT")
                .frameNumber(dto.getFrameNum().longValue())
                .description("Goal detected by AI")
                .tempsVideo(videoId)
                .build();

        evenementMatchRepository.save(event);

    }

    private void updateVideoInfo(String videoId, FrameAnalysisDTO dto) {
        metadataRepository.findById(videoId).ifPresent(video -> {
            video.setFps(dto.getFps().floatValue());
            video.setTotalFrames(dto.getTotalFrames().longValue());
            if(dto.getFps() > 0) {
                video.setDureeSecondes((long) (dto.getTotalFrames() / dto.getFps()));
            }
            metadataRepository.save(video);
        });
    }

    private PositionData mapToPositionEntity(String videoId, Integer frameNum, PlayerDataDTO playerDto) {
        return PositionData.builder()
                .videoId(videoId)
                .frameNumber(frameNum.longValue())
                .joueurId(null)
                .speedKmh(playerDto.getSpeedKmh() != null ? playerDto.getSpeedKmh().floatValue() : 0f)
                .distanceParcourue(playerDto.getDistanceM() != null ? playerDto.getDistanceM().floatValue() : 0f)
                .hasBall(playerDto.getHasBall())
                .pixelX(playerDto.getPositionPixels() != null ? (double) playerDto.getPositionPixels().get(0) : null)
                .pixelY(playerDto.getPositionPixels() != null ? (double) playerDto.getPositionPixels().get(1) : null)
                .FieldX(playerDto.getPositionField() != null ? playerDto.getPositionField().get(0).floatValue() : null)
                .FieldY(playerDto.getPositionField() != null ? playerDto.getPositionField().get(1).floatValue() : null)
                .build();
    }

    private void finalizeAnalysis(String videoId, FrameAnalysisDTO dto) {
        flushBuffer();
        metadataRepository.findById(videoId).ifPresent(video -> {
            if (dto.getFinalPossession() != null) {
                MatchStatistics stats = MatchStatistics.builder()
                        .possessionHome(dto.getFinalPossession().getOrDefault("team_1", 0.0).floatValue())
                        .possessionAway(dto.getFinalPossession().getOrDefault("team_2", 0.0).floatValue())
                        .build();
                video.setStatistics(stats);
            }
            video.setStatut(StatutAnalyse.TERMINE);
            metadataRepository.save(video);
        });
    }

    private void markAsError(String videoId, String errorMsg) {
        metadataRepository.findById(videoId).ifPresent(video -> {
            video.setStatut(StatutAnalyse.ERREUR);
            metadataRepository.save(video);
        });
    }

    private void flushBuffer() {
        if (!positionBuffer.isEmpty()) {
            log.info("💾 Saving batch of {} positions...", positionBuffer.size());
            positionRepository.saveAll(positionBuffer);
            positionBuffer.clear();
        }
    }

    private void handleJerseyDiscovery(String videoId, FrameAnalysisDTO dto) {
        log.info(" New Jersey Detected: #{} for Player ID: {} in Video: {}",
                dto.getJerseyNumber(), dto.getPlayerId(), videoId);

        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/discovery/" + videoId, dto);
        }

         //updatePlayerInfo(videoId, dto);
    }
}