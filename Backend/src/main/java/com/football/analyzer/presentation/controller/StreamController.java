package com.football.analyzer.presentation.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.analyzer.application.service.VideoService;
import com.football.analyzer.domain.entity.VideoMetadata;
import com.football.analyzer.domain.enums.StatutAnalyse;
import com.football.analyzer.domain.repository.VideoRepository;
import com.football.analyzer.presentation.dto.Response.FrameAnalysisDTO;
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

  // pour les messages json
  private final ObjectMapper objectMapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  // pour WebSockets
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


  @GetMapping(value = "/api/analysis/stream/mjpeg")
  public void streamMjpeg(
    @RequestParam String videoPath,
    @RequestParam(required = false) String videoId,
    @RequestParam(defaultValue = "cpu") String device,
    HttpServletResponse response) {

    currentVideoId.set(videoId);
    stopStreamInternal(false);
    setupResponseHeaders(response);
    isStreaming.set(true);

    try {
      File videoFile = resolveVideoFile(videoPath);
      if (videoFile == null) return;

      String absolutePath = videoFile.getAbsolutePath();
      String outputAbsolutePath = absolutePath.replace(".mp4", "_analyzed.mp4");
      logger.info("Starting Unified Python Stream for: {}", absolutePath);

      streamProcess = startPythonProcess(absolutePath, outputAbsolutePath, device);

      startAnalysisListenerThread(streamProcess, videoId);

      streamFramesToResponse(streamProcess, response);

    } catch (Exception e) {
      logger.error("Streaming error: {}", e.getMessage());
    } finally {
      updateVideoStatusToTermine(videoId);
      stopStreamInternal(false);
    }
  }

  @PostMapping("/api/stream/stop")
  public ResponseEntity<String> stopStreamApi() {
    String videoId = currentVideoId.get();
    if (videoId != null) {
      updateVideoStatusToTermine(videoId);
    }
    stopStreamInternal(true);
    return ResponseEntity.ok("{\"success\": true}");
  }

  private void setupResponseHeaders(HttpServletResponse response) {
    response.setContentType("multipart/x-mixed-replace; boundary=frame");
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
  }

  private File resolveVideoFile(String videoPath) {
    File videoFile = new File(videoPath);
    if (!videoFile.exists()) {
      File uploadFolder = new File(System.getProperty("user.dir"), uploadDir);
      videoFile = new File(uploadFolder, new File(videoPath).getName());
    }

    if (!videoFile.exists()) {
      logger.error("CRITICAL: Video file not found at: {}", videoFile.getAbsolutePath());
      return null;
    }
    return videoFile;
  }

  private Process startPythonProcess(String inputPath, String outputPath, String device) throws IOException {
    ProcessBuilder pb = new ProcessBuilder(
      pythonExecutable, "-u", pythonScriptPath,
      "-i", inputPath, "-o", outputPath,
      "-m", modelPath, "-d", device, "-t", teamsConfigPath,
      "--stream-mjpeg"
    );

    pb.directory(new File(System.getProperty("user.dir")));
    pb.environment().put("PYTHONIOENCODING", "utf-8");

    return pb.start();
  }

  private void startAnalysisListenerThread(Process process, String videoId) {
    new Thread(() -> {
      try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
        String line;
        while ((line = errorReader.readLine()) != null) {
          if (line.trim().startsWith("{")) {
            String topic = (videoId == null) ? "/topic/analysis/stream" : "/topic/analysis/" + videoId;
            messagingTemplate.convertAndSend(topic, line);

            if (videoId != null) {
              processJsonMessage(line, videoId);
            }
          }
        }
      } catch (IOException e) {
        logger.error("Error reading analysis stream", e);
      }
    }).start();
  }

  private void processJsonMessage(String jsonLine, String videoId) {
    try {
      JsonNode node = objectMapper.readTree(jsonLine);
      String msgType = node.has("type") ? node.get("type").asText() : "unknown";

      if ("frame_analysis".equals(msgType)) {
        FrameAnalysisDTO dto = objectMapper.treeToValue(node, FrameAnalysisDTO.class);
        if (dto.getFrameNum() != null && dto.getFrameNum() % 30 == 0) {
          logger.info(" Saved Frame {} info for Video {}", dto.getFrameNum(), videoId);
        }
        videoService.processAnalysisMessage(videoId, dto);
      }
    } catch (Exception e) {
      logger.error(" Error parsing JSON", e);
    }
  }

  private void streamFramesToResponse(Process process, HttpServletResponse response) throws IOException {
    InputStream inputStream = new BufferedInputStream(process.getInputStream());
    OutputStream outputStream = response.getOutputStream();
    ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();

    int prevByte = -1, currByte;
    boolean isRecording = false;

    while (isStreaming.get() && process.isAlive()) {
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
  }

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
        logger.info(" [SUCCESS] Video {} status updated to TERMINE and path changed to _analyzed.mp4", videoId);
      }
    } catch (Exception e) {
      logger.error(" [ERROR] Failed to update video status in DB", e);
    }
  }
}
