package com.football.analyzer.presentation.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutSessionRequest {
    private String planName; // "PREMIUM"
    private String successUrl;
    private String cancelUrl;
}

