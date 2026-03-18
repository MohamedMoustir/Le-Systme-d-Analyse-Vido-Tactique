package com.football.analyzer.application.service;

import com.football.analyzer.domain.entity.Joueur;
import com.football.analyzer.presentation.dto.Response.JoueurResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface JoueurService {
    JoueurResponseDTO updateJoueur(String id, String nomComplet, Integer numeroMaillot, String poste, MultipartFile photo) throws IOException;
    JoueurResponseDTO getJoueurById(String id);
    void deleteJoueur(String id);
}