package com.savt.backend.domain.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="equipes")
@Data
@Builder
public class Equipe {

    @Id
    private String id;

    private String nom;
    private String logoUrl;
    private String couleurHex;
}
