package com.football.analyzer.domain.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "evenements_match")
public class EvenementMatch {
    @Id
    private String id;
    private String videoId;
    private String tempsVideo;
    private Long frameNumber;
    private String description;
    private String type;
    private String nomJoueur;
    private Integer numeroJoueur;
    private String nomEquipe;

}