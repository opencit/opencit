/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.x509;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Hex;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

/**
 * Verifies the signature of an incoming X509AuthenticationToken using a known
 * certificate from X509AuthenticationInfo.
 *
 * AuthenticationToken must be an instance of X509AuthenticationToken.
 * AuthenticationInfo must be an instance of X509AuthenticationInfo.
 * 
 * 
 * @author jbuhacoff
 */
public class X509CredentialsMatcher implements CredentialsMatcher {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509CredentialsMatcher.class);

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        if (token.getCredentials() == null) {
            return false;
        }
        if (!(token.getCredentials() instanceof Credential)) {
            return false;
        }
        if (!(info.getCredentials() instanceof X509Certificate)) {
            return false;
        }
        Credential credential = (Credential) token.getCredentials();
        X509Certificate certificate = (X509Certificate) info.getCredentials();

        try {
            log.debug("Verifying signature");
            
            // the credential.getDigest() value is the oid for "SHA1" or "SHA256" concatenated with the digest of the signed document 
            // the credential.getSignature() value is the RSA encryption of the digest
            // so to verify the signature, we just have to decrypt it using the known public key from our database
            // and if the result matches the input digest, then the known public key has verified the 
            // signature and the user is authenticated

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, certificate);
            byte[] digest = cipher.doFinal(credential.getSignature());
            log.debug("Digest (input): {}", Hex.encodeHexString(credential.getDigest()));
            log.debug("Signature (input): {}", Hex.encodeHexString(credential.getSignature()));
            log.debug("Digest (signature decrypted with known public key): {}", Hex.encodeHexString(digest));
            if (Arrays.equals(digest, credential.getDigest())) {
                log.debug("Verified signature");
                // known public key from certificate verified the signature on the incoming token
                return true;
            }
            log.debug("Invalid signature");
            return false;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            log.error("Cannot verify credentials: {}", e.getMessage());
            throw new AuthenticationException(e);
        }
    }
    
    
/*  this is how you verify the signature when you have the complete input document for reference; but instead of passing the entire request entity around we compute its digest in the X509AuthenticationFilter
 * so that in this matcher we get the digest and the signature... and those cannot be used with the Signature
 * class below because the update() method will compute the digest of its input, but we already have the 
 * digest;  if the Signature class had a way to set the "plain" input digest (without the algorithm oid prepended)
 * then it would have been convenient to use it. 
    private boolean verifySignature(byte[] document, Certificate certificate, String signatureAlgorithm, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance(signatureAlgorithm);
        rsa.initVerify(certificate);
        rsa.update(document);
        return rsa.verify(signature);
    }
    */
}
