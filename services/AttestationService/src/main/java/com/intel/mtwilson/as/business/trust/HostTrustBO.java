package com.intel.mtwilson.as.business.trust;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.agent.*;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.as.controller.MwKeystoreJpaController;
import com.intel.mtwilson.as.controller.TblLocationPcrJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestLogJpaController;
import com.intel.mtwilson.as.controller.TblSamlAssertionJpaController;
import com.intel.mtwilson.as.controller.TblTaLogJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblLocationPcr;
import com.intel.mtwilson.as.data.TblSamlAssertion;
import com.intel.mtwilson.as.data.TblTaLog;
import com.intel.mtwilson.as.data.TblModuleManifestLog;
import com.intel.mtwilson.as.helper.BaseBO;
import com.intel.mtwilson.as.helper.saml.SamlAssertion;
import com.intel.mtwilson.as.helper.saml.SamlGenerator;
import com.intel.mtwilson.audit.api.AuditLogger;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.io.FileResource;
import com.intel.mtwilson.io.Resource;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.policy.Fault;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.Policy;
import com.intel.mtwilson.policy.PolicyEngine;
import com.intel.mtwilson.policy.Rule;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.TrustReport;
import com.intel.mtwilson.policy.fault.PcrEventLogMissingExpectedEntries;
import com.intel.mtwilson.policy.impl.HostTrustPolicyManager;
import com.intel.mtwilson.policy.impl.TrustMarker;
import com.intel.mtwilson.policy.rule.PcrEventLogIncludes;
import com.intel.mtwilson.policy.rule.PcrEventLogIntegrity;
import com.intel.mtwilson.policy.rule.PcrMatchesConstant;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.configuration.Configuration;
import org.joda.time.DateTime;
import org.opensaml.xml.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 *
 * @author dsmagadx
 */
public class HostTrustBO extends BaseBO {
    public static final String SAML_KEYSTORE_NAME = "SAML";
    private static final Logger log = LoggerFactory.getLogger(HostTrustBO.class);
    Marker sysLogMarker = MarkerFactory.getMarker(LogMarkers.HOST_ATTESTATION.getValue());
    
    private static final int DEFAULT_CACHE_VALIDITY_SECS = 3600;
    private static final int CACHE_VALIDITY_SECS;
    private MwKeystoreJpaController keystoreJpa = new MwKeystoreJpaController(getEntityManagerFactory());
    private Resource samlKeystoreResource = null;
    
    private HostBO hostBO;
    
    static{
        CACHE_VALIDITY_SECS = ASConfig.getConfiguration().getInt("saml.validity.seconds", DEFAULT_CACHE_VALIDITY_SECS);
        log.info("Config saml.validity.seconds = " + CACHE_VALIDITY_SECS);
    }
    
    public HostTrustBO() {
        super();
        loadSamlSigningKey();
    }
    
    public HostTrustBO(PersistenceManager pm) {
        super(pm);
        loadSamlSigningKey();
    }
    
    public void setHostBO(HostBO hostBO) { this.hostBO = hostBO; }
    
    private void loadSamlSigningKey() {
        // XXX was going to store saml keys in the database but a better way is for each server to have its own and to make a CA sign all of them
        /*
        MwKeystore mwKeystore = keystoreJpa.findMwKeystoreByName(SAML_KEYSTORE_NAME);
        if( mwKeystore != null && mwKeystore.getKeystore() != null ) {
            samlKeystoreResource = new ByteArrayResource(mwKeystore.getKeystore());
        }
        */
        try {
            samlKeystoreResource = new FileResource(ResourceFinder.getFile(ASConfig.getConfiguration().getString("saml.keystore.file", "SAML.jks")));
        }
        catch(FileNotFoundException e) {
            log.error("Cannot find SAML keystore");
        }
    }
        
    /**
     * BUG #607 complete rewrite of this to use the "TrustPolicy" framework in the trust-policy module 
     * instead of the "Strategy" and "IManifest" framework in what was in vmware-trust-utils module.
     * 
     * @param hostName must not be null
     * @return 
     */
    public HostTrustStatus getTrustStatus(Hostname hostName) throws IOException {
        if( hostName == null ) { throw new IllegalArgumentException("missing hostname"); }
        long start = System.currentTimeMillis();
        
        TblHosts tblHosts = getHostByName(hostName);
        return getTrustStatus(tblHosts, hostName.toString());
    }
    
