package com.football.analyzer.presentation.controller;

import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.enums.SubscriptionPlan;
import com.football.analyzer.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ADMIN') or hasRole('ADMIN')")
public class AdminPaymentController {

    private final UserRepository userRepository;

    @GetMapping("/payments/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        long totalUsers = userRepository.count();

        long premiumUsers = userRepository.findAll().stream()
                .filter(u -> "PREMIUM".equals(u.getPlan().name()))
                .count();

        long freeUsers = totalUsers - premiumUsers;
        long mrr = premiumUsers * 29;
        long totalRevenue = mrr * 12;

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", totalUsers);
        stats.put("premiumUsers", premiumUsers);
        stats.put("activePremiumUsers", premiumUsers);

        stats.put("freeUsers", freeUsers);
        stats.put("mrr", mrr);

        stats.put("totalRevenue", totalRevenue);
        stats.put("revenue", totalRevenue);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/payments/transactions")
    public ResponseEntity<List<Map<String, Object>>> getRecentTransactions() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> transactions = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> data = new HashMap<>();

            data.put("userId", user.getId());
            data.put("nom", user.getNom());
            data.put("email", user.getEmail());
            data.put("plan", user.getPlan().name());
            data.put("status", user.isActivated() ? "Actif" : "Inactif");
            data.put("expiration", "Illimité");

            transactions.add(data);
        }

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}/plan")
    public ResponseEntity<Map<String, String>> updateUserPlan(
            @PathVariable String id,
            @RequestParam String newPlan) {

        log.info("L'Admin change le plan de l'utilisateur {} vers {}", id, newPlan);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.setPlan(SubscriptionPlan.valueOf(newPlan.toUpperCase()));
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Le plan a été mis à jour avec succès");
        response.put("newPlan", String.valueOf(user.getPlan()));

        return ResponseEntity.ok(response);
    }
}