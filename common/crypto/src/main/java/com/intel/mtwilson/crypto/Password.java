/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base64;

/**
 * This utility class includes methods for generating and storing one-way passwords
 * (as hashes). This should be used for applications that need to authenticate a
 * user via a password. For example:
 * 
 * String password_utf8 = inputUserPassword();
 * String 
 * 
 * This should not be used where the password is really a 
 * secret key. For such applications, either verify the password first with this
 * class and then use it separately for generating a secret key (such as using
 * a different salt and hash/transformation) or just define a transformation to
 * convert the password to a secret key without using this class.
 * 
 * Currently this class assumes 8-byte salt and 32-byte hash (SHA-256).
 * Other implementations are available, such as using the key generator to create
 * the password hash... should be in different classes.
 * 
 * To simplify usage, this class throws only CryptographyException.
 * The root causes can be:
 * NoSuchAlgorithmException is thrown if SHA-256 is not available
 * UnsupportedEncodingException is thrown if UTF-8 is not available
 * 
 * @since 0.5.3
 * @author jbuhacoff
 */
public class Password {
    private int SALT_LENGTH = 8;
    private byte[] salt;
    private byte[] hash;
    
    public Password(String password, String saltBase64) throws CryptographyException {
        salt = Base64.decodeBase64(saltBase64);
        hash = hash(password);
    }
    public Password(String password, byte[] saltBytes) throws CryptographyException {
        salt = saltBytes;
        hash = hash(password);
    }
    public Password(String password) throws CryptographyException  {
        // generate a random 8-byte salt
        salt = new byte[SALT_LENGTH];
        SecureRandom rnd = new SecureRandom ();
        rnd.nextBytes(salt);
        hash = hash(password);
    }
    protected Password() {
    }
    
    private byte[] hash(String password) throws CryptographyException {
        try {
            byte[] passwordBytes = password.getBytes("UTF-8"); // UnsupportedEncodingException
            return sha256(concat(salt,passwordBytes));
        }
        catch(NoSuchAlgorithmException e) {
            throw new CryptographyException(e);
        }
        catch(UnsupportedEncodingException e) {
            throw new CryptographyException(e);
        }
    }
    
    private byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256"); // NoSuchAlgorithmException
        return sha256.digest(data);
    }
    
    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public byte[] getHash() {
        return hash;
    }
    
    public String getHashBase64() {
        return Base64.encodeBase64String(hash);
    }
    
    public byte[] getSalt() {
        return salt;
    }
    
    public String getSaltBase64() {
        return Base64.encodeBase64String(salt);
    }
    
    /**
     * 
     * @return the salted password in the format base64-encoded-salt ":" base64-encoded-sha256-of-salted-password
     */
    @Override
    public String toString() {
        return getSaltBase64()+":"+getHashBase64();
    }
    
    /**
     * 
     * @param hashedPassword in the format base64-encoded-salt ":" base64-encoded-sha256-of-salted-password
     * @return 
     */
    public static Password valueOf(String hashedPassword) {
        String[] parts = hashedPassword.split(":");
        Password password = new Password();
        password.salt = Base64.decodeBase64(parts[0]);
        password.hash = Base64.decodeBase64(parts[1]);
        return password;
    }
}
