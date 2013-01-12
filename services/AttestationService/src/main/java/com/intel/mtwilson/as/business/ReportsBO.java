package com.intel.mtwilson.as.business;

import com.intel.mtwilson.as.data.TblModuleManifestLog;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblTaLog;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.business.trust.Util;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.helper.BaseBO;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.Hostname;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblTaLogJpaController;
import com.intel.mountwilson.as.hostmanifestreport.data.HostManifestReportType;
import com.intel.mountwilson.as.hostmanifestreport.data.ManifestType;
import com.intel.mountwilson.as.hosttrustreport.data.HostType;
import com.intel.mountwilson.as.hosttrustreport.data.HostsTrustReportType;
import com.intel.mountwilson.manifest.IManifestStrategy;
import com.intel.mountwilson.manifest.IManifestStrategyFactory;
import com.intel.mountwilson.manifest.data.IManifest;
import com.intel.mountwilson.manifest.data.PcrManifest;
import com.intel.mountwilson.manifest.factory.DefaultManifestStrategyFactory;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.datatypes.*;
import java.io.StringWriter;
import java.util.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
//import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class ReportsBO extends BaseBO {
    Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private byte[] dataEncryptionKey = null;


    public void setDataEncryptionKey(byte[] key) {
        dataEncryptionKey = key;
    }

    public HostsTrustReportType getTrustReport(Collection<Hostname> hostNames) { // datatype.Hostname





        try {
            HostsTrustReportType hostsTrustReportType = new HostsTrustReportType();
            for (Hostname host : hostNames) {
                TblHosts tblHosts = new TblHostsJpaController(getEntityManagerFactory(), dataEncryptionKey).findByName(host.toString()); // datatype.Hostname


                if (tblHosts == null) {
                    throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, host);
                }

                List<TblTaLog> logs = new TblTaLogJpaController(getEntityManagerFactory()).findTrustStatusByHostId(tblHosts.getId(), 5);

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
        } catch (CryptographyException e) {
            throw new ASException(e, ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        } catch (Exception e) {
            throw new ASException(e);
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
        TblHosts tblHosts = null;
        try {
            tblHosts = new TblHostsJpaController(getEntityManagerFactory(), dataEncryptionKey).findByName(hostName.toString()); // datatype.Hostname
        } catch (CryptographyException e) {
            throw new ASException(e, ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }

        if (tblHosts == null) {
            throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, hostName.toString());
        }

        Date lastStatusTs = new TblTaLogJpaController(getEntityManagerFactory()).findLastStatusTs(tblHosts.getId());


        if (lastStatusTs != null) {
            List<TblTaLog> logs = new TblTaLogJpaController(getEntityManagerFactory()).findLogsByHostId(tblHosts.getId(), lastStatusTs);
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

    // BUG #497 XXX TODO needs rewrite to use HostAgentFactory and HostAgent interfaces
    public String getHostAttestationReport(Hostname hostName) {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw;
        StringWriter sw = new StringWriter();
        IManifestStrategy manifestStrategy;
        IManifestStrategyFactory strategyFactory;
        HashMap<String, ? extends IManifest> pcrManifestMap = null;
        TblHosts tblHosts = null;
        String attestationReport = "";

        try {

            tblHosts = new TblHostsJpaController(getEntityManagerFactory(), dataEncryptionKey).findByName(hostName.toString());

            if (tblHosts == null) {
                throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, hostName.toString());
            }

            // XXX TODO all hosts will have connection strings soon so need to use HostAgentFactory to figure out which one to create, don't do this here.  use the capability methods in the HostAGent interface to figure out what it does or doesn't support.
            if (tblHosts.getAddOnConnectionInfo() != null && tblHosts.getAddOnConnectionInfo().contains("http")) {

                throw new ASException(ErrorCode.AS_OPERATION_NOT_SUPPORTED, "getHostAttestationReport does not support VMWare hosts.");
            }

            // BUG #497 needs to use HostAgentFactory and HostAgent
            strategyFactory = new DefaultManifestStrategyFactory();

            manifestStrategy = strategyFactory.getManifestStategy(tblHosts, getEntityManagerFactory());
            pcrManifestMap = manifestStrategy.getManifest(tblHosts); // BUG #497  this is now obtained by IntelHostAgent using TAHelper's getQuoteInformationForHost which is what was called by TrustAgentManifestStrategy.getManifest()

        } catch (ASException aex) {

            throw aex;


        } catch (CryptographyException e) {
            throw new ASException(e, ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        } catch (Exception ex) {

            throw new ASException(ex);
        }

        try {
            // XXX BUG #497 this entire section in try{}catch{} has  moved to TAHelper and used by IntelHostAgent
            
            // We need to check if the host supports TPM or not. Only way we can do it
            // using the host table contents is by looking at the AIK Certificate. Based
            // on this flag we generate the attestation report.
            boolean tpmSupport = true;
            String hostType = "";

            if (tblHosts.getAIKCertificate() == null || tblHosts.getAIKCertificate().isEmpty()) {
                tpmSupport = false;
            }

            hostType = tblHosts.getVmmMleId().getName();

            // xtw = xof.createXMLStreamWriter(new FileWriter("c:\\temp\\nb_xml.xml"));
            xtw = xof.createXMLStreamWriter(sw);
            xtw.writeStartDocument();
            xtw.writeStartElement("Host_Attestation_Report");
            xtw.writeAttribute("Host_Name", hostName.toString());
            xtw.writeAttribute("Host_VMM", hostType);
            xtw.writeAttribute("TXT_Support", String.valueOf(tpmSupport));

            if (tpmSupport == true) {
                ArrayList<IManifest> pcrMFList = new ArrayList<IManifest>();
                pcrMFList.addAll(pcrManifestMap.values());

                for (IManifest pcrInfo : pcrMFList) {
                    PcrManifest pInfo = (PcrManifest) pcrInfo;
                    xtw.writeStartElement("PCRInfo");
                    xtw.writeAttribute("ComponentName", String.valueOf(pInfo.getPcrNumber()));
                    xtw.writeAttribute("DigestValue", pInfo.getPcrValue().toUpperCase());
                    xtw.writeEndElement();
                }
            } else {
                xtw.writeStartElement("PCRInfo");
                xtw.writeAttribute("Error", "Host does not support TPM.");
                xtw.writeEndElement();
            }

            xtw.writeEndElement();
            xtw.writeEndDocument();
            xtw.flush();
            xtw.close();
            attestationReport = sw.toString();

        } catch (Exception ex) {

            throw new ASException(ex);
        }

        return attestationReport;
    }

    public AttestationReport getAttestationReport(Hostname hostName, Boolean failureOnly) {

        AttestationReport attestationReport = new AttestationReport();

        /*
         * if (hostName == null || hostName.isEmpty()) { throw new
         * ASException(ErrorCode.VALIDATION_ERROR, "Input Hostname " + hostName
         * + " is empty."); }
         *
         */

        TblHosts tblHosts = null;
        try {
            tblHosts = new TblHostsJpaController(getEntityManagerFactory(), dataEncryptionKey).findByName(hostName.toString()); // datatype.Hostname
        } catch (CryptographyException e) {
            throw new ASException(e, ErrorCode.AS_ENCRYPTION_ERROR, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }

        if (tblHosts == null) {
            throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, hostName.toString());
        }

        Date lastStatusTs = new TblTaLogJpaController(getEntityManagerFactory()).findLastStatusTs(tblHosts.getId());


        if (lastStatusTs != null) {
            List<TblTaLog> logs = new TblTaLogJpaController(getEntityManagerFactory()).findLogsByHostId(tblHosts.getId(), lastStatusTs);
            com.intel.mountwilson.as.hostmanifestreport.data.HostType hostType = new com.intel.mountwilson.as.hostmanifestreport.data.HostType();
            hostType.setName(hostName.toString()); // datatype.Hostname
            if (logs != null) {
                for (TblTaLog log : logs) {
                    if (!failureOnly || (failureOnly && log.getTrustStatus() == false)) {
                        attestationReport.getPcrLogs().add(getPcrManifestLog(tblHosts, log, failureOnly));
                    }
                }
            }
        }

        return attestationReport;
    }
    
    public PcrLogReport getPcrManifestLog(TblHosts tblHosts, TblTaLog log, Boolean failureOnly) throws NumberFormatException {
        TblPcrManifest tblPcrManifest = getPcrModuleManifest(tblHosts,log.getMleId(),log.getManifestName());
        PcrLogReport manifest = new PcrLogReport();
        manifest.setName(Integer.parseInt(log.getManifestName()));
        manifest.setValue(log.getManifestValue());
        manifest.setVerifiedOn(log.getUpdatedOn());
        manifest.setTrustStatus(getTrustStatus(log.getTrustStatus()));
        manifest.setWhiteListValue(tblPcrManifest.getValue());
//        if (log.getTblModuleManifestLogCollection() != null && log.getTblModuleManifestLogCollection().size() > 0) {
            addManifestLogs(manifest, log, failureOnly,tblPcrManifest);
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
    private void addManifestLogs(PcrLogReport manifest, TblTaLog log, Boolean failureOnly,TblPcrManifest tblPcrManifest) {
        HashMap<String,ModuleLogReport> moduleReports = new HashMap<String, ModuleLogReport>();
        
        if(log.getTblModuleManifestLogCollection() != null){
            for (TblModuleManifestLog moduleManifestLog : log.getTblModuleManifestLogCollection()) {
                moduleReports.put(moduleManifestLog.getName(), new ModuleLogReport(moduleManifestLog.getName(),
                        moduleManifestLog.getValue(), moduleManifestLog.getWhitelistValue(),0));
            }
        }
        
        if(!failureOnly){
            logger.info("FailureOnly flag is false. Adding all manifests.");
            for(TblModuleManifest moduleManifest : tblPcrManifest.getMleId().getTblModuleManifestCollection()){
                if(moduleManifest.getExtendedToPCR().equalsIgnoreCase(tblPcrManifest.getName()) && 
                        !moduleReports.containsKey(moduleManifest.getComponentName())){
                    
                    moduleReports.put(moduleManifest.getComponentName(), new ModuleLogReport(moduleManifest.getComponentName(),
                                moduleManifest.getDigestValue(), moduleManifest.getDigestValue(),1));
                    
                }
            }
        }
        
        manifest.getModuleLogs().addAll(moduleReports.values());

    }

    private TblPcrManifest getPcrModuleManifest(TblHosts tblHosts, Integer mleId, String manifestName) {
        Collection<TblPcrManifest> pcrManifestCollection = null;
        
        if (tblHosts.getVmmMleId().getId().intValue() == mleId.intValue()) {
            pcrManifestCollection = tblHosts.getVmmMleId().getTblPcrManifestCollection();
        } else {
            pcrManifestCollection = tblHosts.getBiosMleId().getTblPcrManifestCollection();
        }

        if (pcrManifestCollection != null) {
            for (TblPcrManifest pcrManifest : pcrManifestCollection) {
                if (pcrManifest.getName().equals(manifestName)) {
                    return pcrManifest;
                }
            }
        }
        throw new ASException(ErrorCode.AS_PCR_MANIFEST_MISSING,manifestName,mleId,tblHosts.getName());
        
    }
}
