package com.savt.backend.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "joueurs")
public class Joueur {
    @Id
    private String id;

    private String nomComplet;
    private Integer numeroMaillot;
    private String poste;

    private String equipeId;
}