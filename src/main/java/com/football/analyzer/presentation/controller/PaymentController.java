package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.utils.UserUtils;
import com.football.analyzer.infrastructure.service.StripeService;
import com.football.analyzer.presentation.dto.Request.CheckoutSessionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final StripeService stripeService;
    private final UserUtils userUtils;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestBody CheckoutSessionRequest request) {

        String userId = userUtils.getCurrentUser().getId();

        log.info("📝 Création d'une session checkout pour l'utilisateur: {} (plan: {})", userId, request.getPlanName());

        String checkoutUrl = stripeService.createCheckoutSession(
                userId,
                request.getPlanName(),
                request.getSuccessUrl(),
                request.getCancelUrl()
        );

        Map<String, String> response = new HashMap<>();
        response.put("checkoutUrl", checkoutUrl);

        return ResponseEntity.ok(response);
    }
}

