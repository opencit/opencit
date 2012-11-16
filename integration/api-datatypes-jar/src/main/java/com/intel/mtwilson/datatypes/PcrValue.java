package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.validation.Fault;
import com.intel.mtwilson.validation.Model;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * Representation of a single PCR Value in the TPM. A PCR value consists of
 * the PCR Number and the SHA1 Digest. 
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class PcrValue implements Model {
    private final Pcr pcr;
    private final Sha1Digest sha1Digest;

    public PcrValue(int pcrNumber, String sha1Digest) {
        this(new Pcr(pcrNumber), new Sha1Digest(sha1Digest));
    }
    
    public PcrValue(Pcr pcr, Sha1Digest digest) {
        this.pcr = pcr;
        this.sha1Digest = digest;
    }

    public Pcr getPcr() { return pcr; }
    public Sha1Digest getValue() { return sha1Digest; }
    
    /**
     * Returns a string representing the PCR Value in the format "pcr: value"
     * Example: assert new PcrValue(15,"...").toString().equals("15: ...");
     *
     * @see java.lang.Object#toString()
     */
    @JsonValue
    @Override
    public String toString() {
        return String.format("%d: %s", pcr.toInteger(), sha1Digest.toString());
    }
    
    @Override
    public int hashCode() {
        return pcr.hashCode() + sha1Digest.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PcrValue other = (PcrValue) obj;
        if ((this.pcr == null) ? (other.pcr != null) : !this.pcr.equals(other.pcr)) {
            return false;
        }
        if ((this.sha1Digest == null) ? (other.sha1Digest != null) : !this.sha1Digest.equals(other.sha1Digest)) {
            return false;
        }
        return true;
    }

    private ArrayList<Fault> faults = new ArrayList<Fault>();
    
    @Override
    public boolean isValid() {
        faults.clear();
        if( pcr == null ) { faults.add(new Fault("Pcr is null")); }
        else if( !pcr.isValid() ) { faults.addAll(pcr.getFaults()); }
        if( sha1Digest == null ) { faults.add(new Fault("SHA1 Digest is null")); }
        else if (!sha1Digest.isValid()) { faults.addAll(sha1Digest.getFaults()); }
        return faults.isEmpty();
    }

    @Override
    public List<Fault> getFaults() {
        ArrayList<Fault> copy = new ArrayList<Fault>(faults);
        return copy;
    }
}
