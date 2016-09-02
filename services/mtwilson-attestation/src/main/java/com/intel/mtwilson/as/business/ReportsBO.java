package com.intel.mtwilson.as.business;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import java.util.Date;
import com.intel.mtwilson.as.data.TblModuleManifestLog;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblTaLog;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.business.trust.Util;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.i18n.ErrorCode;
//import com.intel.mtwilson.model;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.mtwilson.as.controller.TblTaLogJpaController;
import com.intel.mountwilson.as.hostmanifestreport.data.HostManifestReportType;
import com.intel.mountwilson.as.hostmanifestreport.data.ManifestType;
import com.intel.mountwilson.as.hosttrustreport.data.HostType;
import com.intel.mountwilson.as.hosttrustreport.data.HostsTrustReportType;
import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.datatypes.*;
import com.intel.dcsg.cpg.jpa.PersistenceManager;
import com.intel.mtwilson.as.data.MwApiClientHttpBasic;
import com.intel.mtwilson.as.data.MwMeasurementXml;
import com.intel.mtwilson.model.Hostname;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.Nonce;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.XmlMeasurementLog;
import java.util.*;
import java.io.IOException;

/**
 *
 * @author dsmagadx
 */
public class ReportsBO {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReportsBO.class);
    private static String ASSET_TAG_PCR = "22";
    private static String ASSET_TAG_PCR_WINDOWS = "23";
    private String assetTagPCR = ASSET_TAG_PCR;
    

    public ReportsBO() {
        super();
    }
    
