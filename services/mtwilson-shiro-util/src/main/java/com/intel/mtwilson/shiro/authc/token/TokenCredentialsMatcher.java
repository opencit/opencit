/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.token;

import com.intel.mtwilson.shiro.authc.x509.*;
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
 * Compares the token from a request to a given token (from a database) 
 * including checking validity dates and max uses.
 * 
 * AuthenticationToken must be an instance of TokenAuthenticationToken.
 * AuthenticationInfo must be an instance of TokenAuthenticationInfo.
 * 
 * 
 * @author jbuhacoff
 */
public class TokenCredentialsMatcher implements CredentialsMatcher {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509CredentialsMatcher.class);

    @Override
    public boolean doCredentialsMatch(AuthenticationToken subject, AuthenticationInfo info) {
        if (subject.getCredentials() == null) {
            return false;
        }
        if (!(subject.getCredentials() instanceof Token)) {
            return false;
        }
        if (!(info.getCredentials() instanceof Token)) {
            return false;
        }
        Token subjectToken = (Token) subject.getCredentials();
        TokenCredential credentialToken = (TokenCredential) info.getCredentials();

        try {
            log.debug("Verifying token");
            
            if( credentialToken.getNotAfter() != null && subjectToken.getDate().after(credentialToken.getNotAfter())) {
                log.debug("Token date {} after {}", subjectToken.getDate(), credentialToken.getNotAfter());
                return false;
            }
            if( credentialToken.getNotBefore() != null && subjectToken.getDate().before(credentialToken.getNotBefore())) {
                log.debug("Token date {} before {}", subjectToken.getDate(), credentialToken.getNotBefore());
                return false;
            }
            
            if( !subjectToken.getValue().equals(credentialToken.getValue())) {
                log.debug("Token value {} does not match credential value {}", subjectToken.getValue(), credentialToken.getValue());
                return false;
            }
            
            if( credentialToken.getUsedMax() != null && credentialToken.getUsed() != null && credentialToken.getUsed() >= credentialToken.getUsedMax() ) {
                log.debug("Token used {} of max {}", credentialToken.getUsed(), credentialToken.getUsedMax());
                return false;
            }
            
            log.debug("Validated token");
            credentialToken.use();
            
            
            return true;
            
        } catch (Exception e) {
            log.error("Cannot verify token: {}", e.getMessage());
            throw new AuthenticationException(e);
        }
    }
    
}
