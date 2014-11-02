/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import com.intel.mtwilson.codec.HexUtil;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
//import org.codehaus.jackson.annotate.JsonCreator;
//import org.codehaus.jackson.annotate.JsonValue;
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.apache.commons.codec.binary.Hex;


/**
 * Represents a UUID.
 * 
 * 
 * Convenience class that adds conversion to/from ByteArray.
 * 
 * Automatically serializes/deserializes to UUID format string with Jackson (both 1.x and 2.x)
 * 
 * @see http://www.ietf.org/rfc/rfc4122.txt
 * @author jbuhacoff
 */
public class UUID implements Serializable {
    private static Logger log = LoggerFactory.getLogger(UUID.class);
    private static final long serialVersionUID = 1264729562147L;
    private final byte[] bytes;
    
    /**
     * Creates a new random UUID (type 4)
     */
    public UUID() {
        this(toByteArray(java.util.UUID.randomUUID()));
    }
    
    public UUID(byte[] bytes) {
        if( bytes.length != 16 ) { throw new IllegalArgumentException("UUID must be 16 bytes"); }
        this.bytes = bytes;
    }
    
    public ByteArray toByteArray() {
        return new ByteArray(bytes);
    }
    
    public BigInteger toBigInteger() {
        return toByteArray().toBigInteger();
    }
    
    
    public java.util.UUID uuidValue() {
        ByteArray array = toByteArray();
        return new java.util.UUID(array.subarray(0, 8).toBigInteger().longValue(), array.subarray(8, 8).toBigInteger().longValue());
    }
    
    /**
     * 
     * @return the UUID representation (32 hex characters in groups of 8, 4, 4, 4, 12 separated by hyphens)
     */
    @org.codehaus.jackson.annotate.JsonValue // jackson 1.x
    @com.fasterxml.jackson.annotation.JsonValue // jackson 2.x
    @Override
    public String toString() {
        ByteArray array = new ByteArray(bytes);
        return String.format("%s-%s-%s-%s-%s", 
                array.subarray(0, 4).toHexString(),
                array.subarray(4, 2).toHexString(),
                array.subarray(6, 2).toHexString(),
                array.subarray(8, 2).toHexString(),
                array.subarray(10,6).toHexString());
    }
    
    /**
     * 
     * @return 32 hex characters (16 bytes) ; same as toString but without hyphens
     */
    public String toHexString() {
        return toString().replace("-", "");
    }
    
    public static UUID valueOf(byte[] bytes) {
        if( bytes.length != 16 ) { throw new IllegalArgumentException("UUID must be 16 bytes"); }
//        return valueOf(new ByteArray(array));
        return new UUID(bytes);
    }
    
    /**
     * 
     * @param array must be 16 bytes;  it's also ok to provide a 17 byte array if the MSB is zero (leading zero, which happens when you serialize a positive 128-bit BigInteger to byte array, to preserve the sign)
     * @return 
     */
    public static UUID valueOf(ByteArray array) {
        // XXX TODO the length 17 check may not be necessary now that ByteArray's fromHex method has been fixed
        /*
        if( array.length() == 17 && array.getBytes()[0] == 0 ) {
            log.debug("UUID.valueOf(bytes17)");
            return valueOf(array.subarray(1)); // skip leading zero; recursive call
        }*/
//        log.debug("UUID.valueOf(bytes {})", array.length());
        if( array.length() != 16 ) { throw new IllegalArgumentException("UUID must be 16 bytes"); }
        return new UUID(array.getBytes());
    }
    
