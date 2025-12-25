package com.savt.backend.domain.entity;

import com.savt.backend.domain.enums.StatutAnalyse;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection="analyses")
@Builder
public class VideoMetadata {

    @Id
    private String id;

    private String titre;
    private String urlFichier;
    private Long dureeSecondes;
    private LocalDateTime dateUpload;

    private StatutAnalyse statut;

    @Indexed
    private String uploaderId;

    private List<EvenementMatch> events = new ArrayList<>();
    private List<Commentaire> commentaires = new ArrayList<>();
}
