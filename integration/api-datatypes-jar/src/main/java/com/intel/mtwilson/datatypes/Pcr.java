package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.validation.Fault;
import com.intel.mtwilson.validation.Model;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * Representation of a single PCR Number in the TPM. A PCR Number is a non-negative
 * integer, typically in the range 0..23 but different TPM models may have a
 * different number of PCR's so we do not limit the high end. 
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class Pcr implements Model {
    private final int number;

    public Pcr(int pcrNumber) {
        number = pcrNumber;
    }
    
    public Integer toInteger() { return number; }
    
    /**
     * Returns a string representing the PCR Number.
     * Example: assert new Pcr(15).toString().equals("15");
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
        final Pcr other = (Pcr) obj;
        if (this.number != other.number) {
            return false;
        }
        return true;
    }

    
    private ArrayList<Fault> faults = new ArrayList<Fault>();
    
    @Override
    public boolean isValid() {
        faults.clear();
        if( number < 0 ) { faults.add(new Fault("Pcr number must be non-negative"));  }
        return faults.isEmpty();
   }

    @Override
    public List<Fault> getFaults() {
        ArrayList<Fault> copy = new ArrayList<Fault>(faults);
        return copy;
    }
}
