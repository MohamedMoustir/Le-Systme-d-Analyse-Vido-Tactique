package com.football.analyzer.presentation.dto.Response;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoueurUpdateDTO {

    @NotBlank(message = "Nom complet est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom doit contenir 3")
    private String nomComplet;

    @Min(value = 1, message = "Numéro de maillot doit être au moins 1")
    @Max(value = 99, message = "Numéro de maillot doit être inférieur à 100")
    private Integer numeroMaillot;

    @NotBlank(message = "Le poste est obligatoire")
    private String poste;

    private String photoUrl;
}