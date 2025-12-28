package com.savt.backend.infrastructure.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_REFRESH_TOKENS_PREFIX = "user_refresh_tokens:";



    public void storeRefreshtoken(String refreshToken) {
        String tokenId = jwtService.extractTokenId(refreshToken);
        String email = jwtService.extractUsername(refreshToken);
        long expiration = jwtService.extractExpiration(refreshToken).getTime() - System.currentTimeMillis();


        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + tokenId,
                email,
                expiration,
                TimeUnit.MILLISECONDS
        );


        redisTemplate.opsForSet().add(USER_REFRESH_TOKENS_PREFIX + email, tokenId);
        redisTemplate.expire(USER_REFRESH_TOKENS_PREFIX + email, expiration, TimeUnit.MILLISECONDS);
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            String tokenId = jwtService.extractTokenId(refreshToken);
            String storedEmail = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + tokenId);

            return storedEmail != null && jwtService.isRefreshToken(refreshToken) && jwtService.isTokenValid(refreshToken);
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromRefreshToken(String refreshToken) {
        try {
            String tokenId = jwtService.extractTokenId(refreshToken);
            return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + tokenId);
        } catch (Exception e) {
            return  null;
        }
    }


    public void revokeRefreshToken(String refreshToken) {
        try {
            String tokenId = jwtService.extractTokenId(refreshToken);
            String username = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + tokenId);

            if(username != null) {
                redisTemplate.delete(REFRESH_TOKEN_PREFIX + tokenId);

                redisTemplate.opsForSet().remove(USER_REFRESH_TOKENS_PREFIX + username, tokenId);
            }
        } catch (Exception e) {
            log.error("Error While Revoking the RefreshToken");
        }
    }

    public void revokeAllUserTokens(String username) {
        var tokenIds = redisTemplate.opsForSet().members(USER_REFRESH_TOKENS_PREFIX + username);

        if(tokenIds != null) {
            tokenIds.forEach(tokenId ->
                    redisTemplate.delete(REFRESH_TOKEN_PREFIX + tokenId));

            redisTemplate.delete(USER_REFRESH_TOKENS_PREFIX + username);
        }
    }
    public String getRefreshToken(String email) {
        Object token = redisTemplate.opsForValue().get("refresh_token:" + email);
        return token != null ? token.toString() : null;
    }
}
