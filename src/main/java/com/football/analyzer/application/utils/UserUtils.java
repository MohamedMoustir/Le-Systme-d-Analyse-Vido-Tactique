package com.football.analyzer.application.utils;

import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.repository.UserRepository;
import com.football.analyzer.infrastructure.service.AutheticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    private final UserRepository userRepository;

    public UserUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("No authenticated user found (User is likely Anonymous)");
        }

        Object principal = authentication.getPrincipal();
        String email = null;

        if (principal instanceof AutheticatedUser) {
            email = ((AutheticatedUser) principal).getUsername();
        } else if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        }

        if (email == null) {
            throw new RuntimeException("Could not extract email from SecurityContext");
        }


        String finalEmail = email;
        return userRepository.findByEmail(finalEmail)
                .orElseThrow(() -> new RuntimeException("User not found in DB with email: " + finalEmail));
    }
}