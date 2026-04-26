package com.backend.global.util;

import com.github.f4b6a3.ulid.UlidCreator;

/**
 * ULID (Universally Unique Lexicographically Sortable Identifier) generator.
 * ULIDs are time-sortable and URL-safe, making them ideal for distributed systems.
 */
public final class UlidGenerator {

    private UlidGenerator() {
        // Utility class
    }

    /**
     * Generates a new ULID string.
     * @return A 26-character ULID string
     */
    public static String generate() {
        return UlidCreator.getUlid().toString();
    }
}
