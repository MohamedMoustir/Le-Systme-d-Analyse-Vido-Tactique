package com.football.analyzer.presentation.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoAdminDTO {
    private String id;
    private String titre;
    private String coachName;
    private Long CountVideo;
    private String status;
    private LocalDateTime dateUpload;
    private double sizeMb;
}
