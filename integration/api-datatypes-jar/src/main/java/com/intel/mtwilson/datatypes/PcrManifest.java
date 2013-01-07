package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.validation.ObjectModel;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * BUG #497   this class should replace the IManifest interface in places
 * where it's referring to a PCR manifest.  
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class PcrManifest extends ObjectModel {
    private final Pcr[] pcrs = new Pcr[24];

    public PcrManifest() {
    }
    
    public void setPcr(Pcr pcr) {
        pcrs[pcr.getIndex().toInteger()] = pcr;
    }
    
    public Pcr getPcr(int index) {
        return pcrs[index];
    }
    
    
    /**
     * Returns a string representing the PCR manifest, one PCR index-value pair
     * per line. Only non-null PCRs are represented in the output. 
     * 
     * @see java.lang.Object#toString()
     */
    @JsonValue
    @Override
    public String toString() {
        String result = "";
        for(int i=0; i<pcrs.length; i++) {
            if( pcrs[i] != null ) { result += pcrs[i].toString()+"\n"; }
        }
        return result;
    }
    
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(17,51);
        for(int i=0; i<pcrs.length; i++) {
            if( pcrs[i] != null ) {
                builder.append(pcrs[i]);
            }
        }
        return builder.toHashCode(); 
    }
    
    /**
     * A PCR Manifest is equal to another if it contains exactly the same
     * digest values in the same registers. In addition, because a PCR Manifest
     * can have ignored (null) digests for some registers, both manifests must
     * have null digests for the same registers.
     * @param other
     * @return 
     */
    @Override
    public boolean equals(Object other) {
        if( other == null ) { return false; }
        if( other == this ) { return true; }
        if( other.getClass() != this.getClass() ) { return false; }
        PcrManifest rhs = (PcrManifest)other;
        EqualsBuilder builder = new EqualsBuilder();
        for(int i=0; i<pcrs.length; i++) {
            builder.append(pcrs[i], rhs.pcrs[i]);
        }
        return builder.isEquals();
    }

    @Override
    public void validate() {
        // XXX TODO what is an invalid condition for the pcr manifest?  is an empty manifest allowed (all 24 pcr's null)? 
    }

}
