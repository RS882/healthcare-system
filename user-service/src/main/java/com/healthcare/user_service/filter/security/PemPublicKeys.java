package com.healthcare.user_service.filter.security;


import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.Base64;

public final class PemPublicKeys {

    private PemPublicKeys() {}

    public static RSAPublicKey readRsaPublicKey(String pem) {
        try {
            String normalized = normalizePem(pem);

            // X.509 / SubjectPublicKeyInfo
            if (normalized.contains("BEGIN PUBLIC KEY")) {
                byte[] der = decodePemBody(normalized,
                        "-----BEGIN PUBLIC KEY-----",
                        "-----END PUBLIC KEY-----");
                return (RSAPublicKey) KeyFactory.getInstance("RSA")
                        .generatePublic(new X509EncodedKeySpec(der));
            }

            // PKCS#1 (BEGIN RSA PUBLIC KEY) => RSA key structure (modulus, exponent)
            if (normalized.contains("BEGIN RSA PUBLIC KEY")) {
                byte[] pkcs1Der = decodePemBody(normalized,
                        "-----BEGIN RSA PUBLIC KEY-----",
                        "-----END RSA PUBLIC KEY-----");

                RSAPublicKeySpec spec = parsePkcs1RsaPublicKeySpec(pkcs1Der);

                return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
            }

            throw new IllegalArgumentException("Unsupported PEM format: expected PUBLIC KEY or RSA PUBLIC KEY");

        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA public key (PEM)", e);
        }
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

        String s = pem.strip();

        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1);
        }

        return s.replace("\\n", "\n");
    }

    /**
     * Minimal ASN.1 DER parser for: RSAPublicKey ::= SEQUENCE { modulus INTEGER, publicExponent INTEGER }
     */
    private static RSAPublicKeySpec parsePkcs1RsaPublicKeySpec(byte[] der) {
        DerReader r = new DerReader(der);

        r.expectTag(0x30); // SEQUENCE
        r.readLength();

        BigInteger modulus = r.readInteger();
        BigInteger exponent = r.readInteger();

        return new RSAPublicKeySpec(modulus, exponent);
    }

    /**
     * Very small DER reader (enough for RSA public key PKCS#1).
     */
    private static final class DerReader {
        private final byte[] data;
        private int pos = 0;

        DerReader(byte[] data) { this.data = data; }

        void expectTag(int tag) {
            int t = readByte() & 0xFF;
            if (t != tag) throw new IllegalArgumentException("Unexpected DER tag: " + t + ", expected: " + tag);
        }

        int readLength() {
            int b = readByte() & 0xFF;
            if (b < 0x80) return b;

            int count = b & 0x7F;
            if (count == 0 || count > 4) throw new IllegalArgumentException("Invalid DER length");
            int len = 0;
            for (int i = 0; i < count; i++) {
                len = (len << 8) | (readByte() & 0xFF);
            }
            return len;
        }

        BigInteger readInteger() {
            int tag = readByte() & 0xFF;
            if (tag != 0x02) throw new IllegalArgumentException("Expected INTEGER (0x02), got: " + tag);

            int len = readLength();
            byte[] val = readBytes(len);

            return new BigInteger(1, val);
        }

        private byte readByte() {
            if (pos >= data.length) throw new IllegalArgumentException("DER: out of bounds");
            return data[pos++];
        }

        private byte[] readBytes(int len) {
            if (pos + len > data.length) throw new IllegalArgumentException("DER: out of bounds");
            byte[] out = new byte[len];
            System.arraycopy(data, pos, out, 0, len);
            pos += len;
            return out;
        }
    }
}
