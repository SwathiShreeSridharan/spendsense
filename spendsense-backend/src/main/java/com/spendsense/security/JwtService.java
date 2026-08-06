package com.spendsense.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expiration;
    private final Clock clock;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            Clock clock
    ) {
        if (expiration <= 0) {
            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero"
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expiration = expiration;
        this.clock = clock;
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = clock.instant();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plusMillis(expiration)
                        )
                )
                .signWith(signingKey)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .clock(
                        () -> Date.from(clock.instant())
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {
        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            Date expirationDate = claims.getExpiration();

            return userDetails.getUsername().equals(username)
                    && expirationDate != null
                    && expirationDate
                    .toInstant()
                    .isAfter(clock.instant());

        } catch (
                JwtException
                | IllegalArgumentException exception
        ) {
            return false;
        }
    }

}
