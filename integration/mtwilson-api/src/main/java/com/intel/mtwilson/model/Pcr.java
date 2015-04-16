package com.intel.mtwilson.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.dcsg.cpg.validation.ObjectModel;
import com.intel.dcsg.cpg.crypto.Sha1Digest;

//import org.codehaus.jackson.annotate.JsonValue;

/**
 * BUG #497   renamed to "Pcr" to represent a pair (index,value)
 * the value can continue to be represented as Sha1Digest. 
 * Representation of a single PCR Value in the TPM. A PCR value consists of
 * the PCR Number and the SHA1 Digest. 
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class Pcr extends ObjectModel {
    private final PcrIndex pcrIndex;
    private final Sha1Digest pcrValue;

    public Pcr(int pcrNumber, byte[] sha1Digest) {
        this(new PcrIndex(pcrNumber), new Sha1Digest(sha1Digest));
    }
    
    @JsonCreator
    public Pcr(@JsonProperty("index") int pcrNumber, @JsonProperty("value") String sha1Digest) {
        this(new PcrIndex(pcrNumber), new Sha1Digest(sha1Digest));
    }
    
    public Pcr(PcrIndex pcr, Sha1Digest digest) {
        this.pcrIndex = pcr;
        this.pcrValue = digest;
    }

    public PcrIndex getIndex() { return pcrIndex; } // BUG #497 needs to be renamed getIndex() and return a type PcrIndex
    public Sha1Digest getValue() { return pcrValue; }
    
    /**
     * Returns a string representing the PCR Value in the format "pcr: value"
     * Example: assert new PcrValue(15,"...").toString().equals("15: ...");
     *
     * @see java.lang.Object#toString()
     */
//    @JsonValue
    @Override
    public String toString() {
        return String.format("%d: %s", pcrIndex.toInteger(), pcrValue.toString());
    }
    
    @Override
    public int hashCode() {
        return pcrIndex.hashCode() + pcrValue.hashCode();
    }

    /**
     * Returns true only if the PcrIndex and PcrValue of this object and the other
     * object are identical.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pcr other = (Pcr) obj;
        if ((this.pcrIndex == null) ? (other.pcrIndex != null) : !this.pcrIndex.equals(other.pcrIndex)) {
            return false;
        }
        if ((this.pcrValue == null) ? (other.pcrValue != null) : !this.pcrValue.equals(other.pcrValue)) {
            return false;
        }
        return true;
    }

    @Override
    public void validate() {
        if( pcrIndex == null ) { fault("Pcr index is null"); }
        else if( !pcrIndex.isValid() ) { fault(pcrIndex, "Invalid pcr index"); }
        if( pcrValue == null ) { fault("SHA1 Digest is null"); }
        //else if (!pcrValue.isValid()) { fault(pcrValue, "Invalid pcr value"); }
        else if (!Sha1Digest.isValid(pcrValue.toByteArray())) { fault("Invalid pcr value"); }
    }

}
