/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.io.ByteArray;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;

/**
 * This utility class includes methods for generating and storing one-way passwords
 * (as hashes). This should be used for applications that need to authenticate a
 * user via a password. For example:
 * 
 * String password_utf8 = inputUserPassword();
 * String salt_base64 = getUserSaltBase64();
 * PasswordHash passwordHash = new PasswordHash(password_utf8, salt_base64);
 * Arrays.equals(recordedHashBase64, passwordHash.getHashBase64())
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
 * @since 0.1
 * @author jbuhacoff
 */
public class PasswordHash {
    private int SALT_LENGTH = 8;
    private byte[] salt;
    private byte[] hash;
    
    public PasswordHash(String password, String saltBase64) throws CryptographyException {
        salt = Base64.decodeBase64(saltBase64);
        hash = hash(password);
    }
    public PasswordHash(String password, byte[] saltBytes) throws CryptographyException {
        salt = saltBytes;
        hash = hash(password);
    }
    public PasswordHash(String password) throws CryptographyException  {
        // generate a random 8-byte salt
        salt = new byte[SALT_LENGTH];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(salt);
        hash = hash(password);
    }
    protected PasswordHash() {
    }
    
    private byte[] hash(String password) throws CryptographyException {
        try {
            byte[] passwordBytes = password.getBytes("UTF-8"); // UnsupportedEncodingException
            return sha256(ByteArray.concat(salt,passwordBytes));
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
    
    public boolean isEqualTo(String password) throws CryptographyException {
        byte[] checkHash = hash(password);
        return Arrays.equals(hash, checkHash);
    }
    
    /**
     * 
     * @param hashedPassword in the format base64-encoded-salt ":" base64-encoded-sha256-of-salted-password
     * @return 
     */
    public static PasswordHash valueOf(String hashedPassword) {
        String[] parts = hashedPassword.split(":");
        PasswordHash password = new PasswordHash();
        password.salt = Base64.decodeBase64(parts[0]);
        password.hash = Base64.decodeBase64(parts[1]);
        return password;
    }
}
