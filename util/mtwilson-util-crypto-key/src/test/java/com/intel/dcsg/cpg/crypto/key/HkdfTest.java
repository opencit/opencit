/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases specified in RFC 5869 to test HKDF implementation
 *
 * @author jbuhacoff
 */
public class HkdfTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HkdfTest.class);

    /**
     * <pre>
     * Hash = SHA-256
     * IKM  = 0x0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b (22 octets)
     * salt = 0x000102030405060708090a0b0c (13 octets)
     * info = 0xf0f1f2f3f4f5f6f7f8f9 (10 octets)
     * L    = 42
     *
     * PRK  = 0x077709362c2e32df0ddc3f0dc47bba63
     * 90b6c73bb50f9c3122ec844ad7c2b3e5 (32 octets)
     * OKM  = 0x3cb25f25faacd57a90434f64d0362f2a
     * 2d2d0a90cf1a5a4c5db02d56ecc4c5bf
     * 34007208d5b887185865 (42 octets)
     * </pre>
     *
     */
    @Test
    public void testCase1() throws DecoderException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] ikm = Hex.decodeHex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b".toCharArray());
        assertEquals(22, ikm.length);
        byte[] salt = Hex.decodeHex("000102030405060708090a0b0c".toCharArray());
        assertEquals(13, salt.length);
        byte[] info = Hex.decodeHex("f0f1f2f3f4f5f6f7f8f9".toCharArray());
        assertEquals(10, info.length);
        int length = 42;
        HKDF hkdf = new HKDF("HmacSHA256");
        byte[] okm = hkdf.deriveKey(salt, ikm, length, info);
        assertEquals("3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865", Hex.encodeHexString(okm));
    }

    /**
     * <pre>
     * Hash = SHA-256
     * IKM  = 0x000102030405060708090a0b0c0d0e0f
     * 101112131415161718191a1b1c1d1e1f
     * 202122232425262728292a2b2c2d2e2f
     * 303132333435363738393a3b3c3d3e3f
     * 404142434445464748494a4b4c4d4e4f (80 octets)
     * salt = 0x606162636465666768696a6b6c6d6e6f
     * 707172737475767778797a7b7c7d7e7f
     * 808182838485868788898a8b8c8d8e8f
     * 909192939495969798999a9b9c9d9e9f
     * a0a1a2a3a4a5a6a7a8a9aaabacadaeaf (80 octets)
     * info = 0xb0b1b2b3b4b5b6b7b8b9babbbcbdbebf
     * c0c1c2c3c4c5c6c7c8c9cacbcccdcecf
     * d0d1d2d3d4d5d6d7d8d9dadbdcdddedf
     * e0e1e2e3e4e5e6e7e8e9eaebecedeeef
     * f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff (80 octets)
     * L    = 82
     *
     * PRK  = 0x06a6b88c5853361a06104c9ceb35b45c
     * ef760014904671014a193f40c15fc244 (32 octets)
     * OKM  = 0xb11e398dc80327a1c8e7f78c596a4934
     * 4f012eda2d4efad8a050cc4c19afa97c
     * 59045a99cac7827271cb41c65e590e09
     * da3275600c2f09b8367793a9aca3db71
     * cc30c58179ec3e87c14c01d5c1f3434f
     * 1d87 (82 octets)
     * </pre>
     *
     */
    @Test
    public void testCase2() throws DecoderException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] ikm = Hex.decodeHex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f404142434445464748494a4b4c4d4e4f".toCharArray());
        assertEquals(80, ikm.length);
        byte[] salt = Hex.decodeHex("606162636465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9fa0a1a2a3a4a5a6a7a8a9aaabacadaeaf".toCharArray());
        assertEquals(80, salt.length);
        byte[] info = Hex.decodeHex("b0b1b2b3b4b5b6b7b8b9babbbcbdbebfc0c1c2c3c4c5c6c7c8c9cacbcccdcecfd0d1d2d3d4d5d6d7d8d9dadbdcdddedfe0e1e2e3e4e5e6e7e8e9eaebecedeeeff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff".toCharArray());
        assertEquals(80, info.length);
        int length = 82;
        HKDF hkdf = new HKDF("HmacSHA256");
        byte[] okm = hkdf.deriveKey(salt, ikm, length, info);
        assertEquals("b11e398dc80327a1c8e7f78c596a49344f012eda2d4efad8a050cc4c19afa97c59045a99cac7827271cb41c65e590e09da3275600c2f09b8367793a9aca3db71cc30c58179ec3e87c14c01d5c1f3434f1d87", Hex.encodeHexString(okm));
    }

    /**
     * <pre>
     * Hash = SHA-256
     * IKM  = 0x0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b (22 octets)
     * salt = (0 octets)
     * info = (0 octets)
     * L    = 42
     *
     * PRK  = 0x19ef24a32c717b167f33a91d6f648bdf
     * 96596776afdb6377ac434c1c293ccb04 (32 octets)
     * OKM  = 0x8da4e775a563c18f715f802a063c5a31
     * b8a11f5c5ee1879ec3454e5f3c738d2d
     * 9d201395faa4b61a96c8 (42 octets)
     * </pre>
     *
     */
    @Test
    public void testCase3() throws DecoderException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] ikm = Hex.decodeHex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b".toCharArray());
        assertEquals(22, ikm.length);
        byte[] salt = new byte[0];
        byte[] info = new byte[0];
        int length = 42;
        HKDF hkdf = new HKDF("HmacSHA256");
        byte[] okm = hkdf.deriveKey(salt, ikm, length, info);
        assertEquals("8da4e775a563c18f715f802a063c5a31b8a11f5c5ee1879ec3454e5f3c738d2d9d201395faa4b61a96c8", Hex.encodeHexString(okm));
    }

    /**
     * <pre>
     * Hash = SHA-1
     * IKM  = 0x0b0b0b0b0b0b0b0b0b0b0b (11 octets)
     * salt = 0x000102030405060708090a0b0c (13 octets)
     * info = 0xf0f1f2f3f4f5f6f7f8f9 (10 octets)
     * L    = 42
     *
     * PRK  = 0x9b6c18c432a7bf8f0e71c8eb88f4b30baa2ba243 (20 octets)
     * OKM  = 0x085a01ea1b10f36933068b56efa5ad81
     * a4f14b822f5b091568a9cdd4f155fda2
     * c22e422478d305f3f896 (42 octets)
     * </pre>
     *
     */
    @Test
    public void testCase4() throws DecoderException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] ikm = Hex.decodeHex("0b0b0b0b0b0b0b0b0b0b0b".toCharArray());
        assertEquals(11, ikm.length);
        byte[] salt = Hex.decodeHex("000102030405060708090a0b0c".toCharArray());
        assertEquals(13, salt.length);
        byte[] info = Hex.decodeHex("f0f1f2f3f4f5f6f7f8f9".toCharArray());
        assertEquals(10, info.length);
        int length = 42;
        HKDF hkdf = new HKDF("HmacSHA1");
        byte[] okm = hkdf.deriveKey(salt, ikm, length, info);
        assertEquals("085a01ea1b10f36933068b56efa5ad81a4f14b822f5b091568a9cdd4f155fda2c22e422478d305f3f896", Hex.encodeHexString(okm));
    }

    /**
     * <pre>
     * Hash = SHA-1
     * IKM  = 0x000102030405060708090a0b0c0d0e0f
     * 101112131415161718191a1b1c1d1e1f
     * 202122232425262728292a2b2c2d2e2f
     * 303132333435363738393a3b3c3d3e3f
     * 404142434445464748494a4b4c4d4e4f (80 octets)
     * salt = 0x606162636465666768696a6b6c6d6e6f
     * 707172737475767778797a7b7c7d7e7f
     * 808182838485868788898a8b8c8d8e8f
     * 909192939495969798999a9b9c9d9e9f
     * a0a1a2a3a4a5a6a7a8a9aaabacadaeaf (80 octets)
     * info = 0xb0b1b2b3b4b5b6b7b8b9babbbcbdbebf
     * c0c1c2c3c4c5c6c7c8c9cacbcccdcecf
     * d0d1d2d3d4d5d6d7d8d9dadbdcdddedf
     * e0e1e2e3e4e5e6e7e8e9eaebecedeeef
     * f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff (80 octets)
     * L    = 82
     *
     * PRK  = 0x8adae09a2a307059478d309b26c4115a224cfaf6 (20 octets)
     * OKM  = 0x0bd770a74d1160f7c9f12cd5912a06eb
     * ff6adcae899d92191fe4305673ba2ffe
     * 8fa3f1a4e5ad79f3f334b3b202b2173c
     * 486ea37ce3d397ed034c7f9dfeb15c5e
     * 927336d0441f4c4300e2cff0d0900b52
     * d3b4 (82 octets)
     * </pre>
     *
     */
    @Test
    public void testCase5() throws DecoderException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] ikm = Hex.decodeHex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f404142434445464748494a4b4c4d4e4f".toCharArray());
        assertEquals(80, ikm.length);
        byte[] salt = Hex.decodeHex("606162636465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9fa0a1a2a3a4a5a6a7a8a9aaabacadaeaf".toCharArray());
        assertEquals(80, salt.length);
        byte[] info = Hex.decodeHex("b0b1b2b3b4b5b6b7b8b9babbbcbdbebfc0c1c2c3c4c5c6c7c8c9cacbcccdcecfd0d1d2d3d4d5d6d7d8d9dadbdcdddedfe0e1e2e3e4e5e6e7e8e9eaebecedeeeff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff".toCharArray());
        assertEquals(80, info.length);
        int length = 82;
        HKDF hkdf = new HKDF("HmacSHA1");
        byte[] okm = hkdf.deriveKey(salt, ikm, length, info);
        assertEquals("0bd770a74d1160f7c9f12cd5912a06ebff6adcae899d92191fe4305673ba2ffe8fa3f1a4e5ad79f3f334b3b202b2173c486ea37ce3d397ed034c7f9dfeb15c5e927336d0441f4c4300e2cff0d0900b52d3b4", Hex.encodeHexString(okm));
    }

    /**
     * <pre>
     * Hash = SHA-1
     * IKM  = 0x0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b (22 octets)
     * salt = (0 octets)
     * info = (0 octets)
     * L    = 42
     *
     * PRK  = 0xda8c8a73c7fa77288ec6f5e7c297786aa0d32d01 (20 octets)
     * OKM  = 0x0ac1af7002b3d761d1e55298da9d0506
     * b9ae52057220a306e07b6b87e8df21d0
     * ea00033de03984d34918 (42 octets) * </pre>
     *
     */
    @Test
    public void testCase6() throws DecoderException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] ikm = Hex.decodeHex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b".toCharArray());
        assertEquals(22, ikm.length);
        byte[] salt = new byte[0];
        byte[] info = new byte[0];
        int length = 42;
        HKDF hkdf = new HKDF("HmacSHA1");
        byte[] okm = hkdf.deriveKey(salt, ikm, length, info);
        assertEquals("0ac1af7002b3d761d1e55298da9d0506b9ae52057220a306e07b6b87e8df21d0ea00033de03984d34918", Hex.encodeHexString(okm));
    }

    /**
     * <pre>
     * Hash = SHA-1
     * IKM  = 0x0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c (22 octets)
     * salt = not provided (defaults to HashLen zero octets)
     * info = (0 octets)
     * L    = 42
     *
     * PRK  = 0x2adccada18779e7c2077ad2eb19d3f3e731385dd (20 octets)
     * OKM  = 0x2c91117204d745f3500d636a62f64f0a
     * b3bae548aa53d423b0d1f27ebba6f5e5
     * 673a081d70cce7acfc48 (42 octets)
     * </pre>
     *
     */
    @Test
    public void testCase7() throws DecoderException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] ikm = Hex.decodeHex("0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c".toCharArray());
        assertEquals(22, ikm.length);
        byte[] info = new byte[0];
        int length = 42;
        HKDF hkdf = new HKDF("HmacSHA1");
        byte[] okm = hkdf.deriveKey(ikm, length, info);
        assertEquals("2c91117204d745f3500d636a62f64f0ab3bae548aa53d423b0d1f27ebba6f5e5673a081d70cce7acfc48", Hex.encodeHexString(okm));
    }
}
