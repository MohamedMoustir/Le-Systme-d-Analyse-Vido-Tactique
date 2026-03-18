package com.football.analyzer.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data  @NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection="equipes")
public class Equipe {

    @Id
    private String id;

    private String nom;
    private String logoUrl;
    private String couleurHex;
    private String userId;
    @DBRef
    private List<Joueur> joueurs;
}