    public HostTrustStatus getTrustStatusByAik(Sha1Digest aik) throws IOException {
        if( aik == null ) { throw new IllegalArgumentException("missing AIK fingerprint"); }
        try {
            TblHosts tblHosts = getHostByAik(aik);
            return getTrustStatus(tblHosts, aik.toString());
        }
        catch(IOException e) {
            log.error("Cannot get trust status for {}", aik.toString(), e); // log the error for sysadmin to troubleshoot, since we are not allowing the original exception to propagate
            throw new IOException("Cannot get trust status for "+aik.toString()); // rethrowing to make sure that the hostname is not leaked from an exception message; we only provide the AIK in the message
        }
    }
    
    /**
     * 
     * @param tblHosts
     * @param hostId can be Hostname or AIK (SHA1 hex) ; it's used in any exceptions to refer to the host.  this allows us to use the same code for a trust report lookup by hostname and by aik
     * @return 
     */
    public HostTrustStatus getTrustStatus(TblHosts tblHosts, String hostId) throws IOException {
        if (tblHosts == null) {
            throw new ASException(
                    ErrorCode.AS_HOST_NOT_FOUND,
                    hostId);
        }
        long start = System.currentTimeMillis();
        log.info( "VMM name for host is {}", tblHosts.getVmmMleId().getName());
        log.info( "OS name for host is {}", tblHosts.getVmmMleId().getOsId().getName());

        TrustReport trustReport = getTrustReportForHost(tblHosts, hostId);
        
        HostTrustStatus trust = new HostTrustStatus();
        trust.bios = trustReport.isTrustedForMarker(TrustMarker.BIOS.name());
        trust.vmm = trustReport.isTrustedForMarker(TrustMarker.VMM.name());

        // previous check for trusted location was if the host's location field is not null, then it's trusted... but i think this is better as it checks the pcr.  
        // XXX TODO need a better feedback mechanism from trust policies... when they succeed, they should be able to set attributes.
        // or else,  just go with the "marks" thing but then we have to post process and look for certain marks and then  set other fields elsewhere based on them ... or maybe that's not necessary??)
//        trust.location = tblHosts.getLocation() != null; // if location is available (it comes from PCR 22), it's trusted
        trust.location = trustReport.isTrustedForMarker(TrustMarker.LOCATION.name());
        
        Date today = new Date(System.currentTimeMillis()); // create the date here and pass it down, in order to ensure that all created records use the same timestamp
        logOverallTrustStatus(tblHosts, trust, today);
        logPcrTrustStatus(tblHosts, trustReport, today);
        

        String userName = new AuditLogger().getAuditUserName();
        Object[] paramArray = {userName, hostId, trust.bios, trust.vmm};
        log.info(sysLogMarker, "User_Name: {} Host_Name: {} BIOS_Trust: {} VMM_Trust: {}.", paramArray);
        
        log.info( "Verfication Time {}", (System.currentTimeMillis() - start));

        return trust;
    }
    
    /**
     * NOTE:  the trust report MUST NOT include the host name or ip address;  it's fine to include the AIK.
     * This property allows the trust report to be used anonymously or to be attached to hostname/ipaddress 
     * at a higher level if needed for a non-privacy application.
     * 
     * @param tblHosts
     * @return
     * @throws IOException 
     */
    public TrustReport getTrustReportForHost(TblHosts tblHosts, String hostId) throws IOException {
        // bug #538 first check if the host supports tpm
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent agent = factory.getHostAgent(tblHosts);
        if( !agent.isTpmEnabled() || !agent.isIntelTxtEnabled() ) {
            throw new ASException(ErrorCode.AS_INTEL_TXT_NOT_ENABLED, hostId.toString());
        }
        
        PcrManifest pcrManifest = agent.getPcrManifest();
        
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = pcrManifest;
        hostReport.tpmQuote = null; // TODO
        hostReport.variables = new HashMap<String,String>(); // TODO
        if( agent.isAikAvailable() ) {
            if( agent.isAikCaAvailable() ) {
                hostReport.aik = new Aik(agent.getAikCertificate());
                // TODO: if the host sends an aik cert, tthen it should ALSO send the privacy ca cert that signed it, and then we can add it to the report hre... instaead of having to contact the database, for exapmle, to try and finding a matching ca first and then add it here.
            }
            else {
                hostReport.aik = new Aik(agent.getAik()); 
            }
        }
        
        HostTrustPolicyManager hostTrustPolicyFactory = new HostTrustPolicyManager(getEntityManagerFactory());

        
        Policy trustPolicy = hostTrustPolicyFactory.loadTrustPolicyForHost(tblHosts, hostId); // must include both bios and vmm policies
//        trustPolicy.setName(policy for hostId) // do we even need a name? or is that just a management thing for the app?
        PolicyEngine policyEngine = new PolicyEngine();
        TrustReport trustReport = policyEngine.apply(hostReport, trustPolicy);
        
        return trustReport;
    }

