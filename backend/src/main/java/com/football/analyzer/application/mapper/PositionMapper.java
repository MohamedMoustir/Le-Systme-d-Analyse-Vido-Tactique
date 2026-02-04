package com.football.analyzer.application.mapper;

import com.football.analyzer.domain.entity.PositionData;
import com.football.analyzer.presentation.dto.Response.PositionResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PositionMapper {

    @Mapping(source = "frameNumber", target = "f")
    @Mapping(source = "pixelX", target = "x")
    @Mapping(source = "pixelY", target = "y")
    @Mapping(source = "speedKmh", target = "s")
    @Mapping(source = "hasBall", target = "b")
    @Mapping(source = "joueurId", target = "pid")
    @Mapping(source = "teamSide", target = "t")
    PositionResponse toResponse(PositionData entity);

}