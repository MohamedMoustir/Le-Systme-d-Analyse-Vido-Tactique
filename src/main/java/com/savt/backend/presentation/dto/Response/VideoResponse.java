package com.savt.backend.presentation.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoResponse {
    private String id;
    private String titre;
    private String urlFichier;
    private Long dureeSecondes;
    private String dateUpload;
    private String statut;

    private UserResponseDTO uploader;

    private List<EventResponse> events;
    private List<CommentResponse> commentaires;
}