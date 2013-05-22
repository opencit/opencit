package com.intel.dcsg.cpg.crypto;

/**
 * Representation of a single SHA256 Digest. An SHA256 Digest is a 32-byte value.
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class Sha256Digest extends AbstractDigest {
    private static final DigestAlgorithm ALGORITHM = DigestAlgorithm.SHA256;
    
    /**
     * This constructor exists so the valueOf methods can instantiate an SHA256 object
     * and set its value after validating the input. If they were to call the public
     * constructors, the validation would happen again there redundantly.
     */
    protected Sha256Digest() {
        super(ALGORITHM);
    }
    
    /**
     * Use this constructor if you expect that the bytes are a valid SHA256 digest.
     * If they are not a valid SHA256 digest an IllegalArgumentException exception will be thrown.
     * 
     * If you need to create a Sha256Digest where the input may be invalid, use valueOf.
     * 
     * @param value 
     */
    public Sha256Digest(byte[] value) {
        super(ALGORITHM, value);
    }
    
    /**
     * Use this constructor if you expect that the hex string represents a valid SHA256 digest.
     * If it does not look like an SHA256 digest an IllegalArgumentException exception will be thrown.
     * 
     * If you need to create a Sha256Digest where the input may be invalid, use valueOf.
     * 
     * @param hex 
     */
    public Sha256Digest(String hex) {
        super(ALGORITHM, hex);
    }
    
    /**
     * 
     * @param value can be null
     * @return true if value is a valid SHA256 digest
     */
    public static boolean isValid(byte[] value) {
        return ALGORITHM.isValid(value);
    }
    
    /**
     * @param hex without any punctuation or spaces; can be null
     * @return true if the value is a valid hex representation of an SHA256 digest
     */
    public static boolean isValidHex(String hexValue) {
        return ALGORITHM.isValidHex(hexValue);
    }

    /**
     * @param base64 value without any punctuation or spaces; can be null
     * @return true if the value is a valid base64 representation of an SHA256 digest
     */
    public static boolean isValidBase64(String base64Value) {
        return ALGORITHM.isValidBase64(base64Value);
    }
    
    /**
     * Assumes the input represents an SHA256 digest and creates a new instance of Sha256Digest to wrap it.
     * This method does NOT compute a digest. If the input is not a valid SHA256 representation, a null
     * will be returned.
     * 
     * Callers must always check the return value for null. 
     * 
     * @param digest
     * @return 
     */
    public static Sha256Digest valueOf(byte[] digest) {
        if( isValid(digest) ) {
            Sha256Digest SHA256 = new Sha256Digest();
            SHA256.value = digest;
            return SHA256;
        }
        return null;
    }

    /**
     * Assumes the input represents an SHA256 digest and creates a new instance of Sha256Digest to wrap it.
     * This method does NOT compute a digest. If the input is not a valid SHA256 representation, a null
     * will be returned.
     * 
     * Callers must always check the return value for null. 
     * 
     * The input can be either hex or base64
     * 
     * @param text either hex or base64 encoded sha256 digest
     * @return 
     */
    public static Sha256Digest valueOf(String text) {
        if( isValidHex(text) ) {
            Sha256Digest digest = new Sha256Digest();
            digest.value = HexUtil.toByteArray(text); // throws HexFormatException if invalid, but shouldn't happen since we check isValid() first
            return digest;
        }
        if( isValidBase64(text) ) {
            Sha256Digest digest = new Sha256Digest();
            digest.value = Base64Util.toByteArray(text); 
            return digest;
        }
        return null;
    }

    public static Sha256Digest valueOfHex(String text) {
        if( isValidHex(text) ) {
            Sha256Digest digest = new Sha256Digest();
            digest.value = HexUtil.toByteArray(text); // throws HexFormatException if invalid, but shouldn't happen since we check isValid() first
            return digest;
        }
        return null;
    }

    public static Sha256Digest valueOfBase64(String text) {
        if( isValidBase64(text) ) {
            Sha256Digest digest = new Sha256Digest();
            digest.value = Base64Util.toByteArray(text); 
            return digest;
        }
        return null;
    }
    
    /**
     * Computes the SHA256 digest of the input and returns the result.
     * @param message
     * @return 
     */
    public static Sha256Digest digestOf(byte[] message) {
        return new Sha256Digest(ALGORITHM.digest(message));
    }
}
