package com.football.analyzer.infrastructure.filter;


import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.enums.Role;
import com.football.analyzer.infrastructure.service.AutheticatedUser;
import com.football.analyzer.infrastructure.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JWTFilter  extends OncePerRequestFilter {

    private final JwtService jwtService;


    public JWTFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.equals("/api/auth/login") ||
                path.equals("/api/auth/register") ||
                path.startsWith("/api/public/") ||
                path.startsWith("/api/analysis/stream/") ||
                path.startsWith("/api/stream/") ||
                path.startsWith("/stream/") ||
                path.startsWith("/api/uploads/") ||
                path.startsWith("/api/stream/stop") ||
                path.startsWith("/api/stripe/webhook") ||
                path.startsWith("/ws-analysis/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if(null == authHeader || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);

            if(jwtService.isRefreshToken(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtService.extractUsername(jwt);
            String role = jwtService.extractRoles(jwt);

            if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if(jwtService.isTokenValid(jwt)) {
                    User user = createUserFromToken(email, role);
                    AutheticatedUser userDetails = new AutheticatedUser(user);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // Check this

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set User authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }



    private User createUserFromToken(String email, String role) {
        return User.builder()
                .email(email)
                .role(Role.valueOf(role))
                .build();
    }
}
