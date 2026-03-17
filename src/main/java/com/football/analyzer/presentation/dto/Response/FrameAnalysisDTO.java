package com.football.analyzer.presentation.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FrameAnalysisDTO {

    private String type;

    @JsonProperty("frame_num")
    private Integer frameNum;
    @JsonProperty("player_id")
    private Integer playerId;
    @JsonProperty("players_count")
    private Integer playersCount;

    @JsonProperty("team_1_count")
    private Integer team1Count;

    @JsonProperty("team_2_count")
    private Integer team2Count;

    @JsonProperty("jersey_number")
    private Integer jerseyNumber;

    @JsonProperty("referees_count")
    private Integer refereesCount;

    @JsonProperty("ball_detected")
    private Boolean ballDetected;

    @JsonProperty("ball_holder_id")
    private Integer ballHolderId;

    private Map<String, Double> possession;

    private List<PlayerDataDTO> players;

    private BallDataDTO ball;

    private String path;

    @JsonProperty("event")
    private String event;

    @JsonProperty("output_path")
    private String outputPath;

    @JsonProperty("annotation_enabled")
    private Boolean annotationEnabled;

    @JsonProperty("total_frames")
    private Integer totalFrames;

    private Double fps;
    private Integer width;
    private Integer height;

    @JsonProperty("output_video")
    private String outputVideo;

    private Double percent;

    private String message;
    private String error;

    @JsonProperty("final_possession")
    private Map<String, Double> finalPossession;

    @JsonProperty("total_frames_processed")
    private Integer totalFramesProcessed;

    private List<PlayerDataDTO> playerData;
    private BallDataDTO BallData;
}
