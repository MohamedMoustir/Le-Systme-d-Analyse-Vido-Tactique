package com.football.analyzer.domain.enums;

import lombok.Getter;

@Getter
public enum SubscriptionPlan {
    FREE(1),
    PREMIUM(100);

    private final int maxVideos;

    SubscriptionPlan(int maxVideos) {
        this.maxVideos = maxVideos;
    }
}

