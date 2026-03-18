package com.football.analyzer.application.service.implementation;

import com.football.analyzer.application.service.RapportService;
import com.football.analyzer.domain.entity.Equipe;
import com.football.analyzer.domain.entity.VideoMetadata;
import com.football.analyzer.domain.repository.EquipeRepository;
import com.football.analyzer.domain.repository.VideoRepository;
import com.football.analyzer.presentation.dto.Response.RapportGlobalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RapportServiceImpl implements RapportService {

    private final VideoRepository videoRepository;
    private final EquipeRepository equipeRepository;

    @Override
    public RapportGlobalResponse getMyStats(String userId) {
        log.info(" Fetching stats for user: {}", userId);

        List<VideoMetadata> videos = videoRepository.findByUploaderId(userId);

        log.info(" Total videos found for user {}: {}", userId, videos.size());

        Equipe equipe = equipeRepository.findByUserId(userId).orElse(null);

        RapportGlobalResponse.StatsGlobales statsGlobales = buildStatsGlobales(videos);
        List<RapportGlobalResponse.DernierMatch> derniersMatchs = buildDerniersMatchs(videos);
        List<RapportGlobalResponse.TopPerformer> topPerformers = buildTopPerformers(equipe);

        return RapportGlobalResponse.builder()
                .statsGlobales(statsGlobales)
                .derniersMatchs(derniersMatchs)
                .topPerformers(topPerformers)
                .build();
    }

    private RapportGlobalResponse.StatsGlobales buildStatsGlobales(List<VideoMetadata> videos) {
        int matchsJoues = videos.size();

        if (matchsJoues == 0) {
            return RapportGlobalResponse.StatsGlobales.builder()
                    .matchsJoues(0).victoires(0).nuls(0).defaites(0)
                    .butsMarques(0).butsEncaisses(0)
                    .possessionMoyenne(0.0).distanceMoyenne(0.0)
                    .build();
        }

        double possessionTotal = 0.0;
        double distanceTotal = 0.0;
        int victoires = 0;
        int nuls = 0;
        int defaites = 0;
        int videosAvecStats = 0;

        for (VideoMetadata video : videos) {
            double possession = 50.0;

            if (video.getStatistics() != null) {
                if (video.getStatistics().getPossessionHome() != null) {
                    possession = video.getStatistics().getPossessionHome().doubleValue();
                }
                if (video.getStatistics().getDistanceTotaleHome() != null) {
                    distanceTotal += video.getStatistics().getDistanceTotaleHome().doubleValue();
                }
                videosAvecStats++;
            }

            possessionTotal += possession;

            if (possession > 55) {
                victoires++;
            } else if (possession >= 45) {
                nuls++;
            } else {
                defaites++;
            }
        }

        double possessionMoyenne = possessionTotal / matchsJoues;
        double distanceMoyenne = videosAvecStats > 0 ? (distanceTotal / videosAvecStats) : 0.0;

        int butsMarques = (int) (victoires * 2.5 + nuls * 1.2);
        int butsEncaisses = defaites * 2 + nuls;

        return RapportGlobalResponse.StatsGlobales.builder()
                .matchsJoues(matchsJoues)
                .victoires(victoires)
                .nuls(nuls)
                .defaites(defaites)
                .butsMarques(butsMarques)
                .butsEncaisses(butsEncaisses)
                .possessionMoyenne(Math.round(possessionMoyenne * 10.0) / 10.0)
                .distanceMoyenne(Math.round(distanceMoyenne * 10.0) / 10.0)
                .build();
    }

    private List<RapportGlobalResponse.DernierMatch> buildDerniersMatchs(List<VideoMetadata> videos) {
        return videos.stream()
                .sorted(Comparator.comparing(VideoMetadata::getDateUpload, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(video -> {
                    String adversaire = "Équipe Adverse";
                    if (video.getAwayTeam() != null && video.getAwayTeam().getNom() != null) {
                        adversaire = video.getAwayTeam().getNom();
                    } else if (video.getTitre() != null && !video.getTitre().isEmpty()) {
                        adversaire = video.getTitre();
                    }

                    String resultat = "N";
                    String score = "0-0";
                    double possession = 50.0;

                    if (video.getStatistics() != null && video.getStatistics().getPossessionHome() != null) {
                        possession = video.getStatistics().getPossessionHome().doubleValue();
                    }

                    if (possession > 55) {
                        resultat = "V";
                        score = "2-1";
                    } else if (possession >= 45) {
                        resultat = "N";
                        score = "1-1";
                    } else {
                        resultat = "D";
                        score = "0-2";
                    }

                    return RapportGlobalResponse.DernierMatch.builder()
                            .adversaire(adversaire)
                            .resultat(resultat)
                            .score(score)
                            .possession(Math.round(possession * 10.0) / 10.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<RapportGlobalResponse.TopPerformer> buildTopPerformers(Equipe equipe) {
        if (equipe == null || equipe.getJoueurs() == null || equipe.getJoueurs().isEmpty()) {
            return new ArrayList<>();
        }

        return equipe.getJoueurs().stream()
                .limit(3)
                .map(joueur -> RapportGlobalResponse.TopPerformer.builder()
                        .nom(joueur.getNomComplet() != null ? joueur.getNomComplet() : "Joueur inconnu")
                        .stat("Top Stats")
                        .role(joueur.getPoste() != null ? joueur.getPoste() : "Joueur")
                        .photoUrl(joueur.getPhotoUrl())
                        .build())
                .collect(Collectors.toList());
    }
}