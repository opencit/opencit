package com.intel.mtwilson.model;

import com.intel.mtwilson.validation.ObjectModel;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * The PcrManifest class represents a list of PCR (index,value) pairs that
 * form a subset of what may be in the TPM.  For example, a PcrManifest may
 * specify values only for PCR's 17, 18, and 19. 
 * 
 * One PcrManifest is equal to another PcrManifest if they specify exactly
 * the same set of PCR indices and corresponding values. Subsets and supersets
 * are not considered equal.
 * 
 * For the purpose of determining if a given host complies with a policy 
 * (matches the whitelist), you need to have an expected (whitelisted) 
 * PcrManifest and an actual (from the host's TPM Quote)  PcrManifest, and
 * compare the expected to the actual - however, you do not want to do this
 * using equals() because the whitelist only specifies PCRs that it is 
 * interested in and all others can have any value. 
 * 
 * 
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
    
    public void clearPcr(int index) {
        pcrs[index] = null;
    }
    
    
    /**
     * Checks to see if the PcrManifest contains the given Pcr (index and value)
     * @param pcr
     * @return true if the PcrManifest contains the given Pcr at its specified index and value, and false in all other cases
     */
    public boolean contains(Pcr pcr) {
        if( pcr == null ) { return false; }
        if( pcrs[pcr.getIndex().toInteger()] == null ) { return false; }
        return pcrs[pcr.getIndex().toInteger()].equals(pcr);
    }
    
    /**
     * Checks to see if the PcrManifest contains an entry for the given index.
     * @param index 0-23
     * @return true if the PcrManifest contains an entry (any non-null value) at the specified index, and false if it does not contain an entry for that index
     */
    public boolean contains(int index) {
        return pcrs[index] != null;
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
            if( pcrs[i] != null ) { result = result.concat(pcrs[i].toString()+"\n"); }
        }
        return result;
    }
    
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(17,51);
        for(int i=0; i<pcrs.length; i++) {
            if( pcrs[i] != null ) {
                builder.append(i);
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
        int countEntries = 0;
        for(int i=0; i<pcrs.length; i++) {
            if( pcrs[i] != null ) {
                countEntries++;
                if( !pcrs[i].isValid() ) {
                    fault(pcrs[i], String.format("Pcr %d is invalid", i));
                }
            }
        }
        if( countEntries == 0 ) {
            fault("Pcr manifest does not have any entries");
        }
    }

}