    @org.codehaus.jackson.annotate.JsonCreator // jackson 1.x
    @com.fasterxml.jackson.annotation.JsonCreator // jackson 2.x
    public static UUID valueOf(String text) {
        if( text.length() != 32 && text.length() != 36 ) { throw new IllegalArgumentException("UUID must be 16 bytes; up to 4 hyphens allowed for standard UUID hex format"); }
        if( text.length() == 32 ) {
//            log.debug("UUID.valueOf(text32: {})", text);
            return valueOf(ByteArray.fromHex(text));
        }
        if( text.length() == 36 ) {
            String withoutHyphens = text.replaceAll("-", ""); // should be exactly 4 hyphens
            if( withoutHyphens.length() != 32 ) { throw new IllegalArgumentException("UUID must be 16 bytes; up to 4 hyphens allowed for standard UUID hex format"); }
//            log.debug("UUID.valueOf(text36: {})", withoutHyphens);
            return valueOf(ByteArray.fromHex(withoutHyphens));
        }
        throw new IllegalArgumentException("Unrecognized UUID format"); 
    }
    
//    Pattern uuidPattern ...
    /**
     * TODO: make the regex pattern for valid UUIDs and use that instead of the looser "replace hyphens" approach below 
     * @param text
     * @return 
     */
    public static boolean isValid(String text) {
        if( text.length() == 32 ) {
            return HexUtil.isHex(text);
        }
        if( text.length() == 36 ) {
            String withoutHyphens = text.replaceAll("-", ""); // should be exactly 4 hyphens
            if( withoutHyphens.length() != 32 ) { return false; }
            return HexUtil.isHex(withoutHyphens);
        }
        return false;
    }
    
    public static boolean isValid(byte[] bytes) {
        return bytes.length == 16;
    }
    
    
/*
        byte[] uuid = array.getBytes();
        if( uuid.length < 16 ) {
            // add leading zeros; in testing if you create thousands of uuids using java.util.UUID.randomUUID() eventually one of them will be a 15-byte number
            byte[] zero = new byte[16-uuid.length];
            bytes = ByteArray.concat(zero, uuid);
        }
        else if( uuid.length > 16) {
            // just in case java.util.UUID provides us with a 17-byte number!
            bytes = ByteArray.subarray(uuid, 0, 16);
        }
        else {
            // this is the normal case
            bytes = uuid;
        }
 * 
 */    
    /**
     * 
     * @param number must be 128 bit number (small numbers are automatically zero-padded)
     * @return 
     */
    public static UUID valueOf(BigInteger number) {
        ByteArray array = new ByteArray(number);
//        log.debug("Array length 1: {}", array.length());
//        log.debug("Array: {}", array.toHexString());
        // BigInteger inserts a leading zero to preserve sign, but UUIDs do not have a sign bit so we need to strip it off
        if( array.length() == 17 && array.getBytes()[0] == 0 ) {
            return valueOf(array.subarray(1)); // skip leading zero
        }
        if( array.length() < 16 ) {
            int padding = 16 - array.length();
//            log.debug("Adding padding: {} bytes", padding);
            ByteArray zero = new ByteArray(new byte[padding]);
            return valueOf(ByteArray.concat(zero, array));
        }
//        log.debug("Array length 2: {}", array.length());
        if( array.length() != 16 ) { throw new IllegalArgumentException("UUID must be 128 bits"); }
        return valueOf(array);
    }
    
    public static UUID valueOf(java.util.UUID uuid) {
        return new UUID(toByteArray(uuid));
    }
    
    private static byte[] toByteArray(java.util.UUID uuid) {
        BigInteger msb = BigInteger.valueOf(uuid.getMostSignificantBits());
        BigInteger lsb = BigInteger.valueOf(uuid.getLeastSignificantBits());
        BigInteger big = msb.add(BigInteger.ONE).shiftLeft(64).add(lsb); // move msb left 64 bits (8 bytes) to make room for lsb on the right;  adding one first is required, otherwise we always get an off-by-one error like this on the assertion:  f1a57ef26111443[9]a03bb01f893d8e7a  actual: f1a57ef26111443[8]a03bb01f893d8e7a
        return toByteArray(big);        
    }
    
    private static byte[] toByteArray(BigInteger number) {
        byte[] array = number.toByteArray();
        if( array.length < 16 ) {
            // add leading zeros; in testing if you create thousands of uuids using java.util.UUID.randomUUID() eventually one of them will be a 15-byte number
            byte[] zero = new byte[16-array.length];
            return ByteArray.concat(zero, array);
        }
        else if( array.length > 16) {
            // just in case java.util.UUID provides us with a 17-byte number!
            return ByteArray.subarray(array, 0, 16);
        }
        else {
            // this is the normal case
            return array;
        }
        
    }

    @Override
    public int hashCode() {
        return new ByteArray(bytes).toHexString().hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if( other == null ) { return false; }
        if( other == this ) { return true; }
        if( other.getClass() != this.getClass() ) { return false; }
        UUID rhs = (UUID)other;
        return Arrays.equals(bytes, rhs.bytes);
    }
    
}
