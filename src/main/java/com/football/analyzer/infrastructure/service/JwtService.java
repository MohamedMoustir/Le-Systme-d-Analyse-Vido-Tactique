package com.football.analyzer.infrastructure.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secret;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;


    public String generateToken(String email, String role) {
        return JWT.create()
                .withSubject(email)
                .withClaim("role", role)
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .sign(Algorithm.HMAC256(secret));

    }

    public String generateRefreshToken(String email) {
        return JWT.create()
                .withSubject(email)
                .withClaim("type", "refresh")
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public String extractUsername(String token) {
        return verifyToken(token).getSubject();
    }

    public String extractRoles(String token) {
        return verifyToken(token).getClaim("role").asString();
    }

    public String extractTokenType(String token) {
        return verifyToken(token).getClaim("type").asString();
    }

    public String extractTokenId(String token) {
        return verifyToken(token).getId();
    }

    public Date extractExpiration(String token) {
        return verifyToken(token).getExpiresAt();
    }


    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(extractTokenType(token));
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            return !verifyToken(token).getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    private DecodedJWT verifyToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build().verify(token);
        } catch (JWTVerificationException ex) {
            throw new JWTVerificationException("Invalid JWT token");
        }
    }
}
