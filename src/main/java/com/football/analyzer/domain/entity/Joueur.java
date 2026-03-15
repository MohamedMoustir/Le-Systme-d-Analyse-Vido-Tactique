package com.football.analyzer.domain.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;



@Data @Builder @NoArgsConstructor
@AllArgsConstructor
@Document(collection = "joueurs")
public class Joueur {
    @Id
    private String id;
    private String nomComplet;
    private Integer numeroMaillot;
    private String poste;
    private String photoUrl;

}