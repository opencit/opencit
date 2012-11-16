package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.validation.Fault;
import com.intel.mtwilson.validation.Model;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * Representation of a single SHA1 Digest. An SHA1 Digest is a 20-byte value.
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class Sha1Digest implements Model {
    private final byte[] value;
    private final String hex;

    public Sha1Digest(byte[] value) {
        if( value.length != 20 ) { throw new IllegalArgumentException("SHA1 Digest must be 20 bytes long"); }
        this.value = value;
        this.hex = Hex.encodeHexString(value);
    }
    
    public Sha1Digest(String hex) {
        if( hex.length() != 40 ) { throw new IllegalArgumentException("SHA1 Digest must be 20 bytes (40 hex digits) long"); }
        this.hex = hex;
        try {
            this.value = Hex.decodeHex(hex.toCharArray());
        }
        catch(DecoderException e) {
            throw new IllegalArgumentException("Invalid SHA1 Digest", e);
        }
    }
    
    /**
     * Returns the bytes comprising the SHA1 Digest.
     * @return 
     */
    public byte[] toByteArray() { return value; }
    
    /**
     * Returns a string representing the SHA1 Digest in hexadecimal form.
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
        final Sha1Digest other = (Sha1Digest) obj;
        if (!Arrays.equals(value, other.value)) {
            return false;
        }
        return true;
    }

    private ArrayList<Fault> faults = new ArrayList<Fault>();

    @Override
    public boolean isValid() {
        faults.clear();
        if( value == null ) { faults.add(new Fault("SHA1 Digest is null")); }
        else if( value.length != 20 ) { faults.add(new Fault("SHA1 Digest must be 20 bytes long")); }
        return faults.isEmpty();
    }

    @Override
    public List<Fault> getFaults() {
        ArrayList<Fault> copy = new ArrayList<Fault>(faults);
        return copy;
    }
}
