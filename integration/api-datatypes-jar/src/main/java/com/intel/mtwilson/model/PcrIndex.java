package com.intel.mtwilson.model;

import com.intel.mtwilson.validation.ObjectModel;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * BUG #497  renamed PcrIndex to be clearer that it's just the register number,
 * not the pair (index,value).
 * 
 * Representation of a single PCR Number in the TPM. A PCR Number is a non-negative
 * integer, typically in the range 0..23 but different TPM models may have a
 * different number of PCR's so in the future we may want to drop the MAX_VALUE
 * check.
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class PcrIndex extends ObjectModel {
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 23;
    
    private final int number;

    public PcrIndex(int pcrNumber) {
        number = pcrNumber;
    }
    
    public Integer toInteger() { return number; }
    
    /**
     * Returns a string representing the PCR Number.
     * Example: assert new PcrIndex(15).toString().equals("15");
     *
     * @see java.lang.Object#toString()
     */
    @JsonValue
    @Override
    public String toString() {
        return String.valueOf(number);
    }
    
    @Override
    public int hashCode() {
        return number;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PcrIndex other = (PcrIndex) obj;
        if (this.number != other.number) {
            return false;
        }
        return true;
    }


    @Override
    protected void validate() {
        if( number < MIN_VALUE ) { fault("Pcr index must be non-negative");  }
        if( number > MAX_VALUE ) { fault(String.format("Pcr index must be in the range %d-%d", MIN_VALUE, MAX_VALUE)); }
    }
}
