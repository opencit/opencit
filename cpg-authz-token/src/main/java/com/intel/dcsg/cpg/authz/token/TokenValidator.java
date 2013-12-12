/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.authz.token;

import com.intel.dcsg.cpg.crypto.key.SecretKeyRepository;
import com.intel.dcsg.cpg.crypto.Aes;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.key.EncryptionKey;
import com.intel.dcsg.cpg.crypto.key.EncryptionKeySource;
import com.intel.dcsg.cpg.crypto.key.HashMapMutableSecretKeyRepository;
import com.intel.dcsg.cpg.crypto.key.KeyNotFoundException;
import com.intel.dcsg.cpg.crypto.key.Protection;
import com.intel.dcsg.cpg.crypto.key.ProtectionBuilder;
import com.intel.dcsg.cpg.crypto.key.ProtectionPolicy;
import com.intel.dcsg.cpg.crypto.key.RandomSource;
import com.intel.dcsg.cpg.util.ByteArray;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import javax.crypto.SecretKey;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class TokenValidator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenValidator.class);
    private EncryptionKeySource encryptionKeySource;
    private TokenCipherCodec codec;
    private Protection protection;
    private ProtectionPolicy protectionPolicy;
    private long expiresAfter = 3600; // how long the token is valid, in seconds XXX TODO convert to TimeUnit for safety ... default is one hour
    private long replaceWhenTokenNearExpires = 300; // if token expires in the next 5 minuets just go ahead and replace it 
    
    public TokenValidator(TokenFactory tokenFactory) {
        protection = tokenFactory.getProtection();
        protectionPolicy = tokenFactory.getProtectionPolicy();
        encryptionKeySource = tokenFactory.getEncryptionKeySource();
        codec = new TokenCipherCodec(encryptionKeySource, protection);
    }
    
    /**
     * 
     * @param expires how long tokens are valid, in seconds;  if token is older than "expiresAfter" seconds it is invalid  (if token age equals expiresAfter seconds it is valid)
     */
    public void setExpiresAfter(long expiresAfter) {
        this.expiresAfter = expiresAfter;
    }

    public void setReplaceWhenTokenNearExpires(long replaceWhenTokenNearExpires) {
        this.replaceWhenTokenNearExpires = replaceWhenTokenNearExpires;
    }
    
    public Token validate(String token) throws UnsupportedTokenVersionException, CryptographyException, ExpiredTokenException, KeyNotFoundException {
        return validate(Base64.decodeBase64(token));
    }
    
    public Token validate(byte[] token) throws UnsupportedTokenVersionException, CryptographyException, ExpiredTokenException, KeyNotFoundException {
        byte version = token[0];
        switch(version) {
            case 0:
                throw new UnsupportedTokenVersionException(version);
            case 1:
                Token object = codec.decrypt(token);
                if( expired(object.getTimestamp()) ) {
                    throw new ExpiredTokenException();
                }
                return object;
            default:
                throw new UnsupportedTokenVersionException(version);
        }
    }
    
    
    private boolean expired(long timestamp) {
        return System.currentTimeMillis()/1000L - timestamp > expiresAfter;
    }

    public boolean expiresSoon(long timestamp) {
        return timestamp + expiresAfter <  System.currentTimeMillis()/1000L + replaceWhenTokenNearExpires;
    }
    
}