    /**
     * 
     * @param hostName must not be null
     * @param tblSamlAssertion must not be null
     * @return 
     */
    public TxtHost getHostWithTrust(Hostname hostName, TblSamlAssertion tblSamlAssertion) throws IOException {
        TblHosts record = getHostByName(hostName);
        HostTrustStatus trust = getTrustStatus(hostName);
        TxtHostRecord data = createTxtHostRecord(record);
        TxtHost host = new TxtHost(data, trust);
        tblSamlAssertion.setHostId(record);
        return host;
    }

    protected TxtHostRecord createTxtHostRecord(TblHosts from) {
        TxtHostRecord to = new TxtHostRecord();
        to.AddOn_Connection_String = from.getAddOnConnectionInfo();
        to.BIOS_Name = from.getBiosMleId().getName();
        to.BIOS_Version = from.getBiosMleId().getVersion();
        to.BIOS_Oem = from.getBiosMleId().getOemId().getName();
        to.Description = from.getDescription();
        to.Email = from.getEmail();
        to.HostName = from.getName();
        to.IPAddress = from.getIPAddress();
        to.Location = from.getLocation();
        to.Port = from.getPort();
        to.VMM_Name = from.getVmmMleId().getName();
        to.VMM_Version = from.getVmmMleId().getVersion();
        to.VMM_OSName = from.getVmmMleId().getOsId().getName();
        to.VMM_OSVersion = from.getVmmMleId().getOsId().getVersion();
        to.AIK_Certificate = from.getAIKCertificate();
        to.AIK_PublicKey = from.getAikPublicKey();
        to.AIK_SHA1 = from.getAikSha1();
        return to;
    }

    /**
     * Gets the host trust status from trust agent
     *
     * @param hostName must not be null
     * @return {@link String}
     */
    public String getTrustStatusString(Hostname hostName) throws IOException { // datatype.Hostname

        HostTrustStatus trust = getTrustStatus(hostName);

        String response = toString(trust);

        log.info("Overall trust status " + response);

        return response;
    }

    
    private String toString(HostTrustStatus trust) {
        return String.format("BIOS:%d,VMM:%d", (trust.bios) ? 1 : 0,
                (trust.vmm) ? 1 : 0);
    }
/*
    private boolean verifyTrust(TblHosts host, TblMle mle,
            HashMap<String, ? extends IManifest> pcrManifestMap,
            HashMap<String, ? extends IManifest> gkvPcrManifestMap) {
        boolean response = true;

        if (gkvPcrManifestMap.size() <= 0) {
            throw new ASException(ErrorCode.AS_MISSING_MANIFEST, mle.getName(),
                    mle.getVersion());
        }

        for (String pcr : gkvPcrManifestMap.keySet()) {
            if (pcrManifestMap.containsKey(pcr)) {
                IManifest pcrMf = pcrManifestMap.get(pcr);
                boolean trustStatus = pcrMf.verify(gkvPcrManifestMap.get(pcr));
                log.info(String.format("PCR %s Host Trust status %s", pcr,
                        String.valueOf(trustStatus)));

*               logTrustStatus(host, mle,  pcrMf);

                if (!trustStatus) {
                    response = false;
                }

            } else {
                log.info(String.format("PCR %s not found in manifest.", pcr));
                throw new ASException(ErrorCode.AS_PCR_NOT_FOUND,pcr);
            }
        }

        return response;
    }
*/
    /*
    private void logTrustStatus(TblHosts host, TblMle mle, IManifest manifest) {
        Date today = new Date(System.currentTimeMillis());
        PcrManifest pcrManifest = (PcrManifest)manifest;
        
        TblTaLog taLog = new TblTaLog();
        taLog.setHostID(host.getId());
        taLog.setMleId(mle.getId());
        taLog.setManifestName(String.valueOf(pcrManifest.getPcrNumber()));
        taLog.setManifestValue(pcrManifest.getPcrValue());
        taLog.setTrustStatus(pcrManifest.getVerifyStatus());
        taLog.setUpdatedOn(today);

        new TblTaLogJpaController(getEntityManagerFactory()).create(taLog);
        
        if(manifest instanceof PcrModuleManifest){
            saveModuleManifestLog((PcrModuleManifest) manifest,taLog);
        }

    }
    * */
    private void logOverallTrustStatus(TblHosts host, HostTrustStatus status, Date today) {
        TblTaLog taLog = new TblTaLog();
        taLog.setHostID(host.getId());
        taLog.setMleId(0);
        taLog.setTrustStatus(status.bios && status.vmm); // XXX TODO should we add && status.location?  this true/false thing doesn't handle a case where location is not expected, so it is neither trusted nor untrusted
        taLog.setError(toString(status));
        taLog.setManifestName(" ");
        taLog.setManifestValue(" ");
        taLog.setUpdatedOn(today);

        TblTaLogJpaController talog = new TblTaLogJpaController(getEntityManagerFactory());
        
        talog.create(taLog); // overall status
/*        
        // bios
        TblTaLog taLogBios = new TblTaLog();
        taLogBios.setHostID(host.getId());
        taLogBios.setMleId(host.getBiosMleId().getId());
        taLogBios.setTrustStatus(status.bios); // XXX TODO should we add && status.location?  this true/false thing doesn't handle a case where location is not expected, so it is neither trusted nor untrusted
        taLogBios.setError(toString(status));
        taLogBios.setManifestName(" "); // XXX TODO there should actually be one record per PCR !!!
        taLogBios.setManifestValue(" ");// XXX TODO there should actually be one record per PCR !!!
        taLogBios.setUpdatedOn(today);
        talog.create(taLogBios);
        
        TblTaLog taLogVmm = new TblTaLog();
        taLogVmm.setHostID(host.getId());
        taLogVmm.setMleId(host.getVmmMleId().getId());
        taLogVmm.setTrustStatus(status.vmm); // XXX TODO should we add && status.location?  this true/false thing doesn't handle a case where location is not expected, so it is neither trusted nor untrusted
        taLogVmm.setError(toString(status));
        taLogVmm.setManifestName(" ");// XXX TODO there should actually be one record per PCR !!!
        taLogVmm.setManifestValue(" ");// XXX TODO there should actually be one record per PCR !!!
        taLogVmm.setUpdatedOn(today);
        talog.create(taLogVmm);
        */
    }
    
