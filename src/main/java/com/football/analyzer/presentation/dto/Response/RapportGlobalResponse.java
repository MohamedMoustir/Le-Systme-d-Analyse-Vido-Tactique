package com.football.analyzer.presentation.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RapportGlobalResponse {

    private StatsGlobales statsGlobales;
    private List<DernierMatch> derniersMatchs;
    private List<TopPerformer> topPerformers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsGlobales {
        private Integer matchsJoues;
        private Integer victoires;
        private Integer nuls;
        private Integer defaites;
        private Integer butsMarques;
        private Integer butsEncaisses;
        private Double possessionMoyenne;
        private Double distanceMoyenne;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DernierMatch {
        private String adversaire;
        private String resultat;
        private String score;
        private Double possession;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformer {
        private String nom;
        private String stat;
        private String role;
        private String photoUrl;
    }
}

