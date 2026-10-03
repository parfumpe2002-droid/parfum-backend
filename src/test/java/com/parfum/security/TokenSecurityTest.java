package com.parfum.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TokenSecurityTest {
    @Test
    void sha256IsStableAndDoesNotExposeRawToken() {
        String raw = "token-secreto-de-prueba";
        String hash = TokenSecurity.sha256(raw);
        assertEquals(64, hash.length());
        assertNotEquals(raw, hash);
        assertEquals(hash, TokenSecurity.sha256(raw));
    }

    @Test
    void matchesOnlyAcceptsCorrectToken() {
        String raw = "otro-token";
        String hash = TokenSecurity.sha256(raw);
        assertTrue(TokenSecurity.matches(raw, hash));
        assertFalse(TokenSecurity.matches("incorrecto", hash));
        assertFalse(TokenSecurity.matches(null, hash));
    }
}
