package com.intel.mtwilson.model;

import com.intel.dcsg.cpg.validation.ObjectModel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import com.fasterxml.jackson.annotation.JsonValue;
//import org.codehaus.jackson.annotate.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for representations of MD5, SHA1 and other digests. 
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public abstract class AbstractMessageDigest extends ObjectModel {
    protected static Logger log = LoggerFactory.getLogger(AbstractMessageDigest.class);
    
    private final String algorithm; // parameter for MessageDigest.getInstance(String)
    private final int digestLength; // in bytes
    private byte[] value = null;
    private String hex = null;

    public AbstractMessageDigest(String algorithm, int digestLength) {
        this.algorithm = algorithm;
        this.digestLength = digestLength;
    }
    
    public String algorithm() { return algorithm; }

    /**
     * Makes a copy of the byte array.
     * @param value 
     */
    protected void setBytes(byte[] value) {
        if( value == null ) { throw new IllegalArgumentException(algorithm+": Null digest"); }
        if( value.length != digestLength ) {  throw new IllegalArgumentException(algorithm+": Digest must be "+digestLength+" bytes long: "+Hex.encodeHexString(value)); }
        this.value = new byte[value.length];
        System.arraycopy(value, 0, this.value, 0, value.length);
        this.hex = Hex.encodeHexString(value);
    }
    protected void setHex(String hex) {
        if( hex == null ) {  throw new IllegalArgumentException(algorithm+": Null digest"); }
        if( hex.isEmpty() ) {  throw new IllegalArgumentException(algorithm+": Empty digest"); }
        if( hex.length() != digestLength*2 ) {  throw new IllegalArgumentException(algorithm+": Digest must be "+digestLength+" bytes ("+(digestLength*2)+" hex digits) long: "+hex); }
        try {
            this.value = Hex.decodeHex(hex.toCharArray());
        }
        catch(DecoderException e) {
             throw new IllegalArgumentException(algorithm+": Invalid digest: "+value, e);
        }
        this.hex = hex;
    }
    
    /**
     * Returns the bytes comprising the Digest.
     * @return 
     */
    public byte[] toByteArray() { return value; }
    
    /**
     * Returns a string representing the Digest in hexadecimal form.
     *
     * @see java.lang.Object#toString()
     */
    @JsonValue
    @Override
    public String toString() {
        return hex;
    }
    
    @Override
    public int hashCode() {
        return hex.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractMessageDigest other = (AbstractMessageDigest) obj;
        if (!Arrays.equals(value, other.value)) {
            return false;
        }
        return true;
    }


    @Override
    protected void validate() {
        if( value == null ) { 
            fault("%s value must not be null", algorithm); 
        }
        else if( value.length != digestLength ) { 
            fault("%s value must be %d bytes long", algorithm, digestLength); 
        }
    }

    /**
     * Subclasses can implement a valueOf function like this:
     * 
     * public Md5Digest valueOf(byte[] message) {
     *  return valueOf(Md5Digest.class, message);
     * }
     * 
     * @param <T>
     * @param clazz
     * @param message
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    protected static <T extends AbstractMessageDigest> T valueOf(Class<T> clazz, byte[] message) {
        try {
            T model = clazz.newInstance();
            MessageDigest hash = MessageDigest.getInstance(model.algorithm());
            byte[] digest = hash.digest(message);
            model.setBytes(digest);
            return model;
        }
        catch(InstantiationException e) {
            log.error("Cannot create instance of "+clazz.getName(), e);
            return null;
        }
        catch(IllegalAccessException e) {
            log.error("Cannot create instance of "+clazz.getName(), e);
            return null;
        }
        catch(NoSuchAlgorithmException e) {
            log.error("Cannot create instance of "+clazz.getName(), e);
            return null;
        }
    }
    
}
