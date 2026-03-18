package com.football.analyzer.infrastructure.service;

import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.enums.SubscriptionPlan;
import com.football.analyzer.domain.exception.BusinessLogicException;
import com.football.analyzer.domain.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class StripeService {

    private final UserRepository userRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.price.premium}")
    private String premiumPriceId;

    public StripeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public String createCheckoutSession(String userId, String planName, String successUrl, String cancelUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException("Utilisateur non trouvé"));

        // Initialiser Stripe avec la clé API
        Stripe.apiKey = stripeApiKey;

        try {
            // Construire les paramètres de la session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setCustomerEmail(user.getEmail())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(premiumPriceId)
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putMetadata("userId", userId)
                    .putMetadata("planName", planName)
                    .putMetadata("userEmail", user.getEmail())
                    .build();

            // Créer la session
            Session session = Session.create(params);

            log.info(" Stripe Checkout Session créée: {} pour userId: {}", session.getId(), userId);

            return session.getUrl(); // URL de redirection vers la page de paiement Stripe

        } catch (StripeException e) {
            log.error(" Erreur lors de la création de la session Stripe: {}", e.getMessage(), e);
            throw new BusinessLogicException("Échec de la création de la session de paiement: " + e.getMessage());
        }
    }

    /**
     * Met à jour le plan d'abonnement de l'utilisateur
     */
    @Transactional
    public void upgradeToPremium(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException("Utilisateur non trouvé avec l'ID: " + userId));

        user.setPlan(SubscriptionPlan.PREMIUM);
        User savedUser = userRepository.save(user);

        log.info("Plan updated: utilisateur {} passe au plan {}", savedUser.getId(), savedUser.getPlan());
    }

    /**
     * Met à jour le plan d'abonnement de l'utilisateur par email
     */
    @Transactional
    public void upgradeToPremiumByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException("Utilisateur non trouvé avec l'email: " + email));

        user.setPlan(SubscriptionPlan.PREMIUM);
        User savedUser = userRepository.save(user);

        log.info(" Utilisateur {} (email: {}) passé au plan {}", savedUser.getId(), email, savedUser.getPlan());
    }
}
