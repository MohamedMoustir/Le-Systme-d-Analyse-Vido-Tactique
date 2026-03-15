package com.football.analyzer.domain.entity;

import com.football.analyzer.domain.enums.StatutAnalyse;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
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
    private Float fps;
    private Long totalFrames;
    @Indexed
    private String uploaderId;
    @DBRef
    private Equipe homeTeam;

    @DBRef
    private Equipe awayTeam;
    private MatchStatistics statistics;

    @Builder.Default
    private List<EvenementMatch> events = new ArrayList<>();
    @Builder.Default
    private List<Commentaire> commentaires = new ArrayList<>();
}
