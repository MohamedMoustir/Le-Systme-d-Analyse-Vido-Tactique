package com.savt.backend.application.service.implementation;

import com.savt.backend.application.mapper.TeamMapper;
import com.savt.backend.application.service.TeamManagementService;
import com.savt.backend.domain.entity.Equipe;
import com.savt.backend.domain.entity.Joueur;
import com.savt.backend.domain.repository.EquipeRepository;
import com.savt.backend.domain.repository.JoueurRepository;
import com.savt.backend.presentation.dto.Request.EquipeRequest;
import com.savt.backend.presentation.dto.Request.JoueurRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamManagementServiceImpl  implements TeamManagementService{

    private final EquipeRepository equipeRepository;
    private final JoueurRepository joueurRepository;
    private final TeamMapper teamMapper;
    @Override
    public Equipe createEquipe(EquipeRequest request) {
        if (equipeRepository.existsByNom(request.getNom())) {
            throw new RuntimeException("Equipe avec ce nom existe déjà !");
        }
        Equipe equipe = teamMapper.toEquipe(request);

        return equipeRepository.save(equipe);
    }

    @Override
    public List<Equipe> getAllEquipes() {
        return equipeRepository.findAll();
    }

    @Override
    public Joueur addJoueurToTeam(JoueurRequest request) {
        if (!equipeRepository.existsById(request.getEquipeId())) {
            throw new RuntimeException("Equipe introuvable avec l'ID : " + request.getEquipeId());
        }

        Joueur joueur = teamMapper.toJoueur(request);

        return joueurRepository.save(joueur);
    }

    @Override
    public List<Joueur> getJoueursByEquipe(String equipeId) {
        return joueurRepository.findByEquipeId(equipeId);
    }
}
