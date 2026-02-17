package com.healthcare.api_gateway.filter.signing;

final class Pkcs1ToPkcs8 {

    private Pkcs1ToPkcs8() {}

    /**
     * Wraps PKCS#1 RSA private key DER bytes into PKCS#8 DER.
     * PKCS#8 structure: PrivateKeyInfo ::= SEQUENCE { version, algId, privateKey OCTET STRING }
     * algId for RSA: 1.2.840.113549.1.1.1 with NULL params
     */
    static byte[] wrapRsaPkcs1ToPkcs8(byte[] pkcs1Der) {
        // DER encoding pieces:
        // SEQUENCE
        //   INTEGER 0
        //   SEQUENCE (AlgorithmIdentifier)
        //     OID rsaEncryption (1.2.840.113549.1.1.1)
        //     NULL
        //   OCTET STRING (pkcs1Der)

        byte[] algId = new byte[] {
                0x30, 0x0D,
                0x06, 0x09,
                0x2A, (byte)0x86, 0x48, (byte)0x86, (byte)0xF7, 0x0D, 0x01, 0x01, 0x01,
                0x05, 0x00
        };

        byte[] version = new byte[] { 0x02, 0x01, 0x00 };

        byte[] privateKeyOctetString = derOctetString(pkcs1Der);

        byte[] seqBody = concat(version, algId, privateKeyOctetString);
        return derSequence(seqBody);
    }

    private static byte[] derSequence(byte[] body) {
        return concat(new byte[] { 0x30 }, derLength(body.length), body);
    }

    private static byte[] derOctetString(byte[] body) {
        return concat(new byte[] { 0x04 }, derLength(body.length), body);
    }

    private static byte[] derLength(int len) {
        if (len < 0x80) {
            return new byte[] { (byte) len };
        }
        // support lengths up to 65535 (enough for RSA keys)
        if (len <= 0xFF) {
            return new byte[] { (byte) 0x81, (byte) len };
        }
        return new byte[] { (byte) 0x82, (byte) (len >> 8), (byte) (len & 0xFF) };
    }

    private static byte[] concat(byte[]... parts) {
        int total = 0;
        for (byte[] p : parts) total += p.length;
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, pos, p.length);
            pos += p.length;
        }
        return out;
    }
}