//    public ReportsBO(PersistenceManager pm) {
//        super(pm);
//    }

    public HostsTrustReportType getTrustReport(Collection<Hostname> hostNames) { // datatype.Hostname





        try {
            HostsTrustReportType hostsTrustReportType = new HostsTrustReportType();
            for (Hostname host : hostNames) {
                TblHosts tblHosts = My.jpa().mwHosts().findByName(host.toString()); // datatype.Hostname


                if (tblHosts == null) {
                    throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, host);
                }

                List<TblTaLog> logs = My.jpa().mwTaLog().findTrustStatusByHostId(tblHosts.getId(), 5);

                if (logs != null) {

                    for (TblTaLog log : logs) {
                        HostType hostType = new HostType();
                        hostType.setHostName(host.toString()); // datatype.Hostname
                        hostType.setMLEInfo(getMleInfo(tblHosts));
                        hostType.setTrustStatus(getTrustStatus(log.getError()));
                        // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
                        // hostType.setCreatedOn(Util.getCalendar(tblHosts.getCreatedOn()));
                        hostType.setVerifiedOn(Util.getCalendar(log.getUpdatedOn()));
                        hostsTrustReportType.getHost().add(hostType);
                    }
                }


            }
            return hostsTrustReportType;
        } catch (IOException | CryptographyException | ASException e) {
            // throw new ASException(e);
            // Bug: 1038 - prevent leaks in error messages to client
            logger.error("Error during retrieval of host trust report.", e);
            throw new ASException(ErrorCode.AS_HOST_REPORT_ERROR, e.getClass().getSimpleName());
        }

    }

    public HostManifestReportType getReportManifest(Hostname hostName) {  // datatype.Hostname
        HostManifestReportType hostManifestReportType = new HostManifestReportType();

        /*
         * if (hostName == null || hostName.isEmpty()) { throw new
         * ASException(ErrorCode.VALIDATION_ERROR, "Input Hostname " + hostName
         * + " is empty."); }
         *
         */
        TblHosts tblHosts;
        
        try {
        tblHosts = My.jpa().mwHosts().findByName(hostName.toString()); // datatype.Hostname

        if (tblHosts == null) {
            throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, hostName.toString());
        }

        Date lastStatusTs = My.jpa().mwTaLog().findLastStatusTs(tblHosts.getId());


        if (lastStatusTs != null) {
            List<TblTaLog> logs = My.jpa().mwTaLog().findLogsByHostId(tblHosts.getId(), lastStatusTs);
            com.intel.mountwilson.as.hostmanifestreport.data.HostType hostType = new com.intel.mountwilson.as.hostmanifestreport.data.HostType();
            hostType.setName(hostName.toString()); // datatype.Hostname
            if (logs != null) {
                for (TblTaLog log : logs) {
                    ManifestType manifest = new ManifestType();
                    manifest.setName(Integer.parseInt(log.getManifestName()));
                    manifest.setValue(log.getManifestValue());
                    manifest.setVerifiedOn(Util.getCalendar(log.getUpdatedOn()));
                    manifest.setTrustStatus(getTrustStatus(log.getTrustStatus()));
                    hostType.getManifest().add(manifest);
                }
            }

            hostManifestReportType.setHost(hostType);
        }
        return hostManifestReportType;
        }
        catch(Exception e) {
            // throw new ASException(ErrorCode.HTTP_INTERNAL_SERVER_ERROR, e.toString());
            // Bug: 1038 - prevent leaks in error messages to client
            logger.error("Error during retrieval of host trust report.", e);
            throw new ASException(ErrorCode.AS_HOST_REPORT_ERROR, e.getClass().getSimpleName());
        }
    }

    private String getMleInfo(TblHosts tblHosts) {
        return String.format("BIOS:%s-%s,VMM:%s:%s",
                tblHosts.getBiosMleId().getName(),
                tblHosts.getBiosMleId().getVersion(),
                tblHosts.getVmmMleId().getName(),
                tblHosts.getVmmMleId().getVersion());
    }

    private Integer getTrustStatus(String trustString) {
        int bios = 0;
        int vmm = 0;
        String[] parts = trustString.split(",");
        for (String sub : parts) {
            String[] subParts = sub.split(":");
            if (subParts[0].equals("BIOS")) {
                bios = Integer.parseInt(subParts[1]);
            } else {
                vmm = Integer.parseInt(subParts[1]);
            }
        }
        return (bios == 1 && vmm == 1) ? 1 : 0;

    }

    private Integer getTrustStatus(boolean trustStatus) {
        if (trustStatus) {
            return 1;
        } else {
            return 0;
        }
    }

    public String getHostAttestationReport(Hostname hostName) {
        return getHostAttestationReport(hostName, null);
    }
    public String getHostAttestationReport(Hostname hostName, Nonce challenge) {
        TblHosts tblHosts;

        try {

            tblHosts = My.jpa().mwHosts().findByName(hostName.toString());

            if (tblHosts == null) {
                throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, hostName.toString());
            }

            if (tblHosts.getAddOnConnectionInfo() != null && tblHosts.getAddOnConnectionInfo().contains("http")) {

                throw new ASException(ErrorCode.AS_OPERATION_NOT_SUPPORTED, "getHostAttestationReport does not support VMWare hosts.");
            }
            
            HostAgentFactory factory = new HostAgentFactory();
            HostAgent agent = factory.getHostAgent(tblHosts);
//            PcrManifest pcrManifest = agent.getPcrManifest();
            return agent.getHostAttestationReport("0,17,18,19,20", challenge); // maybe  just 17,18,19,20  // "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23"

        } catch (ASException aex) {

            throw aex;


        }  catch (Exception ex) {

            // throw new ASException(ex);
            // Bug: 1038 - prevent leaks in error messages to client
            logger.error("Error during retrieval of host attestation report.", ex);
            throw new ASException(ErrorCode.AS_HOST_ATTESTATION_REPORT_ERROR, ex.getClass().getSimpleName());
        }
    }

    public AttestationReport getAttestationReport(Hostname hostName, Boolean failureOnly)  {

        try {
            logger.debug("getAttestationReport - Received request to generate attestation report for {} with failuresOnly set to {}.", hostName.toString(), failureOnly);
        AttestationReport attestationReport = new AttestationReport();

        /*
         * if (hostName == null || hostName.isEmpty()) { throw new
         * ASException(ErrorCode.VALIDATION_ERROR, "Input Hostname " + hostName
         * + " is empty."); }
         *
         */

        TblHosts tblHosts = My.jpa().mwHosts().findByName(hostName.toString()); // datatype.Hostname
        
        if (tblHosts == null) {
            throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, hostName.toString());
        }

        TblTaLogJpaController tblTaLogJpaController = My.jpa().mwTaLog();
        Date lastStatusTs = tblTaLogJpaController.findLastStatusTs(tblHosts.getId());


        if (lastStatusTs != null) {
            List<TblTaLog> logs = tblTaLogJpaController.findLogsByHostId(tblHosts.getId(), lastStatusTs);
            com.intel.mountwilson.as.hostmanifestreport.data.HostType hostType = new com.intel.mountwilson.as.hostmanifestreport.data.HostType();
            hostType.setName(hostName.toString()); // datatype.Hostname
            //logger.debug("host info: " + getMleInfo(tblHosts) + "hostType" + hostType.getName());
            
            if (tblHosts.getVmmMleId().getName().toLowerCase().contains("windows")) {
                assetTagPCR = ASSET_TAG_PCR_WINDOWS;
            }
            else
                assetTagPCR = ASSET_TAG_PCR;
            
            if (logs != null) {
                Map<String, PcrLogReport> report = new TreeMap<>();
                for (TblTaLog log : logs) {
                    logger.debug("getAttestationReport - Processing the PCR {} with trust status {}.", log.getManifestName(), log.getTrustStatus());
                    boolean value = (failureOnly && log.getTrustStatus() == false);
                    
                    if (!failureOnly || value) {
                        if (log.getManifestName().equalsIgnoreCase(assetTagPCR)) {
                            //attestationReport.getPcrLogs().add(getPcrLogReportForAssetTag(log, tblHosts.getId()));
                            PcrLogReport r = getPcrLogReportForAssetTag(log, tblHosts.getId());
                            if(!report.containsKey(log.getManifestName())) {
                                report.put(log.getManifestName(), r);   
                            } else if(r != null) {
                                report.get(log.getManifestName()).getModuleLogs().addAll(r.getModuleLogs());
                            }                            
                        } else {                            
                            //attestationReport.getPcrLogs().add(getPcrManifestLog(tblHosts, log, failureOnly));
                            
                            PcrLogReport r = getPcrManifestLog(tblHosts, log, failureOnly);
                            if(!report.containsKey(log.getManifestName())) {
                                report.put(log.getManifestName(), r);   
                            } else {
                                PcrLogReport existing = report.get(log.getManifestName());
                                if(existing.getTrustStatus().intValue() != r.getTrustStatus().intValue()) {
                                    existing.setTrustStatus(0);
                                }
                                existing.getModuleLogs().addAll(r.getModuleLogs());
                                
                            }      
                        }
                    }
                }
                for(PcrLogReport r: report.values()) {
                    attestationReport.getPcrLogs().add(r);
                }
            }
        }
        
        // temp fix to get pcr showing up in trust report
        /*HostAgentFactory factory = new HostAgentFactory();
        HostAgent agent = factory.getHostAgent(tblHosts);
        if(agent != null) {
            String hostUUID;
            Map<String,String> attrs = agent.getHostAttributes();
             if (attrs != null && attrs.containsKey("Host_UUID")) {
                hostUUID = attrs.get("Host_UUID");
                AssetTagCertBO atagCertBO = new AssetTagCertBO();
            MwAssetTagCertificate atagCert = atagCertBO.findValidAssetTagCertForHost(hostUUID);
            if (atagCert != null) {
                logger.debug("Found a valid asset tag certificate for the host {} with UUID {}.", tblHosts.getName(), hostUUID);        
                PcrManifest pcrManifest = agent.getPcrManifest();
                Pcr pcr = pcrManifest.getPcr(22);             
                PcrLogReport manifest = new PcrLogReport();
                manifest.setName(22);
                manifest.setValue(pcr.getValue().toString());
                //Sha1Digest sha1d = new Sha1Digest(atagCert.getPCREvent());
                
                manifest.setWhiteListValue(new  Sha1Digest(atagCert.getPCREvent()).toString());
                if(manifest.getValue().equals(manifest.getWhiteListValue())) {
                    manifest.setTrustStatus(1);
                }else{
                    manifest.setTrustStatus(0);
                }
                manifest.setVerifiedOn(new Date());
                attestationReport.getPcrLogs().add(manifest);
            } 
            } else {
               logger.debug("assetTag trustVerfication could not find UUID for " + tblHosts.getName());
            }
        }*/
        return attestationReport;
        }
        catch(IOException | CryptographyException | ASException | NumberFormatException ex) {
            // throw new ASException(ErrorCode.HTTP_INTERNAL_SERVER_ERROR, e.toString());
            // Bug: 1038 - prevent leaks in error messages to client
            logger.error("Error during retrieval of host attestation report.", ex);
            throw new ASException(ErrorCode.AS_HOST_ATTESTATION_REPORT_ERROR, ex.getClass().getSimpleName());
        }
    }
    
    public PcrLogReport getPcrManifestLog(TblHosts tblHosts, TblTaLog log, Boolean failureOnly) throws NumberFormatException, IOException {
        TblPcrManifest tblPcrManifest = getPcrModuleManifest(tblHosts,log.getMleId(),log.getManifestName());
        logger.debug("getPcrManifestLog - Got data from mw_pcr_manifest table with pcr name {} and value {}.", tblPcrManifest.getName(), tblPcrManifest.getValue());
        PcrLogReport manifest = new PcrLogReport();
        manifest.setName(Integer.parseInt(log.getManifestName()));
        manifest.setValue(log.getManifestValue());
        manifest.setVerifiedOn(log.getUpdatedOn());
        manifest.setTrustStatus(getTrustStatus(log.getTrustStatus()));
        manifest.setWhiteListValue(tblPcrManifest.getValue());
//        if (log.getTblModuleManifestLogCollection() != null && log.getTblModuleManifestLogCollection().size() > 0) {
            addManifestLogs(tblHosts, manifest, log, failureOnly,tblPcrManifest);// 20130417 added host id to parameter list so addManifestLogs can find host-specific module values
//        }
        return manifest;
    }
