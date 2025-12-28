package com.savt.backend.presentation.dto.Request;
import lombok.Data;

@Data
public class JoueurRequest {
    private String nomComplet;
    private Integer numeroMaillot;
    private String poste;
    private String equipeId;
}