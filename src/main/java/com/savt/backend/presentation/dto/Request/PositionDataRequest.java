package com.savt.backend.presentation.dto.Request;

import lombok.Data;

@Data
public class PositionDataRequest {
    private Long frameNumber;
    private Float x;
    private Float y;
    private Float speed;
    private String joueurId;

}