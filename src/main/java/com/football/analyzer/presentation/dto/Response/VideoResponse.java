package com.football.analyzer.presentation.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoResponse {
    private String id;
    private String titre;

    private String statut;
    private String urlFichier;

    private Float fps;
    private Long totalFrames;

    private TeamSummaryDTO homeTeam;
    private TeamSummaryDTO awayTeam;

    private MatchStatsDTO stats;

    private List<EventResponse> events;

    @Data @Builder
    public static class TeamSummaryDTO {
        private String id;
        private String name;
        private String logo;
        private String color;
    }

    @Data @Builder
    public static class MatchStatsDTO {
        private Float possessionHome;
        private Float possessionAway;
        private Float distanceHome;
        private Float distanceAway;
    }
}