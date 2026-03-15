package com.football.analyzer.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerDataDTO {

    private Integer id;
    private Integer team;
    private List<Double> bbox;

    @JsonProperty("position_pixels")
    private List<Integer> positionPixels;

    @JsonProperty("position_field")
    private List<Double> positionField;

    @JsonProperty("speed_kmh")
    private Double speedKmh;

    @JsonProperty("distance_m")
    private Double distanceM;

    @JsonProperty("has_ball")
    private Boolean hasBall;

    @JsonProperty("jersey_number")
    private Integer jerseyNumber;

    @JsonProperty("player_name")
    private String playerName;
}
