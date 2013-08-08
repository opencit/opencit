package com.intel.dcsg.cpg.crypto;

/**
 * Representation of a single SHA1 Digest. An SHA1 Digest is a 20-byte value.
 * 
 * Implementation is flexible about the SHA1 input format
 * and allows spaces, colons, uppercase and lowercase characters
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class Sha1Digest extends AbstractDigest {
    private static final DigestAlgorithm ALGORITHM = DigestAlgorithm.SHA1;
    
    /**
     * This constructor exists so the valueOf methods can instantiate an SHA1 object
     * and set its value after validating the input. If they were to call the public
     * constructors, the validation would happen again there redundantly.
     */
    protected Sha1Digest() {
        super(ALGORITHM);
    }
    
    /**
     * Use this constructor if you expect that the bytes are a valid SHA1 digest.
     * If they are not a valid SHA1 digest an IllegalArgumentException exception will be thrown.
     * 
     * If you need to create a Sha1Digest where the input may be invalid, use valueOf.
     * 
     * 
     * @param value 
     */
    public Sha1Digest(byte[] value) {
        super(ALGORITHM, value);
    }
    
    /**
     * Use this constructor if you expect that the hex string represents a valid SHA1 digest.
     * If it does not look like an SHA1 digest an IllegalArgumentException exception will be thrown.
     * 
     * If you need to create a Sha1Digest where the input may be invalid, use valueOf.
     * 
     * @param hex 
     */
    public Sha1Digest(String hex) {
        super(ALGORITHM, hex);
    }
    
    /**
     * 
     * @param value can be null
     * @return true if value is a valid SHA1 digest
     */
    public static boolean isValid(byte[] value) {
        return ALGORITHM.isValid(value);
    }
    
    /**
     * @param hex without any punctuation or spaces; can be null
     * @return true if the value is a valid hex representation of an SHA1 digest
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
     * Assumes the input represents an SHA1 digest and creates a new instance of Sha1Digest to wrap it.
     * This method does NOT compute a digest. If the input is not a valid SHA1 representation, a null
     * will be returned.
     * 
     * Callers must always check the return value for null. 
     * 
     * @param digest
     * @return 
     */
    public static Sha1Digest valueOf(byte[] digest) {
        if( isValid(digest) ) {
            Sha1Digest SHA1 = new Sha1Digest();
            SHA1.value = digest;
            return SHA1;
        }
        return null;
    }

    /**
     * Assumes the input represents an SHA1 digest and creates a new instance of Sha1Digest to wrap it.
     * This method does NOT compute a digest. If the input is not a valid SHA1 representation, a null
     * will be returned.
     * 
     * Callers must always check the return value for null. 
     * 
     * Starting with version 0.1.2 this method also allows base64 input.
     * 
     * @param hex
     * @return 
     */
    public static Sha1Digest valueOf(String text) {
        if( isValidHex(text) ) {
            Sha1Digest digest = new Sha1Digest();
            digest.value = HexUtil.toByteArray(text); // throws HexFormatException if invalid, but shouldn't happen since we check isValid() first
            return digest;
        }
        if( isValidBase64(text) ) {
            Sha1Digest digest = new Sha1Digest();
            digest.value = Base64Util.toByteArray(text); 
            return digest;
        }
        return null;
    }
    
    /**
     * @since 0.1.2
     * @param text
     * @return 
     */
    public static Sha1Digest valueOfHex(String text) {
        if( isValidHex(text) ) {
            Sha1Digest digest = new Sha1Digest();
            digest.value = HexUtil.toByteArray(text); // throws HexFormatException if invalid, but shouldn't happen since we check isValid() first
            return digest;
        }
        return null;
    }

    /**
     * @since 0.1.2
     * @param text
     * @return 
     */
    public static Sha1Digest valueOfBase64(String text) {
        if( isValidBase64(text) ) {
            Sha1Digest digest = new Sha1Digest();
            digest.value = Base64Util.toByteArray(text); 
            return digest;
        }
        return null;
    }
    
    
    /**
     * Computes the SHA1 digest of the input and returns the result.
     * @param message
     * @return 
     */
    public static Sha1Digest digestOf(byte[] message) {
        return new Sha1Digest(ALGORITHM.digest(message));
    }
}
