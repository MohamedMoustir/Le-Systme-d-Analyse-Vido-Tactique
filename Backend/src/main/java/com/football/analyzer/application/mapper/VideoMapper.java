package com.football.analyzer.application.mapper;


import com.football.analyzer.domain.entity.Equipe;
import com.football.analyzer.domain.entity.MatchStatistics;
import com.football.analyzer.domain.entity.VideoMetadata;
import com.football.analyzer.presentation.dto.Response.VideoAdminDTO;
import com.football.analyzer.presentation.dto.Response.VideoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, EventMapper.class})
public interface VideoMapper {
    @Mapping(source = "titre", target = "titre")
    @Mapping(source = "statut", target = "statut")
    @Mapping(source = "statistics", target = "stats")
    @Mapping(source = "events", target = "events")
    @Mapping(source = "homeTeam", target = "homeTeam", qualifiedByName = "mapTeamSummary")
    @Mapping(source = "awayTeam", target = "awayTeam", qualifiedByName = "mapTeamSummary")
    VideoResponse toResponse(VideoMetadata entity);

    @Named("mapTeamSummary")
    @Mapping(source = "nom", target = "name")
    @Mapping(source = "logoUrl", target = "logo")
    @Mapping(source = "couleurHex", target = "color")
    VideoResponse.TeamSummaryDTO toTeamSummary(Equipe equipe);

    @Mapping(source = "distanceTotaleHome", target = "distanceHome")
    @Mapping(source = "distanceTotaleAway", target = "distanceAway")
    VideoResponse.MatchStatsDTO toStatsDTO(MatchStatistics stats);

    List<VideoAdminDTO> toResponseList(List<VideoMetadata> metadata);
}
