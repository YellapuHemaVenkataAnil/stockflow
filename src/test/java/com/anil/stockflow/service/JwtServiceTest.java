package com.anil.stockflow.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.Clock;
import java.time.Instant;

import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString(
                "01234567890123456789012345678901"
                        .getBytes()
        );

        jwtService = new JwtService(secret, 3600000);
    }

    @Test
    void generateTokenAndExtractUsername() {
        String token = jwtService.generateToken("anil");

        assertNotNull(token);
        assertEquals("anil", jwtService.extractUsername(token));
    }

    @Test
    void validTokenAndMatchingUsernameReturnsTrue() {
        String token = jwtService.generateToken("anil");

        assertTrue(jwtService.isTokenValid(token, "anil"));
    }

    @Test
    void validTokenAndDifferentUsernameReturnsFalse() {
        String token = jwtService.generateToken("anil");

        assertFalse(jwtService.isTokenValid(token, "anotherUser"));
    }


    @Test
    void expiredTokenThrowsException() throws InterruptedException {
        JwtService shortLivedService = new JwtService(
                Base64.getEncoder().encodeToString(
                        "01234567890123456789012345678901"
                                .getBytes()
                ),
                1
        );

        String token = shortLivedService.generateToken("anil");
        Thread.sleep(1100);

        assertThrows(
                io.jsonwebtoken.ExpiredJwtException.class,
                () -> shortLivedService.isTokenValid(token, "anil")
        );
    }


    @Test
    void malformedTokenThrowsException() {
        assertThrows(
                JwtException.class,
                () -> jwtService.extractUsername("not-a-valid-token")
        );
    }
        
    @Test
    void nullUsernameThrowsException() {
        String token = jwtService.generateToken("anil");

        assertThrows(
                NullPointerException.class,
                () -> jwtService.isTokenValid(token, null)
        );
    }

    @Test
    void matchingUsernameWithExpiredTokenReturnsFalse() {
        String secret = Base64.getEncoder().encodeToString(
                "01234567890123456789012345678901".getBytes()
        );

        Clock fixedClock = () -> Date.from(Instant.EPOCH);

        JwtService serviceWithFixedClock =
                new JwtService(secret, 3600000, fixedClock);

        String token = serviceWithFixedClock.generateToken("anil");

        assertFalse(
                serviceWithFixedClock.isTokenValid(token, "anil")
        );
    }
        

}