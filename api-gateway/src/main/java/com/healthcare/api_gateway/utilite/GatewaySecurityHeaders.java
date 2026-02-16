package com.healthcare.api_gateway.utilite;

import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GatewaySecurityHeaders {

    private GatewaySecurityHeaders() {
    }

    /**
     * Remove headers that match given names (case-insensitive).
     */
    public static void removeByNames(HttpHeaders headers, List<String> names) {
        if (headers == null || names == null || names.isEmpty()) return;

        List<String> keys = new ArrayList<>(headers.keySet());
        for (String key : keys) {
            for (String name : names) {
                if (equalsIgnoreCase(key, name)) {
                    headers.remove(key);
                    break;
                }
            }
        }
    }

    public static void removeByNames(HttpHeaders headers, String name) {
        if (headers == null || name == null || name.isBlank()) return;

        List<String> keys = new ArrayList<>(headers.keySet());
        for (String key : keys) {
            if (equalsIgnoreCase(key, name)) {
                headers.remove(key);
            }
        }
    }

    /**
     * Remove headers that start with any of the given prefixes (case-insensitive).
     */
    public static void removeByPrefixes(HttpHeaders headers, List<String> prefixes) {
        if (headers == null || prefixes == null || prefixes.isEmpty()) return;

        List<String> keys = new ArrayList<>(headers.keySet());
        for (String key : keys) {
            for (String prefix : prefixes) {
                if (startsWithIgnoreCase(key, prefix)) {
                    headers.remove(key);
                    break;
                }
            }
        }
    }

    public static void removeByPrefixes(HttpHeaders headers, String prefix) {
        if (headers == null || prefix == null || prefix.isBlank()) return;

        List<String> keys = new ArrayList<>(headers.keySet());
        for (String key : keys) {
            if (startsWithIgnoreCase(key, prefix)) {
                headers.remove(key);
            }
        }
    }

    /**
     * Set trusted headers (overwrites existing values).
     */
    public static void setTrusted(HttpHeaders headers, Map<String, String> trusted) {
        if (headers == null || trusted == null || trusted.isEmpty()) return;

        trusted.forEach((k, v) -> {
            if (k != null && !k.isBlank() && v != null && !v.isBlank()) {
                headers.set(k, v);
            }
        });
    }

    public static void setTrusted(HttpHeaders headers, String name, String value) {
        if (headers == null) return;
        if (name == null || name.isBlank()) return;
        if (value == null || value.isBlank()) return;

        headers.set(name, value);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        if (value == null || prefix == null) return false;
        if (value.length() < prefix.length()) return false;
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
