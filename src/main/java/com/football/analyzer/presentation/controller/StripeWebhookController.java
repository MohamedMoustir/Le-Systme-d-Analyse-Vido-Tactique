package com.football.analyzer.presentation.controller;

import com.football.analyzer.infrastructure.service.StripeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final StripeService stripeService;
    private final ObjectMapper objectMapper;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;


    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Signature webhook invalide: {}", e.getMessage());
            return ResponseEntity.status(400).body("Signature invalide");
        } catch (Exception e) {
            log.error("Erreur lors du parsing du webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Erreur de parsing");
        }

        log.info("Evenement Stripe recu: {}", event.getType());

        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            Session session = null;

            Optional<StripeObject> stripeObject = dataObjectDeserializer.getObject();
            if (stripeObject.isPresent()) {
                session = (Session) stripeObject.get();
            } else {
                try {
                    String rawJson = dataObjectDeserializer.getRawJson();
                    if (rawJson != null && !rawJson.isEmpty()) {
                        JsonNode root = objectMapper.readTree(rawJson);
                        Session fallbackSession = new Session();
                        fallbackSession.setId(root.path("id").asText(null));

                        JsonNode metadataNode = root.path("metadata");
                        if (metadataNode.isObject()) {
                            Map<String, String> metadata = new HashMap<>();
                            metadataNode.fields().forEachRemaining(entry -> metadata.put(entry.getKey(), entry.getValue().asText()));
                            fallbackSession.setMetadata(metadata);
                        }

                        session = fallbackSession;
                        log.info("Session reconstruite via raw JSON pour eventId={}", event.getId());
                    } else {
                        log.warn("Raw JSON indisponible pour checkout.session.completed. eventId={}", event.getId());
                    }
                } catch (Exception e) {
                    log.error("Echec du fallback de deserialisation Session pour eventId={}: {}", event.getId(), e.getMessage(), e);
                }
            }

            if (session != null) {
                handleCheckoutSessionCompleted(session);
            } else {
                log.warn("checkout.session.completed recu mais Session absente/non deserialisable. eventId={}", event.getId());
            }
        }

        return ResponseEntity.ok("Webhook recu");
    }

    /**
     * Traiter l'événement checkout.session.completed
     */
    private void handleCheckoutSessionCompleted(Session session) {
        log.info("Paiement reussi pour la session: {}", session.getId());

        Map<String, String> metadata = session.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            log.warn("Aucune metadata Stripe recue pour la session {}", session.getId());
            return;
        }

        log.info("Metadata Stripe complete pour session {}: {}", session.getId(), metadata);

        String userId = metadata.get("userId");
        String userEmail = metadata.get("userEmail");
        String planName = metadata.get("planName");

        log.info("Metadata extraite -> userId: {}, userEmail: {}, planName: {}", userId, userEmail, planName);

        try {
            if (userId != null && !userId.isEmpty()) {
                stripeService.upgradeToPremium(userId);
                log.info("Plan updated: userId {} passe en PREMIUM", userId);
            } else if (userEmail != null && !userEmail.isEmpty()) {
                stripeService.upgradeToPremiumByEmail(userEmail);
                log.info("Plan updated via email: {}", userEmail);
            } else {
                log.error("Aucun userId ni userEmail trouve dans les metadata de la session {}", session.getId());
            }
        } catch (Exception e) {
            log.error("Erreur lors de la mise a jour du plan: {}", e.getMessage(), e);
        }
    }
}
