package com.football.analyzer.presentation.dto.Response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class VideoAdminDTO {
    private String id;
    private String titre;
    private String coachName;
    private String status; 
    private LocalDateTime dateUpload;
    private double sizeMb;
}