    /**
     * Searches for all the PcrMatchesConstant policies in the TrustReport and creates 
     * an entry for each one in the mw_ta_log table... the contents of that table are used
     * to create the "trust report" in the Trust Dashboard 
     * 
     * @param host
     * @param report 
     */
    private void logPcrTrustStatus(TblHosts host, TrustReport report, Date today) {
        TblTaLogJpaController talogJpa = new TblTaLogJpaController(getEntityManagerFactory());
        TblModuleManifestLogJpaController moduleLogJpa = new TblModuleManifestLogJpaController(getEntityManagerFactory());
        List<String> biosPcrList = Arrays.asList(host.getBiosMleId().getRequiredManifestList().split(","));
        List<String> vmmPcrList = Arrays.asList(host.getVmmMleId().getRequiredManifestList().split(","));
        List<RuleResult> results = report.getResults();
        log.debug("Found {} results", results.size());
        // we log at most ONE record per PCR ... so keep track here in case multiple rules refer to the same PCR... so we only record it once... hopefully there is no overlap between bios and vmm pcr's!
        HashMap<PcrIndex,TblTaLog> taLogMap = new HashMap<PcrIndex,TblTaLog>();
        for(String biosPcrIndex : biosPcrList) {
            TblTaLog pcr = new TblTaLog();
            pcr.setHostID(host.getId());
            pcr.setMleId(host.getBiosMleId().getId());
            pcr.setUpdatedOn(today);
            pcr.setTrustStatus(true); // start as true, later we'll change to false if there are any faults
            pcr.setManifestName(biosPcrIndex);
            pcr.setManifestValue(report.getHostReport().pcrManifest.getPcr(Integer.valueOf(biosPcrIndex)).getValue().toString());
            taLogMap.put(PcrIndex.valueOf(Integer.valueOf(biosPcrIndex)), pcr);
        }
        for(String vmmPcrIndex : vmmPcrList) {
            TblTaLog pcr = new TblTaLog();
            pcr.setHostID(host.getId());
            pcr.setMleId(host.getVmmMleId().getId());
            pcr.setUpdatedOn(today);
            pcr.setTrustStatus(true); // start as true, later we'll change to false if there are any faults
            pcr.setManifestName(vmmPcrIndex);
            pcr.setManifestValue(report.getHostReport().pcrManifest.getPcr(Integer.valueOf(vmmPcrIndex)).getValue().toString());
            taLogMap.put(PcrIndex.valueOf(Integer.valueOf(vmmPcrIndex)), pcr);
        }
        
        for(RuleResult result : results) {
            log.debug("Looking at policy {}", result.getRuleName());
            Rule rule = result.getRule();
            if( rule instanceof PcrMatchesConstant ) {
                PcrMatchesConstant pcrPolicy = (PcrMatchesConstant)rule;
                log.debug("Expected PCR {} = {}", pcrPolicy.getExpectedPcr().getIndex().toString(), pcrPolicy.getExpectedPcr().getValue().toString());
                // XXX we can do this because we know the policy passed and it's a constant pcr value... but ideally we need to be logging the host's actual value from its HostReport!!!
                // find out which MLE this policy corresponds to and then log it
                TblTaLog pcr = taLogMap.get(pcrPolicy.getExpectedPcr().getIndex());
                pcr.setTrustStatus(result.isTrusted());
                if( !result.isTrusted() ) {
                    pcr.setError("Incorrect value for PCR "+pcrPolicy.getExpectedPcr().getIndex().toString());
                }
//                pcr.setManifestName(pcrPolicy.getExpectedPcr().getIndex().toString());
//                pcr.setManifestValue(report.getHostReport().pcrManifest.getPcr(pcrPolicy.getExpectedPcr().getIndex()).getValue().toString()); 
                /*
                if( biosPcrList.contains(pcrPolicy.getExpectedPcr().getIndex().toString()) ) {
                    pcr.setTrustStatus(true);
                    pcr.setMleId(host.getBiosMleId().getId());
                }
                if( vmmPcrList.contains(pcrPolicy.getExpectedPcr().getIndex().toString()) ) {
                    pcr.setTrustStatus(true);
                    pcr.setMleId(host.getVmmMleId().getId());
                    
                }*/
            }
            if( rule instanceof PcrEventLogIntegrity ) { // for now assuming there is only one, for pcr 19...
                PcrEventLogIntegrity eventLogIntegrityRule = (PcrEventLogIntegrity)rule;
                TblTaLog pcr = taLogMap.get(eventLogIntegrityRule.getPcrIndex());
                pcr.setTrustStatus(result.isTrusted()); 
                if( !result.isTrusted() ) {
                    pcr.setError("No integrity in PCR "+eventLogIntegrityRule.getPcrIndex().toString());
                }
//                pcr.setError(null);
//                pcr.setManifestName(eventLogIntegrityRule.getPcrIndex().toString());
//                pcr.setManifestValue(report.getHostReport().pcrManifest.getPcr(eventLogIntegrityRule.getPcrIndex()).getValue().toString());
                /*
                if( biosPcrList.contains(eventLogIntegrityRule.getPcrIndex().toString()) ) {
                    pcr.setMleId(host.getBiosMleId().getId());
                }
                if( vmmPcrList.contains(eventLogIntegrityRule.getPcrIndex().toString()) ) {
                    pcr.setMleId(host.getVmmMleId().getId());
                }
                talogJpa.create(pcr);
                */
            }
            // in mtwilson-1.1, the mw_module_manifest_log table is used to record only when host module values do not match the whitelist
            if( rule instanceof PcrEventLogIncludes ) {
                /*
                PcrEventLogIncludes eventLogRule = (PcrEventLogIncludes)rule;
                Set<Measurement> measurements = eventLogRule.getMeasurements();
                for(Measurement m : measurements) {
                    TblModuleManifestLog event = new TblModuleManifestLog();
                }
                */
                List<Fault> faults = result.getFaults();
                for(Fault fault : faults) {
                    if( fault instanceof PcrEventLogMissingExpectedEntries ) { // there would only be one of these faults per PcrEventLogIncludes rule. XXX this might change in the future to have a bunch of individual faults, one per missing entry.
                        PcrEventLogMissingExpectedEntries missingEntriesFault = (PcrEventLogMissingExpectedEntries)fault;

                        TblTaLog pcr = taLogMap.get(missingEntriesFault.getPcrIndex());
//                        pcr.setHostID(host.getId());
                        pcr.setTrustStatus(false); // PCR not trusted since one or more required modules are missing, which we will detail below
                        pcr.setError("Missing modules");
//                        pcr.setUpdatedOn(today);
//                        pcr.setManifestName(missingEntriesFault.getPcrIndex().toString());
//                        pcr.setManifestValue(""); // doesn't match up with how we store data. we would need to look for another related fault about the dynamic value not matching... 
//                        if( biosPcrList.contains(missingEntriesFault.getPcrIndex().toString()) ) {
//                            pcr.setMleId(host.getBiosMleId().getId());
//                        }
//                        if( vmmPcrList.contains(missingEntriesFault.getPcrIndex().toString()) ) {
//                            pcr.setMleId(host.getVmmMleId().getId());
//                        }
                        talogJpa.create(pcr); // exception to creating all at the end... 
                        
                        Set<Measurement> missingEntries = missingEntriesFault.getMissingEntries();
                        for(Measurement m : missingEntries) {
                            // try to find the same module in the host report (hopefully it has the same name , and only the value changed)
                            List<Measurement> actualEntries = report.getHostReport().pcrManifest.getPcrEventLog(missingEntriesFault.getPcrIndex()).getEventLog();
                            Measurement found = null;
                            for(Measurement a : actualEntries) {
                                // TODO SUDHIR: This below test is failing for open source since the label in the measurement is set to initrd, where as the pcrManifest is having OpenSource.initrd
                                // Need to probably change the attestation generator itself.
                                //  if( a.getInfo().get("ComponentName").equals(m.getLabel()) ) {
                                if( a.getLabel().equals(m.getLabel()) ) {
                                    found = a;
                                }
                            }
                            // does the host have a module with the same name but different value? if so, we should log it in TblModuleManifestLog... but from here we don't have access to the HostReport.  XXX maybe need to change method signature and get the HostReport as well.  or maybe the TrustReport should include a reference to the host report in it. 
                            TblModuleManifestLog event = new TblModuleManifestLog();
                            event.setName(m.getLabel());
                            event.setTaLogId(pcr);
                            event.setValue( found == null ? "" : found.getValue().toString() ); // we don't know from our report what the "actual" value is since we only logged that an expected value was missing... so maybe there's a module with the same name and wrong value in the host report, which we don't know here... see comment above,  this probably needs to change.
                            event.setWhitelistValue(m.getValue().toString());
                            moduleLogJpa.create(event);
                        }
                    }
                }
            }
        }
        // now create all those mw_ta_log records (one per pcr)
        for(TblTaLog pcr : taLogMap.values()) {
            if( pcr.getId() == null ) {
                talogJpa.create(pcr);
            }
            else {
                try {
                    talogJpa.edit(pcr); // it it was already created (reasonable instance of PcrEventLogIncludes or not)
                }
                catch(Exception e) {
                    log.error(e.toString());
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    private TblHosts getHostByName(Hostname hostName) { // datatype.Hostname
        try {
            return hostBO.getHostByName(hostName);
        }
        catch(CryptographyException e) {
            throw new ASException(e,ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
    }
    private TblHosts getHostByAik(Sha1Digest fingerprint) { // datatype.Hostname
        try {
            return hostBO.getHostByAik(fingerprint);
        }
        catch(CryptographyException e) {
            throw new ASException(e,ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
    }

    public OpenStackHostTrustLevelReport getPollHosts(OpenStackHostTrustLevelQuery input) {

        OpenStackHostTrustLevelReport hostTrusts = new OpenStackHostTrustLevelReport();
        Date today = new Date(System.currentTimeMillis());
        String trustLevel;

        for (Hostname hostName : input.hosts) {

            try {

                String hostTrustStatus = getTrustStatusString(hostName);

                log.info("The trust status of {} is :{}",
                        new String[]{hostName.toString(), hostTrustStatus});

                trustLevel = parseTrustStatus(hostTrustStatus);
            } catch (ASException e) {
                log.error( "Error while getting trust of host "
                        + hostName, e);
                trustLevel = "unknown";
            } catch (Exception e) {
                log.error( "Error while getting trust of host "
                        + hostName, e);
                trustLevel = "unknown";
            }
            HostTrustLevel1String trust = new HostTrustLevel1String();
            trust.hostname = hostName.toString();
            trust.trustLevel = trustLevel;
            trust.timestamp = Util.getDateString(today);
            hostTrusts.pollHosts.add(trust);

        }

        

        return hostTrusts;
    }

    private String parseTrustStatus(String hostTrustStatus) {
        String result = "untrusted";

        Boolean biostrust = false;
        Boolean vmmtrust = false;
        String[] parts = hostTrustStatus.split(",");

        for (String part : parts) {
            String[] subParts = part.split(":");
            if (subParts[0].equals("BIOS")) {
                biostrust = subParts[1].equals("1");
            } else {
                vmmtrust = subParts[1].equals("1");
            }

        }

        if (biostrust && vmmtrust) {
            result = "trusted";
        }

        return result;
    }

    // PREMIUM FEATURE ? 
    /**
     * Gets the location of the host from db table tblHosts
     *
     * @param hostName
     * @return {@link HostLocation}
     */
    public HostLocation getHostLocation(Hostname hostName) {
        try {
            TblHosts tblHosts = getHostByName(hostName);

            if (tblHosts == null) {
                throw new ASException(
                        ErrorCode.AS_HOST_NOT_FOUND,
                        String.format(
                        "%s",
                        hostName));
            }

            HostLocation location = new HostLocation(tblHosts.getLocation());
            return location;
        } catch (ASException e) {
            throw e;
        } catch (Exception e) {
            throw new ASException(e);
        }
    }
    
    /**
     * Author: Sudhir
     * 
     * Add a new location mapping entry into the table.
     * 
     * @param hlObj
     * @return 
     */
    public Boolean addHostLocation(HostLocation hlObj) {

        TblLocationPcrJpaController locJpaController = new TblLocationPcrJpaController(getEntityManagerFactory());
        try {
            if (hlObj != null && !hlObj.white_list_value.isEmpty()) {
                TblLocationPcr locPCR = locJpaController.findTblLocationPcrByPcrValueEx(hlObj.white_list_value);
                if (locPCR != null) {
                    log.info(String.format("An entry already existing in the location table for the white list specified [%s | %s]"
                            , locPCR.getLocation(), hlObj.white_list_value));
                    if (locPCR.getLocation().equals(hlObj.location)) {
                        // No need to do anything. Just exit.
                        return true;
                    }
                    else {
                        // Need to update the entry
                        log.info(String.format("Updating the location value for the white list specified to %s.", hlObj.location));
                        locPCR.setLocation(hlObj.location);
                        locJpaController.edit(locPCR);
                    }
                } else {
                    // Add a new entry for the location mapping table.
                    locPCR = new TblLocationPcr();
                    locPCR.setLocation(hlObj.location);
                    locPCR.setPcrValue(hlObj.white_list_value);
                    locJpaController.create(locPCR);
                    log.info(String.format("Successfully added a new location value %s with white list %s.", hlObj.location, hlObj.white_list_value));
                }
            }
        } catch (ASException e) {
            throw e;
        } catch (Exception e) {
            throw new ASException( e);
        }

        return true;
    }
    

    /**
     * @param hostName
     * @return
     */
    public String getTrustWithSaml(String hostName) {
        try {
            //String location = hostTrustBO.getHostLocation(new Hostname(hostName)).location; // example: "San Jose"
            //HostTrustStatus trustStatus = hostTrustBO.getTrustStatus(new Hostname(hostName)); // example:  BIOS:1,VMM:1
            TblSamlAssertion tblSamlAssertion = new TblSamlAssertion();

            TxtHost host = getHostWithTrust(new Hostname(hostName),tblSamlAssertion);
            
            tblSamlAssertion.setBiosTrust(host.isBiosTrusted());
            tblSamlAssertion.setVmmTrust(host.isVmmTrusted());

            
            SamlAssertion samlAssertion = getSamlGenerator().generateHostAssertion(host);

            log.info("Expiry {}" , samlAssertion.expiry_ts.toString());

            tblSamlAssertion.setSaml(samlAssertion.assertion);
            tblSamlAssertion.setExpiryTs(samlAssertion.expiry_ts);
            tblSamlAssertion.setCreatedTs(samlAssertion.created_ts);
            
            
            new TblSamlAssertionJpaController(getEntityManagerFactory()).create(tblSamlAssertion);

            return samlAssertion.assertion ;
        } catch (ASException e) {
            // ASException sets HTTP Status to 400 for all errors
            // We override that here to give more specific codes when possible:
            if (e.getErrorCode().equals(ErrorCode.AS_HOST_NOT_FOUND)) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }
            /*
             * if( e.getErrorCode().equals(ErrorCode.TA_ERROR)) { throw new
             * WebApplicationException(Status.INTERNAL_SERVER_ERROR); }
             *
             */
            throw e;
        } catch (Exception e) {
            throw new ASException( e);
        }
    }

    private SamlGenerator getSamlGenerator() throws UnknownHostException, ConfigurationException {
        Configuration conf = ASConfig.getConfiguration();
        InetAddress localhost = InetAddress.getLocalHost();
        String defaultIssuer = "https://" + localhost.getHostAddress() + ":8181/AttestationService";
        String issuer = conf.getString("saml.issuer", defaultIssuer);
        SamlGenerator saml = new SamlGenerator(samlKeystoreResource, conf);
        saml.setIssuer(issuer);
        return saml;
    }

    public String getTrustWithSaml(String host, boolean forceVerify) {
        log.info("Getting trust for host: " + host + " Force verify flag: " + forceVerify);
        // Bug: 702: For host not supporting TXT, we need to return back a proper error
         TblHosts tblHosts = getHostByName(new Hostname((host)));
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent agent = factory.getHostAgent(tblHosts);
       // log.info("Value of the TPM flag is : " +  Boolean.toString(agent.isTpmEnabled()));
        
        if (!agent.isTpmPresent()) {
            throw new ASException(ErrorCode.AS_TPM_NOT_SUPPORTED, host);
        }
                
        if(forceVerify != true){
            TblSamlAssertion tblSamlAssertion = new TblSamlAssertionJpaController((getEntityManagerFactory())).findByHostAndExpiry(host);
            if(tblSamlAssertion != null){
                log.info("Found assertion in cache. Expiry time : " + tblSamlAssertion.getExpiryTs());
                return tblSamlAssertion.getSaml();
            }
        }
        
       log.info("Getting trsut and saml assertion from host.");
        

        return getTrustWithSaml(host);
    }

    public HostTrust getTrustWithCache(String host, Boolean forceVerify) {
        log.info("Getting trust for host: " + host + " Force verify flag: " + forceVerify);
        try {
            
            if(forceVerify != true){
                TblHosts tblHosts = getHostByName(new Hostname(host));
                if(tblHosts != null){
                    TblTaLog tblTaLog = new TblTaLogJpaController(getEntityManagerFactory()).getHostTALogEntryBefore(
                            tblHosts.getId() , getCacheStaleAfter() );
                    
                    if(tblTaLog != null)
                        return getHostTrustObj(tblTaLog);
                }else{
                    throw new ASException(
                            ErrorCode.AS_HOST_NOT_FOUND,
                                       host);
                }
            }
        
           log.info("Getting trust and saml assertion from host.");
        
           HostTrustStatus status = getTrustStatus(new Hostname(host));
           
           HostTrust hostTrust = new HostTrust(ErrorCode.OK,"OK");
           hostTrust.setBiosStatus((status.bios)?1:0);
           hostTrust.setVmmStatus((status.vmm)?1:0);
           hostTrust.setIpAddress(host);
           
           return hostTrust;
            
        } catch (ASException e) {
            log.error("Error while getting trust for host " + host,e );
            return new HostTrust(e.getErrorCode(),e.getErrorMessage(),host,null,null);
        }catch(Exception e){
            log.error("Error while getting trust for host " + host,e );
            return new HostTrust(ErrorCode.SYSTEM_ERROR,
                    new AuthResponse(ErrorCode.SYSTEM_ERROR,e.getMessage()).getErrorMessage(),host,null,null);
        }

    }
    private Date getCacheStaleAfter(){
        return new DateTime().minusSeconds(CACHE_VALIDITY_SECS).toDate();
    }
    private HostTrust getHostTrustObj(TblTaLog tblTaLog) {
        HostTrust hostTrust = new HostTrust(ErrorCode.OK,"");
        
        String[] parts = tblTaLog.getError().split(",");
        
        for(String part : parts){
            String[] subparts = part.split(":");
            if(subparts[0].equalsIgnoreCase("BIOS")){
                hostTrust.setBiosStatus(Integer.valueOf(subparts[1]));
            }else{
                hostTrust.setVmmStatus(Integer.valueOf(subparts[1]));
            }
        }
        
        
        return hostTrust;
    }

    /*
    private void saveModuleManifestLog(PcrModuleManifest pcrModuleManifest, TblTaLog taLog) {
        TblModuleManifestLogJpaController controller = new TblModuleManifestLogJpaController(getEntityManagerFactory());
        for(ModuleManifest moduleManifest : pcrModuleManifest.getUntrustedModules()){
            TblModuleManifestLog moduleManifestLog = new TblModuleManifestLog();
            moduleManifestLog.setTaLogId(taLog);
            moduleManifestLog.setName(moduleManifest.getComponentName());
            moduleManifestLog.setValue(moduleManifest.getDigestValue());
            moduleManifestLog.setWhitelistValue(moduleManifest.getWhiteListValue());
            log.info("Adding the module manifest log.");
            controller.create(moduleManifestLog);
        }
    }
    */
}
