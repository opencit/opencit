package com.intel.mtwilson.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Representation of a single SHA1 Digest. An SHA1 Digest is a 20-byte value.
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class Sha1Digest extends AbstractMessageDigest {
    public Sha1Digest() {
        super("SHA-1", 20);
    }
    
    public Sha1Digest(byte[] value) {
        this();
        setBytes(value);
    }
    
    public Sha1Digest(String hex) {
        this();
        setHex(hex);
    }
    
    public static Sha1Digest valueOf(byte[] message) {
        return valueOf(Sha1Digest.class, message);
    }

    /**
     * Creates a NEW instance of Sha1Digest that contains the result of 
     * extending this value with the specified message.
     * @param message
     * @return a new instance with the extended value, or null if there was an error
     */
    public Sha1Digest extend(byte[] message) {
        try {
            MessageDigest hash = MessageDigest.getInstance(algorithm());
            hash.update(toByteArray());
            hash.update(message);
            return new Sha1Digest(hash.digest());
        }
        catch(NoSuchAlgorithmException e) {
            log.error("No such algorithm: "+algorithm(), e);
            return null;
        }
    }
}
