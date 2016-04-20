package com.intel.mtwilson.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Representation of a single SHA1 Digest. An SHA1 Digest is a 20-byte value.
 * 
 * An Sha1Digest object is immutable.  When you create one with any value,  you cannot
 * change it later. You must create a new instance with another value.  When you create
 * an instance by passing a byte array,  the byte array is copied so that your immutable
 * Sha1Digest instance will not mysteriously change if you re-use the byte array for
 * something else.
 * @since 0.5.4
 * @author jbuhacoff
 */
public class Sha1Digest extends AbstractMessageDigest {
    public final static Sha1Digest ZERO = new Sha1Digest(new byte[] {0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0});
    
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
     * extending this value with the specified data.
     * @param data
     * @return a new instance with the extended value, or null if there was an error
     */
    public Sha1Digest extend(Sha1Digest data) {
        return extend(data.toByteArray());
    }

    /**
     * Creates a NEW instance of Sha1Digest that contains the result of 
     * extending this value with the specified data.
     * @param data
     * @return a new instance with the extended value, or null if there was an error
     */
    public Sha1Digest extend(byte[] data) {
        try {
            MessageDigest hash = MessageDigest.getInstance(algorithm());
            hash.update(toByteArray());
            hash.update(data);
            return new Sha1Digest(hash.digest());
        }
        catch(NoSuchAlgorithmException e) {
            log.error("No such algorithm: "+algorithm(), e);
            return null;
        }
    }
}
