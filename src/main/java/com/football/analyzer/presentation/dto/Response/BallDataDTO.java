package com.football.analyzer.presentation.dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public  class BallDataDTO {

    private List<Double> bbox;

    @JsonProperty("position_pixels")
    private List<Integer> positionPixels;

    @JsonProperty("position_field")
    private List<Double> positionField;
}