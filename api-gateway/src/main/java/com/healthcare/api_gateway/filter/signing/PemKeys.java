package com.healthcare.api_gateway.filter.signing;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public final class PemKeys {

    private PemKeys() {}

    public static PrivateKey readRsaPrivateKey(String pem) {
        try {
            String normalized = normalizePem(pem);

            // PKCS#8
            if (normalized.contains("BEGIN PRIVATE KEY")) {
                byte[] der = decodePemBody(normalized,
                        "-----BEGIN PRIVATE KEY-----",
                        "-----END PRIVATE KEY-----");
                return generatePkcs8(der);
            }

            // PKCS#1 (BEGIN RSA PRIVATE KEY) -> convert to PKCS#8 wrapper
            if (normalized.contains("BEGIN RSA PRIVATE KEY")) {
                byte[] pkcs1Der = decodePemBody(normalized,
                        "-----BEGIN RSA PRIVATE KEY-----",
                        "-----END RSA PRIVATE KEY-----");

                // Wrap PKCS#1 in PKCS#8 (minimal DER wrapper)
                byte[] pkcs8Der = Pkcs1ToPkcs8.wrapRsaPkcs1ToPkcs8(pkcs1Der);
                return generatePkcs8(pkcs8Der);
            }

            throw new IllegalArgumentException("Unsupported PEM format: expected PKCS#8 or PKCS#1 RSA private key");

        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA private key (PEM)", e);
        }
    }

    private static PrivateKey generatePkcs8(byte[] pkcs8Der) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pkcs8Der);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static byte[] decodePemBody(String pem, String begin, String end) {
        String body = pem
                .replace(begin, "")
                .replace(end, "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(body);
    }

    private static String normalizePem(String pem) {
        if (pem == null) return "";

        String s = pem.trim();

        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1);
        }

        s = s.replace("\\n", "\n");

        return s;
    }
}
