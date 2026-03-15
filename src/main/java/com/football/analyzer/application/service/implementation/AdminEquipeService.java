package com.football.analyzer.application.service.implementation;

import com.football.analyzer.domain.entity.Equipe;
import com.football.analyzer.domain.repository.EquipeRepository;
import com.football.analyzer.presentation.dto.request.EquipeCreateDTO;
import com.football.analyzer.presentation.dto.response.EquipeAdminDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminEquipeService {

    private final EquipeRepository equipeRepository;

    public List<EquipeAdminDTO> getAllEquipes() {
        return equipeRepository.findAll().stream()
                .map(eq -> EquipeAdminDTO.builder()
                        .id(eq.getId())
                        .nom(eq.getNom())
                        .logoUrl(eq.getLogoUrl())
                        .couleurPrimaire(eq.getCouleurHex())
                        .build())
                .collect(Collectors.toList());
    }

    public EquipeAdminDTO createEquipe(EquipeCreateDTO dto) {
        Equipe equipe = new Equipe();
        equipe.setNom(dto.getNom());
        equipe.setLogoUrl(dto.getLogoUrl());
        equipe.setCouleurHex(dto.getCouleurPrimaire());

        Equipe savedEquipe = equipeRepository.save(equipe);

        return EquipeAdminDTO.builder()
                .id(savedEquipe.getId())
                .nom(savedEquipe.getNom())
                .logoUrl(savedEquipe.getLogoUrl())
                .couleurPrimaire(savedEquipe.getCouleurHex())
                .build();
    }

    public void deleteEquipe(String id) {
        equipeRepository.deleteById(id);
    }
}