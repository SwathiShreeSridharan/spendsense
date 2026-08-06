package com.spendsense.security;

import com.spendsense.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "01234567890123456789012345678901";

    private static final long EXPIRATION =
            3_600_000L;

    private static final Instant NOW =
            Instant.parse("2026-08-02T10:00:00Z");

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                NOW,
                ZoneId.of("UTC")
        );

        jwtService = new JwtService(
                SECRET,
                EXPIRATION,
                fixedClock
        );

        user = new User(
                "Swathi",
                "swathi@gmail.com",
                "9876543210",
                "hashed-password"
        );
    }

    @Test
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken(user);

        assertEquals(
                "swathi@gmail.com",
                jwtService.extractUsername(token)
        );

        assertEquals(
                Date.from(NOW.plusMillis(EXPIRATION)),
                jwtService.extractExpiration(token)
        );

        assertTrue(
                jwtService.isTokenValid(token, user)
        );
    }

    @Test
    void shouldReturnFalseWhenTokenBelongsToDifferentUser() {
        String token = jwtService.generateToken(user);

        User anotherUser = new User(
                "Another User",
                "another@gmail.com",
                "9999999999",
                "hashed-password"
        );

        boolean valid =
                jwtService.isTokenValid(
                        token,
                        anotherUser
                );

        assertFalse(valid);
    }

    @Test
    void shouldRejectNonPositiveExpirationConfiguration() {
        Clock fixedClock = Clock.fixed(
                NOW,
                ZoneId.of("UTC")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(
                        SECRET,
                        0,
                        fixedClock
                )
        );
    }

    @Test
    void shouldReturnFalseWhenTokenIsExpired() {
        String token = jwtService.generateToken(user);

        Clock futureClock = Clock.fixed(
                NOW.plusMillis(EXPIRATION + 1_000),
                ZoneId.of("UTC")
        );

        JwtService futureJwtService =
                new JwtService(
                        SECRET,
                        EXPIRATION,
                        futureClock
                );

        boolean valid =
                futureJwtService.isTokenValid(
                        token,
                        user
                );

        assertFalse(valid);
    }
}
