package com.savt.backend.application.service;

import com.savt.backend.domain.entity.Equipe;
import com.savt.backend.domain.entity.Joueur;
import com.savt.backend.presentation.dto.Request.EquipeRequest;
import com.savt.backend.presentation.dto.Request.JoueurRequest;

import java.util.List;

public interface TeamManagementService {
    Equipe createEquipe(EquipeRequest request);
    List<Equipe> getAllEquipes();
    Joueur addJoueurToTeam(JoueurRequest request);
    List<Joueur> getJoueursByEquipe(String equipeId);
}
