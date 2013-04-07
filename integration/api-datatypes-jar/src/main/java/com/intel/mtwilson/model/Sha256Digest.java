package com.intel.mtwilson.model;

/**
 * Representation of a single SHA1 Digest. An SHA256 Digest is a 32-byte value.
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class Sha256Digest extends AbstractMessageDigest {
    public final static Sha256Digest ZERO = new Sha256Digest(new byte[] {0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0});

    
    public Sha256Digest() {
        super("SHA-256", 32);
    }
    
    public Sha256Digest(byte[] value) {
        this();
        setBytes(value);
    }
    
    public Sha256Digest(String hex) {
        this();
        setHex(hex);
    }
    
    public static Sha256Digest valueOf(byte[] message) {
        return valueOf(Sha256Digest.class, message);
    }

}
