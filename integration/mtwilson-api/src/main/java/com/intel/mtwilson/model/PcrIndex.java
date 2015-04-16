package com.intel.mtwilson.model;

import com.intel.dcsg.cpg.validation.ObjectModel;
import com.fasterxml.jackson.annotation.JsonValue;
//import org.codehaus.jackson.annotate.JsonValue;

/**
 * BUG #497  renamed PcrIndex to be clearer that it's just the register number,
 * not the pair (index,value).
 * 
 * Representation of a single PCR Number in the TPM. A PCR Number is a non-negative
 * integer, typically in the range 0..23 but different TPM models may have a
 * different number of PCR's so in the future we may want to drop the MAX_VALUE
 * check.
 * 
 * The PcrIndex instances are immutable. Use the static valueOf method to obtain
 * constant instances for PCRs 0-23, or use the constructor to create new instances.
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
    
    
    public static final PcrIndex PCR0 = new PcrIndex(0);
    public static final PcrIndex PCR1 = new PcrIndex(1);
    public static final PcrIndex PCR2 = new PcrIndex(2);
    public static final PcrIndex PCR3 = new PcrIndex(3);
    public static final PcrIndex PCR4 = new PcrIndex(4);
    public static final PcrIndex PCR5 = new PcrIndex(5);
    public static final PcrIndex PCR6 = new PcrIndex(6);
    public static final PcrIndex PCR7 = new PcrIndex(7);
    public static final PcrIndex PCR8 = new PcrIndex(8);
    public static final PcrIndex PCR9 = new PcrIndex(9);
    public static final PcrIndex PCR10 = new PcrIndex(10);
    public static final PcrIndex PCR11 = new PcrIndex(11);
    public static final PcrIndex PCR12 = new PcrIndex(12);
    public static final PcrIndex PCR13 = new PcrIndex(13);
    public static final PcrIndex PCR14 = new PcrIndex(14);
    public static final PcrIndex PCR15 = new PcrIndex(15);
    public static final PcrIndex PCR16 = new PcrIndex(16);
    public static final PcrIndex PCR17 = new PcrIndex(17);
    public static final PcrIndex PCR18 = new PcrIndex(18);
    public static final PcrIndex PCR19 = new PcrIndex(19);
    public static final PcrIndex PCR20 = new PcrIndex(20);
    public static final PcrIndex PCR21 = new PcrIndex(21);
    public static final PcrIndex PCR22 = new PcrIndex(22);
    public static final PcrIndex PCR23 = new PcrIndex(23);
    
    /**
     * Returns a constant PcrIndex instance for the given index if it
     * is between MIN_VALUE and MAX_VALUE,  otherwise attempts to
     * instantiate a new PcrIndex with the given index (in case your
     * TPM has more PCRs than are hard-coded here!)
     * @param i
     * @return 
     */
    public static PcrIndex valueOf(int i) {
        switch(i) {
            case 0: return PCR0;
            case 1: return PCR1;
            case 2: return PCR2;
            case 3: return PCR3;
            case 4: return PCR4;
            case 5: return PCR5;
            case 6: return PCR6;
            case 7: return PCR7;
            case 8: return PCR8;
            case 9: return PCR9;
            case 10: return PCR10;
            case 11: return PCR11;
            case 12: return PCR12;
            case 13: return PCR13;
            case 14: return PCR14;
            case 15: return PCR15;
            case 16: return PCR16;
            case 17: return PCR17;
            case 18: return PCR18;
            case 19: return PCR19;
            case 20: return PCR20;
            case 21: return PCR21;
            case 22: return PCR22;
            case 23: return PCR23;
            default:
                return new PcrIndex(i);
        }
    }
    
    public static PcrIndex valueOf(String istr) {
        return valueOf(Integer.valueOf(istr));
    }
}
