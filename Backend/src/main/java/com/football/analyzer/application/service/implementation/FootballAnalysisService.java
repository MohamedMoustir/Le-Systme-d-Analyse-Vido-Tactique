package com.football.analyzer.application.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.analyzer.domain.entity.VideoMetadata;
import com.football.analyzer.domain.enums.StatutAnalyse;
import com.football.analyzer.domain.repository.PositionRepository;
import com.football.analyzer.domain.repository.VideoRepository;
import com.football.analyzer.presentation.dto.Response.FrameAnalysisDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class FootballAnalysisService {

    private final VideoRepository videoRepository;
    private final PositionRepository positionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final VideoServiceImpl videoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${python.executable:python}") private String pythonExecutable;
    @Value("${python.script.path:python/football_analyzer.py}") private String pythonScriptPath;
    @Value("${yolo.model.path:models/best.pt}") private String modelPath;
    @Value("${teams.config.path:config/teams.json}") private String teamsConfigPath;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final Map<String, Process> activeProcesses = new ConcurrentHashMap<>();

    @Async
    public void startAnalysis(String videoId) {
        if (isRunning.get()) {
            log.warn("Analysis already running. Please wait.");
            return;
        }

        VideoMetadata video = videoRepository.findById(videoId).orElse(null);
        if (video == null) {
            log.error("Video not found: {}", videoId);
            return;
        }

        positionRepository.deleteByVideoId(videoId);
        updateStatus(video, StatutAnalyse.EN_COURS);

        isRunning.set(true);

        String originalFileName = video.getUrlFichier();
        String annotatedFileName = originalFileName.replace(".mp4", "_annotated.mp4");

        String inputPath = new File("uploads/" + originalFileName).getAbsolutePath();
        String outputPath = new File("uploads/" + annotatedFileName).getAbsolutePath();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable, "-u", pythonScriptPath,
                    "-i", inputPath,
                    "-o", outputPath,
                    "-m", modelPath,
                    "--teams", teamsConfigPath,
                    "-d", "cpu"
            );
            pb.redirectErrorStream(true);

            log.info("Starting Python Analysis for Video: {}", videoId);
            Process process = pb.start();

            activeProcesses.put(videoId, process);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().startsWith("{")) {
                        log.debug("[PYTHON]: {}", line);
                        continue;
                    }

                    try {
                        FrameAnalysisDTO dto = objectMapper.readValue(line, FrameAnalysisDTO.class);
                        messagingTemplate.convertAndSend("/topic/analysis/" + videoId, dto);
                        videoService.processAnalysisMessage(videoId, dto);
                    } catch (Exception e) {
                        log.warn(" Failed to parse Python JSON: {}", line);
                    }
                }
            }

            int exitCode = process.waitFor();
            activeProcesses.remove(videoId);

            File annotatedVideoFile = new File(outputPath);
            if (annotatedVideoFile.exists() && annotatedVideoFile.length() > 0) {
                log.info(" Annotated video successfully created and saved!");

                video.setUrlFichier("/api/uploads/" + annotatedFileName);

                updateStatus(video, StatutAnalyse.TERMINE);
            } else {
                log.error("Python finished (Code: {}), but annotated video was NOT created.", exitCode);
                updateStatus(video, StatutAnalyse.ERREUR);
            }

        } catch (Exception e) {
            log.error(" Analysis Exception", e);
            updateStatus(video, StatutAnalyse.ERREUR);
        } finally {
            isRunning.set(false);
            activeProcesses.remove(videoId);
        }
    }

    public void stopAnalysis(String videoId) {
        Process process = activeProcesses.get(videoId);
        if (process != null && process.isAlive()) {
            log.info("Stopping Analysis for Video: {}", videoId);

            process.destroy();
        } else {
            log.warn(" No active process found for video: {}", videoId);
        }
    }

    private void updateStatus(VideoMetadata video, StatutAnalyse statut) {
        video.setStatut(statut);
        videoRepository.save(video);
    }
}