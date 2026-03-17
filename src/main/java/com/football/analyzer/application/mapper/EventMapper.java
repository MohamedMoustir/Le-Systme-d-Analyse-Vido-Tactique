package com.football.analyzer.application.mapper;

import com.football.analyzer.domain.entity.EvenementMatch;
import com.football.analyzer.presentation.dto.Response.EventResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(source = "tempsVideo", target = "time")
    @Mapping(source = "frameNumber", target = "frame")
    @Mapping(source = "nomJoueur", target = "playerName")
    EventResponse toResponse(EvenementMatch entity);
}
