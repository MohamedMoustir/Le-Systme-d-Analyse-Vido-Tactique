package com.football.analyzer.application.mapper;


import com.football.analyzer.domain.entity.MatchStatistics;
import com.football.analyzer.presentation.dto.Response.FrameAnalysisDTO;
import com.football.analyzer.domain.entity.PositionData;
import com.football.analyzer.presentation.dto.Response.PlayerDataDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PythonDataMapper {

    public List<PositionData> mapFrameToPositions(FrameAnalysisDTO analysis, String videoId) {
        List<PositionData> positions = new ArrayList<>();

        Long frameNum = analysis.getFrameNum() != null ? Long.valueOf(analysis.getFrameNum()) : 0L;

        if (analysis.getPlayers() != null) {
            for (PlayerDataDTO p : analysis.getPlayers()) {

                int x = 0;
                int y = 0;
                if (p.getBbox() != null && p.getBbox().size() == 4) {
                    x = (int) ((p.getBbox().get(0) + p.getBbox().get(2)) / 2);
                    y = (int) (p.getBbox().get(3).doubleValue());
                }

                PositionData.PositionDataBuilder builder = PositionData.builder()
                        .videoId(videoId)
                        .frameNumber(frameNum)
                        .joueurId(p.getId() != null ? Long.valueOf(p.getId()) : null)
                        .teamSide(p.getTeam() == 1 ? "HOME" : "AWAY")
                        .pixelX((double) x)
                        .pixelY((double) y)
                        .hasBall(p.getHasBall() != null ? p.getHasBall() : false);

                if (p.getSpeedKmh() != null) {
                    builder.speedKmh(p.getSpeedKmh().floatValue());
                }

                if (p.getDistanceM() != null) {
                    builder.distanceParcourue(p.getDistanceM().floatValue());
                }

                if (p.getPositionField() != null && p.getPositionField().size() >= 2) {
                    builder.FieldX(p.getPositionField().get(0).floatValue());
                    builder.FieldY(p.getPositionField().get(1).floatValue());
                }

                positions.add(builder.build());
            }
        }


        return positions;
    }


    public MatchStatistics mapFinalStats(FrameAnalysisDTO analysis) {
        if (analysis.getFinalPossession() == null) {
            return MatchStatistics.builder().build();
        }

        return MatchStatistics.builder()
                .possessionHome(analysis.getFinalPossession().getOrDefault("1", 50.0).floatValue())
                .possessionAway(analysis.getFinalPossession().getOrDefault("2", 50.0).floatValue())

                .build();
    }
}