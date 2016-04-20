package com.intel.mtwilson.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.validation.ObjectModel;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.HashCodeBuilder;
//import org.codehaus.jackson.annotate.JsonValue;

/**
 * Represents an ordered list of modules (hash,description), options, or other data that
 * are extended into a specific PCR.
 * 
 * One PcrModuleManifest is equal to another PcrModuleManifest if they specify exactly
 * the same set of module values, in any order. Subsets and supersets
 * are not considered equal.
 * 
 * For the purpose of determining if a given host complies with a policy 
 * (matches the whitelist), you need to have an expected (whitelisted) 
 * PcrModuleManifest and an actual (from the HostReport)  PcrModuleManifest, and
 * compare the expected to the actual. 
 * 
 * @since 1.2
 * @author jbuhacoff
 */
public class PcrEventLog extends ObjectModel {
    private final PcrIndex pcrIndex;
    private final List<Measurement> eventLog = new ArrayList<>();

    public PcrEventLog(PcrIndex pcrIndex) {
        this.pcrIndex = pcrIndex;
    }
    
    @JsonCreator
    public PcrEventLog(@JsonProperty("pcr_index") PcrIndex pcrIndex, @JsonProperty("event_log") List<Measurement> moduleManifest) {
        this.pcrIndex = pcrIndex;
        if( moduleManifest != null ) {
            this.eventLog.addAll(moduleManifest);
        }
    }
    
    public PcrIndex getPcrIndex() { return pcrIndex; }
    public List<Measurement> getEventLog() { return eventLog; }
    
    /**
     * Checks to see if the PcrModuleManifest contains the given Measurement (value & description)
     * @param measurement
     * @return true if the PcrModuleManifest contains the given Measurement value
     */
    public boolean contains(Measurement m) {
        if( m == null ) { return false; }
        return eventLog.contains(m); 
    }
    
    /**
     * Checks to see if the PcrModuleManifest contains a Measurement with the given SHA1 digest value
     * @param value
     * @return true if the PcrModuleManifest contains an entry with the specified value, false otherwise
     */
    public boolean contains(Sha1Digest value) {
        if( value == null ) { return false; }
        for(Measurement m : eventLog) {
            if( m.getValue().equals(value) ) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns a string representing the PCR manifest, one PCR index-value pair
     * per line. Only non-null PCRs are represented in the output. 
     * 
     * @see java.lang.Object#toString()
     */
//    @JsonValue
    @Override
    public String toString() {
        String result = String.format("PCR %d module manifest:", pcrIndex.toInteger());
        for(Measurement m : eventLog) {
            result = result.concat(m.getValue().toString()+" "+m.getLabel()+"\n");
        }
        return result;
    }
    
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(17,57);
        builder.append(pcrIndex);
        for(Measurement m : eventLog) {
            builder.append(m);
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
        PcrEventLog rhs = (PcrEventLog)other;
//        EqualsBuilder builder = new EqualsBuilder(); // org.apache.commons.lang3.builder.EqualsBuilder
        if( !pcrIndex.equals(rhs.pcrIndex)) { return false; }
        if( !eventLog.equals(rhs.eventLog)) { return false; }
        return true;
    }

    @Override
    public void validate() {
        if( eventLog == null ) {
            fault("Measurement set is null");
        }
        else if( eventLog.isEmpty() ) {
            fault("Measurement set is empty");
        }
        else {
            for(Measurement m : eventLog) {
                if( !m.isValid() ) {
                    fault(m, "Invalid measurement %s in module manifest", m.getLabel());
                }
            }
        }
    }

}
