/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.tpm12;

import java.io.IOException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import java.security.GeneralSecurityException;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class DataBindTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataBindTest.class);

    @Test
    public void testBindSecretKey() throws IOException, GeneralSecurityException {
        byte[] inputSecretKey = IOUtils.toByteArray(getClass().getResourceAsStream("/secret.key"));
        byte[] tpmBindPublicKey = IOUtils.toByteArray(getClass().getResourceAsStream("/tpmbind.public.key"));

        log.debug("secret key: {}", Hex.encodeHex(inputSecretKey));
        log.debug("tpm bind public key: {}", Hex.encodeHex(tpmBindPublicKey));

        /**
         *
         * <pre>
         * SubjectPublicKeyInfo  ::=  SEQUENCE  {
         * algorithm         AlgorithmIdentifier,
         * subjectPublicKey  BIT STRING
         * }         *          *
         * AlgorithmIdentifier  ::=  SEQUENCE  {
         * algorithm   OBJECT IDENTIFIER,
         * parameters  ANY DEFINED BY algorithm OPTIONAL
         * }         *
         *
         * RSAPublicKey ::= SEQUENCE {
         * modulus            INTEGER,    -- n
         * publicExponent     INTEGER  }  -- e
         *
         *
         * The OID rsaEncryption identifies RSA public keys
         * pkcs-1 OBJECT IDENTIFIER ::= { iso(1) member-body(2) us(840)
         * rsadsi(113549) pkcs(1) 1 }
         *
         * rsaEncryption OBJECT IDENTIFIER ::=  { pkcs-1 1}
         *
         * so RSA OID = 1.2.840.113549.1.1
         *
         * INTEGER  tag number 0x02
         * BIT STRING  tag number 0x03
         * OCTET STRING tag number 0x04
         * OBJECT IDENTIFIER  tag number 0x06
         * SEQUENCE tag number 0x10
         *
         * TCG specifies that RSA public key exponent must be 2^16 + 1 = 65537 = 0x00010001
         * Because this is a constant in the spec, the TPM does not include the exponent
         * when it exports the public key.
         *
         * The public key blob exported by the TPM is 284 bytes.
         * Assuming first 28 bytes are header information, that leaves
         * 256 bytes for the public key modulus (2048 bits).
         *
         * </pre>
         */
        /*
         * 
         * This code is encapsulated by TpmPublicKey:
         * <pre>
         BigInteger publicKeyExponent = BigInteger.valueOf(65537);
         BigInteger publicKeyModulusBytes = new BigInteger(1, ByteArray.subarray(tpmBindPublicKey, 28, 256));
        
         KeyFactory keyFactory = KeyFactory.getInstance("RSA");// throws NoSuchAlgorithmException
         PublicKey publicKey = keyFactory.generatePublic(new RSAPublicKeySpec(publicKeyModulusBytes, publicKeyExponent)); // throws InvalidKeySpecException
         * </pre>
         */
        TpmPublicKey tpmPublicKey = TpmPublicKey.valueOf(tpmBindPublicKey);
        log.debug("re-encoded public key: {}", Hex.encodeHex(tpmPublicKey.getEncoded()));
        /*
         * This code is encapsulated by DataBind:
        Cipher cipher = DataBind.getCipher(tpmPublicKey.toPublicKey()); // throws NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
//        byte[] encrypted = cipher.wrap(secretKey); // throws IllegalBlockSizeException

        byte[] encrypted = cipher.doFinal(new DataBind.TpmBoundData(DataBind.VERSION_1_1, DataBind.TpmPayloadType.TPM_PT_BIND, inputSecretKey).toByteArray()); // throws BadPaddingException
*/
        byte[] encrypted = DataBind.bind(inputSecretKey, tpmPublicKey);
        
        log.debug("encrypted secret key: {}", Base64.encodeBase64String(encrypted));
        /**
         * You could take the base64 encrypted secret key, attempt to unwrap  and compare with the original:
         * 
         * <pre>
         * echo 'DWm2ZQw+vgl9DZUqTEqVjbJwAzJogRbWfDodze5HuEfEW86mwU3KKfGPe1CN1e0K9DZQMEycdYrZJtK5E8Cvf0SBOTVJLg1pqoV1Yf3L5PKkN92pORb8P7ny6DrYk/sVcVDYfrQGXx+pg21+qK3RhghZ0//HWyTeF133bit0DNVgGNFMkrBbhdA+1OIssyIyKcJ0PS9lOclY1dY2x72E6oxTrQNRYBhHlxhknb38+/kynY5xp7IwxCokg940faanzbLw72n2Qzvun7fT5+zsAfiLVM41ZZRswA4mu9iWY+dS7ZCrpoqF40UaUuAODcoBsph9RXfpS1mnb/G7b+YemA==' | base64 --decode > secret.key.tpm.javabind
         * tpm_unbindaeskey -k tpmbind.private.key -i secret.key.tpm.javabind -o secret.key.tpm.javabind.unwrapped
         * </pre>
         * 
         * In the example above,  tpmbind.private.key is the private key blob correspodning to the tpmbind.public.key in our test resources.
         * 
         */
        
    }
}