//    private String getWhitelListValue(TblHosts tblHosts, Integer mleId, String manifestName) {
//
//        Collection<TblPcrManifest> pcrManifestCollection = null;
//
//        if (tblHosts.getVmmMleId().getId() == mleId) {
//            pcrManifestCollection = tblHosts.getVmmMleId().getTblPcrManifestCollection();
//        } else {
//            pcrManifestCollection = tblHosts.getBiosMleId().getTblPcrManifestCollection();
//        }
//
//        if (pcrManifestCollection != null) {
//            for (TblPcrManifest pcrManifest : pcrManifestCollection) {
//                if (pcrManifest.getName().equals(manifestName)) {
//                    return pcrManifest.getValue();
//                }
//            }
//        }
//        return null;
//
//    }
    
    
    private void addManifestLogs(TblHosts host, PcrLogReport manifest, TblTaLog log, Boolean failureOnly,TblPcrManifest tblPcrManifest) throws IOException {
        Integer hostId = host.getId();
        String registeredPcrBank = host.getPcrBank();
        HashMap<String,ModuleLogReport> moduleReports = new HashMap<>();
        ModuleLogReport tbootxmModuleLogReport = new ModuleLogReport();
        HashMap<String, ModuleLogReport> temptbootxmSubModuleReport = new HashMap<>();
        
        if(log.getTblModuleManifestLogCollection() != null){
            logger.debug("addManifestLogs - This is module based attestation with {} of modules.", log.getTblModuleManifestLogCollection().size());
            for (TblModuleManifestLog moduleManifestLog : log.getTblModuleManifestLogCollection()) {
                logger.debug("addManifestLogs - {} - {}", moduleManifestLog.getName(), moduleManifestLog.getValue());
                if (moduleManifestLog.getName().equalsIgnoreCase("tbootxm")) {
                    logger.debug("addManifestLogs - Adding the root tbootxm with errors.");
                    tbootxmModuleLogReport.setComponentName(moduleManifestLog.getName());
                    tbootxmModuleLogReport.setValue(moduleManifestLog.getValue());
                    tbootxmModuleLogReport.setWhitelistValue(moduleManifestLog.getWhitelistValue());
                    tbootxmModuleLogReport.setTrustStatus(0);
                } else if (moduleManifestLog.getName().startsWith("tbootxm-")) {
                    logger.debug("addManifestLogs - Adding the sub module {} for tbootxm module with errors.", moduleManifestLog.getName());                    
                    ModuleLogReport subModuleLogReport = new ModuleLogReport(moduleManifestLog.getName().substring(("tbootxm-").length()),
                                                              moduleManifestLog.getValue(), moduleManifestLog.getWhitelistValue(),0);
                    temptbootxmSubModuleReport.put(subModuleLogReport.getComponentName(), subModuleLogReport);
                } else {
                    logger.debug("addManifestLogs - Adding the sub module {} for non-tbootxm module with errors.", moduleManifestLog.getName());                    
                    moduleReports.put(moduleManifestLog.getName(), new ModuleLogReport(moduleManifestLog.getName(),
                        moduleManifestLog.getValue(), moduleManifestLog.getWhitelistValue(),0));
                }
            }
        }
        
        if(!failureOnly){
            logger.debug("FailureOnly flag is false. Adding all manifests.");
            for(TblModuleManifest moduleManifest : tblPcrManifest.getMleId().getTblModuleManifestCollection()){
                logger.debug("addManifestLogs - {} - {}", moduleManifest.getComponentName(), moduleManifest.getDigestValue());

                if(moduleManifest.getExtendedToPCR().equalsIgnoreCase(tblPcrManifest.getName()) && 
                        !moduleReports.containsKey(moduleManifest.getComponentName()) && registeredPcrBank.equals(moduleManifest.getPcrBank())){
                    
                    if( moduleManifest.getUseHostSpecificDigestValue() != null && moduleManifest.getUseHostSpecificDigestValue().booleanValue() ) {
                        // For open source we used to have multiple module manifests for the same hosts. So, the below query by hostID was returning multiple results.
                        //String hostSpecificDigestValue = new TblHostSpecificManifestJpaController(getEntityManagerFactory()).findByHostID(hostId).getDigestValue();
                        String hostSpecificDigestValue = My.jpa().mwHostSpecificManifest().findByModuleIdHostIdPcrBank(hostId, moduleManifest.getId(), moduleManifest.getPcrBank()).getDigestValue();
                        moduleReports.put(moduleManifest.getComponentName(), new ModuleLogReport(moduleManifest.getComponentName(),
                                hostSpecificDigestValue, hostSpecificDigestValue, 1));
                    }
                    else {                        
                        if (moduleManifest.getComponentName().equalsIgnoreCase("tbootxm")) {

                            if (tbootxmModuleLogReport.getComponentName() == null || tbootxmModuleLogReport.getComponentName().isEmpty()) {
                                logger.debug("addManifestLogs - Adding the tbootxm root module", moduleManifest.getComponentName());                    
                                tbootxmModuleLogReport.setComponentName(moduleManifest.getComponentName());
                                tbootxmModuleLogReport.setValue(moduleManifest.getDigestValue());
                                tbootxmModuleLogReport.setWhitelistValue(moduleManifest.getDigestValue());
                                tbootxmModuleLogReport.setTrustStatus(1);
                            }

                            MwMeasurementXml findByMleId = My.jpa().mwMeasurementXml().findByMleId(tblPcrManifest.getMleId().getId());
                            List<Measurement> measurements = new XmlMeasurementLog(PcrIndex.valueOf(tblPcrManifest.getName()), findByMleId.getContent()).getMeasurements();
                            for(Measurement m : measurements) {
                                logger.debug("addManifestLogs - Adding the sub module {} for tbootxm root module.", m.getLabel());                    
                                
                                ModuleLogReport subModuleLogReport = new ModuleLogReport(m.getLabel(), m.getValue().toString(), m.getValue().toString(),1);
                                if (!temptbootxmSubModuleReport.containsKey(subModuleLogReport.getComponentName()))
                                    temptbootxmSubModuleReport.put(subModuleLogReport.getComponentName(), subModuleLogReport);
                            }
                        } else {

                            moduleReports.put(moduleManifest.getComponentName(), new ModuleLogReport(moduleManifest.getComponentName(),
                                    moduleManifest.getDigestValue(), moduleManifest.getDigestValue(),1)); 
                        }
                    }
                    
                }
            }
        }
        
        for (ModuleLogReport te : temptbootxmSubModuleReport.values())
            logger.debug("addManifestLogs - post processing sub modules {} - {} - {}", te.getComponentName(), te.getValue(), te.getWhitelistValue());
        Collection<ModuleLogReport> temptbootxmSubModuleReportValues = temptbootxmSubModuleReport.values();
        if (temptbootxmSubModuleReportValues != null && temptbootxmSubModuleReportValues.size() > 0) {
            tbootxmModuleLogReport.getModuleLogs().addAll(temptbootxmSubModuleReportValues);
        }
        List<ModuleLogReport> tbootxmModuleLogs = tbootxmModuleLogReport.getModuleLogs();
        if (tbootxmModuleLogs != null && tbootxmModuleLogs.size() > 0) {
            moduleReports.put("tbootxm", tbootxmModuleLogReport);
        }

        manifest.getModuleLogs().addAll(moduleReports.values());

    }

    private TblPcrManifest getPcrModuleManifest(TblHosts tblHosts, Integer mleId, String manifestName) {
        Collection<TblPcrManifest> pcrManifestCollection;
        
        if (tblHosts.getVmmMleId().getId().intValue() == mleId.intValue()) {
            pcrManifestCollection = tblHosts.getVmmMleId().getTblPcrManifestCollection();
        } else {
            pcrManifestCollection = tblHosts.getBiosMleId().getTblPcrManifestCollection();
        }

        if (pcrManifestCollection != null) {
            for (TblPcrManifest pcrManifest : pcrManifestCollection) {
                if (pcrManifest.getName().equals(manifestName) && pcrManifest.getPcrBank().equals(tblHosts.getPcrBank())) {
                    return pcrManifest;
                }
            }
        }
        throw new ASException(ErrorCode.AS_PCR_MANIFEST_MISSING,manifestName,mleId,tblHosts.getName());
        
    }
    
    private PcrLogReport getPcrLogReportForAssetTag(TblTaLog taLog, Integer hostId) {
        logger.debug("getPcrLogReportForAssetTag : Creating pcr log report for asset tag verification for host with uuid {}.", hostId);
        AssetTagCertBO atagCertBO = new AssetTagCertBO();
        MwAssetTagCertificate atagCert = atagCertBO.findValidAssetTagCertForHost(hostId);
        if (atagCert != null) {  
            logger.debug("getPcrLogReportForAssetTag : Found a valid asset tag certificate for the host with white list value {}", atagCert.getPCREvent().toString());
            PcrLogReport manifest = new PcrLogReport();
            manifest.setName(Integer.parseInt(assetTagPCR));
            manifest.setValue(taLog.getManifestValue());
            manifest.setWhiteListValue(new  Sha1Digest(atagCert.getPCREvent()).toString());
            if(manifest.getValue().equals(manifest.getWhiteListValue())) {
                manifest.setTrustStatus(1);
            }else{
                manifest.setTrustStatus(0);
            }
            manifest.setVerifiedOn(new Date());
            return manifest;
        }
        return null;
    }
}
