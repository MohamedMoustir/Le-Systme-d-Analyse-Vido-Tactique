package com.football.analyzer.application.service;

import com.football.analyzer.presentation.dto.Response.EquipeResponse;
import org.springframework.web.multipart.MultipartFile;

public interface EquipeService {
    EquipeResponse getMyTeam(String userId);

    EquipeResponse addJoueur(String userId, String nom, Integer numero, String poste, MultipartFile photo) throws Exception;

    EquipeResponse importCsv(String userId, MultipartFile file) throws Exception;

    EquipeResponse deleteJoueur(String userId, String joueurId) throws Exception;
        EquipeResponse createEquipe(String userId, String nomEquipe, String couleurHex);
}