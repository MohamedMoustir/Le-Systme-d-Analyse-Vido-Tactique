package com.football.analyzer.presentation.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.analyzer.application.service.VideoService;
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

@RestController
public class StreamController {

    private static final Logger logger = LoggerFactory.getLogger(StreamController.class);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final SimpMessagingTemplate messagingTemplate;
    private final VideoService videoService;

    @Value("${python.executable:python}") private String pythonExecutable;
    @Value("${python.script.path:python/football_analyzer.py}") private String pythonScriptPath;
    @Value("${yolo.model.path:models/best.pt}") private String modelPath;
    @Value("${teams.config.path:config/teams.json}") private String teamsConfigPath;
    @Value("${upload.path:uploads}")
    private String uploadDir;

    private Process streamProcess;
    private final AtomicBoolean isStreaming = new AtomicBoolean(false);

    public StreamController(SimpMessagingTemplate messagingTemplate, VideoService videoService) {
        this.messagingTemplate = messagingTemplate;
        this.videoService = videoService;
    }

    @GetMapping(value = "/stream/mjpeg")
    public void streamMjpeg(
            @RequestParam String videoPath,
            @RequestParam(required = false) String videoId,
            @RequestParam(defaultValue = "cpu") String device,
            HttpServletResponse response) {

        stopStream();

        response.setContentType("multipart/x-mixed-replace; boundary=frame");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        isStreaming.set(true);

        try {
            File videoFile = new File(videoPath);

            if (!videoFile.exists()) {
                File uploadFolder = new File(System.getProperty("user.dir"), uploadDir);

                String fileName = new File(videoPath).getName();

                videoFile = new File(uploadFolder, fileName);
            }

            if (!videoFile.exists()) {
                logger.error("CRITICAL: Video file not found at: {}", videoFile.getAbsolutePath());
                return;
            }

            String absolutePath = videoFile.getAbsolutePath();
            logger.info("Starting Unified Python Stream for: {}", absolutePath);

            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    "-u",
                    pythonScriptPath,
                    "-i", absolutePath,
                    "-m", modelPath,
                    "-d", device,
                    "--teams", teamsConfigPath,
                    "--stream-mjpeg"
            );


            pb.directory(new File(System.getProperty("user.dir")));
            pb.environment().put("PYTHONIOENCODING", "utf-8");


            streamProcess = pb.start();

            new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(streamProcess.getErrorStream(), "UTF-8"))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {

                        if (line.trim().startsWith("{")) {

                            if (videoId == null) {
                                logger.error(" VideoID is NULL! Cannot save to DB.");
                                continue;
                            }

                            messagingTemplate.convertAndSend("/topic/analysis/" + videoId, line);

                            try {
                                FrameAnalysisDTO dto = objectMapper.readValue(line, FrameAnalysisDTO.class);

                                if (dto.getFrameNum() % 30 == 0) {
                                    logger.info("💾 Saving Frame {} for Video {}", dto.getFrameNum(), videoId);
                                }

                                videoService.processAnalysisMessage(videoId, dto);

                            } catch (Exception e) {
                                logger.error(" DB SAVE ERROR: " + e.getMessage());
                                logger.error("Bad JSON: " + line);
                            }
                        }
                        else if(line.contains("Error") || line.contains("Exception")) {
                            logger.error("[Python Error]: {}", line);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Error reading analysis stream", e);
                }
            }).start();


            InputStream inputStream = new BufferedInputStream(streamProcess.getInputStream());
            OutputStream outputStream = response.getOutputStream();

            ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();
            int prevByte = -1;
            int currByte;
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
                            logger.warn("Browser disconnected.");
                            break;
                        }
                        frameBuffer.reset();
                    }
                }
                prevByte = currByte;
            }

        } catch (Exception e) {
            logger.error(" Streaming error: {}", e.getMessage());
        } finally {
            stopStream();
        }
    }

    @PostMapping("/api/stream/stop")
    public ResponseEntity<String> stopStream() {
        isStreaming.set(false);
        if (streamProcess != null) {
            streamProcess.destroyForcibly();
            streamProcess = null;
            logger.info("Stream process killed.");
        }
        return ResponseEntity.ok("{\"success\": true}");
    }
}