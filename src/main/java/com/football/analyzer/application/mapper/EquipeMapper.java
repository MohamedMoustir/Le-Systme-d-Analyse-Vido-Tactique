package com.football.analyzer.application.mapper;

import com.football.analyzer.domain.entity.Equipe;
import com.football.analyzer.domain.entity.Joueur;
import com.football.analyzer.presentation.dto.Response.EquipeResponse;
import com.football.analyzer.presentation.dto.Response.JoueurResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface EquipeMapper {
    EquipeResponse toDto(Equipe equipe);

    JoueurResponse toJoueurDto(Joueur joueur);
}