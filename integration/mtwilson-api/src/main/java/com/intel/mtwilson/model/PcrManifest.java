package com.intel.mtwilson.model;

import com.intel.dcsg.cpg.validation.ObjectModel;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;
//import org.codehaus.jackson.annotate.JsonValue;

/**
 * The PcrManifest class represents a list of PCR numbers, their values,
 * and any event information that is available about each PCR that is
 * reported from a specific host.  
 * 
 * DO NOT USE THIS CLASS AS A "WHITELIST", IT IS ONLY FOR "ACTUAL" VALUES.
 * 
 * Bug #607 the whitelist is now represented as a collection of TrustPolicy 
 * instances, which is much more powerful than a list of PCR's and their 
 * values because those policy instances can also encapsulate formulas with
 * variables that allow us to verify things such as a host's UUID being extended 
 * into its PCR 0.
 * 
 * For example, a PcrManifest instance may include values for a
 * list of 3 PCRs only, such as 17, 18, and 19.
 * 
 * In order to store event information, there must be a value stored
 * for the PCR as well.
 * 
 * The equals() method has not been defined for this class. Do NOT use equals()
 * to determine if two PcrManifest instances have the same contents.
 * 
 * BUG #497  and BUG #607  this class should replace the IManifest interface in places
 * where it's referring to a PCR manifest.  
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class PcrManifest extends ObjectModel {
    private final Pcr[] pcrs = new Pcr[24];
    private final PcrEventLog[] pcrEventLogs = new PcrEventLog[24];

    public PcrManifest() {
    }
    
    public void setPcr(Pcr pcr) {
        pcrs[pcr.getIndex().toInteger()] = pcr;
    }
    
    public Pcr getPcr(int index) {
        return pcrs[index];
    }

    public Pcr getPcr(PcrIndex pcrIndex) {
        return pcrs[pcrIndex.toInteger()];
    }
    
    public List<Pcr> getPcrs() {
        ArrayList<Pcr> pcrsList = new ArrayList<>();
        for (Pcr pcr : pcrs) {
            if (pcr != null)
                pcrsList.add(pcr);
        }
        return pcrsList;
    }
    
    public void setPcrs(List<Pcr> pcrsList) {
        for (int i = 0; i < 23; i++) {
            pcrs[i] = null;
        }
        for (Pcr pcr : pcrsList) {
            setPcr(pcr);
        }
    }
    
    public List<PcrEventLog> getPcrEventLogs() {
        ArrayList<PcrEventLog> pcrEventLogsList = new ArrayList<>();
        for (PcrEventLog pcrEventLog : pcrEventLogs) {
            if (pcrEventLog != null)
                pcrEventLogsList.add(pcrEventLog);
        }
        return pcrEventLogsList;
    }
    
    public void setPcrEventLogs(List<PcrEventLog> pcrEventLogsList) {
        for (int i = 0; i < 23; i++) {
            pcrEventLogs[i] = null;
        }
        for (PcrEventLog pcrEventLog : pcrEventLogsList) {
            setPcrEventLog(pcrEventLog);
        }
    }
    
    public void clearPcr(int index) {
        pcrs[index] = null;
    }
    
    public void clearPcr(PcrIndex pcrIndex) {
        pcrs[pcrIndex.toInteger()] = null;
    }
    
    public void setPcrEventLog(PcrEventLog pcrEventLog) {
        pcrEventLogs[pcrEventLog.getPcrIndex().toInteger()] = pcrEventLog;
    }
    
    public PcrEventLog getPcrEventLog(int index) {
        return pcrEventLogs[index];
    }

    public PcrEventLog getPcrEventLog(PcrIndex pcrIndex) {
        return pcrEventLogs[pcrIndex.toInteger()];
    }
    
    public void clearPcrEventLog(int index) {
        pcrEventLogs[index] = null;
    }
    
    public void clearPcrEventLog(PcrIndex pcrIndex) {
        pcrEventLogs[pcrIndex.toInteger()] = null;
    }
    
    
    /**
     * Checks to see if the PcrManifest contains the given Pcr (index and value)
     * @param pcr
     * @return true if the PcrManifest contains the given Pcr at its specified index and value, and false in all other cases
     */
    public boolean containsPcr(PcrIndex index) {
        if( index == null ) { return false; }
        if( pcrs[index.toInteger()] == null ) { return false; }
        return false;
    }

    /**
     * Checks to see if the PcrManifest contains a PcrEventLog for the same pcr index as the given PcrEventLog (index only, does not check contents) 
     * @param pcr
     * @return true if the PcrManifest contains the given Pcr at its specified index and value, and false in all other cases
     */
    public boolean containsPcrEventLog(PcrIndex index) {
        if( index == null ) { return false; }
        if( pcrEventLogs[index.toInteger()] == null ) { return false; }
        return true;
    }
    
    /**
     * Returns a string representing the PCR manifest, one PCR index-value pair
     * per line. Only non-null PCRs are represented in the output. PcrEventLogs are ignored.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String result = "";
        for(int i=0; i<pcrs.length; i++) {
            if( pcrs[i] != null ) { result = result.concat(pcrs[i].toString()+"\n"); }
        }
        return result;
    }
 
    @Override
    public void validate() {
        int countPcrEntries = 0;
        for(int i=0; i<pcrs.length; i++) {
            if( pcrs[i] != null ) {
                countPcrEntries++;
                if( !pcrs[i].isValid() ) {
                    fault(pcrs[i], String.format("Pcr %d is invalid", i));
                }
            }
        }
        if( countPcrEntries == 0 ) {
            fault("Pcr manifest does not have any entries");
        }
        // following section commented out because it is not an error to be missing pcr event logs ..... well the policy should decide that ! 
        /*
        int countPcrEventLogEntries = 0;
        for(int i=0; i<pcrEventLogs.length; i++) {
            if( pcrEventLogs[i] != null ) {
                countPcrEventLogEntries++;
                if( !pcrEventLogs[i].isValid() ) {
                    fault(pcrEventLogs[i], String.format("PcrEventLog %d is invalid", i));
                }
            }
        }
        if( countPcrEventLogEntries == 0 ) {
            fault("Pcr manifest does not have any event log entries");
        }*/
        
    }

}
