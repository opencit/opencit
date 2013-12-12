/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.pem;

import com.intel.dcsg.cpg.crypto.Aes;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.rfc822.Message;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class SignatureTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignatureTest.class);

    /**
     * Sample message:
Content-Length: 155
Content-Type: text/plain; charset="UTF-8"

In cryptography and computer security, length extension attacks are a type of attack on certain types of hashes which allow inclusion of extra information.
     * 
     * Signature for this message with randomly generated key:
     * 
Content-Length: 78
Content-Transfer-Encoding: gzip, base64
Content-Type: application/signature.java; alg="HmacSHA256"; key="key1"; digest-alg="SHA-256"
Link: <cid:LS7yrq02+VNfpaDdFbW6LUAKfdpuHuXFRQVujWPiJ7Q=>; rel="cite"

H4sIAAAAAAAAAAEgAN//+7FIBzxupojbAJuiS6gv8DZUQNMnph78oYIGquFypBeBVP/RIAAAAA==

     * 
     * Sample message again after the content-id header was added by the signature writer:
Content-ID: LS7yrq02+VNfpaDdFbW6LUAKfdpuHuXFRQVujWPiJ7Q=
Content-Length: 155
Content-Type: text/plain; charset="UTF-8"

In cryptography and computer security, length extension attacks are a type of attack on certain types of hashes which allow inclusion of extra information.
     * 
     * All together in the pem format... notice that ONLY THE CONTENT IS SIGNED, NOT THE HEADERS.  in order to sign headers too you need to create a message object with the headers and conetnt to protect and then sign the entire thing... the content-id goes on the outer message because it includes a hash of the signed content(a message) which cannot be embedded in the protected message itself  (since that would affect it's hash and that's circular)
     * 
----- BEGIN SIGNED DATA -----
Content-ID: LS7yrq02+VNfpaDdFbW6LUAKfdpuHuXFRQVujWPiJ7Q=
Content-Length: 155
Content-Type: text/plain; charset="UTF-8"

In cryptography and computer security, length extension attacks are a type of attack on certain types of hashes which allow inclusion of extra information.
----- END SIGNED DATA -----
----- BEGIN SIGNATURE -----
Content-Length: 74
Content-Transfer-Encoding: gzip, base64
Content-Type: application/signature.java; alg="HmacSHA256"; key="key1"; digest-alg="SHA-256"
Link: <cid:LS7yrq02+VNfpaDdFbW6LUAKfdpuHuXFRQVujWPiJ7Q=>; rel="cite"

H4sIAAAAAAAAAFvaEbpiHc+qJu3ry9rlNtbJ1fj9/XBcIv9WxTKuguVG04QAuWKgOSAAAAA=

----- END SIGNATURE -----
     * 
     * @throws CryptographyException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException 
     */
    @Test
    public void testSignature() throws CryptographyException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        // create a test message
        Charset utf8 = Charset.forName("UTF-8");
        String text = "In cryptography and computer security, length extension attacks are a type of attack on certain types of hashes which allow inclusion of extra information."; // a sentence from wikipedia, http://en.wikipedia.org/wiki/Length_extension_attack
        byte[] textBytes = text.getBytes(utf8);
        Message message = new Message();
        message.setContent(textBytes);
        message.setContentLength(textBytes.length);
        message.setContentType("text/plain; charset=\"UTF-8\"");
        log.debug("Message:\n{}", new String(message.toByteArray(), "UTF-8"));
        // create a random key for testing
        SecretKey key = Aes.generateKey(128); // throws CryptographyException
        // sign it
        SignatureWriter signer = new SignatureWriter();
        Message signature = signer.sign(message, key, "key1"); // throws IOException, NoSuchAlgorithmException, InvalidKeyException
        log.debug("Message again:\n{}", new String(message.toByteArray(), "UTF-8"));
        log.debug("Signature:\n{}", new String(signature.toByteArray(), "UTF-8"));
        // write out the message and signature in pem format
        log.debug("PEM:\n{}", String.format("----- BEGIN SIGNED DATA -----\n%s\n----- END SIGNED DATA -----\n----- BEGIN SIGNATURE -----\n%s\n----- END SIGNATURE -----\n", new String(message.toByteArray(), "UTF-8"), new String(signature.toByteArray(), "UTF-8")));
    }
}
