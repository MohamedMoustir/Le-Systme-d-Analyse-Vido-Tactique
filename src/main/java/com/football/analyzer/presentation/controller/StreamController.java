package com.football.analyzer.presentation.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.analyzer.application.service.VideoService;
import com.football.analyzer.domain.entity.VideoMetadata;
import com.football.analyzer.domain.enums.StatutAnalyse;
import com.football.analyzer.domain.repository.VideoRepository;
import com.football.analyzer.presentation.dto.response.FrameAnalysisDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class StreamController {

    private static final Logger logger = LoggerFactory.getLogger(StreamController.class);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final SimpMessagingTemplate messagingTemplate;
    private final VideoService videoService;
    private final VideoRepository videoRepository;

    @Value("${python.executable:python}") private String pythonExecutable;
    @Value("${python.script.path:python/main.py}") private String pythonScriptPath;
    @Value("${yolo.model.path:models/yolov8m_soccer.pt}") private String modelPath;
    @Value("${teams.config.path:config/teams.json}") private String teamsConfigPath;
    @Value("${upload.path:uploads}") private String uploadDir;

    private Process streamProcess;
    private final AtomicBoolean isStreaming = new AtomicBoolean(false);
    private final AtomicReference<String> currentVideoId = new AtomicReference<>(null);

    public StreamController(SimpMessagingTemplate messagingTemplate, VideoService videoService, VideoRepository videoRepository) {
        this.messagingTemplate = messagingTemplate;
        this.videoService = videoService;
        this.videoRepository = videoRepository;
    }

    @GetMapping(value = "/stream/mjpeg")
    public void streamMjpeg(
            @RequestParam String videoPath,
            @RequestParam(required = false) String videoId,
            @RequestParam(defaultValue = "cpu") String device,
            HttpServletResponse response) {

        // 1. كنسجلو الـ ID ديال الفيديو ملي كيبدا الستريم
        currentVideoId.set(videoId);

        // كنحبسو أي ستريم قديم، بلا ما نحدثو الداتابيز دابا
        stopStreamInternal(false);

        response.setContentType("multipart/x-mixed-replace; boundary=frame");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        isStreaming.set(true);

        try {
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) {
                File uploadFolder = new File(System.getProperty("user.dir"), uploadDir);
                videoFile = new File(uploadFolder, new File(videoPath).getName());
            }

            if (!videoFile.exists()) {
                logger.error("CRITICAL: Video file not found at: {}", videoFile.getAbsolutePath());
                return;
            }

            String absolutePath = videoFile.getAbsolutePath();
            String outputAbsolutePath = absolutePath.replace(".mp4", "_analyzed.mp4");

            logger.info("Starting Unified Python Stream for: {}", absolutePath);

            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable, "-u", pythonScriptPath,
                    "-i", absolutePath, "-o", outputAbsolutePath,
                    "-m", modelPath, "-d", device, "-t", teamsConfigPath,
                    "--stream-mjpeg"
            );

            pb.directory(new File(System.getProperty("user.dir")));
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            streamProcess = pb.start();

            // Thread اللي كيقرى الإحصائيات من Python
            new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(streamProcess.getErrorStream(), "UTF-8"))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        if (line.trim().startsWith("{")) {
                            String topic = (videoId == null) ? "/topic/analysis/stream" : "/topic/analysis/" + videoId;
                            messagingTemplate.convertAndSend(topic, line);

                            if (videoId != null) {
                                try {
                                    JsonNode node = objectMapper.readTree(line);
                                    String msgType = node.has("type") ? node.get("type").asText() : "unknown";

                                    if ("frame_analysis".equals(msgType)) {
                                        FrameAnalysisDTO dto = objectMapper.treeToValue(node, FrameAnalysisDTO.class);
                                        if (dto.getFrameNum() != null && dto.getFrameNum() % 30 == 0) {
                                            logger.info("💾 Saved Frame {} info for Video {}", dto.getFrameNum(), videoId);
                                        }
                                        videoService.processAnalysisMessage(videoId, dto);
                                    }
                                } catch (Exception e) {
                                    logger.error("❌ Error parsing JSON", e);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error("Error reading analysis stream", e);
                }
            }).start();

            // قراءة الصور (Frames) من Python
            InputStream inputStream = new BufferedInputStream(streamProcess.getInputStream());
            OutputStream outputStream = response.getOutputStream();
            ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();
            int prevByte = -1, currByte;
            boolean isRecording = false;

            while (isStreaming.get() && streamProcess.isAlive()) {
                currByte = inputStream.read();
                if (currByte == -1) break;

                if (prevByte == 0xFF && currByte == 0xD8) {
                    isRecording = true;
                    frameBuffer.reset();
                    frameBuffer.write(0xFF);
                    frameBuffer.write(0xD8);
                    prevByte = currByte;
                    continue;
                }

                if (isRecording) {
                    frameBuffer.write(currByte);
                    if (prevByte == 0xFF && currByte == 0xD9) {
                        isRecording = false;
                        byte[] imageBytes = frameBuffer.toByteArray();
                        try {
                            outputStream.write(("--frame\r\n").getBytes());
                            outputStream.write(("Content-Type: image/jpeg\r\n").getBytes());
                            outputStream.write(("Content-Length: " + imageBytes.length + "\r\n\r\n").getBytes());
                            outputStream.write(imageBytes);
                            outputStream.write(("\r\n").getBytes());
                            outputStream.flush();
                        } catch (IOException e) {
                            break;
                        }
                        frameBuffer.reset();
                    }
                }
                prevByte = currByte;
            }

        } catch (Exception e) {
            logger.error("Streaming error: {}", e.getMessage());
        } finally {
            // 2. ملي كيسالي الستريم، كنعطيو الميثود الـ ID ديريكت باش تحدث الداتابيز
            updateVideoStatusToTermine(videoId);
            stopStreamInternal(false);
        }
    }

    @PostMapping("/api/stream/stop")
    public ResponseEntity<String> stopStreamApi() {
        // 3. ملي اليوزر كيكليكي على Stop، كنجبدو الـ ID وكنحدثو الداتابيز قبل ما نقتلو Python
        String videoId = currentVideoId.get();
        if (videoId != null) {
            updateVideoStatusToTermine(videoId);
        }
        stopStreamInternal(true);
        return ResponseEntity.ok("{\"success\": true}");
    }

    // ميثود داخلية باش نحبسو بايثون بلا ما نتسببو فـ حلقة مفرغة
    private void stopStreamInternal(boolean clearId) {
        isStreaming.set(false);
        if (streamProcess != null) {
            streamProcess.destroyForcibly();
            streamProcess = null;
            logger.info("Stream process killed.");
        }
        if (clearId) {
            currentVideoId.set(null);
        }
    }

    // 4. الميثود الجديدة اللي كتاخد الـ ID وكترد الفيديو TERMINE وتزيد _analyzed
    private void updateVideoStatusToTermine(String videoId) {
        if (videoId == null || videoId.trim().isEmpty()) {
            return;
        }

        try {
            VideoMetadata video = videoRepository.findById(videoId).orElse(null);
            if (video != null && video.getStatut() != StatutAnalyse.TERMINE) {
                video.setStatut(StatutAnalyse.TERMINE);

                String originalName = video.getUrlFichier();
                if (originalName != null && !originalName.contains("_analyzed")) {
                    String analyzedName = originalName.toLowerCase().endsWith(".mp4")
                            ? originalName.substring(0, originalName.length() - 4) + "_analyzed.mp4"
                            : originalName + "_analyzed.mp4";
                    video.setUrlFichier(analyzedName);
                }

                videoRepository.save(video);
                logger.info("✅ [SUCCESS] Video {} status updated to TERMINE and path changed to _analyzed.mp4", videoId);
            }
        } catch (Exception e) {
            logger.error("❌ [ERROR] Failed to update video status in DB", e);
        }
    }
}