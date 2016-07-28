package com.intel.mtwilson.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.intel.dcsg.cpg.crypto.AbstractDigest;
import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.dcsg.cpg.validation.ObjectModel;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;

//import org.codehaus.jackson.annotate.JsonValue;

/**
 * BUG #497   renamed to "Pcr" to represent a pair (index,value)
 * the value can continue to be represented as Sha1Digest. 
 * Representation of a single PCR Value in the TPM. A PCR value consists of
 * the PCR Number and the SHA1 Digest. 
 * 
 * @param <T>
 * @since 0.5.4
 * @author jbuhacoff
 */

@JsonTypeInfo(use = Id.CLASS,
              include = JsonTypeInfo.As.PROPERTY,
              property = "digest_type")
@JsonSubTypes({
    @Type(value = PcrSha1.class),
    @Type(value = PcrSha256.class)
})
public abstract class Pcr<T extends AbstractDigest> extends ObjectModel {
    private final PcrIndex pcrIndex;          
    
    protected Pcr(PcrIndex pcrNumber) {
        pcrIndex = pcrNumber;
    }
    
    public PcrIndex getIndex() { return pcrIndex; } // BUG #497 needs to be renamed getIndex() and return a type PcrIndex
    abstract public T getValue();
    abstract public DigestAlgorithm getPcrBank();
    /**
     * Returns a string representing the PCR Value in the format "pcr: value"
     * Example: assert new PcrValue(15,"...").toString().equals("15: ...");
     *
     * @return 
     * @see java.lang.Object#toString()
     */
//    @JsonValue
    @Override
    public String toString() {
        return String.format("%s: %d: %s", getPcrBank(), pcrIndex.toInteger(), getValue().toString());
    }
    
    @Override
    public int hashCode() {
        return pcrIndex.hashCode() + getValue().hashCode();
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
        if ((this.getValue() == null) ? (other.getValue() != null) : !this.getValue().equals(other.getValue())) {
            return false;
        }
        return true;
    }

    @Override
    public void validate() {
        if( pcrIndex == null ) { fault("Pcr index is null"); }
        else if( !pcrIndex.isValid() ) { fault(pcrIndex, "Invalid pcr index"); }
        if( getValue() == null ) { fault("Digest is null"); }
        //else if (!pcrValue.isValid()) { fault(pcrValue, "Invalid pcr value"); }
        
        validateOverride();                
    }
    
    protected abstract void validateOverride();

}
