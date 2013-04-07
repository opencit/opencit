package com.intel.mtwilson.as.business.trust;

import com.intel.mtwilson.as.controller.TblSamlAssertionJpaController;
import com.intel.mtwilson.as.controller.TblTaLogJpaController;
import com.intel.mtwilson.as.controller.TblLocationPcrJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestLogJpaController;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblModuleManifestLog;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblSamlAssertion;
import com.intel.mtwilson.as.data.TblTaLog;
import com.intel.mtwilson.as.data.TblLocationPcr;
import com.intel.mtwilson.as.business.HostBO;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.opensaml.xml.ConfigurationException;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.helper.BaseBO;
import com.intel.mtwilson.as.helper.saml.SamlAssertion;
import com.intel.mtwilson.as.helper.saml.SamlGenerator;
import com.intel.mtwilson.as.controller.MwKeystoreJpaController;
import com.intel.mtwilson.audit.api.AuditLogger;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.io.FileResource;
import com.intel.mtwilson.io.Resource;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.FileNotFoundException;
import org.apache.commons.configuration.Configuration;
import org.joda.time.DateTime;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import com.intel.mtwilson.agent.*;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.PolicyEngine;
import com.intel.mtwilson.policy.TrustPolicy;
import com.intel.mtwilson.policy.TrustReport;
import com.intel.mtwilson.policy.impl.HostTrustPolicyFactory;
import com.intel.mtwilson.policy.impl.TrustedBios;
import com.intel.mtwilson.policy.impl.TrustedLocation;
import com.intel.mtwilson.policy.impl.TrustedVmm;

/**
 *
 * @author dsmagadx
 */
public class HostTrustBO extends BaseBO {
    public static final String SAML_KEYSTORE_NAME = "SAML";
    private static final Logger log = LoggerFactory.getLogger(HostTrustBO.class);
    Marker sysLogMarker = MarkerFactory.getMarker("SYSLOG"); // TODO we should create a single class to contain all the markers we want to use throughout the code
    
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
    public HostTrustStatus getTrustStatus(Hostname hostName) {
        if( hostName == null ) { throw new IllegalArgumentException("missing hostname"); }
        long start = System.currentTimeMillis();
        
        TblHosts tblHosts = getHostByName(hostName);
        return getTrustStatus(tblHosts, hostName.toString());
    }
    
    public HostTrustStatus getTrustStatusByAik(Sha1Digest aik) {
        if( aik == null ) { throw new IllegalArgumentException("missing AIK fingerprint"); }
        
        TblHosts tblHosts = getHostByAik(aik);
        return getTrustStatus(tblHosts, aik.toString());
    }
    
    /**
     * 
     * @param tblHosts
     * @param hostId can be Hostname or AIK (SHA1 hex) ; it's used in any exceptions to refer to the host.  this allows us to use the same code for a trust report lookup by hostname and by aik
     * @return 
     */
    public HostTrustStatus getTrustStatus(TblHosts tblHosts, String hostId) {
        if (tblHosts == null) {
            throw new ASException(
                    ErrorCode.AS_HOST_NOT_FOUND,
                    hostId);
        }
        long start = System.currentTimeMillis();
        log.info( "VMM name for host is {}", tblHosts.getVmmMleId().getName());
        log.info( "OS name for host is {}", tblHosts.getVmmMleId().getOsId().getName());

        // bug #538 first check if the host supports tpm
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent agent = factory.getHostAgent(tblHosts);
        if( !agent.isTpmEnabled() || !agent.isIntelTxtEnabled() ) {
            throw new ASException(ErrorCode.AS_INTEL_TXT_NOT_ENABLED, tblHosts.toString());
        }
        
        PcrManifest pcrManifest = agent.getPcrManifest();
        
        HostReport hostReport = new HostReport();
        hostReport.aik = null; // TODO
        hostReport.pcrManifest = pcrManifest;
        hostReport.tpmQuote = null; // TODO
        hostReport.variables = new HashMap<String,String>(); // TODO
        
        HostTrustPolicyFactory hostTrustPolicyFactory = new HostTrustPolicyFactory(getEntityManagerFactory());

        
        TrustPolicy trustPolicy = hostTrustPolicyFactory.loadTrustPolicyForHost(tblHosts); // must include both bios and vmm policies

        PolicyEngine policyEngine = new PolicyEngine();
        TrustReport trustReport = policyEngine.apply(hostReport, trustPolicy);
        
        HostTrustStatus trust = new HostTrustStatus();
        TrustReport biosReport = trustReport.findMark(TrustedBios.class.getName());
        if( biosReport != null && biosReport.isTrusted() ) {
            trust.bios = true;
        }
        TrustReport vmmReport = trustReport.findMark(TrustedVmm.class.getName());
        if( vmmReport != null && vmmReport.isTrusted() ) {
            trust.vmm = true;
        }
        // previous check for trusted location was if the host's location field is not null, then it's trusted... but i think this is better as it checks the pcr.  
        // XXX TODO need a better feedback mechanism from trust policies... when they succeed, they should be able to set attributes.
        // or else,  just go with the "marks" thing but then we have to post process and look for certain marks and then  set other fields elsewhere based on them ... or maybe that's not necessary??)
//        trust.location = tblHosts.getLocation() != null; // if location is available (it comes from PCR 22), it's trusted
        TrustReport locationReport = trustReport.findMark(TrustedLocation.class.getName());
        if( locationReport != null && locationReport.isTrusted() ) {
            trust.location = true;
        }
        
        logOverallTrustStatus(tblHosts, toString(trust));
        

        String userName = new AuditLogger().getAuditUserName();
        Object[] paramArray = {userName, hostId, trust.bios, trust.vmm};
        log.info(sysLogMarker, "User_Name: {} Host_Name: {} BIOS_Trust: {} VMM_Trust: {}.", paramArray);
        
        log.info( "Verfication Time {}", (System.currentTimeMillis() - start));

        return trust;
    }

    /**
     * 
     * @param hostName must not be null
     * @param tblSamlAssertion must not be null
     * @return 
     */
    public TxtHost getHostWithTrust(Hostname hostName, TblSamlAssertion tblSamlAssertion) {
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
        return to;
    }

    /**
     * Gets the host trust status from trust agent
     *
     * @param hostName must not be null
     * @return {@link String}
     */
    public String getTrustStatusString(Hostname hostName) { // datatype.Hostname

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
    private void logOverallTrustStatus(TblHosts host, String response) {
        Date today = new Date(System.currentTimeMillis());
        TblTaLog taLog = new TblTaLog();
        taLog.setHostID(host.getId());
        taLog.setMleId(0);
        taLog.setTrustStatus(false);
        taLog.setError(response);
        taLog.setManifestName(" ");
        taLog.setManifestValue(" ");
        taLog.setUpdatedOn(today);

        new TblTaLogJpaController(getEntityManagerFactory()).create(taLog);

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
