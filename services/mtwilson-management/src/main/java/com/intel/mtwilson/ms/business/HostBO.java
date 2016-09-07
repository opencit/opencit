 /*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.*;
import com.intel.mtwilson.agent.*;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.as.controller.MwProcessorMappingJpaController;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblLocationPcrJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.data.MwProcessorMapping;
import java.io.IOException;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblLocationPcr;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.datatypes.Vendor;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.business.BulkHostMgmtBO;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.controller.MwMeasurementXmlJpaController;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwMeasurementXml;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.rest.v2.model.WhitelistConfigurationData;
import static com.intel.mtwilson.datatypes.HostWhiteListTarget.VMM_GLOBAL;
import static com.intel.mtwilson.datatypes.HostWhiteListTarget.VMM_HOST;
import static com.intel.mtwilson.datatypes.HostWhiteListTarget.VMM_OEM;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.util.ResourceFinder;
import com.intel.mtwilson.wlm.business.MleBO;
import com.intel.mtwilson.wlm.business.OemBO;
import com.intel.mtwilson.wlm.business.OsBO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.util.StringUtils;

/**
 *
 * @author ssbangal
 */
public class HostBO {

    private static int MAX_BIOS_PCR = 17;
    private static int LOCATION_PCR = 22;
    private static int LOCATION_PCR_WINDOWS = 23;
    private static String BIOS_PCRs = "0,17";
    private static String VMWARE_PCRs = "18,19,20";
    private static String OPENSOURCE_PCRs = "18,19";
    private static String OPENSOURCE_DA_PCRs = "17,18";
    private static String CITRIX_PCRs = "18"; //"17,18";
    private static String WINDOWS_BIOS_PCRs = "0";
    private static String WINDOWS_PCRs = "13,14";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostBO.class);

    /**
     * Private class to support multithreading to retrieve the attestation report from the target host.
     */
    private class HostAttReport extends Thread {

        private HostAgent agent;
        private String requiredPCRs;
        private String attestationReport;
        private boolean isError = false;
        private String errorMessage = "";
        private String measurementXmlLog;
        private Nonce challenge;

        public HostAttReport(HostAgent agent, String requiredPCRs) {
            this.agent = agent;
            this.requiredPCRs = requiredPCRs;
            this.challenge = null;
        }

        public HostAttReport(HostAgent agent, String requiredPCRs, Nonce challenge) {
            this.agent = agent;
            this.requiredPCRs = requiredPCRs;
            this.challenge = challenge;
        }

        @Override
        public void run() {
            if (isError()) {
                return;
            }
            try {
                long threadStart = System.currentTimeMillis();
                // TODO: the below two calls can be optimized heavily, probably to speed up both, or combine them.
                attestationReport = agent.getHostAttestationReport(requiredPCRs, challenge);
                measurementXmlLog = agent.getPcrManifest(challenge).getMeasurementXml();
                log.debug("TIMETAKEN: by the attestation report thread: {}", (System.currentTimeMillis() - threadStart));
            } catch (Throwable te) {
                isError = true;
                attestationReport = null;
                measurementXmlLog = null;
                log.debug("Unexpected error while retrieving attestation report.", te);
                errorMessage = te.getClass().getSimpleName();
            }
        }

        public boolean isError() {
            return isError;
        }

        public String getResult() {
            return attestationReport;
        }

        public String getMeasurementXmlLog() {
            return measurementXmlLog;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Private class to support multithreading of checking the existence of MLE before creating a new one.
     */
    private class MLEVerify extends Thread {

        private String result;
        private boolean isError = false;
        private String errorMessage = "";
        private HostConfigData hostConfigData;

        public MLEVerify(HostConfigData hostConfigData) {
            this.hostConfigData = hostConfigData;
        }

        @Override
        public void run() {
            if (isError()) {
                return;
            }
            try {
                // If the user has chosen to overwrite the white list, then we do need to check anything. Just 
                // return back as not found.
                if (hostConfigData.getOverWriteWhiteList()) {
                    result = "BIOS:false|VMM:false";
                } else {
                    long threadStart = System.currentTimeMillis();
                    result = new HostTrustBO().checkMatchingMLEExists(hostConfigData);
                    log.debug("TIMETAKEN: by the checkMLE thread:" + (System.currentTimeMillis() - threadStart));
                }

            } catch (MSException e) {
                log.error("Failed to verify MLE", e);
                isError = true;
                result = null;
                errorMessage = e.getClass().getSimpleName();
            } catch (Exception e) {
                log.error("Failed to verify MLE", e);
                isError = true;
                result = null;
                errorMessage = e.getClass().getSimpleName();
            }
        }

        public boolean isError() {
            return isError;
        }

        public String getResult() {
            return result;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public HostBO() {
    }

    /**
     *
     * @param processorNameOrCPUID
     * @return
     */
    public String getPlatformName(String processorNameOrCPUID) {

        String platformName = "";
        try {
            MwProcessorMappingJpaController jpaController = My.jpa().mwProcessorMapping();  //new MwProcessorMappingJpaController(getASEntityManagerFactory());

            // Let us first search in the processorName field. If it cannot find, then we will search on the CPU ID field
            MwProcessorMapping procMap = jpaController.findByProcessorType(processorNameOrCPUID);
            if (procMap == null) {
                // Since we did not find the platform by processor type, the search criteria is very likely to be CPU ID
                // In some cases we have observed that the stepping can change within a processor generation and can be 
                // ignored for mapping to the platform time.
                // EX: C1 06 02. Here 1 is the stepping. To ignore this the below query has been modified to use the LIKE option. So,
                // we need to set the second character to %
                processorNameOrCPUID = processorNameOrCPUID.substring(0, 1) + "%" + processorNameOrCPUID.substring(2);
                procMap = jpaController.findByCPUID(processorNameOrCPUID);
            }

            if (procMap != null) {
                platformName = procMap.getPlatformName();
            }
        } catch (MSException me) {
            log.error("Error during retrieval of platform name details. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            log.error("Unexpected errror during retrieval of platform name details. ", ex);
            // throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during retrieval of platform name details." + ex.getMessage());
            throw new MSException(ErrorCode.MS_PLATFORM_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }

        return platformName;

    }

    private void calibrateMLENames(HostConfigData hostConfigObj, Boolean isBIOSMLE) throws MalformedURLException {
        TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();

        if (isBIOSMLE) {
            if (hostObj.BIOS_Oem.contains("Intel")) {
                // For Romley servers, below is the format of the BIOS string. We need to split the string on . and 
                // consider the 3,4 & 5 substrings to make up the complete BIOS string. This will not work for Thurley
                // which is ok since we will support just Romley servers.
                String[] biosSubStrings = hostObj.BIOS_Version.split("\\.");
                hostObj.BIOS_Version = biosSubStrings[2] + "." + biosSubStrings[3] + "." + biosSubStrings[4];
            }

            // Now that we know that the PCR 0 should be cosistent across the different hypervisors for the same BIOS version, we
            // need not append the OS name
            hostObj.BIOS_Name = hostObj.BIOS_Oem.replace(' ', '_');

            // If we are setting host specific MLE, then we need to append the host name to the BIOS Name as well
            if (hostConfigObj.getBiosWLTarget() == HostWhiteListTarget.BIOS_HOST) {
                hostObj.BIOS_Name = hostObj.HostName + "_" + hostObj.BIOS_Name;
            }

        } else {
            // Need to do some data massaging. Firstly we need to change the White Spaces in the OS name to underscores. This is to ensure that it works correctly with
            // the WLM portal. In case of Intel's BIOS, need to trim it since it will be very long.
            hostObj.VMM_OSName = hostObj.VMM_OSName.replace(' ', '_');

            // Update the host object with the names of BIOS and VMM, which is needed during host registration.
            hostObj.VMM_Version = hostObj.VMM_OSVersion + "-" + hostObj.VMM_Version;

            // Bug 798
            if (ConnectionString.from(hostObj).getVendor() == Vendor.CITRIX && hostObj.VMM_OSName.toLowerCase().contains("xen")) {
                hostObj.VMM_Name = "Citrix_" + hostObj.VMM_OSName;
            }

            // For VMware since there is no separate OS and VMM, we use the same name
            if (hostObj.VMM_OSName.contains("ESX")) {
                hostObj.VMM_Name = hostObj.VMM_OSName;
            }

            // If we are setting host specific MLE, then we need to append the host name to the VMM Name as well
            if (hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_HOST) {
                String platformName = getPlatformName(hostObj.Processor_Info);
                if (!platformName.isEmpty()) {
                    hostObj.VMM_Name = hostObj.HostName + "_" + platformName + "_" + hostObj.VMM_Name;
                } else {
                    hostObj.VMM_Name = hostObj.HostName + "_" + hostObj.VMM_Name;
                }
            } else if (hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_OEM) {
                // Bug 799 & 791: Need to append the platform name too
                String platformName = getPlatformName(hostObj.Processor_Info);
                if (!platformName.isEmpty()) {
                    hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0] + "_" + platformName + "_" + hostObj.VMM_Name;
                } else {
                    hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0] + "_" + hostObj.VMM_Name;
                }
            } else if (hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_GLOBAL) {
                // Bug #951 where in we need to append the platform name to the global white lists also.
                String platformName = getPlatformName(hostObj.Processor_Info);
                if (!platformName.isEmpty()) {
                    hostObj.VMM_Name = platformName + "_" + hostObj.VMM_Name;
                }
            }
        }
        hostConfigObj.setTxtHostRecord(hostObj);
    }

    /**
     * This is a helper function which if provided the host name and connection string, would retrieve the BIOS & VMM configuration from the host, verifies if those corresponding MLEs are already configured in the Mt.Wilson system. If not, it would throw appropriate exception back. The object returned back from his helper function could be used to directly register the host.
     *
     * @param hostConfigObj
     * @return
     */
    private HostConfigData getHostMLEDetails(HostConfigData hostConfigObj) {

        try {
            My.initDataEncryptionKey();

            TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();
            TblHosts tblHosts = new TblHosts();
            // Since the connection string passed in by the caller may not be complete (including the vendor), we need to parse it
            // first and make up the complete connection string.
            ConnectionString cs = new ConnectionString(hostObj.AddOn_Connection_String);
            hostObj.AddOn_Connection_String = cs.getConnectionStringWithPrefix();

            tblHosts.setTlsKeystore(null);
            tblHosts.setName(hostObj.HostName);
            tblHosts.setAddOnConnectionInfo(hostObj.AddOn_Connection_String);
            tblHosts.setTlsPolicyChoice(hostObj.tlsPolicyChoice);
            tblHosts.setIPAddress(hostObj.HostName);
            if (hostObj.Port != null) {
                tblHosts.setPort(hostObj.Port);
            }
            HostAgentFactory factory = new HostAgentFactory();
            HostAgent agent = factory.getHostAgent(tblHosts);
            try {
                TxtHostRecord hostDetails = agent.getHostDetails();
                hostObj.BIOS_Oem = hostDetails.BIOS_Oem;
                hostObj.BIOS_Version = hostDetails.BIOS_Version;
                hostObj.VMM_Name = hostDetails.VMM_Name;
                hostObj.VMM_Version = hostDetails.VMM_Version;
                hostObj.VMM_OSName = hostDetails.VMM_OSName;
                hostObj.VMM_OSVersion = hostDetails.VMM_OSVersion;
                hostObj.Processor_Info = hostDetails.Processor_Info;
                hostObj.TpmVersion = hostDetails.TpmVersion;
                hostObj.PcrBanks = hostDetails.PcrBanks;
            } catch (Throwable te) {
                log.error("Unexpected error in registerHostFromCustomData: {}", te);
                throw new MSException(ErrorCode.MS_HOST_COMMUNICATION_ERROR, te.getClass().getSimpleName());
            }

            // Update the connnection string if needed.
            String updatedConnectionString = factory.getHostConnectionString();
            hostObj.AddOn_Connection_String = updatedConnectionString;
            // Let us verify if we got all the data back correctly or not 
            if (hostObj.BIOS_Oem == null || hostObj.BIOS_Version == null || hostObj.VMM_OSName == null || hostObj.VMM_OSVersion == null || hostObj.VMM_Version == null) {
                throw new MSException(ErrorCode.MS_HOST_CONFIGURATION_ERROR);
            }

            hostConfigObj.setTxtHostRecord(hostObj);
            log.debug("Successfully retrieved the host information. Details: " + hostObj.BIOS_Oem + ":" + hostObj.BIOS_Version + ":"
                    + hostObj.VMM_OSName + ":" + hostObj.VMM_OSVersion + ":" + hostObj.VMM_Version + ":" + hostObj.Processor_Info);

            // Change the BIOS and VMM MLE names as per the target white list chosen by the user
            calibrateMLENames(hostConfigObj, true);
            calibrateMLENames(hostConfigObj, false);

            return hostConfigObj;

        } catch (MSException me) {
            log.error("Error during retrieval of host MLE information. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            // bug #1038 prevent leaks in error messages to client
            log.error("Unexpected errror during retrieval of host MLE information. ", ex);
            // throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during retrieval of host MLE information." + ex.getMessage());
            throw new MSException(ErrorCode.MS_MLE_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Helper function that checks if the host specified is already configured in the system or not.
     *
     * @param hostObj
     * @return
     */
    private boolean isHostConfigured(TxtHostRecord hostObj) {
        boolean isHostConfigured;
        try {
            TblHostsJpaController hostsJpaController = My.jpa().mwHosts(); //new TblHostsJpaController(getASEntityManagerFactory());

            log.debug("Processing host {}.", hostObj.HostName);
            TblHosts hostSearchObj = hostsJpaController.findByName(hostObj.HostName);
            if (hostSearchObj != null) {
                log.debug(String.format("Host '%s' is already configured in the system.", hostObj.HostName));
                isHostConfigured = true;
            } else {
                log.debug(String.format("Host '%s' is currently not configured. ", hostObj.HostName));
                isHostConfigured = false;
            }

            return isHostConfigured;

        } catch (MSException me) {
            log.error("Error during bulk host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            log.error("Unexpected errror during bulk host registration. ", ex);
            throw new MSException(ErrorCode.MS_BULK_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Registers the host with Mount Wilson.
     *
     * @param hostObj : Details of the host to be registered.
     * @return : True if the host is registered successfully.
     */
    public boolean registerHost(TxtHostRecord hostObj) {

        try {
            if (hostObj != null) {

                HostConfigData hostConfigObj = new HostConfigData();
                hostConfigObj.setTxtHostRecord(hostObj);

                // Set the default parameters
                hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
                hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
                return (registerHostFromCustomData(hostConfigObj));
            } else {
                log.error("Input not specified for registering the host.");
                return false;
            }

        } catch (MSException me) {
            log.error("Error during host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (Exception ex) {
            log.error("Unexpected errror during host registration. ", ex);
            throw new MSException(ErrorCode.MS_HOST_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Function that supports bulk host registration. If the user has just specified the host details to be registered, then we would use the default white list target of OEM for both BIOS and VMM.
     *
     * @param hostRecords
     * @return
     */
    public HostConfigResponseList registerHosts(TxtHostRecordList hostRecords) {
        HostConfigDataList hostList = new HostConfigDataList();
        HostConfigResponseList hostResponseList;

        try {

            if (hostRecords != null) {

                log.info("About to process {} servers", hostRecords.getHostRecords().size());

                // For all the hosts specified, setup the default parameters and add it to the list
                for (TxtHostRecord hostObj : hostRecords.getHostRecords()) {

                    HostConfigData hostConfigObj = new HostConfigData();
                    hostConfigObj.setTxtHostRecord(hostObj);
                    hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
                    hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
                    hostList.getHostRecords().add(hostConfigObj);
                }

            }

            hostResponseList = registerHosts(hostList);
            return hostResponseList;

        } catch (MSException me) {
            log.error("Error during bulk host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (Exception ex) {

            log.error("Unexpected errror during bulk host registration. ", ex);
            throw new MSException(ErrorCode.MS_BULK_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Bulk host registration/update function.
     *
     * @param hostRecords : List of hosts to be updated or newly registered.
     * @return
     */
    public HostConfigResponseList registerHosts(HostConfigDataList hostRecords) {
        HostConfigDataList hostsToBeAddedList = new HostConfigDataList();
        HostConfigDataList hostsToBeUpdatedList = new HostConfigDataList();
        HostConfigResponseList results = new HostConfigResponseList();

        try {
            //TblHostsJpaController hostsJpaController = My.jpa().mwHosts();// new TblHostsJpaController(getASEntityManagerFactory());
            log.debug("About to start processing {} the hosts", hostRecords.getHostRecords().size());

            // We first need to check if the hosts are already registered or not. Accordingly we will create 2 separate TxtHostRecordLists
            // One will be for the new hosts that need to be registered and the other one would be for the existing hosts that
            // need to be updated.
            for (HostConfigData hostConfigObj : hostRecords.getHostRecords()) {                
                TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();                
                if (isHostConfigured(hostObj)) {
                    log.debug(String.format("Since '%s' is already configured, we will update the host with the new MLEs.", hostObj.HostName));
                    // Retrieve the details of the MLEs for the host. If we get any exception that we will not process that host and 
                    // return back the same error to the user
                    try {

                        hostConfigObj = getHostMLEDetails(hostConfigObj);
                        hostsToBeUpdatedList.getHostRecords().add(hostConfigObj);

                    } catch (MSException mse) {
                        HostConfigResponse error = new HostConfigResponse();
                        error.setHostName(hostObj.HostName);
                        error.setStatus("false");
                        error.setErrorMessage(mse.getErrorMessage() + "[" + mse.getErrorCode().toString() + "]");
                        // add this to the final result list
                        results.getHostRecords().add(error);
                    }
                } else {
                    log.debug(String.format("Host '%s' is currently not configured. Host will be registered.", hostObj.HostName));
                    try {

                        hostConfigObj = getHostMLEDetails(hostConfigObj);
                        hostsToBeAddedList.getHostRecords().add(hostConfigObj);

                    } catch (MSException mse) {
                        HostConfigResponse error = new HostConfigResponse();
                        error.setHostName(hostObj.HostName);
                        error.setStatus("false");
                        error.setErrorMessage(mse.getErrorMessage() + "[" + mse.getErrorCode().toString() + "]");
                        // add this to the final result list
                        results.getHostRecords().add(error);
                    }
                }
            }

            // We will call into the addHosts API first for all the new hosts that need to be registered and then updateHosts API for the
            // hosts to be updated. We will combine the resuls of both and return back to the caller.
            BulkHostMgmtBO bulkHostMgmtBO = new BulkHostMgmtBO();
            if (!hostsToBeAddedList.getHostRecords().isEmpty()) {
                HostConfigResponseList addHostResults = bulkHostMgmtBO.addHosts(hostsToBeAddedList);
                for (HostConfigResponse hcr : addHostResults.getHostRecords()) {
                    results.getHostRecords().add(hcr);
                }
            }
            if (!hostsToBeUpdatedList.getHostRecords().isEmpty()) {
                HostConfigResponseList updateHostResults = bulkHostMgmtBO.updateHosts(hostsToBeUpdatedList);
                for (HostConfigResponse hcr : updateHostResults.getHostRecords()) {
                    results.getHostRecords().add(hcr);
                }
            }

            return results;

        } catch (MSException me) {
            log.error("Error during bulk host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            log.error("Unexpected errror during bulk host registration. ", ex);
            throw new MSException(ErrorCode.MS_BULK_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Registers the host using the additional details like which MLE to use etc for registration.
     *
     * @param hostConfigObj : Host Configuration object having the details of the host to be registered along with the details of the MLE that needs to be used for registration.
     *
     * @return : True if success or else an exception.
     */
    public boolean registerHostFromCustomData(HostConfigData hostConfigObj) {
        try {
            log.debug("Starting to process the registration for host: " + hostConfigObj.getTxtHostRecord().HostName);

            hostConfigObj = getHostMLEDetails(hostConfigObj);

            new HostTrustBO().getTrustStatusOfHostNotInDBAndRegister(hostConfigObj);

            log.debug("Successfully registered the host: " + hostConfigObj.getTxtHostRecord().HostName);
            return true;

        } catch (MSException me) {
            log.error("Error during host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (Exception ex) {
            log.error("Unexpected errror during host registration. ", ex);
            throw new MSException(ErrorCode.MS_HOST_REGISTRATION_ERROR, ex.getClass().getSimpleName());
        }
    }

    private List<String> calculateBiosPCRMapForHost(TxtHostRecord gkvHost) throws MalformedURLException {
        ConnectionString connString = ConnectionString.from(gkvHost);
        
        String biosPCRs;
        
        if(connString.getVendor().equals(Vendor.MICROSOFT)) {
            biosPCRs = WINDOWS_BIOS_PCRs;
        } else {
            biosPCRs = BIOS_PCRs;
        }
        
        return Arrays.asList(biosPCRs.split(","));
    }
    
    private List<String> calculateVmmPCRMapForHost(TxtHostRecord gkvHost) throws MalformedURLException {
        ConnectionString connString = ConnectionString.from(gkvHost);
        
        String vmmPCRs;
        
        if(connString.getVendor().equals(Vendor.VMWARE)) {
            vmmPCRs = VMWARE_PCRs;            
        } else if(connString.getVendor().equals(Vendor.CITRIX)) {
            vmmPCRs = CITRIX_PCRs;
        } else if(connString.getVendor().equals(Vendor.MICROSOFT)) {
            vmmPCRs = WINDOWS_PCRs;
        } else if(gkvHost.getDaMode()) {
            vmmPCRs = OPENSOURCE_DA_PCRs;
        } else {
            vmmPCRs = OPENSOURCE_PCRs;
        }
        
        return Arrays.asList(vmmPCRs.split(","));
    }   
    /**
     * Configures the white list using the host specified.
     *
     * @param gkvHost: User need to provide just the Host Name and vCenter Connection string in case of VMware hosts. For Open Source hypervisors, user need to provide the IP Address and port number.
     * @return : true if the white list is configured successfully.
     */
    public boolean configureWhiteListFromHost(TxtHostRecord gkvHost) throws ApiException {
        boolean configStatus;

        try {

            if (gkvHost != null) {

                WhitelistConfigurationData hostConfigObj = new WhitelistConfigurationData();                

                hostConfigObj.setTxtHostRecord(gkvHost);

                // Set the default parameters
                hostConfigObj.setBiosWhiteList(true);
                hostConfigObj.setVmmWhiteList(true);
                hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
                hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
                hostConfigObj.setOverWriteWhiteList(false);
                hostConfigObj.setRegisterHost(false);
                if (hostConfigObj == null)
                    throw new MSException(ErrorCode.AS_HOST_NOT_FOUND);
                configStatus = configureWhiteListFromCustomData(hostConfigObj);
                return configStatus;
            } else {
                log.error("Good know host has not been specified.");
                throw new MSException(ErrorCode.AS_HOST_NOT_FOUND);
            }

        } catch (MSException | ASException me) {
            log.error("Error during white list configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            log.error("Unexpected error during white list configuration. ", ex);
            throw new MSException(ErrorCode.MS_WHITELIST_CONFIG_ERROR, ex.getClass().getSimpleName());
        }
    }

    public boolean configureWhiteListFromCustomData(WhitelistConfigurationData hostConfigObj) {
        if (hostConfigObj.getChallengeHex() == null || hostConfigObj.getChallengeHex().isEmpty()) {
            return configureWhiteListFromCustomData(hostConfigObj, null);
        } else if (Digest.sha1().isValidHex(hostConfigObj.getChallengeHex())) {
            Nonce challenge = new Nonce(Digest.sha1().valueHex(hostConfigObj.getChallengeHex()).getBytes());
            return configureWhiteListFromCustomData(hostConfigObj, challenge);
        } else {
            log.error("Invalid challenge: {}", hostConfigObj.getChallengeHex());
            throw new IllegalArgumentException("Invalid challenge");
        }

    }

    /**
     * This function using the white list configuration settings including pcr details, whether the whitelist is for an individual host/for OEM specific host/global white list, etc, configures the DB with the whitelist from the specified good known host.
     *
     * @param hostConfigObj : White List configuration object having all the details.
     * @param challenge
     * @return : true on success.
     */
    public boolean configureWhiteListFromCustomData(WhitelistConfigurationData hostConfigObj, Nonce challenge) {
//        // debug only
//        try {
//        ObjectMapper mapper = new ObjectMapper();
//        log.debug("configureWhiteListFromCustomData: {}", mapper.writeValueAsString(hostConfigObj)); //This statement may contain clear text passwords
//        } catch(Exception e) { log.error("configureWhiteListFromCustomData cannot serialize input" ,e); }
//        // debug only

        boolean configStatus = false;
        String attestationReport;

        long configWLStart = System.currentTimeMillis();

        try {
            My.initDataEncryptionKey();
            // Let us ensure that the user has specified the PCRs to be used
            if (hostConfigObj != null) {
                              
                if ((hostConfigObj.addBiosWhiteList() == true) && (hostConfigObj.getBiosWLTarget() == null
                        || hostConfigObj.getBiosWLTarget().getValue().isEmpty())) {
                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostConfigObj.getBiosWLTarget().toString());
                }

                if ((hostConfigObj.addVmmWhiteList() == true) && (hostConfigObj.getVmmWLTarget() == null
                        || hostConfigObj.getVmmWLTarget().getValue().isEmpty())) {
                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostConfigObj.getBiosWLTarget().toString());
                }

                TxtHostRecord gkvHost = hostConfigObj.getTxtHostRecord();

                log.debug("configWhiteListFromCustom vmmPCRs: " + hostConfigObj.getVmmPCRs());
//        // debug only
//        try {
//        ObjectMapper mapper = new ObjectMapper();
//        String writeValueAsString = mapper.writeValueAsString(gkvHost);
//        log.debug("configureWhiteListFromCustomData TxtHostRecord2: {}", mapper.writeValueAsString(gkvHost)); //This statement may contain clear text passwords
//        } catch(Exception e) { log.error("configureWhiteListFromCustomData cannot serialize TxtHostRecord2" ,e); }
//        // debug only
                ConnectionString cs;
                if (gkvHost.AddOn_Connection_String == null) {
                    cs = ConnectionString.from(gkvHost);
                    gkvHost.AddOn_Connection_String = cs.getConnectionStringWithPrefix();
                } else {
                    // Oct 23, 2013: We just make sure that the connection string has the prefix. Otherwise the agent factory will not be
                    // able to instantiate the correct one.
                    cs = new ConnectionString(gkvHost.AddOn_Connection_String);
                    gkvHost.AddOn_Connection_String = cs.getConnectionStringWithPrefix();
                }
                TblHosts tblHosts = new TblHosts();
                tblHosts.setName(gkvHost.HostName);
                tblHosts.setAddOnConnectionInfo(gkvHost.AddOn_Connection_String);
                tblHosts.setTlsPolicyChoice(gkvHost.tlsPolicyChoice);
                tblHosts.setIPAddress(gkvHost.HostName);
                if (gkvHost.Port != null) {
                    tblHosts.setPort(gkvHost.Port);
                }

                HostAgentFactory factory = new HostAgentFactory();
                HostAgent agent = factory.getHostAgent(tblHosts);
                try {
                    TxtHostRecord gkvHostDetails = agent.getHostDetails();
                    gkvHost.BIOS_Oem = gkvHostDetails.BIOS_Oem;
                    gkvHost.BIOS_Version = gkvHostDetails.BIOS_Version;
                    gkvHost.VMM_Name = gkvHostDetails.VMM_Name;
                    gkvHost.VMM_Version = gkvHostDetails.VMM_Version;
                    gkvHost.VMM_OSName = gkvHostDetails.VMM_OSName;
                    gkvHost.VMM_OSVersion = gkvHostDetails.VMM_OSVersion;
                    gkvHost.Processor_Info = gkvHostDetails.Processor_Info;
                    gkvHost.TpmVersion = gkvHostDetails.TpmVersion;
                    gkvHost.PcrBanks = gkvHostDetails.PcrBanks;
                } catch (Throwable te) {
                    log.error("Unexpected error in configureWhiteListFromCustomData: {}", te.getMessage());
                    log.debug("Unexpected error in configureWhiteListFromCustomData", te);
                    throw new MSException(ErrorCode.MS_HOST_COMMUNICATION_ERROR, te.getClass().getSimpleName());
                }

                // Update the connnection string if needed.
                String updateAddOnConnectionString = factory.getHostConnectionString();
                gkvHost.AddOn_Connection_String = updateAddOnConnectionString;
                tblHosts.setAddOnConnectionInfo(updateAddOnConnectionString);

                log.debug("TIMETAKEN: for getting host information is: {}", (System.currentTimeMillis() - configWLStart));
                log.debug("Starting to process the white list configuration from host: {}", gkvHost.HostName);

                // Let us verify if we got all the data back correctly or not (Bug: 442)
                if (gkvHost.BIOS_Oem == null || gkvHost.BIOS_Version == null || gkvHost.VMM_OSName == null || gkvHost.VMM_OSVersion == null || gkvHost.VMM_Version == null) {
                    throw new MSException(ErrorCode.MS_HOST_CONFIGURATION_ERROR);
                }

                // We need to validate the AIK Certificate first if the the AIK CA is available. This would be applicable only for Trust Agent hosts. Both
                // Citrix and VMware do not support this.
                if (agent.isAikAvailable()) { // INTEL and CITRIX
                    // stores the AIK public key (and certificate, if available) in the host record, and sets AIK_SHA1=SHA1(AIK_PublicKey) on the host record too
                    setAikForHost(tblHosts, agent);
                    // Intel hosts return an X509 certificate for the AIK public key, signed by the privacy CA.  so we must verify the certificate is ok.
                    if (agent.isAikCaAvailable()) {
                        // we have to check that the aik certificate was signed by a trusted privacy ca
                        X509Certificate hostAikCert = X509Util.decodePemCertificate(tblHosts.getAIKCertificate()); //agent.getAikCertificate();
                        hostAikCert.checkValidity(); // AIK certificate must be valid today
                        boolean validCaSignature = isAikCertificateTrusted(hostAikCert);
                        if (!validCaSignature) {
                            throw new MSException(ErrorCode.MS_INVALID_AIK_CERTIFICATE, gkvHost.HostName);
                        }
                    }
                }

                hostConfigObj.setTxtHostRecord(gkvHost);
                log.debug("Successfully retrieved the host information. Details: {}", gkvHost.BIOS_Oem + ":"
                        + gkvHost.BIOS_Version + ":" + gkvHost.VMM_OSName + ":" + gkvHost.VMM_OSVersion
                        + ":" + gkvHost.VMM_Version + ":" + gkvHost.Processor_Info);
                                                       
                Set<String> biosManifestSet = new TreeSet<>();
                Set<String> vmmManifestSet = new TreeSet<>();
                
                Set<String> reqdManifestSet = new TreeSet<>();

//                TblHostsJpaController hostsJpaController =  My.jpa().mwHosts();//new TblHostsJpaController(getASEntityManagerFactory());
                log.debug("TIMETAKEN: for getting API Client object is: {}", (System.currentTimeMillis() - configWLStart));

                // check if it has a TPM first  (should be at the beginning of this method but currently trust agent doesn't support a real is-tpm-available capability)   bug #540
                if (!agent.isTpmEnabled()) {
                    throw new MSException(ErrorCode.AS_VMW_TPM_NOT_SUPPORTED, tblHosts.getName());
                }

                calibrateMLENames(hostConfigObj, true);
                calibrateMLENames(hostConfigObj, false);
                if (hostConfigObj.addBiosWhiteList()) {  
                    biosManifestSet.addAll(calculateBiosPCRMapForHost(gkvHost));
                                                                
                    if(hostConfigObj.getBiosPCRs() != null && !hostConfigObj.getBiosPCRs().isEmpty()) {
                        // advanced user specified PCRs
                        String[] pcrs = hostConfigObj.getBiosPCRs().split(",");
                        biosManifestSet.addAll(Arrays.asList(pcrs));
                    }                    
                    hostConfigObj.setBiosPCRs(StringUtils.join(biosManifestSet.iterator(), ","));
                    reqdManifestSet.addAll(biosManifestSet);                
                }
                                
                if (hostConfigObj.addVmmWhiteList()) {
                    vmmManifestSet.addAll(calculateVmmPCRMapForHost(gkvHost));
                    
                    if(hostConfigObj.getVmmPCRs() != null && !hostConfigObj.getVmmPCRs().isEmpty()) {
                       String[] pcrs = hostConfigObj.getVmmPCRs().split(",");
                       vmmManifestSet.addAll(Arrays.asList(pcrs));
                    }
                    hostConfigObj.setVmmPCRs(StringUtils.join(vmmManifestSet.iterator(), ","));
                    reqdManifestSet.addAll(vmmManifestSet);
                }                
                
                String reqdManifestList = StringUtils.join(reqdManifestSet.iterator(), ",");
                
                
                log.debug("TIMETAKEN: for calibrating MLE names: {} ", (System.currentTimeMillis() - configWLStart));

                // Now we need to spawn 2 threads. One for retriving the attestation report from the host and another one for checking whether MLE with
                // matching whitelists exists or not.
                HostAttReport hostAttReportObj = new HostAttReport(agent, reqdManifestList, challenge);
                hostAttReportObj.start();

                MLEVerify mleVerifyObj = new MLEVerify(hostConfigObj);
                mleVerifyObj.start();

                hostAttReportObj.join();
                log.debug("TIMETAKEN: for generating host report: {}", (System.currentTimeMillis() - configWLStart));

                mleVerifyObj.join();
                log.debug("TIMETAKEN: for checking MLE names: {}", (System.currentTimeMillis() - configWLStart));

                attestationReport = hostAttReportObj.getResult();
                String mleVerifyStatus = mleVerifyObj.getResult();
                // Verify the retrived attestation status report. If null, throw an error
                if ((attestationReport == null || attestationReport.isEmpty()) && hostAttReportObj.isError) {
                    throw new MSException(ErrorCode.MS_HOST_COMMUNICATION_ERROR, hostAttReportObj.errorMessage);
                }

                // We are checking for component name since in the attestation report all the pcr and the event logs would use componentname as the label 
                if (attestationReport != null && !attestationReport.isEmpty()) {
                    if (!attestationReport.contains("ComponentName")) {
                        log.debug("Attestation report content: {}", attestationReport);
                        throw new MSException(ErrorCode.MS_INVALID_ATTESTATION_REPORT);
                    }
                }

                if (!validateAttestationReport(attestationReport, reqdManifestList)) {
                    log.error("PCR or Module value is not valid.");
                    throw new MSException(ErrorCode.MS_INVALID_ATTESTATION_REPORT);
                }

                log.debug("Successfully retrieved the attestation report from host: {}", gkvHost.HostName);
                log.debug("Attestation report is : {}", attestationReport);

                boolean biosMLEExists = false;
                boolean vmmMLEExists = false;
                // Verify the MLE status. If in case the MLE verification status is NULL, we will just create new MLE
                if (!mleVerifyObj.isError && mleVerifyStatus != null && !mleVerifyStatus.isEmpty()) {
                    // The return format would be BIOS:true|VMM:true
                    biosMLEExists = Boolean.parseBoolean(mleVerifyStatus.substring("BIOS:".length(), mleVerifyStatus.indexOf("|")));
                    vmmMLEExists = Boolean.parseBoolean(mleVerifyStatus.substring(mleVerifyStatus.lastIndexOf(":") + 1));
                } else {
                    log.error("Error during verification of MLE. Details: {}. So, defaulting to creation of new MLEs.", mleVerifyObj.errorMessage);
                }

                // If either the BIOS or VMM or both already exists, then we should not be creating them again even if the user has requested for
                // as it will create duplicates and leads to confusion.
                // The reason we are updating the hostConfigObj flags is because when the attestation report is getting uploaded to the
                // DB, these flags are checked before making the DB transaction.
                if (hostConfigObj.addBiosWhiteList() && biosMLEExists) {
                    hostConfigObj.setBiosWhiteList(false);
                    log.info("BIOS MLE already exists with the matching whitelist (name might be different). So, now new BIOS MLE would be created.");
                }
                if (hostConfigObj.addVmmWhiteList() && vmmMLEExists) {
                    hostConfigObj.setVmmWhiteList(false);
                    log.info("VMM MLE already exists with the matching whitelist (name might be different). So, now new VMM MLE would be created.");
                }

                // Now that we have retrieved the details of the host, let us configure the BIOS MLE if needed
                if (hostConfigObj.addBiosWhiteList()) {
                    configureBIOSMLE(hostConfigObj);
                }

                if (hostConfigObj.addVmmWhiteList()) {
                    configureVMMMLE(hostConfigObj);
                }

                log.debug("TIMETAKEN: for configuring MLES : {}", (System.currentTimeMillis() - configWLStart));

                // Finally store the attestation report 
                uploadToDB(hostConfigObj, attestationReport);
                log.debug("Successfully updated the white list database with the good known white list from host: {}", gkvHost.HostName);

                log.debug("TIMETAKEN: for uploading to DB: {}", (System.currentTimeMillis() - configWLStart));

                // For hosts that have support for attesting application and data on OS, we would get this measurementXml from the host.
                // We will store it as it in the mw_measurementXml table and use it during attestation for verification.   
                // We need to store the measurement log only if the VMM MLE is also being created since the final value of the measurement
                // log would be extended to PCR 19
                if (hostConfigObj.addVmmWhiteList()) {
                    String measurementXmlLog = hostAttReportObj.getMeasurementXmlLog();
                    if (measurementXmlLog == null || measurementXmlLog.isEmpty()) {
                        log.info("ConfigureWhiteListFromCustomData: No measurement xml log found on the host.");
                    } else {
                        configureMeasurementXmlLog(hostConfigObj, measurementXmlLog);
                        log.debug("ConfigureWhiteListFromCustomData: Found the following measurement xml log on the host. {}");
                    }
                }
                // Register host only if required.
                /*if (hostConfigObj.isRegisterHost() == true) {
                    com.intel.mtwilson.as.business.HostBO hostbo = new com.intel.mtwilson.as.business.HostBO();
                    // First let us check if the host is already configured. If yes, we will return back success
                    TblHosts hostSearchObj = hostsJpaController.findByName(gkvHost.HostName);
                    if (hostSearchObj == null) {
                        log.debug("Could not find the host {}", gkvHost.HostName);
                        log.debug("Creating a new host.");

                        TxtHost hostObj = new TxtHost(gkvHost);
                        hostbo.addHost(hostObj, null, null, null);
                        log.info("Successfully registered the host : {}", hostObj.getHostName());

                    } else {
                        log.debug("Database already has the configuration details for host : {}", hostSearchObj.getName());
                        // Since we might have changed the MLE configuration on the host, let us update the host
                        if (gkvHost.Port == null) {
                            gkvHost.Port = 0;
                        }
                        TxtHost newHostObj = new TxtHost(gkvHost);
                        hostbo.updateHost(newHostObj, null, null, null);
                        log.info("Successfully updated the host {} with the new MLE information.", gkvHost.HostName);
                    }
                }

                log.debug("TIMETAKEN: for registering host: {} ", (System.currentTimeMillis() - configWLStart));*/

                // Now we need to configure the MleSource table with the details of the host that was used for white listing the MLE.
                if (hostConfigObj.addBiosWhiteList()) {
                    configureMleSource(gkvHost, true);
                    log.debug("Successfully configured the details of the host that was used to white list the BIOS MLE - {}.", gkvHost.BIOS_Name);
                }

                if (hostConfigObj.addVmmWhiteList()) {
                    configureMleSource(gkvHost, false);
                    log.debug("Successfully configured the details of the host that was used to white list the VMM MLE - {}.", gkvHost.VMM_Name);
                }

                log.debug("TIMETAKEN: for configuring MLE source: {}", (System.currentTimeMillis() - configWLStart));

                configStatus = true;
            }
        } catch (MSException | ASException me) {
            log.error("Error during white list configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            log.error("Unexpected error during white list configuration. ", ex);
            throw new MSException(ErrorCode.MS_WHITELIST_CONFIG_ERROR, ex.getClass().getSimpleName());
        }

        return configStatus;
    }

    private void setAikForHost(TblHosts tblHosts, HostAgent agent) {
        if (agent.isAikAvailable()) {
            if (agent.isAikCaAvailable()) {
                X509Certificate cert = agent.getAikCertificate();
                try {
                    String certPem = X509Util.encodePemCertificate(cert);
                    tblHosts.setAIKCertificate(certPem);
                    tblHosts.setAikPublicKey(RsaUtil.encodePemPublicKey(cert.getPublicKey())); // NOTE: we are getting the public key from the cert, NOT by calling agent.getAik() ... that's to ensure that someone doesn't give us a valid certificate and then some OTHER public key that is not bound to the TPM
                    tblHosts.setAikSha1(Sha1Digest.valueOf(cert.getEncoded()).toString());
                    tblHosts.setAikPublicKeySha1(Sha1Digest.valueOf(cert.getPublicKey().getEncoded()).toString());
                } catch (Exception e) {
                    log.error("Cannot encode AIK certificate: " + e.toString(), e);
                }
            } else {
                PublicKey publicKey = agent.getAik();
                String pem = RsaUtil.encodePemPublicKey(publicKey);
                tblHosts.setAIKCertificate(null);
                tblHosts.setAikPublicKey(pem);
                tblHosts.setAikSha1(null);
                tblHosts.setAikPublicKeySha1(Sha1Digest.valueOf(publicKey.getEncoded()).toString());
            }
        }
    }

    /**
     * Author: Savino 11/13/2013
     *
     * Verifies that the attestation report is valid.
     *
     * @param attestationReport: String in XML format of the attestation report.
     * @param reqdPCRs: String of required PCR values separated by commas ",".
     * @return : True if all required PCRs and modules do not match the regular expression format [0-9A-Fa-f]{40} or [0]{40}|[Ff]{40}.
     */
    private Boolean validateAttestationReport(String attestationReport, String reqdPCRs) throws XMLStreamException {
        List<String> reqdManifestList = Arrays.asList(reqdPCRs.split(","));
        XMLInputFactory xif = XMLInputFactory.newInstance();
        StringReader sr = new StringReader(attestationReport);
        XMLStreamReader reader = xif.createXMLStreamReader(sr);

        // Process all the Event and PCR nodes in the attestation report.
        while (reader.hasNext()) {
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equalsIgnoreCase("Host_Attestation_Report")) {
                } else if (reader.getLocalName().equalsIgnoreCase("EventDetails")) {
                    // Check if the package is a dynamic package. If it is, then we should not be storing it in the database
                    if (reader.getAttributeValue("", "PackageName").length() == 0
                            && reader.getAttributeValue("", "EventName").equalsIgnoreCase("Vim25Api.HostTpmSoftwareComponentEventDetails")) {
                        reader.next();
                        continue;
                    }

                    String compName = reader.getAttributeValue("", "ComponentName");
                    String pcrNum = reader.getAttributeValue("", "ExtendedToPCR");
                    String digVal = reader.getAttributeValue("", "DigestValue");
                    String algBank = reader.getAttributeValue("", "DigestAlgorithm");

                    if (reqdManifestList.contains(pcrNum)) {
                        if (!isComponentValid(algBank, digVal)) {
                            log.error("Module '{}' specified for '{}' is not valid.", digVal, compName);
                            return false;
                        }
                    }
                } else if (reader.getLocalName().equalsIgnoreCase("PCRInfo")) {
                    String pcrNum = reader.getAttributeValue("", "ComponentName");
                    String digVal = reader.getAttributeValue(null, "DigestValue");
                    String algBank = reader.getAttributeValue("", "DigestAlgorithm");

                    if (reqdManifestList.contains(pcrNum)) {
                        if (!isComponentValid(algBank, digVal)) {
                            log.error("PCR '{}' specified for '{}' is not valid.", digVal, pcrNum);
                            return false;
                        }
                    }
                }
            }
            reader.next();
        }

        return true;
    }

    private Boolean isComponentValid(String pcrBank, String modValue) {
        String hexadecimalRegEx = "[0-9A-Fa-f]+";
        String invalidWhiteList = "[0]+|[Ff]+";

        int expectedSize = (pcrBank != null && "SHA256".equalsIgnoreCase(pcrBank)) ? 32 * 2 : 20 * 2;

        if (modValue == null || modValue.trim().isEmpty()) {
            return true;
        } // we allow empty values because in mtwilson 1.2 they are used to indicate dynamic information, for example vmware pcr 19, and the command line event that is extended into vmware pcr 19
        // Bug:775 & 802: If the TPM is reset we have seen that all the PCR values would be set to Fs. So, we need to disallow that since it is invalid. Also, all 0's are also invalid.

        if (modValue.length() != expectedSize) {
            return false;
        }

        if (modValue.matches(invalidWhiteList)) {
            return false;
        }
        if (modValue.matches(hexadecimalRegEx)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Author: Sudhir
     *
     * Configures the BIOS MLE using the Good known host specified in the WhiteListConfiguration object.
     *
     * @param apiClientObj : ApiClient object for making the ApiClient calls.
     * @param hostConfigObj : WhiteListConfiguration object having the details of the Host and how the white list should be configured.
     * @return : A boolean indicating if the BIOS MLE was created or it already existed.
     */
    private void configureBIOSMLE(WhitelistConfigurationData hostConfigObj) {

        try {
            // Extract the host object
            if (hostConfigObj != null) {
                TblOemJpaController oemJpa = My.jpa().mwOem();
                TblMleJpaController mleJpa = My.jpa().mwMle();

                TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();

                // If the user has provided the name, let us use it.
                if (hostConfigObj.getBiosMleName() != null && !hostConfigObj.getBiosMleName().isEmpty()) {
                    hostObj.BIOS_Name = hostConfigObj.getBiosMleName();
                }

                // Bug: 957 Need to change the MLE file name only if the user has requested for it. Otherwise we will update the
                // white list of the existing MLE
                if (!hostConfigObj.getOverWriteWhiteList()) {
                    hostObj.BIOS_Name = getNextAvailableBIOSMLEName(hostObj.BIOS_Name, hostObj.BIOS_Version, hostObj.BIOS_Oem);
                }

                TblOem oemTblObj = oemJpa.findTblOemByName(hostObj.BIOS_Oem);

                // Create the OEM if it does not exist
                if (oemTblObj == null) {
                    OemData oemObj = new OemData(hostObj.BIOS_Oem, hostObj.BIOS_Oem);
                    new OemBO().createOem(oemObj, null);
                    log.debug("Successfully created the OEM : " + hostObj.BIOS_Oem);
                } else {
                    log.debug("Database already has the configuration details for OEM : " + hostObj.BIOS_Oem);
                }

                boolean useDaMode = hostConfigObj.getTxtHostRecord().getDaMode();

                // Create the BIOS MLE for the host. 
                MleData mleObj = new MleData();
                mleObj.setName(hostObj.BIOS_Name);
                mleObj.setVersion(hostObj.BIOS_Version);
                mleObj.setAttestationType(useDaMode ? "MODULE" : "PCR");
                mleObj.setMleType("BIOS");
                mleObj.setDescription("");
                mleObj.setOsName("");
                mleObj.setOsVersion("");
                mleObj.setOemName(hostObj.BIOS_Oem);
                mleObj.setTarget_type(hostConfigObj.getBiosWLTarget().getValue());
                switch (hostConfigObj.getBiosWLTarget()) {
                    case BIOS_OEM:
                        mleObj.setTarget_value(hostObj.BIOS_Oem);
                        break;
                    case BIOS_HOST:
                        mleObj.setTarget_value(hostObj.HostName);
                        break;
                    default:
                        mleObj.setTarget_value("");
                }

                // Now we need to create empty manifests for all the BIOS PCRs that need to be verified. 
                String biosPCRs = hostConfigObj.getBiosPCRs();
                if (biosPCRs.isEmpty()) {
                    biosPCRs = BIOS_PCRs;
                }
                String[] biosPCRList = biosPCRs.split(",");

                List<ManifestData> biosMFList = new ArrayList<>();
                for (String biosPCR : biosPCRList) {
                    if(hostObj.PcrBanks != null && !hostObj.PcrBanks.isEmpty()) {
                        List<String> banks = Arrays.asList(hostObj.PcrBanks.split(" "));
                        for(String bank : banks) {
                            biosMFList.add(new ManifestData(biosPCR, "", bank));
                        }
                    } else {
                        biosMFList.add(new ManifestData(biosPCR, "", hostObj.getBestPcrAlgorithmBank()));
                    }
                }

                mleObj.setManifestList(biosMFList);

                // If the MLE does not exist, then let us create it.
                TblMle tblMleObj = mleJpa.findBiosMle(hostObj.BIOS_Name, hostObj.BIOS_Version, hostObj.BIOS_Oem);
                if (tblMleObj == null) {
                    new MleBO().addMLe(mleObj, null);
                    log.debug("Successfully created the BIOS MLE : " + hostObj.BIOS_Name);

                } else {
                    log.debug("Database already has the configuration details for BIOS MLE : " + hostObj.BIOS_Name);
                    if (hostConfigObj.getOverWriteWhiteList()) {
                        log.debug("Details of MLE - {} information would be updated", hostObj.BIOS_Name);
                        new MleBO().updateMle(mleObj, tblMleObj.getUuid_hex());
                    }
                }
            }
        } catch (MSException me) {
            log.error("Error during OEM - BIOS MLE configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            log.error("Unexpected errror during OEM - BIOS MLE configuration. ", ex);
            throw new MSException(ErrorCode.MS_BIOS_MLE_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Configures the Hypervisor(VMM) MLE using the Good known host specified in the WhiteListConfiguration object.
     *
     * @param apiClientObj : ApiClient object for making the ApiClient calls.
     * @param hostConfigObj : WhiteListConfiguration object having the details of the Host and how the white list should be configured.
     * @return : A boolean indicating if the VMM MLE was created or it already existed.
     */
    private void configureVMMMLE(WhitelistConfigurationData hostConfigObj) {
        String attestationType;
        try {
            if (hostConfigObj != null) {
                TblOsJpaController osJpa = My.jpa().mwOs();
                TblMleJpaController mleJpa = My.jpa().mwMle();

                TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();

                // If the user has provided the name, let us use it.
                if (hostConfigObj.getVmmMleName() != null && !hostConfigObj.getVmmMleName().isEmpty()) {
                    hostObj.VMM_Name = hostConfigObj.getVmmMleName();
                }

                // Bug: 957 Need to change the MLE file name only if the user has requested for it. Otherwise we will update the
                // white list of the existing MLE
                if (!hostConfigObj.getOverWriteWhiteList()) {
                    hostObj.VMM_Name = getNextAvailableVMMMLEName(hostObj.VMM_Name, hostObj.VMM_Version, hostObj.VMM_OSName, hostObj.VMM_OSVersion);
                }

                ConnectionString connString = new ConnectionString(hostObj.AddOn_Connection_String);
                if (connString.getVendor() == Vendor.CITRIX) {
                    attestationType = "PCR";
                } else {
                    attestationType = "MODULE";
                }

                TblOs tblOsObj = osJpa.findTblOsByNameVersion(hostObj.VMM_OSName, hostObj.VMM_OSVersion);
                if (tblOsObj == null) {
                    OsData osObj = new OsData(hostObj.VMM_OSName, hostObj.VMM_OSVersion, "");
                    new OsBO().createOs(osObj, null);
                    log.info("Successfully created the OS : " + hostObj.VMM_OSName);
                } else {
                    log.info("Database already has the configuration details for the OS : " + hostObj.VMM_OSName);
                }

                // Create the VMM MLE
                MleData mleVMMObj = new MleData();
                mleVMMObj.setName(hostObj.VMM_Name);
                mleVMMObj.setVersion(hostObj.VMM_Version);
                mleVMMObj.setAttestationType(attestationType);
                mleVMMObj.setMleType("VMM");
                mleVMMObj.setDescription("");
                mleVMMObj.setOsName(hostObj.VMM_OSName);
                mleVMMObj.setOsVersion(hostObj.VMM_OSVersion);
                mleVMMObj.setOemName("");
                mleVMMObj.setTarget_type(hostConfigObj.getVmmWLTarget().getValue());
                switch (hostConfigObj.getVmmWLTarget()) {
                    case VMM_OEM:
                        mleVMMObj.setTarget_value(hostObj.BIOS_Oem);
                        break;
                    case VMM_HOST:
                        mleVMMObj.setTarget_value(hostObj.HostName);
                        break;
                    case VMM_GLOBAL:
                    default:
                        mleVMMObj.setTarget_value("");
                }

                // Let us create the dummy manifests now. We will update it later after
                // host registration. NOTE: We should not add PCR19 into the required manifest
                // list if the host is ESXi 5.0 as it will change. 
                String vmmPCRs = hostConfigObj.getVmmPCRs();
                String[] vmmPCRList = vmmPCRs.split(",");

                List<ManifestData> vmmMFList = new ArrayList<>();
                for (String vmmPCR : vmmPCRList) {
                    if(hostObj.PcrBanks != null && !hostObj.PcrBanks.isEmpty()) {
                        List<String> banks = Arrays.asList(hostObj.PcrBanks.split(" "));
                        for (String bank : banks) {
                            vmmMFList.add(new ManifestData(vmmPCR, "", bank));
                        }   
                    } else {
                        vmmMFList.add(new ManifestData(vmmPCR, "", hostObj.getBestPcrAlgorithmBank())); // whitelist service now allows empty pcr's 
                    }                    
                }

                mleVMMObj.setManifestList(vmmMFList);

                TblMle tblMleObj = mleJpa.findVmmMle(hostObj.VMM_Name, hostObj.VMM_Version, hostObj.VMM_OSName, hostObj.VMM_OSVersion);
                if (tblMleObj == null) {
                    new MleBO().addMLe(mleVMMObj, null);
                    log.debug("Successfully created the VMM MLE : " + hostObj.VMM_Name);

                    // NOTE: In order to avoid the look up queries to decide whether to do a add or a update
                    // to the white lists, we will use the overwrite flag. If the overwrite flag is set to false
                    // then, we will do a "ADD" operation. If the overwrite flag is set to "TRUE", then we do an
                    // "UPDATE" operation. But users can set the overwrite flag to "TRUE" for MLEs that dont exist.
                    // So, here we know that the VMM MLE does not exist since we just created a new one. So, if
                    // the overwrite flag is set, we will unset it here.
                    if (hostConfigObj.getOverWriteWhiteList()) {
                        hostConfigObj.setOverWriteWhiteList(false);
                    }

                } else {
                    log.debug("Database already has the configuration details for VMM MLE : " + hostObj.VMM_Name);
                    if (hostConfigObj.getOverWriteWhiteList()) {
                        log.debug("Details of MLE - {} information would be updated", hostObj.VMM_Name);
                        new MleBO().updateMle(mleVMMObj, tblMleObj.getUuid_hex());

                        // Now that we have updated the PCR manifest list, let us clear the modules so that if the host
                        // from which we are updating the whitelist has fewer modules, then we would not have any older 
                        // modules left behind from the previous host.
                        deleteModulesForMLE(hostConfigObj);
                    }

                }
            }
        } catch (MSException me) {

            log.error("Error during OS - VMM MLE configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            log.error("Error during OS - VMM MLE configuration. ", ex);
            throw new MSException(ErrorCode.MS_VMM_MLE_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Configures the measurement xml log for the host if it exists. During host attestation even this log would be verified. This log had additional applications/data that we want to extend the chain of trust to.
     *
     * Sample format of the log would like:
     * <Measurements xmlns="mtwilson:trustdirector:measurements:1.1" DigestAlg="sha256">
     * <Dir Path="/boot">296fae22949de5e16c75e0fef135e6346b0e125768d72f7f30e25662e9b114a3</Dir>
     * <File Path="/boot/grub/stage1">77c1024a494c2170d0236dabdb795131d8a0f1809792735b3dd7f563ef5d951e</File>
     * <File Path="/boot/grub/e2fs_stage1_5">1d317c1e94328cdbe00dc05d50b02f0cb9ec673159145b7f4448cec28a33dc14</File>
     * <File Path="/boot/grub/stage2">5aa718ea1ecc59140eef959fc343f8810e485a44acc35805a0f6e9a7ffb10973</File>
     * <File Path="/boot/grub/menu.lst">0a1cfafd98f3f87dc079d41b9fe1391dcac8a41badf2d845648f95fe0edcd6c4</File>
     * <File Path="/boot/initrd.img-3.0.0-12-virtual">683972bff3c4d3d69f25504a2ca0a046772e21ebba4c67e4b857f4061e3cb143</File>
     * <File Path="/boot/config-3.0.0-12-virtual">2be73211f10b30c5d2705058d4d4991d0108b3b787578145a7e8dfb740b7c232</File>
     * <File Path="/boot/vmlinuz-3.0.0-12-virtual">fd844dea53352d5165a056bbb0f1af5af195600545de601c824decd5a30d3c49</File>
     * <Dir Path="/path/to/directory" Include="^include.regex.here$" Exclude="^exclude.regex.here$">da39a3ee5e6b4b0d3255bfef95601890afd80709</Dir>
     * </Measurements>
     *
     * @param hostConfigObj
     * @param measurementXmlLog
     */
    private void configureMeasurementXmlLog(HostConfigData hostConfigObj, String measurementXmlLog) {
        try {
            if (measurementXmlLog != null && !measurementXmlLog.isEmpty()) {
                MwMeasurementXmlJpaController mxJpa = My.jpa().mwMeasurementXml();
                TblMleJpaController mleJpa = My.jpa().mwMle();

                TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();

                TblMle tblMleObj = mleJpa.findVmmMle(hostObj.VMM_Name, hostObj.VMM_Version, hostObj.VMM_OSName, hostObj.VMM_OSVersion);
                if (tblMleObj == null) {
                    log.error("Cannot find the MLE with name {} and version {} for updating the measurement xml log.", hostObj.VMM_Name, hostObj.VMM_Version);
                    throw new MSException(ErrorCode.MS_VMM_MLE_NOT_FOUND);
                }

                MwMeasurementXml measurementXml = mxJpa.findByMleId(tblMleObj.getId());
                if (measurementXml == null) {
                    measurementXml = new MwMeasurementXml();
                    measurementXml.setId(new UUID().toString());
                    measurementXml.setMleId(tblMleObj);
                    measurementXml.setContent(measurementXmlLog);

                    mxJpa.create(measurementXml);
                    log.debug("Succesfully added the measurement xml log for Mle {}", tblMleObj.getName());

                } else {
                    measurementXml.setContent(measurementXmlLog);
                    mxJpa.edit(measurementXml);
                    log.debug("Succesfully updated the measurement xml log for Mle {}", tblMleObj.getName());
                }
            }
        } catch (MSException me) {
            log.error("Error during measurement xml log configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            log.error("Error during measurement xml log configuration. ", ex);
            throw new MSException(ErrorCode.MS_VMM_MLE_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     *
     * @param apiClientObj
     * @param hostObj
     * @param isBIOSMLE
     */
    private void configureMleSource(TxtHostRecord hostObj, Boolean isBIOSMLE) {
        MleSource mleSourceObj = new MleSource();
        MleData mleDataObj = new MleData();
        try {

            if (isBIOSMLE) {
                mleDataObj.setName(hostObj.BIOS_Name);
                mleDataObj.setVersion(hostObj.BIOS_Version);
                mleDataObj.setOemName(hostObj.BIOS_Oem);
                mleDataObj.setOsName("");
                mleDataObj.setOsVersion("");
                mleDataObj.setMleType(MleData.MleType.BIOS.toString());

            } else {
                mleDataObj.setName(hostObj.VMM_Name);
                mleDataObj.setVersion(hostObj.VMM_Version);
                mleDataObj.setOemName("");
                mleDataObj.setOsName(hostObj.VMM_OSName);
                mleDataObj.setOsVersion(hostObj.VMM_OSVersion);
                mleDataObj.setMleType(MleData.MleType.VMM.toString());
            }

            // Because of the way MleData works it needs the MleType and AttestationType data when
            // retrieving the contents from it. So, we are just populating it with PCR. This value
            // will not be used during the creation of MleSource mapping.
            mleDataObj.setAttestationType(MleData.AttestationType.PCR.toString());
            mleSourceObj.setMleData(mleDataObj);
            if (hostObj.HostName != null && !hostObj.HostName.isEmpty()) {
                mleSourceObj.setHostName(hostObj.HostName);
            }

            log.debug("Host details for MLE white list host mapping are: " + hostObj.HostName + ":" + hostObj.HostName);
            // Since this function would be called during both creation and updation, we need to handle both the scenarios.
            try {
                new MleBO().addMleSource(mleSourceObj, null, null);
            } catch (ASException aex) {
                if (aex.getErrorCode().equals(ErrorCode.WS_MLE_SOURCE_MAPPING_ALREADY_EXISTS)) {
                    log.warn("Mapping already exists for the MLE white list host for MLE: " + mleSourceObj.getHostName());
                    // Since the mapping already exists, it means that the user is updating the white list. So, let us call the update method
                    new MleBO().updateMleSource(mleSourceObj, null);
                } else {
                    throw new MSException(ErrorCode.MS_API_EXCEPTION, aex.getErrorCode().toString()
                            + ": Error during MLE white list host mapping. " + aex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.error("Error during MLE white list host mapping. ", ex);
            throw new MSException(ErrorCode.MS_MLE_WHITELIST_HOST_MAPPING_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Deletes all the modules except for host specific ones from the VMM MLE specified.
     *
     * @param hostConfigObj
     */
    private void deleteModulesForMLE(HostConfigData hostConfigObj) {

        try {

            // We need to delete all the modules only if the overwrite flag is set. 
            if (hostConfigObj.getOverWriteWhiteList() && hostConfigObj.addVmmWhiteList()) {

                TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();

                TblMle tblMle = My.jpa().mwMle().findVmmMle(hostObj.VMM_Name, hostObj.VMM_Version, hostObj.VMM_OSName, hostObj.VMM_OSVersion);
                if (tblMle != null) {
                    // Retrieve the list of all the modules for the specified VMM MLE.
                    List<TblModuleManifest> moduleList = My.jpa().mwModuleManifest().findTblModuleManifestByMleUuid(tblMle.getUuid_hex());
                    if (moduleList != null && moduleList.size() > 0) {
                        for (TblModuleManifest moduleObj : moduleList) {
                            if (moduleObj.getUseHostSpecificDigestValue()) // we cannot delete the host specific one since it would be referenced by the Hosts
                            {
                                continue;
                            }
                            My.jpa().mwModuleManifest().destroy(moduleObj.getId());
                        }
                    }
                }

            }

        } catch (IOException | IllegalOrphanException | NonexistentEntityException ex) {
            log.error("Error during the deletion of VMM modules {}. ", hostConfigObj.getTxtHostRecord().VMM_Name, ex);
            throw new MSException(ErrorCode.WS_MODULE_WHITELIST_DELETE_ERROR, ex.getClass().getSimpleName());
        }

    }

    /**
     * Uploads the attestation report (event info and pcr info) into the White List Database for the specified good known host.
     *
     * @param hostConfigObj : WhiteList Configuration object having the details of the host, which would be used as the good known host
     * @param attestationReport : XML report having all the details of the good known measurements that need to be uploaded to the database.
     * @param apiClientObj: ApiClient object.
     *
     */
    private void uploadToDB(HostConfigData hostConfigObj, String attestationReport) throws IOException {

        TblPcrManifestJpaController pcrJpa = My.jpa().mwPcrManifest();
        TblMleJpaController mleJpa = My.jpa().mwMle();
        MleBO mleBO = new MleBO();

        // NOTE: In order to avoid the look up queries to decide whether to do a add or a update
        // to the white lists, we will use the overwrite flag. If the overwrite flag is set to false
        // then, we will do a "ADD" operation. If the overwrite flag is set to "TRUE", then we do an
        // "UPDATE" operation. We have already modified this flag during the creation of VMM MLE to address
        // the scenario of user's setting the flag to "TRUE" even if the MLE does not exist.
        //TO REVIEW: Should we even move this whitelisting functionality to the HostAgents. Right now we have specific things for
        // each different type of of hosts.
        EntityManager emt = My.jpa().mwModuleManifest().getEntityManager();
        emt.getTransaction().begin();

        // If in case we need to support additional pcrs for event logs, we need to just update this and add the new PCR
        List<Integer> pcrsSupportedForEventLog = Arrays.asList(17, 19);
        // Since the attestation report has all the PCRs we need to upload only the required PCR values into the white list tables.
        // Location PCR (22) is added by default. We will check if PCR 22 is configured or not. If the digest value for PCR 22 exists, then
        // we will configure the location table as well.

        //define a local int variable for location since different host/OS may use different PCR - hxia
        TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();

        int locationPCR = LOCATION_PCR;
        if (hostObj.VMM_OSName.toLowerCase().contains("windows")) {
            locationPCR = LOCATION_PCR_WINDOWS;
        }
        log.debug("locationPCR: " + locationPCR);

        //List<String> pcrsToWhiteList = Arrays.asList((hostConfigObj.getBiosPCRs() + "," + hostConfigObj.getVmmPCRs() + "," + "22").split(","));
        List<String> pcrsToWhiteList = Arrays.asList((hostConfigObj.getBiosPCRs() + "," + hostConfigObj.getVmmPCRs() + "," + Integer.toString(locationPCR)).split(","));

        List<String> biosPCRList = Arrays.asList(hostConfigObj.getBiosPCRs().split(","));
        List<String> vmmPCRList = Arrays.asList(hostConfigObj.getVmmPCRs().split(","));

        try {

            //TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();
            log.info("Starting the white list upload to database");

            XMLInputFactory xif = XMLInputFactory.newInstance();
            StringReader sr = new StringReader(attestationReport);
            XMLStreamReader reader = xif.createXMLStreamReader(sr);

            TblMle mleSearchObj = mleJpa.findVmmMle(hostObj.VMM_Name, hostObj.VMM_Version, hostObj.VMM_OSName, hostObj.VMM_OSVersion);
            TblMle mleBiosSearchObj = mleJpa.findBiosMle(hostObj.BIOS_Name, hostObj.BIOS_Version, hostObj.BIOS_Oem);
            // Process all the Event and PCR nodes in the attestation report.
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equalsIgnoreCase("Host_Attestation_Report")) {
                    } else if (reader.getLocalName().equalsIgnoreCase("EventDetails")) {

                        // Check if the package is a dynamic package. If it is, then we should not be storing it in the database
                        if (reader.getAttributeValue("", "PackageName").length() == 0
                                && reader.getAttributeValue("", "EventName").equalsIgnoreCase("Vim25Api.HostTpmSoftwareComponentEventDetails")) {
                            reader.next();
                            continue;
                        }

                        // currently event details would be available for all host except for Citrix Xen. Also we should not process the event log information for all the PCRs. We just 
                        // need to do it for PCR 19
                        // We also process PCR 17 for TPM 2.0 servers
                        // Bug#: 768: We need to process the modules only if the user has requested for verifying that PCR. If not, we should not process PCR 19 at all.
                        if ((pcrsSupportedForEventLog.contains(Integer.parseInt(reader.getAttributeValue("", "ExtendedToPCR"))))
                                && (pcrsToWhiteList.contains(reader.getAttributeValue("", "ExtendedToPCR")))) {

                            // only support EventLog for PCR 17 if it's TPM 2.0
                            int pcr = Integer.parseInt(reader.getAttributeValue("", "ExtendedToPCR"));                            
                            boolean useDaMode = hostConfigObj.getTxtHostRecord().getDaMode();
                            ModuleWhiteList moduleObj = new ModuleWhiteList();
                            if (pcr == 17 && useDaMode) {// bug 2013-02-04 inserting the space here worked with mysql because mysql automatically trims spaces in queries but other database systems DO NOT;  it's OK for componentName to be empty string but somewhere else we have validation check and throw an error if it's empty
                                if (reader.getAttributeValue("", "ComponentName").isEmpty()) {
                                    moduleObj.setComponentName(" ");
                                    log.info("uploadToDB: component name set to single-space");
                                } else {
                                    moduleObj.setComponentName(reader.getAttributeValue("", "ComponentName")); // it could be empty... see TestVmwareEsxi51.java in AttestationService/src/test/java to see how this can be easily handled using the vendor-specific classes, where the vmware implementation automatically sets component name to something appropriate
                                }
                                moduleObj.setDigestValue(reader.getAttributeValue("", "DigestValue"));
                                moduleObj.setPcrBank(reader.getAttributeValue("", "DigestAlgorithm"));
                                if(moduleObj.getPcrBank() == null) {
                                    moduleObj.setPcrBank("SHA1");
                                }
                                moduleObj.setEventName(reader.getAttributeValue("", "EventName"));
                                moduleObj.setExtendedToPCR(reader.getAttributeValue("", "ExtendedToPCR"));
                                moduleObj.setPackageName(reader.getAttributeValue("", "PackageName"));
                                moduleObj.setPackageVendor(reader.getAttributeValue("", "PackageVendor"));
                                moduleObj.setPackageVersion(reader.getAttributeValue("", "PackageVersion"));
                                moduleObj.setUseHostSpecificDigest(Boolean.valueOf(reader.getAttributeValue("", "UseHostSpecificDigest")));
                                moduleObj.setDescription("");
                                
                                if(moduleObj.getUseHostSpecificDigest()) {
                                    moduleObj.setMleName(hostObj.VMM_Name);
                                    moduleObj.setMleVersion(hostObj.VMM_Version);
                                    moduleObj.setOsName(hostObj.VMM_OSName);
                                    moduleObj.setOsVersion(hostObj.VMM_OSVersion);
                                    moduleObj.setOemName("");
                                } else {
                                    moduleObj.setMleName(hostObj.BIOS_Name);
                                    moduleObj.setMleVersion(hostObj.BIOS_Version);
                                    //moduleObj.setOsName(hostObj.VMM_OSName);
                                    //moduleObj.setOsVersion(hostObj.VMM_OSVersion);
                                    moduleObj.setOemName(hostObj.BIOS_Oem);
                                }
                            } else if(pcr == 19 && !useDaMode) {
                                if (reader.getAttributeValue("", "ComponentName").isEmpty()) {
                                    moduleObj.setComponentName(" ");
                                    log.info("uploadToDB: component name set to single-space");
                                } else {
                                    moduleObj.setComponentName(reader.getAttributeValue("", "ComponentName")); // it could be empty... see TestVmwareEsxi51.java in AttestationService/src/test/java to see how this can be easily handled using the vendor-specific classes, where the vmware implementation automatically sets component name to something appropriate
                                }
                                moduleObj.setDigestValue(reader.getAttributeValue("", "DigestValue"));
                                moduleObj.setPcrBank(reader.getAttributeValue("", "DigestAlgorithm"));
                                if(moduleObj.getPcrBank() == null) {
                                    moduleObj.setPcrBank("SHA1");
                                }
                                moduleObj.setEventName(reader.getAttributeValue("", "EventName"));
                                moduleObj.setExtendedToPCR(reader.getAttributeValue("", "ExtendedToPCR"));
                                moduleObj.setPackageName(reader.getAttributeValue("", "PackageName"));
                                moduleObj.setPackageVendor(reader.getAttributeValue("", "PackageVendor"));
                                moduleObj.setPackageVersion(reader.getAttributeValue("", "PackageVersion"));
                                moduleObj.setUseHostSpecificDigest(Boolean.valueOf(reader.getAttributeValue("", "UseHostSpecificDigest")));
                                moduleObj.setDescription("");
                                moduleObj.setMleName(hostObj.VMM_Name);
                                moduleObj.setMleVersion(hostObj.VMM_Version);
                                moduleObj.setOsName(hostObj.VMM_OSName);
                                moduleObj.setOsVersion(hostObj.VMM_OSVersion);
                                moduleObj.setOemName("");
                            } else {
                                reader.next();
                                continue;
                            }
                            if (!hostConfigObj.getOverWriteWhiteList()) {
                                // add the module if we are to add a new bios or vmm mle
                                boolean addModuleToBios = !moduleObj.getUseHostSpecificDigest() && hostConfigObj.addBiosWhiteList();
                                boolean addModuleToVmm = moduleObj.getUseHostSpecificDigest() && hostConfigObj.addVmmWhiteList();
                                
                                if(addModuleToBios || addModuleToVmm){
                                    mleBO.addModuleWhiteList(moduleObj, emt, null, null);
                                    log.debug("Successfully created a new module manifest for : " + hostObj.VMM_Name + ":" + moduleObj.getComponentName());
                                }
                            } else {
                                try {
                                    mleBO.updateModuleWhiteList(moduleObj, emt, null);
                                    log.debug("Successfully updated the module manifest for : " + hostObj.VMM_Name + ":" + moduleObj.getComponentName());
                                } catch (ASException ae) {
                                    if (ae.getErrorCode() == ErrorCode.WS_MODULE_WHITELIST_DOES_NOT_EXIST) {
                                        mleBO.addModuleWhiteList(moduleObj, emt, null, null);
                                        log.debug("Successfully created a new module manifest for : " + hostObj.VMM_Name + ":" + moduleObj.getComponentName());
                                    }
                                }
                            }

                        }
                    } else if (reader.getLocalName().equalsIgnoreCase("PCRInfo")) { // pcr information would be available for all the hosts.
                        // We need to white list only thos pcrs that were requested by the user. We will ignore the remaining ones
                        if (pcrsToWhiteList.contains(reader.getAttributeValue(null, "ComponentName"))) {
                            TblPcrManifest tblPCR;
                            PCRWhiteList pcrObj = new PCRWhiteList();

                            pcrObj.setPcrName(reader.getAttributeValue(null, "ComponentName"));
                            pcrObj.setPcrDigest(reader.getAttributeValue(null, "DigestValue"));
                            pcrObj.setPcrBank(reader.getAttributeValue(null, "DigestAlgorithm"));
                            if(pcrObj.getPcrBank() == null) {
                                pcrObj.setPcrBank("SHA1");
                            }
                            Integer mleID;

                            if (pcrObj.getPcrName() == null) {
                                log.error("uploadToDB: PCR name is null: " + hostObj.toString());
//                            } else if ((Integer.parseInt(reader.getAttributeValue(null, "ComponentName")) <= MAX_BIOS_PCR)) {
                            } else if (biosPCRList.contains(reader.getAttributeValue(null, "ComponentName"))) {

                                if (hostConfigObj.addBiosWhiteList() == true) {
                                    pcrObj.setMleName(hostObj.BIOS_Name);
                                    pcrObj.setMleVersion(hostObj.BIOS_Version);
                                    pcrObj.setOsName("");
                                    pcrObj.setOsVersion("");
                                    pcrObj.setOemName(hostObj.BIOS_Oem);
                                    mleID = mleBiosSearchObj.getId();

                                    // if we are using TPM 2.0 (soon to be DA mode) we need to clear this pcr so its dynamic
                                    if (hostObj.getDaMode() && pcrObj.getPcrName().equals("17")) {
                                        pcrObj.setPcrDigest("");
                                    }
                                    //log.info(String.format("Adding BiosWhiteList: Name=%s Version=%s OEM=%s mleID=%s",hostObj.BIOS_Name,hostObj.BIOS_Version,hostObj.BIOS_Oem,mleBiosSearchObj.getId().toString()));
                                    tblPCR = pcrJpa.findByMleIdNamePcrBank(mleID, pcrObj.getPcrName(), pcrObj.getPcrBank());
                                    if (tblPCR == null) {
                                        mleBO.addPCRWhiteList(pcrObj, emt, null, null);
                                        log.debug("Successfully created a new BIOS PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());

                                    } else {
                                        mleBO.updatePCRWhiteList(pcrObj, emt, null);
                                        log.debug("Successfully updated the BIOS PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());
                                    }
                                }

                            } else if ((Integer.parseInt(reader.getAttributeValue(null, "ComponentName")) == locationPCR)) {
                                // We will add the location white list only if it is valid. If now we will skip it. Today only VMware supports PCR 22
                                if (!reader.getAttributeValue(null, "DigestValue").equals(Sha1Digest.ZERO.toString())) {
                                    //  Here we need update the location table. Since we won't know what the readable location string for the hash value we will just add it with host  name
                                    TblLocationPcrJpaController locationJpa = My.jpa().mwLocationPcr(); //new TblLocationPcrJpaController((getASEntityManagerFactory()));
                                    TblLocationPcr tblLoc = locationJpa.findTblLocationPcrByPcrValueEx(reader.getAttributeValue(null, "DigestValue"));

                                    if (tblLoc == null) {
                                        tblLoc = new TblLocationPcr();
                                        tblLoc.setLocation(hostObj.HostName);
                                        tblLoc.setPcrValue(reader.getAttributeValue(null, "DigestValue"));

                                        locationJpa.create(tblLoc);
                                        log.info("Created a white list location entry using PCR 22 for location: {} with PCR value: {}.", tblLoc.getLocation(), tblLoc.getPcrValue());
                                    } else {
                                        log.info("White list location entry using PCR 22 for location: {} with PCR value: {} already exists.", tblLoc.getLocation(), tblLoc.getPcrValue());
                                    }
                                }
                            } else if (vmmPCRList.contains(reader.getAttributeValue(null, "ComponentName")) && hostConfigObj.addVmmWhiteList() == true) {
                                //log.info(String.format("Adding VMM white list: Name=%s Version=%s OsName=%s OsVersion=%s mleID=%s", hostObj.VMM_Name,hostObj.VMM_Version,hostObj.VMM_OSName,hostObj.VMM_OSVersion,mleSearchObj.getId().toString()));
                                pcrObj.setMleName(hostObj.VMM_Name);
                                pcrObj.setMleVersion(hostObj.VMM_Version);
                                pcrObj.setOsName(hostObj.VMM_OSName);
                                pcrObj.setOsVersion(hostObj.VMM_OSVersion);
                                pcrObj.setOemName("");
                                mleID = mleSearchObj.getId();

                                // If the vendor is Citrix, then only we need to write the PCR 19. Otherwise we need to null it out. 
                                if (!hostObj.AddOn_Connection_String.toLowerCase().contains("citrix")) {
                                    if (pcrObj.getPcrName() != null && pcrObj.getPcrName().equalsIgnoreCase("19")) {
                                        pcrObj.setPcrDigest("");
                                    }
                                }

                                tblPCR = pcrJpa.findByMleIdNamePcrBank(mleID, pcrObj.getPcrName(), pcrObj.getPcrBank());
                                if (tblPCR == null) {
                                    mleBO.addPCRWhiteList(pcrObj, emt, null, null);
                                    log.debug("Successfully created a new VMM PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());
                                } else {
                                    mleBO.updatePCRWhiteList(pcrObj, emt, null);
                                    log.debug("Successfully updated the VMM PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());
                                }
                            }
                        }
                    }
                }
                reader.next();
            }
            emt.getTransaction().commit();
            emt.close();

        } catch (MSException me) {
            log.error("Error during white list upload to database. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            log.error("Error during white list upload to database. ", ex);
            throw new MSException(ErrorCode.MS_WHITELIST_UPLOAD_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Finds the next available name that can be used for creation of MLE. If the default name is not used, then the function returns back the same, otherwise it adds the next available numeric extension to the default name and returns back to the caller.
     *
     * @param biosName
     * @param biosVersion
     * @param oemName
     * @return
     */
    public String getNextAvailableBIOSMLEName(String biosName, String biosVersion, String oemName) {
        TblMle tblMleObj;
        int counter = 1;
        String tempBIOSName = biosName;

        try {
            TblMleJpaController mleJpa = My.jpa().mwMle();

            while (true) {
                tblMleObj = mleJpa.findBiosMle(tempBIOSName, biosVersion, oemName);
                if (tblMleObj == null) {
                    // Since the MLE name does not exist, then we will use this name to create the BIOS MLE
                    log.info("Sinc no MLE exists with name {}, we will use it to create the new BIOS MLE.", tempBIOSName);
                    return tempBIOSName;
                } else {
                    log.debug("MLE with name {} already exists in the database.", tempBIOSName);
                    tempBIOSName = biosName + "_" + String.format("%03d", counter++);
                }
            }
        } catch (MSException me) {
            log.error("Error during BIOS MLE name generation. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (Exception ex) {
            log.error("Unexpected errror during BIOS MLE name generation. ", ex);
            // throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during BIOS MLE name generation. " + ex.getMessage());
            throw new MSException(ErrorCode.MS_BIOS_MLE_NAME_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * Finds the next available name that can be used for creation of MLE. If the default name is not used, then the function returns back the same, otherwise it adds the next available numeric extension to the default name and returns back to the caller.
     *
     * @param vmmName
     * @param vmmVersion
     * @param osName
     * @param osVersion
     * @return
     */
    public String getNextAvailableVMMMLEName(String vmmName, String vmmVersion, String osName, String osVersion) {
        TblMle tblMleObj;
        int counter = 1;
        String tempVMMName = vmmName;

        try {
            TblMleJpaController mleJpa = My.jpa().mwMle();

            while (true) {
                tblMleObj = mleJpa.findVmmMle(tempVMMName, vmmVersion, osName, osVersion);
                if (tblMleObj == null) {
                    // Since the MLE name does not exist we will use this name to create the VMM MLE
                    log.info("Sinc no MLE exists with name {}, we will use it to create the new VMM MLE.", tempVMMName);
                    return tempVMMName;
                } else {
                    log.debug("MLE with name {} already exists in the database.", tempVMMName);
                    tempVMMName = vmmName + "_" + String.format("%03d", counter++);
                }
            }
        } catch (MSException me) {
            log.error("Error during VMM MLE name generation. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (Exception ex) {
            log.error("Unexpected errror during VMM MLE name generation. ", ex);
            // throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during VMM MLE name generation. " + ex.getMessage());
            throw new MSException(ErrorCode.MS_VMM_MLE_NAME_ERROR, ex.getClass().getSimpleName());
        }
    }

    /**
     * THIS IS A DUPLICATE OF WHAT IS THERE IN ATTESTATION SERVICE HOSTBO.JAVA. IF YOU MAKE ANY CHANGE, PLEASE CHANGE IT IN THE OTHER LOCATION AS WELL.
     *
     * @param hostAikCert
     * @return
     */
    private boolean isAikCertificateTrusted(X509Certificate hostAikCert) {
        log.debug("isAikCertificateTrusted {}", hostAikCert.getSubjectX500Principal().getName());
        // read privacy ca certificate.  if there is a privacy ca list file available (PrivacyCA.pem) we read the list from that. otherwise we just use the single certificate in PrivacyCA.cer (DER formatt)
        HashSet<X509Certificate> pcaList = new HashSet<>();
        List<X509Certificate> privacyCaCerts;
        //#5837: Variable 'pcaListPemFile' was never read after being assigned.
        File pcaListPemFile;
        try {
            pcaListPemFile = ResourceFinder.getFile("PrivacyCA.list.pem");
            try (InputStream privacyCaIn = new FileInputStream(pcaListPemFile)) {
                privacyCaCerts = X509Util.decodePemCertificates(IOUtils.toString(privacyCaIn));
                pcaList.addAll(privacyCaCerts);
                //IOUtils.closeQuietly(privacyCaIn);
                log.info("Added {} certificates from PrivacyCA.list.pem", privacyCaCerts.size());
            } catch (IOException | CertificateException e) {
                // FileNotFoundException: cannot find PrivacyCA.pem
                // CertificateException: error while reading certificates from file
                log.error("Cannot load PrivacyCA.list.pem");
            }
        } catch (FileNotFoundException e) {
            log.debug("Cannot load external certificates from PrivacyCA.list.pem: {}", e.getMessage());
        }
        //#5838: Variable 'pcaPemFile' was never read after being assigned.
        File pcaPemFile;
        try {
            pcaPemFile = ResourceFinder.getFile("PrivacyCA.pem");
            try (InputStream privacyCaIn = new FileInputStream(pcaPemFile)) {
                X509Certificate privacyCaCert = X509Util.decodeDerCertificate(IOUtils.toByteArray(privacyCaIn));
                pcaList.add(privacyCaCert);
                //IOUtils.closeQuietly(privacyCaIn);
                log.info("Added certificate from PrivacyCA.pem");
            } catch (IOException | CertificateException e) {
                // FileNotFoundException: cannot find PrivacyCA.pem
                // CertificateException: error while reading certificate from file
                log.error("Cannot load PrivacyCA.pem", e);
            }
        } catch (FileNotFoundException e) {
            log.debug("Cannot load local certificates from PrivacyCA.pem: {}", e.getMessage());
        }
        boolean validCaSignature = false;
        for (X509Certificate pca : pcaList) {
            try {
                if (Arrays.equals(pca.getSubjectX500Principal().getEncoded(), hostAikCert.getIssuerX500Principal().getEncoded())) {
                    log.debug("Found matching CA: {}", pca.getSubjectX500Principal().getName());
                    pca.checkValidity(hostAikCert.getNotBefore()); // Privacy CA certificate must have been valid when it signed the AIK certificate
                    hostAikCert.verify(pca.getPublicKey()); // verify the trusted privacy ca signed this aik cert.  throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
                    validCaSignature = true;
                    log.debug("Verified CA signature: {}", pca.getSubjectX500Principal().getName());
                    break;
                }
            } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
                log.error("Failed to verify AIK signature with CA", e); // but don't re-throw because maybe another cert in the list is a valid signer
            }
        }
        return validCaSignature;
    }

}
