package com.intel.mtwilson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intel.dcsg.cpg.validation.ObjectModel;
import com.fasterxml.jackson.annotation.JsonValue;
import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import static com.intel.dcsg.cpg.crypto.DigestAlgorithm.SHA1;
import java.util.ArrayList;
import java.util.List;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
    private final PcrSha1[] sha1pcrs = new PcrSha1[24];    
    private final PcrSha256[] sha2pcrs = new PcrSha256[24];
    //private final Pcr<Sha512Digest>[] sha512pcrs = new Pcr[24];
    
    private final PcrEventLogSha1[] pcrEventLogsSha1 = new PcrEventLogSha1[24];
    private final PcrEventLogSha256[] pcrEventLogsSha256 = new PcrEventLogSha256[24];
    private String measurementXml;
    private byte[] ProvisionedTag; //this is additional field added to support the new way of assetag attestation -- Haidong

    public byte[] getProvisionedTag() {
        return ProvisionedTag;
    }

    public void setProvisionedTag(byte[] ProvisionedTag) {
        this.ProvisionedTag = ProvisionedTag;
    }

    
    public PcrManifest() {
        this.measurementXml = "";
        this.ProvisionedTag = null;
    }
    
    public void setPcr(DigestAlgorithm bank, Pcr pcr) {
        switch(bank) {
            case SHA256:
                sha2pcrs[pcr.getIndex().toInteger()] = (PcrSha256)pcr;
                break;
            default:
                sha1pcrs[pcr.getIndex().toInteger()] = (PcrSha1)pcr;
        }
    }
    
    public void setPcr(Pcr pcr) {        
        setPcr(pcr.getPcrBank(), pcr);
    }
            
    public Pcr getPcr(DigestAlgorithm bank, int index) {
        switch(bank) {
            case SHA256:
                return sha2pcrs[index];
            default:
                return sha1pcrs[index];
        }
    }
    
    @Deprecated
    public Pcr getPcr(int index) {
        return sha1pcrs[index];
    }

    public Pcr getPcr(DigestAlgorithm bank, PcrIndex pcrIndex) {
        return getPcr(bank, pcrIndex.toInteger());
    }
    
    @Deprecated
    public Pcr getPcr(PcrIndex pcrIndex) {
        return getPcr(DigestAlgorithm.SHA1, pcrIndex);
    }
    
    /**
     *
     * @return
     */
    @JsonIgnore
    public Map<DigestAlgorithm, List<Pcr>> getPcrsMap() {
        Map<DigestAlgorithm, List<Pcr>> pcrsMap = new LinkedHashMap<>();
        
        List<Pcr> sha1 = new ArrayList<>();
        for(PcrSha1 pcr : sha1pcrs) {
            if(pcr != null) {
                sha1.add(pcr);
            }
        }
        
        List<Pcr> sha2 = new ArrayList<>();
        for (PcrSha256 pcr : sha2pcrs) {
            if(pcr != null) {
                sha2.add(pcr);
            }
        }
        
        pcrsMap.put(DigestAlgorithm.SHA1, sha1);
        pcrsMap.put(DigestAlgorithm.SHA256, sha2);
        
        return pcrsMap;
    }
    
    public Map<DigestAlgorithm, List<PcrEventLog>> getPcrEventLogMap() {
        Map<DigestAlgorithm, List<PcrEventLog>> eventLogMap = new LinkedHashMap<>();
        
        List<PcrEventLog> sha1 = getPcrEventLogs(DigestAlgorithm.SHA1);        
        List<PcrEventLog> sha2 = getPcrEventLogs(DigestAlgorithm.SHA256);
        
        eventLogMap.put(DigestAlgorithm.SHA1, sha1);
        eventLogMap.put(DigestAlgorithm.SHA256, sha2);
        
        return eventLogMap;
    }
    
    public List<Pcr> getPcrs() {
        List<Pcr> pcrs = new ArrayList<>();
        for(Pcr pcr : sha1pcrs) {
            if(pcr != null) {
                pcrs.add(pcr);
            }
        }
        
        for(Pcr pcr: sha2pcrs) {
            if(pcr != null) {
                pcrs.add(pcr);
            }
        }
        
        return pcrs;
    }
    
    public List<Pcr> getPcrs(DigestAlgorithm bank) {        
        Pcr[] p = bank == DigestAlgorithm.SHA256 ? sha2pcrs : sha1pcrs;
        
        ArrayList<Pcr> pcrsList = new ArrayList<>();
        for (Pcr pcr : p) {
            if (pcr != null)
                pcrsList.add(pcr);
        }        
        return pcrsList;
    }
    
    public void setPcrs(DigestAlgorithm bank, List<Pcr> pcrsList) {
        Pcr[] p = bank == DigestAlgorithm.SHA256 ? sha2pcrs : sha1pcrs;
        
        for (int i = 0; i < 23; i++) {
            p[i] = null;
        }
        for (Pcr pcr : pcrsList) {
            setPcr(bank, pcr);
        }
    }
    
    @Deprecated
    public List<PcrEventLog> getPcrEventLogs() {
        ArrayList<PcrEventLog> pcrEventLogsList = new ArrayList<>();
        for (PcrEventLog pcrEventLog : pcrEventLogsSha1) {
            if (pcrEventLog != null)
                pcrEventLogsList.add(pcrEventLog);
        }
        return pcrEventLogsList;
    }
    
    public List<PcrEventLog> getPcrEventLogs(DigestAlgorithm bank) {
        PcrEventLog[] logs = getEventLogBank(bank);
        
        ArrayList<PcrEventLog> pcrEventLogsList = new ArrayList<>();
        for (PcrEventLog pcrEventLog : logs) {
            if (pcrEventLog != null)
                pcrEventLogsList.add(pcrEventLog);
        }
        return pcrEventLogsList;
    }
    
    @Deprecated
    public void setPcrEventLogs(List<PcrEventLog> pcrEventLogsList) {
        for (int i = 0; i < 23; i++) {
            pcrEventLogsSha1[i] = null;
        }
        for (PcrEventLog pcrEventLog : pcrEventLogsList) {
            setPcrEventLog(pcrEventLog);
        }
    }
    
    public void setPcrEventLogs(DigestAlgorithm bank, List<PcrEventLog> pcrEventLogsList) {         
        for (int i = 0; i < 23; i++) {
            getEventLogBank(bank)[i] = null;
        }
        for (PcrEventLog pcrEventLog : pcrEventLogsList) {
            setPcrEventLog(bank, pcrEventLog);
        }
    }
    
    public void clearPcr(DigestAlgorithm bank, int index) {
        switch(bank) {
            case SHA256:
                sha2pcrs[index] = null;
            default:
                sha1pcrs[index] = null;
        }
    }
    
    @Deprecated
    public void clearPcr(int index) {
        clearPcr(DigestAlgorithm.SHA1, index);
    }
    
    public void clearPcr(DigestAlgorithm bank, PcrIndex pcrIndex) {
        clearPcr(bank, pcrIndex.toInteger());
    }
    
    @Deprecated
    public void clearPcr(PcrIndex pcrIndex) {
        clearPcr(DigestAlgorithm.SHA1, pcrIndex.toInteger());
    }
        
    public void setPcrEventLog(PcrEventLog pcrEventLog) {
        setPcrEventLog(pcrEventLog.getPcrBank(), pcrEventLog);
    }
    
    private void setPcrEventLog(DigestAlgorithm bank, PcrEventLog pcrEventLog) {        
        getEventLogBank(bank)[pcrEventLog.getPcrIndex().toInteger()] = pcrEventLog;
    }
    
    @Deprecated
    public PcrEventLog getPcrEventLog(int index) {
        return pcrEventLogsSha1[index];
    }
    
    public PcrEventLog getPcrEventLog(String bank, int index) {
        return getPcrEventLog(DigestAlgorithm.valueOf(bank), index);
    }
    
    public PcrEventLog getPcrEventLog(DigestAlgorithm bank, int index) {
        PcrEventLog[] log = getEventLogBank(bank);
        return log[index];
    }
        
    @Deprecated
    public PcrEventLog getPcrEventLog(PcrIndex pcrIndex) {
        return pcrEventLogsSha1[pcrIndex.toInteger()];
    }
    
    public PcrEventLog getPcrEventLog(DigestAlgorithm bank, PcrIndex pcrIndex) {
        return getPcrEventLog(bank, pcrIndex.toInteger());
    }
    
    @Deprecated
    public void clearPcrEventLog(int index) {
        pcrEventLogsSha1[index] = null;
    }
    
    public void clearPcrEventLog(DigestAlgorithm bank, int index) {
        PcrEventLog[] log = getEventLogBank(bank);
        log[index] = null;
    }
    
    @Deprecated
    public void clearPcrEventLog(PcrIndex pcrIndex) {
        pcrEventLogsSha1[pcrIndex.toInteger()] = null;
    }
       
    public boolean containsPcr(DigestAlgorithm bank, PcrIndex index) {
        if( index == null) return false;
        
        switch(bank) {
            case SHA1:
                return sha1pcrs[index.toInteger()] != null;
            case SHA256:
                return sha2pcrs[index.toInteger()] != null;
            default:
                return false;
        }
    }
    
    /**
     * Checks to see if the PcrManifest contains the given Pcr (index and value)
     * @param pcr
     * @return true if the PcrManifest contains the given Pcr at its specified index and value, and false in all other cases
     */
    @Deprecated
    public boolean containsPcr(PcrIndex index) {
        return containsPcr(DigestAlgorithm.SHA1, index);
    }

    /**
     * Checks to see if the PcrManifest contains a PcrEventLog for the same pcr index as the given PcrEventLog (index only, does not check contents) 
     * @param pcr
     * @return true if the PcrManifest contains the given Pcr at its specified index and value, and false in all other cases
     */
    @Deprecated
    public boolean containsPcrEventLog(PcrIndex index) {
        if( index == null ) { return false; }
        if( pcrEventLogsSha1[index.toInteger()] == null ) { return false; }
        return true;
    }
    
    public boolean containsPcrEventLog(String bank, PcrIndex index) {
        DigestAlgorithm b = DigestAlgorithm.valueOf(bank.toUpperCase());
        return containsPcrEventLog(b, index);
    }
    
    public boolean containsPcrEventLog(DigestAlgorithm bank, PcrIndex index) {
        if(index == null) { return false; }
        PcrEventLog[] log = getEventLogBank(bank);
        if(log[index.toInteger()] == null) { return false; };
        return true;
    }
    
    /**
     * Returns a string representing the PCR manifest, one PCR index-value pair
     * per line. Only non-null PCRs are represented in the output. PcrEventLogs are ignored.
     * 
     * @return 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String result = "";
        for (Pcr<Sha1Digest> sha1pcr : sha1pcrs) {
            if (sha1pcr != null) {
                result = result.concat(sha1pcr.toString() + "\n");
            }
        }
        
        for(Pcr<Sha256Digest> p : sha2pcrs) {
            if(p != null) {
                result = result.concat(p.toString() + "\n");
            }
        }
        return result;
    }
 
    @Override
    public void validate() {
        int countPcrEntries = 0;
        for(int i=0; i<sha1pcrs.length; i++) {
            if( sha1pcrs[i] != null ) {
                countPcrEntries++;
                if( !sha1pcrs[i].isValid() ) {
                    fault(sha1pcrs[i], String.format("SHA1 Pcr %d is invalid", i));
                }
            }
        }
        
        for(Pcr<Sha256Digest> p : sha2pcrs) {
            if(p != null) {
                countPcrEntries++;
                if(!p.isValid()) {
                    fault(p, String.format("SHA256 Pcr %d is invalid", p.getIndex().toInteger()));
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

    public String getMeasurementXml() {
        return measurementXml;
    }

    public void setMeasurementXml(String measurementXml) {
        this.measurementXml = measurementXml;
    }

    private PcrEventLog[] getEventLogBank(DigestAlgorithm bank) {
        switch(bank) {
            case SHA1:
                return this.pcrEventLogsSha1;
            case SHA256:
                return this.pcrEventLogsSha256;
            default:
                throw new UnsupportedOperationException("PCRBank: " + bank + " not currently supported");
        }
    }
}
