package com.savt.backend.application.mapper;

import com.savt.backend.domain.entity.Equipe;
import com.savt.backend.domain.entity.Joueur;
import com.savt.backend.presentation.dto.Request.EquipeRequest;
import com.savt.backend.presentation.dto.Request.JoueurRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    @Mapping(target = "id", ignore = true)
    Equipe toEquipe(EquipeRequest request);

    @Mapping(target = "id", ignore = true)
    Joueur toJoueur(JoueurRequest request);
}