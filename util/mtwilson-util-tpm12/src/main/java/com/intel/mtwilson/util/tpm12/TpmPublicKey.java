/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.tpm12;

import com.intel.dcsg.cpg.io.ByteArray;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

/**
 * A 2048 bit RSA public key exported from the TPM is 284 bytes long. The first
 * 28 bytes are the header and the remaining 256 bytes are the public key
 * modulus. The TCG specifies the public key exponent to be 2^16+1 = 65537 =
 * 0x00010001
 *
 * @author jbuhacoff
 */
public class TpmPublicKey {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmPublicKey.class);
    private static final int RSA_2048_ENCODED_LENGTH = 284;
//    private static final int RSA_2048_HEADER_LENGTH = 28;
    private static final byte[] RSA_2048_HEADER = new byte[]{
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x01,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0c, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00
    };
    private static final BigInteger TCG_RSA_EXPONENT = BigInteger.valueOf(65537);
    private final BigInteger modulus;

    protected TpmPublicKey(BigInteger modulus) {
        this.modulus = modulus;
    }

    public static TpmPublicKey valueOf(byte[] encoded) {
        log.debug("Encoded key length: {}", encoded.length);
        if (encoded.length == RSA_2048_ENCODED_LENGTH) {
            byte[] header = ByteArray.subarray(encoded, 0, RSA_2048_HEADER.length);
            log.debug("Header length: {}", header.length);
            if (Arrays.equals(RSA_2048_HEADER, header)) {
                return new TpmPublicKey(new BigInteger(1, ByteArray.subarray(encoded, RSA_2048_HEADER.length, 256)));
            }
        }
        throw new IllegalArgumentException("Invalid TPM RSA 2048-bit public key");
    }

    protected static byte[] toByteArray(BigInteger number, int length) {
        ByteArray array = new ByteArray(number);
        if (array.length() == length + 1 && array.getBytes()[0] == 0) {
            return array.subarray(1).getBytes(); // skip leading zero
        }
        if (array.length() < length) {
            int padding = length - array.length();
//            log.debug("Adding padding: {} bytes", padding);
            ByteArray zero = new ByteArray(new byte[padding]);
            return ByteArray.concat(zero, array);
        }
        if (array.length() != length) {
            throw new IllegalArgumentException(String.format("Cannot convert %s to %d bytes", number.toString(), length));
        }
        return array.getBytes();
    }

    public byte[] getEncoded() {
        return ByteArray.concat(RSA_2048_HEADER, toByteArray(modulus, 256));
    }

    public PublicKey toPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");// throws NoSuchAlgorithmException
        PublicKey publicKey = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, TCG_RSA_EXPONENT)); // throws InvalidKeySpecException
        return publicKey;
    }
}
