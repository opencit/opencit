/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.password;

import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.util.ByteArray;
import com.intel.mtwilson.shiro.jdbi.model.UserLoginPassword;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

/**
 * This class is similar to HashedCredentialsMatcher that comes wtih 
 * Apache Shiro but instead of having a static configuration of 
 * the algorithm name and iteration count (which requires downtime
 * while upgrading passwords on the server for all accounts), 
 * this matcher allows a per-instance configuration using the
 * corresponding PasswordAuthenticationInfo class used by the
 * JdbcPasswordRealm 
 * 
 * @author jbuhacoff
 */
public class PasswordCredentialsMatcher implements CredentialsMatcher {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordCredentialsMatcher.class);
    
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        log.debug("doCredentialsMatch with token {} and info {}", token, info);
        if( token.getCredentials() == null ) { return false; }
        if( !(info.getCredentials() instanceof UserLoginPassword)) { return false; }
        UserLoginPassword userLoginPasswordHash = (UserLoginPassword)info.getCredentials();
        byte[] tokenBytes = toBytes(token.getCredentials());
        if( tokenBytes == null || tokenBytes.length == 0 ) {
            throw new IllegalArgumentException("Empty password credential"); // TODO:  error code  /  i18n
        }
        
        byte[] hashedTokenBytes = passwordHash(tokenBytes, userLoginPasswordHash);
        if( Arrays.equals(hashedTokenBytes, userLoginPasswordHash.getPasswordHash()) ) {
            return true;
        }
        return false;
    }

    public static byte[] passwordHash(byte[] inputPasswordBytes, UserLoginPassword storedPassword) {
        // SHA-256 is the standard Java name but we also accept SHA256 
        if( "SHA-256".equalsIgnoreCase(storedPassword.getAlgorithm()) ||  "SHA256".equalsIgnoreCase(storedPassword.getAlgorithm()) ) {
            // first iteration is mandatory
            Sha256Digest digest = Sha256Digest.digestOf(ByteArray.concat(storedPassword.getSalt(), inputPasswordBytes));
            int max = storedPassword.getIterations() - 1; // -1 because we just completed the first iteration
            for(int i=0; i<max; i++) {
                digest = Sha256Digest.digestOf(digest.toByteArray());
            }
            return digest.toByteArray();
        }
        throw new UnsupportedOperationException("Algorithm not supported: "+storedPassword.getAlgorithm()); // TODO:  i18n
    }
    
    /**
     * Convert the input password from String or char[] into byte[] 
     * Assumes UTF-8 encoding 
     * 
     * If the input password is already byte[] it is returned as-is
     * 
     * @param credentials
     * @return 
     */
    protected byte[] toBytes(Object credentials) {
        if( credentials == null ) { return null; }
        if( credentials instanceof byte[]) {
            return (byte[])credentials;
        }
        if( credentials instanceof char[]) {
            //ByteBuffer bytebuffer = Charset.forName("UTF-8").encode(CharBuffer.wrap((char[])credentials));  // this is wrong because the Charset encode() method appends a null terminator which will cause the resulting hash to be wrong
//            return bytebuffer.array();
            return String.valueOf((char[])credentials).getBytes(Charset.forName("UTF-8"));
        }
        if( credentials instanceof String ) {
            return ((String)credentials).getBytes(Charset.forName("UTF-8"));
        }
        return null;
    }
    
}
