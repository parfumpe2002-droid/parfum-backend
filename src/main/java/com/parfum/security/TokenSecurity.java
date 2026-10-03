package com.parfum.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class TokenSecurity {
    private TokenSecurity() {}

    public static String sha256(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Token vacío");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 no disponible", error);
        }
    }

    public static boolean matches(String raw, String expectedHash) {
        if (raw == null || raw.isBlank() || expectedHash == null || expectedHash.isBlank()) return false;
        byte[] actual = sha256(raw.trim()).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = expectedHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(actual, expected);
    }
}
