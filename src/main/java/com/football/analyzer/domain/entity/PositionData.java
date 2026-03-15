package com.football.analyzer.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder @NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(def = "{'videoId': 1, 'frameNumber': 1}")
@Document(collection = "tracking_data")
public class PositionData {

    @Id
    private String id;

    @Indexed
    private String videoId;

    private Long frameNumber;

    private Double pixelX;
    private Double pixelY;

    private Float FieldX;
    private Float FieldY;

    private Float speedKmh;
    private Float distanceParcourue;
    private Boolean hasBall;

    private Long joueurId;
    private String teamSide;
}
