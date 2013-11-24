/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.business;

import com.intel.mtwilson.*;
import com.intel.mtwilson.agent.*;
import com.intel.mtwilson.api.*;
//import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.controller.MwProcessorMappingJpaController;
import com.intel.mtwilson.as.controller.TblEventTypeJpaController;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblLocationPcrJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.data.MwProcessorMapping;
import java.io.IOException;
import com.intel.mtwilson.as.data.TblEventType;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblLocationPcr;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.util.DataCipher;
import com.intel.mtwilson.crypto.Aes128;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.crypto.RsaCredential;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.datatypes.Vendor;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponse;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.mtwilson.ms.helper.BaseBO;
import com.intel.mtwilson.ms.helper.MSPersistenceManager;
//import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.tls.InsecureTlsPolicy;
import com.intel.mtwilson.util.ResourceFinder;
import com.intel.mtwilson.wlm.business.MleBO;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class HostBO extends BaseBO {

    private static int MAX_BIOS_PCR = 17;
    private static int LOCATION_PCR = 22;
    private static String BIOS_PCRs = "0,17";
    private static String VMWARE_PCRs = "18,19,20";
    private static String OPENSOURCE_PCRs = "18";
    private static String CITRIX_PCRs = "18"; //"17,18";
    
    //private static String propertiesFile = "management-service.properties";
    //Configuration config = ConfigurationFactory.loadConfiguration(propertiesFile);
    Logger log = LoggerFactory.getLogger(getClass().getName());
    MSPersistenceManager mspm = new MSPersistenceManager();
    //private MwKeystoreJpaController keystoreJpa = new MwKeystoreJpaController(mspm.getEntityManagerFactory("ASDataPU"));
//    private MwPortalUserJpaController keystoreJpa;// = My.jpa().mwPortalUser();//new MwPortalUserJpaController(mspm.getEntityManagerFactory("MSDataPU"));
//    private byte[] dataEncryptionKey;
/*
     public static class Aes128DataCipher implements DataCipher {
            private Logger log = LoggerFactory.getLogger(getClass());
            private Aes128 cipher;
            public Aes128DataCipher(Aes128 cipher) { this.cipher = cipher; }
            
            @Override
            public String encryptString(String plaintext) {
                try {
                    return cipher.encryptString(plaintext);
                }
                catch(CryptographyException e) {
                    log.error("Failed to encrypt data", e);
                    return null;
                }
            }

            @Override
            public String decryptString(String ciphertext) {
                try {
                    return cipher.decryptString(ciphertext);
                }
                catch(CryptographyException e) {
                    log.error("Failed to decrypt data", e);
                    return null;
                }
            }
            
        }
    
    public void setDataEncryptionKey(byte[] key) {
                    try {
                        TblHosts.dataCipher = new Aes128DataCipher(new Aes128(key));
                    }
                    catch(CryptographyException e) {
                        log.error("Cannot initialize data encryption cipher", e);
                    }      
    }
    */
    
    
    /**
     * Private class to support multithreading to retrieve the attestation report from the target host.
     */
    private class HostAttReport extends Thread {

        private HostAgent agent;
        private String requiredPCRs;
        private String attestationReport;
        private boolean isError = false;
        private String errorMessage = "";
        
        public HostAttReport(HostAgent agent, String requiredPCRs) {
            this.agent = agent;
            this.requiredPCRs = requiredPCRs;
        }

        @Override
        public void run() {
            if (isError()) {
                return;
            }
            try {
                long threadStart = System.currentTimeMillis();
                 attestationReport = agent.getHostAttestationReport(requiredPCRs);
                 log.debug("TIMETAKEN: by the attestation report thread:" + (System.currentTimeMillis() - threadStart));
            } catch (Throwable te) {
                isError = true;
                attestationReport = null;
                log.debug("Unexpected error from getHostAttestationReport in registerHostFromCustomData: " + te.toString());
                errorMessage = te.getMessage();
            }
        }

        public boolean isError() {
            return isError;
        }

        public String getResult() {
            return attestationReport;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Private class to support multithreading of checking the existence of MLE before creating a new one.
     */
    private class MLEVerify extends Thread {

        private ApiClient apiClient;
        private MLEVerificationRequest mleVerifyObj;
        private String result;
        private boolean isError = false;
        private String errorMessage = "";
        private HostAgent agent;
        private boolean overwriteWhiteList = false;

        public MLEVerify(HostAgent agent, ApiClient apiClient, boolean overwriteWhiteList, MLEVerificationRequest mleVerifyObj) {
            this.apiClient = apiClient;
            this.mleVerifyObj = mleVerifyObj;
            this.agent = agent;
            this.overwriteWhiteList = overwriteWhiteList;
        }

        @Override
        public void run() {
            if (isError()) {
                return;
            }
            try {
                // If the user has chosen to overwrite the white list, then we do need to check anything. Just 
                // return back as not found.
                if (overwriteWhiteList) {
                    result = "BIOS:false|VMM:false";
                } else {
                    long threadStart = System.currentTimeMillis();
                    //result = apiClient.checkMatchingMLEExists(mleVerifyObj);
                    TxtHostRecord hostObj = mleVerifyObj.getHostObj();
                    hostObj.Location = mleVerifyObj.getBiosPCRs() + "|" + mleVerifyObj.getVmmPCRs();
                    result = apiClient.checkMatchingMLEExists(hostObj);
                    /*PcrManifest pcrManifest = agent.getPcrManifest();
                    log.debug("TIMETAKEN: for getting pcr manifest is: " + (System.currentTimeMillis() - threadStart));
                    HostTrustBO hostTrustBO = new HostTrustBO();
                    log.debug("About to call into the checkMatchingMLEExists");
                    result = hostTrustBO.checkMatchingMLEExists(pcrManifest, mleVerifyObj.getHostObj(), 
                            mleVerifyObj.getBiosPCRs(), mleVerifyObj.getVmmPCRs());*/
                    log.debug("TIMETAKEN: by the checkMLE thread:" + (System.currentTimeMillis() - threadStart));
                }

            } catch (MSException e) {
                isError = true;
                result = null;
                errorMessage = e.getErrorMessage();
            } catch (Exception e) {
                isError = true;
                result = null;
                errorMessage = e.getMessage();
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

    private ApiClient createAPIObject() {
        ApiClient rsaApiClient;

        try {
            // Retrieve the required values from the configuration
            String keyAliasName = MSConfig.getConfiguration().getString("mtwilson.api.key.alias");
            log.error("MEOW_001: " + keyAliasName);
            String keyPassword = MSConfig.getConfiguration().getString("mtwilson.api.key.password");
            log.error("MEOW_002: " + keyPassword);
            URL baseURL = new URL(MSConfig.getConfiguration().getString("mtwilson.api.baseurl"));
            log.error("MEOW_003: " + baseURL.toString());

            // stdalex 1/15 jks2db!disk
            MwPortalUser keyTable = My.jpa().mwPortalUser().findMwPortalUserByUserName(keyAliasName);
            log.error("MEOW_004: " + keyTable.getUsername());
            ByteArrayResource keyResource = new ByteArrayResource(keyTable.getKeystore());
            log.error("MEOW_005: " + keyResource.toString());
            SimpleKeystore keystore = new SimpleKeystore(keyResource, keyPassword);
            log.error("MEOW_006: " + keystore.toString());
            RsaCredential credential = keystore.getRsaCredentialX509(keyAliasName, keyPassword);
            log.error("MEOW_007: " + credential.toString());

            Properties prop = new Properties();
            log.error("MEOW_008: ");
            // prop.setProperty("mtwilson.api.ssl.policy", MSConfig.getConfiguration().getString("mtwilson.api.ssl.policy", "TRUST_CA_VERIFY_HOSTNAME")); // must be secure out of the box!
            // prop.setProperty("mtwilson.api.ssl.requireTrustedCertificate", MSConfig.getConfiguration().getString("mtwilson.api.ssl.requireTrustedCertificate", "true")); // must be secure out of the box!
            // prop.setProperty("mtwilson.api.ssl.verifyHostname", MSConfig.getConfiguration().getString("mtwilson.api.ssl.verifyHostname", "true")); // must be secure out of the box!
            prop.setProperty("mtwilson.api.ssl.policy", My.configuration().getDefaultTlsPolicyName());
            log.error("MEOW_009: " + My.configuration().getDefaultTlsPolicyName());
            prop.setProperty("mtwilson.api.ssl.requireTrustedCertificate", My.configuration().getConfiguration().getString("mtwilson.api.ssl.requireTrustedCertificate","true"));
            log.error("MEOW_010: " + My.configuration().getConfiguration().getString("mtwilson.api.ssl.requireTrustedCertificate","true"));
            prop.setProperty("mtwilson.api.ssl.verifyHostname", My.configuration().getConfiguration().getString("mtwilson.api.ssl.verifyHostname", "true"));
            log.error("MEOW_011: " + My.configuration().getConfiguration().getString("mtwilson.api.ssl.verifyHostname", "true"));

            rsaApiClient = new ApiClient(baseURL, credential, keystore, new MapConfiguration(prop));
            log.error("MEOW_012: " + rsaApiClient.toString());
            log.info("Successfully created the API object for Management Service");

        } catch (MSException me) {
            log.error("Error during Api Client registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (Exception ex) {
            log.error("Error while creating the Api Client object. " + ex.getMessage());
            ex.printStackTrace(System.err);
            throw new MSException(ErrorCode.SYSTEM_ERROR, "Error while creating the Api Client object. " + ex.getMessage(), ex);

        }

        return rsaApiClient;
    }

    
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
                processorNameOrCPUID = processorNameOrCPUID.substring(0,1) + "%" + processorNameOrCPUID.substring(2);
                procMap = jpaController.findByCPUID(processorNameOrCPUID);
            }
            
            if (procMap != null){
                platformName = procMap.getPlatformName();
            }
        } catch (MSException me) {
            log.error("Error during retrieval of platform name details. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            log.error("Unexpected errror during retrieval of platform name details. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during retrieval of platform name details." + ex.getMessage());
        }
        
        return platformName;
        
    }
    
    private HostConfigData calibrateMLENames(HostConfigData hostConfigObj, Boolean isBIOSMLE) {
        TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();
        
        if (isBIOSMLE) {
                if (hostObj.BIOS_Oem.contains("Intel")) {
                    // For Romley servers, below is the format of the BIOS string. We need to split the string on . and 
                    // consider the 3,4 & 5 substrings to make up the complete BIOS string. This will not work for Thurley
                    // which is ok since we will support just Romley servers.
                    String [] biosSubStrings = hostObj.BIOS_Version.split("\\.");
                    hostObj.BIOS_Version = biosSubStrings[2] + "." + biosSubStrings[3] + "." + biosSubStrings[4];
                }

                // Update the host object with the names of BIOS. For the name we are using a combination of the OEM
                // and the hypervisor running on the host since we have seen different PCR 0 for the same
                // OEM having different hypervisors.
                //hostObj.BIOS_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + tempVMMOSName.split("_")[0].toString();
                
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

                // Update the host object with the names of BIOS and VMM, which is needed during
                // host registration.
                hostObj.VMM_Version = hostObj.VMM_OSVersion + "-" + hostObj.VMM_Version;

                // For VMware since there is no separate OS and VMM, we use the same name
                if (hostObj.VMM_OSName.contains("ESX")) {
                    hostObj.VMM_Name = hostObj.VMM_OSName;
                }

                // If we are setting host specific MLE, then we need to append the host name to the VMM Name as well
                if (hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_HOST) {
                    hostObj.VMM_Name = hostObj.HostName + "_" + hostObj.VMM_Name;
                } else if (hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_OEM) {
                    // Bug 799 & 791: Need to append the platform name too
                    String platformName = getPlatformName(hostObj.Processor_Info);
                    if (!platformName.isEmpty())
                        hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + platformName + "_" + hostObj.VMM_Name;
                    else
                        hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" +  hostObj.VMM_Name;                    
                } else if (hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_GLOBAL) {
                    // Bug #951 where in we need to append the platform name to the global white lists also.
                    String platformName = getPlatformName(hostObj.Processor_Info);
                    if (!platformName.isEmpty())
                        hostObj.VMM_Name = platformName + "_" + hostObj.VMM_Name;
                    else
                        hostObj.VMM_Name = hostObj.VMM_Name;
                }
        }
        hostConfigObj.setTxtHostRecord(hostObj);
        return hostConfigObj;
    }
    
    /**
     * This is a helper function which if provided the host name and connection
     * string, would retrieve the BIOS & VMM configuration from the host,
     * verifies if those corresponding MLEs are already configured in the
     * Mt.Wilson system. If not, it would throw appropriate exception back. The
     * object returned back from his helper function could be used to directly
     * register the host.
     *     
* @param hostConfigObj
     * @return
     */
    private HostConfigData getHostMLEDetails(HostConfigData hostConfigObj, ApiClient apiClientObj, boolean registerHost) {
        
        try {
            My.initDataEncryptionKey();
            // Retrieve the host object.
            //System.err.println("JIM DEBUG: Retrieve the host object."); 
            TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();
            TblHosts tblHosts = new TblHosts();
            tblHosts.setTlsPolicyName(My.configuration().getDefaultTlsPolicyName());
            
            // Since the connection string passed in by the caller may not be complete (including the vendor), we need to parse it
            // first and make up the complete connection string.
            ConnectionString cs = new ConnectionString(hostObj.AddOn_Connection_String);
            hostObj.AddOn_Connection_String = cs.getConnectionStringWithPrefix();
            
            // TODO: Check with jonathan on the policy used.
            // XXX  we are assuming that the host is in an initial trusted state and that no attackers are executing a 
            //man-in-the-middle attack against us at the moment.  TODO maybe we need an option for a global default 
            //policy (including global default trusted certs or ca's) to choose here and that way instead of us making this 
            //assumption, it's the operator who knows the environment.
            tblHosts.setTlsKeystore(null);
            int i=0;
            tblHosts.setName(hostObj.HostName);
            tblHosts.setAddOnConnectionInfo(hostObj.AddOn_Connection_String);
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
            } catch (Throwable te) {
                log.error("Unexpected error in registerHostFromCustomData: {}", te.toString());
                throw new MSException(te, ErrorCode.MS_HOST_COMMUNICATION_ERROR, te.getMessage());
            }
            
            // Let us verify if we got all the data back correctly or not 
            if (hostObj.BIOS_Oem == null || hostObj.BIOS_Version == null || hostObj.VMM_OSName == null || hostObj.VMM_OSVersion == null || hostObj.VMM_Version == null) {
                throw new MSException(ErrorCode.MS_HOST_CONFIGURATION_ERROR);
            }
            
            hostConfigObj.setTxtHostRecord(hostObj);
            log.debug("Successfully retrieved the host information. Details: " + hostObj.BIOS_Oem + ":" + hostObj.BIOS_Version + ":"
                    + hostObj.VMM_OSName + ":" + hostObj.VMM_OSVersion + ":" + hostObj.VMM_Version + ":" + hostObj.Processor_Info);

            // Change the BIOS and VMM MLE names as per the target white list chosen by the user
            hostConfigObj = calibrateMLENames(hostConfigObj, true);
            hostConfigObj = calibrateMLENames(hostConfigObj, false);
            // Let us first verify if all the configuration details required for host registration already exists. If not, it will throw
            // corresponding exception.
            // verifyMLEForHost(hostConfigObj);
            
            // Here we need to capture the correct MLE names and update the hostConfigObj accordingly, which would be used during host registration
            // If in case no BIOS/VMM MLE exists, this function would thrown an exception
            if (registerHost) {
                HostResponse hostResponse = apiClientObj.registerHostByFindingMLE(hostObj);
            }
            /*log.debug("Host should be mapped to {} MLEs.", mleNamesToUse);
            String [] mleNames = mleNamesToUse.split("\\|\\|");
            if (mleNames.length == 2) {
                hostConfigObj.getTxtHostRecord().BIOS_Name = mleNames[0];
                hostConfigObj.getTxtHostRecord().VMM_Name = mleNames[1];
            } else {
                log.error("Unable to retrieve the MLE names to be mapped to the host.");
                throw new MSException(ErrorCode.MS_MLE_CONFIGURATION_NOT_FOUND);
            }*/
            
            return hostConfigObj;
            
        } catch (MSException me) {
            log.error("Error during retrieval of host MLE information. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (ApiException ae) {
            log.error("API Client error during host registration. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString() + ":" + ae.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected errror during retrieval of host MLE information. " + ex.getMessage());
            ex.printStackTrace(System.err);
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during retrieval of host MLE information." + ex.getMessage());
        }
    }

    /**
     * Helper function that checks if the host specified is already configured
     * in the system or not.
     *
     * @param hostObj
     * @return
     */
    private boolean isHostConfigured(TxtHostRecord hostObj) {
        boolean isHostConfigured = false;
        try {
            TblHostsJpaController hostsJpaController = My.jpa().mwHosts(); //new TblHostsJpaController(getASEntityManagerFactory());

            log.debug("Processing host {}.", hostObj.HostName);
            TblHosts hostSearchObj = hostsJpaController.findByName(hostObj.HostName);
            //if (hostSearchObj == null) {
            //    hostSearchObj = hostsJpaController.findByIPAddress(hostObj.IPAddress);
            //}
            if (hostSearchObj != null) {
                log.debug(String.format("Host '%s' is already configured in the system.", hostObj.HostName));
                isHostConfigured = true;
            } else {
                log.debug(String.format("Host '%s' is currently not configured. ", hostObj.HostName));
                isHostConfigured = false;
            }

            // Return back the results
            return isHostConfigured;

        } catch (MSException me) {
            log.error("Error during bulk host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (Exception ex) {
            log.error("Unexpected errror during bulk host registration. " + ex.getMessage());
            ex.printStackTrace(System.err);
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during bulk host registration." + ex.getMessage());
        }
    }

    /**
     * Author: Sudhir
     *
     * Registers the host with Mount Wilson.
     *
     * @param hostObj : Details of the host to be registered.
     * @return : True if the host is registered successfully.
     */
    public boolean registerHost(TxtHostRecord hostObj) {
        HostConfigData hostConfigObj = null;
        boolean registerStatus = false;
        //String biosPCRs = "";
        //String vmmPCRs = "";

        try {

            if (hostObj != null) {

                hostConfigObj = new HostConfigData();
                //TxtHost tempHostObj = new TxtHost(hostObj);
                //ConnectionString connString = new ConnectionString(tempHostObj.getAddOn_Connection_String());

                //TODO: Modify the HostVMMType ENUM to match the Vendor ENUM or combine them. Also have the 
                // option to separately configure BIOS and VMM PCRs

                // Bug 957: Since PCR configuration is not needed during host registration, commenting them out.
                // The below changes are to address the bug in which if the REST API is directly called the default
                // PCRs were being read from the property file which does not match the UI defaults.
                /*if (connString.getVendor().equals(Vendor.VMWARE)) {
                 biosPCRs = "0,17";
                 vmmPCRs = "18,19,20";
                 } else if (connString.getVendor().equals(Vendor.CITRIX)) {
                 biosPCRs = "0,17";
                 vmmPCRs = "18";                    
                 } else {
                 // Assuming INTEL
                 biosPCRs = "0,17";
                 vmmPCRs = "18";                    
                 }
                
                 hostConfigObj.setBiosPCRs(BIOS_PCRs);
                 hostConfigObj.setVmmPCRs(vmmPCRs);*/

                hostConfigObj.setTxtHostRecord(hostObj);

                // Set the default parameters
                hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
                hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
            }

            registerStatus = registerHostFromCustomData(hostConfigObj);

        } catch (MSException me) {
            log.error("Error during host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (Exception ex) {

            log.error("Unexpected errror during host registration. " + ex.getMessage());
            ex.printStackTrace(System.err);
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during host registration." + ex.getMessage());
        }
        return registerStatus;
    }

 

    /**
     * Function that supports bulk host registration. If the user has just
     * specified the host details to be registered, then we would use the
     * default white list target of OEM for both BIOS and VMM.
     *
     * @param hostRecords
     * @return
     */
    public HostConfigResponseList registerHosts(TxtHostRecordList hostRecords) {
        HostConfigDataList hostList = new HostConfigDataList();
        HostConfigResponseList hostResponseList = null;
        //String biosPCRs = "";
        //String vmmPCRs = "";

        try {

            log.error("About to process {} servers", hostRecords.getHostRecords().size());

            if (hostRecords != null) {

                // For all the hosts specified, setup the default parameters and add it to the list
                for (TxtHostRecord hostObj : hostRecords.getHostRecords()) {
                    HostConfigData hostConfigObj = new HostConfigData();
                    //TxtHost tempHostObj = new TxtHost(hostObj);
                    //ConnectionString connString = new ConnectionString(tempHostObj.getAddOn_Connection_String());

                    //TODO: Modify the HostVMMType ENUM to match the Vendor ENUM or combine them. Also have the 
                    // option to separately configure BIOS and VMM PCRs

                    // Bug 957: Since PCR configuration is not needed during host registration, commenting them out.
                    // The below changes are to address the bug in which if the REST API is directly called the default
                    // PCRs were being read from the property file which does not match the UI defaults.
                                        /*
                     if (connString.getVendor().equals(Vendor.VMWARE)) {
                     biosPCRs = "0,17";
                     vmmPCRs = "18,19,20";
                     } else if (connString.getVendor().equals(Vendor.CITRIX)) {
                     biosPCRs = "0,17";
                     vmmPCRs = "18";                    
                     } else {
                     // Assuming INTEL
                     biosPCRs = "0,17";
                     vmmPCRs = "18";                    
                     }

                     hostConfigObj.setBiosPCRs(BIOS_PCRs);
                     hostConfigObj.setVmmPCRs(vmmPCRs);*/

                    hostConfigObj.setTxtHostRecord(hostObj);

                    hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
                    hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
                    hostList.getHostRecords().add(hostConfigObj);
                }

            }

            // Call into the overloaded method for actually calling into the ApiClient library and getting the results.
            hostResponseList = registerHosts(hostList);

            return hostResponseList;

        } catch (MSException me) {
            log.error("Error during bulk host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (Exception ex) {

            log.error("Unexpected errror during bulk host registration. " + ex.getMessage());
            ex.printStackTrace(System.err);
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during bulk host registration." + ex.getMessage());
        }
    }

    /**
     * Bulk host registration/update function.
     *     
* @param hostRecords : List of hosts to be updated or newly registered.
     * @return
     */
    public HostConfigResponseList registerHosts(HostConfigDataList hostRecords) {
        TxtHostRecordList hostsToBeAddedList = new TxtHostRecordList();
        TxtHostRecordList hostsToBeUpdatedList = new TxtHostRecordList();
        HostConfigResponseList results = new HostConfigResponseList();
        
        try {
            ApiClient apiClient = createAPIObject();
            TblHostsJpaController hostsJpaController = My.jpa().mwHosts();// new TblHostsJpaController(getASEntityManagerFactory());
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
                        hostConfigObj = getHostMLEDetails(hostConfigObj, apiClient, false);
                        hostsToBeUpdatedList.getHostRecords().add(hostConfigObj.getTxtHostRecord());
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
                        hostConfigObj = getHostMLEDetails(hostConfigObj, apiClient, false);
                        hostsToBeAddedList.getHostRecords().add(hostConfigObj.getTxtHostRecord());
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
            if (!hostsToBeAddedList.getHostRecords().isEmpty()) {
                HostConfigResponseList addHostResults = apiClient.addHosts(hostsToBeAddedList);
                for (HostConfigResponse hcr : addHostResults.getHostRecords()) {
                    results.getHostRecords().add(hcr);
                }
            }
            if (!hostsToBeUpdatedList.getHostRecords().isEmpty()) {
                HostConfigResponseList updateHostResults = apiClient.updateHosts(hostsToBeUpdatedList);
                for (HostConfigResponse hcr : updateHostResults.getHostRecords()) {
                    results.getHostRecords().add(hcr);
                }
            }

            // PERFORMANCE ISSUE: Now that we are mapping the hosts to the MLEs that actually match, and perform attestation as part of
            // that we do not need to do the below explict attestation again.

            /*
            // Before we return back errors let us update the trust status of the hosts that were updated.
            Set<Hostname> hostsToBeAttested = new HashSet<Hostname>();
            if (!hostsToBeUpdatedList.getHostRecords().isEmpty()) {
                for(TxtHostRecord hostsUpdated: hostsToBeUpdatedList.getHostRecords()) {
                    hostsToBeAttested.add(new Hostname(hostsUpdated.HostName));
                }
                                
                try {
                    log.debug("Refreshing the trust status of the hosts that were updated : {}.", hostsToBeAttested.toString());
                    List<HostTrustXmlResponse> samlForMultipleHosts = apiClient.getSamlForMultipleHosts(hostsToBeAttested, true);
                    
                } catch (Exception ex) {
                    // We cannot do much here.. we can ignore this at this point of time
                    log.error("Error refreshing the trust status of the hosts {}. {}.", hostsToBeAttested.toString(), ex.getMessage());

                }
            } */
            // Return back the results
            return results;

        } catch (MSException me) {
            log.error("Error during bulk host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (ApiException ae) {

            log.error("API Client error during bulk host registration. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString() + ":" + ae.getMessage());

        } catch (Exception ex) {

            log.error("Unexpected errror during bulk host registration. " + ex.getMessage());
            ex.printStackTrace(System.err);
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during bulk host registration." + ex.getMessage());
        }
    }

    
    /**
     * Author: Sudhir
     *
     * Registers the host using the additional details like which MLE to use etc for registration.
     *
     * @param hostConfigObj : Host Configuration object having the details of the host to be registered along with the
     * details of the MLE that needs to be used for registration.
     *
     * @return : True if success or else an exception.
     */
    public boolean registerHostFromCustomData(HostConfigData hostConfigObj) {

        boolean registerStatus = false;
        //HostInfoInterface vmmHelperObj = null;
        TxtHost txtHost;

        try {

            ApiClient apiClient = createAPIObject();

            // extract the host object
            TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();
            log.debug("Starting to process the registration for host: " + hostObj.HostName);

            TblHostsJpaController hostsJpaController = My.jpa().mwHosts(); //new TblHostsJpaController(getASEntityManagerFactory());

            // This helper function verifies if all the required MLEs are present for the host registration. If no exception is 
            // thrown, we can go ahead with the host registration.
            hostConfigObj = getHostMLEDetails(hostConfigObj, apiClient, true);

            /*
            // First let us check if the host is already configured.
            TblHosts hostSearchObj = hostsJpaController.findByName(hostObj.HostName);
            //if (hostSearchObj == null) {
            //    hostSearchObj = hostsJpaController.findByIPAddress(hostObj.IPAddress);
            //}

            // If the host already exists in the Mt.Wilson, all we will do is check what is the new MLE
            // that the user has opted for, update the host with the corresponding MLEs.
            if (hostSearchObj != null) {

                log.debug(String.format("Since '%s' is already configured, we will update the host with the new MLEs.",
                        hostSearchObj.getName()));
                boolean updateHostStatus = updateHost(apiClient, hostSearchObj, hostConfigObj);
                return updateHostStatus;
            }*/
            
            /*
            // bug #497   this should be a different object than TblHosts  
            TblHosts tblHosts = new TblHosts();
            tblHosts.setTlsPolicyName(My.configuration().getDefaultTlsPolicyName());  // XXX  we are assuming that the host is in an initial trusted state and that no attackers are executing a man-in-the-middle attack against us at the moment.  TODO maybe we need an option for a global default policy (including global default trusted certs or ca's) to choose here and that way instead of us making this assumption, it's the operator who knows the environment.
            tblHosts.setTlsKeystore(null);
            tblHosts.setName(hostObj.HostName);
            tblHosts.setAddOnConnectionInfo(hostObj.AddOn_Connection_String);
            tblHosts.setIPAddress(hostObj.IPAddress);
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
            } catch (Throwable te) {
                log.error("Unexpected error in registerHostFromCustomData: {}", te.toString());
                te.printStackTrace();
                throw new MSException(te, ErrorCode.MS_HOST_COMMUNICATION_ERROR, te.getMessage());
            }

            // Let us verify if we got all the data back correctly or not (Bug: 442)
            if (hostObj.BIOS_Oem == null || hostObj.BIOS_Version == null || hostObj.VMM_OSName == null || hostObj.VMM_OSVersion == null || hostObj.VMM_Version == null) {
                throw new MSException(ErrorCode.MS_HOST_CONFIGURATION_ERROR);
            }

            hostConfigObj.setTxtHostRecord(hostObj);
            log.info("Successfully retrieved the host information. Details: " + hostObj.BIOS_Oem + ":" + hostObj.BIOS_Version + ":"
                    + hostObj.VMM_OSName + ":" + hostObj.VMM_OSVersion + ":" + hostObj.VMM_Version);

            // Let us first verify if all the configuration details required for host registration already exists 
            boolean verifyStatus = verifyMLEForHost(hostConfigObj);

            if (verifyStatus == true) {

                // Finally register the host. */
            /*txtHost = new TxtHost(hostConfigObj.getTxtHostRecord());
            apiClient.addHost(txtHost);*/
           // }

            // If everything is successful, set the status flag to true
            registerStatus = true;
            log.debug("Successfully registered the host: " + hostObj.HostName);

        } catch (MSException me) {
            log.error("Error during host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (Exception ex) {

            ex.printStackTrace(System.err);
            log.error("Unexpected errror during host registration. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during host registration." + ex.getMessage());
        }
        return registerStatus;
    }

    /**
     * Updates the host with the new MLE information that the user has chosen.
     *
     * @param apiClientObj
     * @param tblHostObj
     * @param hostWLTargetObject
     * @return
     */
    private boolean updateHost(ApiClient apiClientObj, TblHosts tblHostObj, HostConfigData hostConfigObj) {
        boolean updateStatus = false;
        HostWhiteListTarget hostVMMWLTargetObj , hostBIOSWLTargetObj ;
        TxtHostRecord hostObj = new TxtHostRecord();

        try {

            // Get the new WL target for the host
            hostBIOSWLTargetObj = hostConfigObj.getBiosWLTarget();
            hostVMMWLTargetObj = hostConfigObj.getVmmWLTarget();

            // Get the current values of the host
            hostObj.HostName = tblHostObj.getName();
            hostObj.IPAddress = tblHostObj.getName();
            hostObj.Port = tblHostObj.getPort();
            hostObj.AddOn_Connection_String = tblHostObj.getAddOnConnectionInfo();
            hostObj.Description = tblHostObj.getDescription();
            hostObj.Email = tblHostObj.getEmail();
            hostObj.Location = tblHostObj.getLocation();
            hostObj.BIOS_Name = tblHostObj.getBiosMleId().getName();
            hostObj.BIOS_Oem = tblHostObj.getBiosMleId().getOemId().getName();
            hostObj.BIOS_Version = tblHostObj.getBiosMleId().getVersion();
            hostObj.VMM_Name = tblHostObj.getVmmMleId().getName();
            hostObj.VMM_Version = tblHostObj.getVmmMleId().getVersion();
            hostObj.VMM_OSName = tblHostObj.getVmmMleId().getOsId().getName();
            hostObj.VMM_OSVersion = tblHostObj.getVmmMleId().getOsId().getVersion();
            hostObj.Processor_Info = hostConfigObj.getTxtHostRecord().Processor_Info;

            // Retrieve the platform name, which will be used later
            String platformName = getPlatformName(hostObj.Processor_Info);
            
            // Find out what is the current White List target that the host is configured. For
            // white list target of "Specified Good Known host", we will have the host name appended
            // to the VMM_Name. For "OEM specific ones" we will have the OEM name appended to it. This
            // is what we will look out for now.

            // First we will process the BIOS MLE
            if (hostObj.BIOS_Name.startsWith(String.format("%s_", hostObj.HostName))) {

                log.debug(String.format("'%s' is currently configured to use '%s' BIOS MLE '%s'.",
                        hostObj.HostName, HostWhiteListTarget.BIOS_HOST.getValue(), hostObj.BIOS_Name));

                // The host is currently configured for BIOS_HOST option. 
                // We need to check the new MLE that the user wants to use. Accordingly we will update the BIOS name.
                if (hostBIOSWLTargetObj == HostWhiteListTarget.BIOS_HOST) {

                    // Then we do not need to do anything. We are set. 
                    log.debug(String.format("'%s' is being updated to use '%s' BIOS MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.BIOS_HOST.getValue(), hostObj.BIOS_Name));

                } else if (hostBIOSWLTargetObj == HostWhiteListTarget.BIOS_OEM) {

                    // Now the user wants to change from HOST SPECIFIC option to OEM SPECIFIC option. So,
                    // We need to change the name of BIOS_Name to remove the Host Name                    
                    hostObj.BIOS_Name = hostObj.BIOS_Name.substring(String.format("%s_", hostObj.HostName).length());

                    log.debug(String.format("'%s' is being updated to use '%s' BIOS MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.BIOS_OEM.getValue(), hostObj.BIOS_Name));

                } else {

                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostBIOSWLTargetObj.toString());
                }

            } else {

                log.debug(String.format("'%s' is currently configured to use '%s' BIOS MLE '%s'.",
                        hostObj.HostName, HostWhiteListTarget.BIOS_OEM.getValue(), hostObj.BIOS_Name));

                // The host is currently configured for BIOS_OEM option. 
                // We need to check the new MLE that the user wants to use. Accordingly we will
                // update the BIOS and VMM names.
                if (hostBIOSWLTargetObj == HostWhiteListTarget.BIOS_HOST) {

                    // NOTE: This condition is there only if people use the API directly. If the user uses UI
                    // This option will not be allowed. You can only change from HOST SPECIFIC option but not to
                    // HOST SPECIFIC.  
                    log.debug(String.format("'%s' is being updated to use '%s' BIOS MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.BIOS_HOST.getValue(), hostObj.BIOS_Name));

                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostBIOSWLTargetObj.toString() + "."
                            + " Use WhiteList configuration option to register with Host specific white list values.");

                } else if (hostBIOSWLTargetObj == HostWhiteListTarget.BIOS_OEM) {

                    // We do not need to do anything;
                    log.debug(String.format("'%s' is being updated to use '%s' BIOS MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.BIOS_OEM.getValue(), hostObj.BIOS_Name));

                } else {

                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostBIOSWLTargetObj.toString());
                }
            }


            // Now let us process the VMM MLE and modify the MLE Names if needed
            if (hostObj.VMM_Name.startsWith(String.format("%s_", hostObj.HostName))) {

                log.debug(String.format("'%s' is currently configured to use '%s' VMM MLE '%s'.",
                        hostObj.HostName, HostWhiteListTarget.VMM_HOST.getValue(), hostObj.VMM_Name));

                // The host is currently configured for VMM_HOST option. 
                // We need to check the new MLE that the user wants to use. Accordingly we will update the VMM name.
                if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_HOST) {

                    // Then we do not need to do anything. We are set. Just ignore the register host
                    // request and send back success.
                    log.debug(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.VMM_HOST.getValue(), hostObj.VMM_Name));

                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_OEM) {

                    // Now the user wants to change from SPECIFIC HOST option to OEM specific option. So,
                    // We need to change the name of VMM_Name to remove the Host Name                    
                    hostObj.VMM_Name = hostObj.VMM_Name.substring(String.format("%s_", hostObj.HostName).length());

                    // Also we need to add back the OEM name to the VMM_Name. We do not need to add it for
                    // BIOS as BIOS_Name is always OEM specific.                    
                    if (!platformName.isEmpty()){
                        hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + platformName + "_" + hostObj.VMM_Name;
                    }else {
                        hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" +  hostObj.VMM_Name;    
                    }
                    
                    
                    log.debug(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.VMM_OEM.getValue(), hostObj.VMM_Name));

                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_GLOBAL) {

                    // Now the user wants to change from SPECIFIC HOST option to Global option. So,
                    // We need to change the name of VMM_Name to remove the Host Name and ensure that the platform name is added back.                   
                    hostObj.VMM_Name = hostObj.VMM_Name.substring(String.format("%s_", hostObj.HostName).length());

                    // We need to add back the platform name
                    if (!platformName.isEmpty())
                        hostObj.VMM_Name = platformName + "_" + hostObj.VMM_Name;
                    else
                        hostObj.VMM_Name = hostObj.VMM_Name;
                    
                    log.debug(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.VMM_GLOBAL.getValue(), hostObj.VMM_Name));
                } else {

                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostVMMWLTargetObj.toString());

                }

            } else if (hostObj.VMM_Name.startsWith(String.format("%s_", hostObj.BIOS_Oem.split(" ")[0].toString()))) {

                log.debug(String.format("'%s' is currently configured to use '%s' VMM MLE '%s'.",
                        hostObj.HostName, HostWhiteListTarget.VMM_OEM.getValue(), hostObj.VMM_Name));

                // The host is currently configured for VMM_OEM option. 
                // We need to check the new MLE that the user wants to use. Accordingly we will update VMM name.
                if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_HOST) {

                    // NOTE: This condition is there only if people use the API directly. If the user uses UI
                    // This option will not be allowed. You can only change from HOST SPECIFIC option but not to
                    // HOST SPECIFIC.  

                    log.debug(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.VMM_HOST.getValue(), hostObj.VMM_Name));

                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostVMMWLTargetObj.toString() + "."
                            + " Use WhiteList configuration option to register with Host specific white list values.");

                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_OEM) {

                    // We do not need to do anything;
                    log.debug(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.VMM_OEM.getValue(), hostObj.VMM_Name));

                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_GLOBAL) {

                    // Now the user wants to change from VMM_OEM option to Global option. So,we need to change the names of VMM_Name 
                    // Since it is currently using the OEM name, which is a combination of OEM_Platform_VMM name, we just remove the OEM part
                    // and keep the remaining content as such.
                    hostObj.VMM_Name = hostObj.VMM_Name.substring(String.format("%s_", hostObj.BIOS_Oem.split(" ")[0].toString()).length());

                    log.debug(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.VMM_GLOBAL.getValue(), hostObj.VMM_Name));
                } else {

                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostVMMWLTargetObj.toString());
                }

            } else {

                log.debug(String.format("'%s' is currently configured to use '%s' VMM MLE '%s'.",
                        hostObj.HostName, HostWhiteListTarget.VMM_GLOBAL.getValue(), hostObj.VMM_Name));

                // The host is currently configured for VMM_GLOBAL option. 
                // We need to check the new MLE that the user wants to use. Accordingly we will update the VMM name.
                if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_HOST) {

                    // NOTE: This condition is there only if people use the API directly. If the user uses UI
                    // This option will not be allowed. You can only change from HOST SPECIFIC option but not to
                    // HOST SPECIFIC.  

                    log.debug(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.VMM_HOST.getValue(), hostObj.VMM_Name));

                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostVMMWLTargetObj + "."
                            + " Use WhiteList configuration option to register with Host specific white list values.");

                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_OEM) {

                    // We need to add OEM name to the VMM_Name.     
                    platformName = getPlatformName(hostObj.Processor_Info);
                    if (!platformName.isEmpty()){
                        hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + platformName + "_" + hostObj.VMM_Name;
                    }else {
                        hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" +  hostObj.VMM_Name;               
                    }
                    
                    log.debug(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.VMM_OEM.getValue(), hostObj.VMM_Name));

                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_GLOBAL) {

                    // We do not need to do anything here.
                    log.debug(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.",
                            hostObj.HostName, HostWhiteListTarget.VMM_OEM.getValue(), hostObj.VMM_Name));
                } else {

                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostVMMWLTargetObj.toString());

                }
            }

            // Now that we are done with all the updates, let us update the HostConfigData object with 
            // the updated host object
            hostConfigObj.setTxtHostRecord(hostObj);

            // Call into the API client to update the host with the updated MLEs
            apiClientObj.updateHost(new TxtHost(hostObj));
            log.debug(String.format("'%s' has been successfully updated to use the '%s' BIOS MLE and '%s' VMM MLE.",
                    hostObj.HostName, hostObj.BIOS_Name, hostObj.VMM_Name));

            updateStatus = true;
            
            try {
                log.debug("Refreshing the trust status of the host {}.", hostObj.HostName);
                // Now that the host status is updated, let us refresh the trust status
                Set<Hostname> hostsToBeAttested = new HashSet<Hostname>();
                hostsToBeAttested.add(new Hostname(hostObj.HostName));
                apiClientObj.getSamlForMultipleHosts(hostsToBeAttested, true);
                //List<HostTrustXmlResponse> samlForMultipleHosts =               
            } catch(Exception ex) {
                // We cannot do much here.. we can ignore this at this point of time
                log.error("Error refreshing the trust status of the host {}. {}.",hostObj.HostName, ex.getMessage());
            }

        } catch (MSException me) {
            log.error("Error during host update. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (ApiException ae) {

            log.error("API Client exception during host update. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString() + ":" + ae.getMessage());

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            log.error("Unexpected errror during host update. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during host update." + ex.getMessage());
        }

        return updateStatus;
    }

    /**
     * Author: Sudhir
     *
     * Configures the white list using the host specified.
     *
     * @param gkvHost: User need to provide just the Host Name and vCenter Connection string in case of VMware hosts.
     * For Open Source hypervisors, user need to provide the IP Address and port number.
     * @return : true if the white list is configured successfully.
     */
    public boolean configureWhiteListFromHost(TxtHostRecord gkvHost) throws ApiException {
        HostConfigData hostConfigObj = null;
        boolean configStatus = false;

        try {
           
            if (gkvHost != null) {

                hostConfigObj = new HostConfigData();
                
                //String biosPCRs = "";
                String vmmPCRs = "";                
                TxtHost tempHostObj = new TxtHost(gkvHost);
                ConnectionString connString = new ConnectionString(tempHostObj.getAddOn_Connection_String());                
                // The below changes are to address the bug in which if the REST API is directly called the default
                // PCRs were being read from the property file which does not match the UI defaults.
                if (connString.getVendor().equals(Vendor.VMWARE)) {
                    //biosPCRs = "0,17";
                    vmmPCRs = VMWARE_PCRs;
                } else if (connString.getVendor().equals(Vendor.CITRIX)) {
                    //biosPCRs = "0,17";
                    vmmPCRs = CITRIX_PCRs;                    
                } else {
                    // Assuming INTEL
                    //biosPCRs = "0,17";
                    vmmPCRs = OPENSOURCE_PCRs;                    
                }
                
                hostConfigObj.setTxtHostRecord(gkvHost);

                // Set the default parameters
                hostConfigObj.setBiosWhiteList(true);
                hostConfigObj.setVmmWhiteList(true);
                hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
                hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
                hostConfigObj.setOverWriteWhiteList(true);
                // By default we should not be registering the host since the user wants to just configure the white list.
                hostConfigObj.setRegisterHost(false);

                hostConfigObj.setBiosPCRs(BIOS_PCRs);
                hostConfigObj.setVmmPCRs(vmmPCRs);
            }

            configStatus = configureWhiteListFromCustomData(hostConfigObj);

        } catch (MSException me) {
            log.error("Error during white list configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            log.error("Unexpected errror during white list configuration. " + ex.toString());
            ex.printStackTrace();
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during white list configuration." + ex.getMessage());
        }

        return configStatus;
    }

    /**
     * Author: Sudhir
     *
     * This function using the white list configuration settings including pcr details, whether the whitelist is for an
     * individual host/for OEM specific host/global white list, etc, configures the DB with the whitelist from the
     * specified good known host.
     *
     * @param hostConfigObj : White List configuration object having all the details.
     * @return : true on success.
     */
    public boolean configureWhiteListFromCustomData(HostConfigData hostConfigObj) {

        boolean configStatus = false;
        String attestationReport;
        //boolean hostAlreadyConfigured = false;
        //boolean biosMLEAlreadyExists = false;
        //boolean vmmMLEAlreadyExists = false;

        long configWLStart = System.currentTimeMillis();
        
        try {
             My.initDataEncryptionKey();
            // Let us ensure that the user has specified the PCRs to be used
            if (hostConfigObj != null) {

                if ((hostConfigObj.addBiosWhiteList() == true) && (hostConfigObj.getBiosPCRs() == null
                        || hostConfigObj.getBiosPCRs().isEmpty())) {
                    throw new MSException(ErrorCode.MS_INVALID_PCRS);
                }

                if ((hostConfigObj.addVmmWhiteList() == true) && (hostConfigObj.getVmmPCRs() == null
                        || hostConfigObj.getVmmPCRs().isEmpty())) {
                    throw new MSException(ErrorCode.MS_INVALID_PCRS);
                }

                if ((hostConfigObj.addBiosWhiteList() == true) && (hostConfigObj.getBiosWLTarget() == null
                        || hostConfigObj.getBiosWLTarget().getValue().isEmpty())) {
                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostConfigObj.getBiosWLTarget().toString());
                }

                if ((hostConfigObj.addVmmWhiteList() == true) && (hostConfigObj.getVmmWLTarget() == null
                        || hostConfigObj.getVmmWLTarget().getValue().isEmpty())) {
                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostConfigObj.getBiosWLTarget().toString());
                }

                TxtHostRecord gkvHost = hostConfigObj.getTxtHostRecord();
                
                if(gkvHost.AddOn_Connection_String == null) {
                    //System.err.println("configureWhiteListFromCustomData cs == null");
                    ConnectionString cs = ConnectionString.from(gkvHost);
                    gkvHost.AddOn_Connection_String = cs.getConnectionStringWithPrefix();
                    //System.err.println("configureWhiteListFromCustomData cs now == " + gkvHost.AddOn_Connection_String );
                } else {
                    // Oct 23, 2013: We just make sure that the connection string has the prefix. Otherwise the agent factory will not be
                    // able to instantiate the correct one.
                    ConnectionString cs = new ConnectionString(gkvHost.AddOn_Connection_String);
                    gkvHost.AddOn_Connection_String = cs.getConnectionStringWithPrefix();
                }
                // bug #497   this should be a different object than TblHosts  
                TblHosts tblHosts = new TblHosts();
                tblHosts.setTlsPolicyName(My.configuration().getDefaultTlsPolicyName()); 
                tblHosts.setTlsKeystore(null); // XXX previously the default policy name was hardcoded to TRUST_FIRST_CERTIFICATE but is now configurable; but because we are still starting with a null keystore, the only two values that would work as a default are TRUST_FIRST_CERTIFICATE and INSECURE
                tblHosts.setName(gkvHost.HostName);
                tblHosts.setAddOnConnectionInfo(gkvHost.AddOn_Connection_String);
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
                } catch (Throwable te) {
                    log.error("Unexpected error in configureWhiteListFromCustomData: {}", te.toString());
                    te.printStackTrace();
                    throw new MSException(te, ErrorCode.MS_HOST_COMMUNICATION_ERROR, te.getMessage());
                }

                log.debug("TIMETAKEN: for getting host information is: " + (System.currentTimeMillis() - configWLStart));
                
                System.err.println("Starting to process the white list configuration from host: " + gkvHost.HostName);

                // Let us verify if we got all the data back correctly or not (Bug: 442)
                if (gkvHost.BIOS_Oem == null || gkvHost.BIOS_Version == null || gkvHost.VMM_OSName == null || gkvHost.VMM_OSVersion == null || gkvHost.VMM_Version == null) {
                    throw new MSException(ErrorCode.MS_HOST_CONFIGURATION_ERROR);
                }
                
                // We need to validate the AIK Certificate first if the the AIK CA is available. This would be applicable only for Trust Agent hosts. Both
                // Citrix and VMware do not support this.
                if (agent.isAikCaAvailable()) {
                    X509Certificate cert = agent.getAikCertificate();
                    cert.checkValidity(); // AIK certificate must be valid today
                    boolean validCaSignature = isAikCertificateTrusted(cert);
                    if( !validCaSignature ) {
                        throw new MSException(ErrorCode.MS_INVALID_AIK_CERTIFICATE, gkvHost.HostName);
                    }                    
                }

                hostConfigObj.setTxtHostRecord(gkvHost);
                System.err.println("Successfully retrieved the host information. Details: " + gkvHost.BIOS_Oem + ":"
                        + gkvHost.BIOS_Version + ":" + gkvHost.VMM_OSName + ":" + gkvHost.VMM_OSVersion
                        + ":" + gkvHost.VMM_Version + ":" + gkvHost.Processor_Info);

                String reqdManifestList = "";

                TblHostsJpaController hostsJpaController =  My.jpa().mwHosts();//new TblHostsJpaController(getASEntityManagerFactory());
                ApiClient apiClient = createAPIObject();

                log.debug("TIMETAKEN: for getting API Client object is: " + (System.currentTimeMillis() - configWLStart));                
                
                // Similar to VMware even TA supports retrieval of Host information and attestation report without needing the host to be registered. So,
                // we don't need to handle these host types differently.

                // check if it has a TPM first  (should be at the beginning of this method but currently trust agent doesn't support a real is-tpm-available capability)   bug #540
                if (!agent.isTpmEnabled()) {
                    throw new MSException(ErrorCode.AS_VMW_TPM_NOT_SUPPORTED, tblHosts.getName());
                }
                
                hostConfigObj = calibrateMLENames(hostConfigObj, true);
                hostConfigObj = calibrateMLENames(hostConfigObj, false);
                if (hostConfigObj.addBiosWhiteList())
                    reqdManifestList = hostConfigObj.getBiosPCRs();
                if (hostConfigObj.addVmmWhiteList()) {
                    if (reqdManifestList.isEmpty()) {
                        reqdManifestList = hostConfigObj.getVmmPCRs();
                    } else {
                        reqdManifestList = reqdManifestList + "," + hostConfigObj.getVmmPCRs();
                    }
                }

                log.debug("TIMETAKEN: for calibrating MLE names: " + (System.currentTimeMillis() - configWLStart));                
                
                // Now we need to spawn 2 threads. One for retriving the attestation report from the host and another one for checking whether MLE with
                // matching whitelists exists or not.
                HostAttReport hostAttReportObj = new HostAttReport(agent, reqdManifestList);
                hostAttReportObj.start();
                
                MLEVerify mleVerifyObj = new MLEVerify(agent, apiClient, hostConfigObj.getOverWriteWhiteList(),
                        new MLEVerificationRequest(hostConfigObj.getTxtHostRecord(), hostConfigObj.getBiosPCRs(), hostConfigObj.getVmmPCRs()));
                mleVerifyObj.start();
                
                hostAttReportObj.join();
                log.debug("TIMETAKEN: for generating host report: " + (System.currentTimeMillis() - configWLStart));
                
                mleVerifyObj.join();
                log.debug("TIMETAKEN: for checking MLE names: " + (System.currentTimeMillis() - configWLStart));
                
                attestationReport = hostAttReportObj.getResult();
                String mleVerifyStatus = mleVerifyObj.getResult();
                // Verify the retrived attestation status report. If null, throw an error
                if ((attestationReport == null || attestationReport.isEmpty()) && hostAttReportObj.isError)
                {
                    throw new MSException(ErrorCode.MS_HOST_COMMUNICATION_ERROR, hostAttReportObj.errorMessage);
                }

                // We are checking for component name since in the attestation report all the pcr and the event logs would use componentname as the label 
                if (attestationReport != null && !attestationReport.isEmpty()) {
                    if (!attestationReport.contains("ComponentName")) {
                       System.err.println("Attestation report content: " + attestationReport);
                        throw new MSException(ErrorCode.MS_INVALID_ATTESTATION_REPORT);
                    }
                }
                
                //Boolean arValid = validateAttestationReport(attestationReport, reqdManifestList);
                if (!validateAttestationReport(attestationReport, reqdManifestList)) {
                    log.error("PCR or Module value is not valid.");
                    throw new MSException(ErrorCode.MS_INVALID_ATTESTATION_REPORT);
                }

                System.err.println("Successfully retrieved the attestation report from host: " + gkvHost.HostName);
                System.err.println("Attestation report is : " + attestationReport);
                
                boolean biosMLEExists = false;
                boolean vmmMLEExists = false;
                // Verify the MLE status. If in case the MLE verification status is NULL, we will just create new MLE
                if (!mleVerifyObj.isError && mleVerifyStatus != null && !mleVerifyStatus.isEmpty()) {
                    // The return format would be BIOS:true|VMM:true
                    biosMLEExists = Boolean.parseBoolean(mleVerifyStatus.substring("BIOS:".length(), mleVerifyStatus.indexOf("|")));
                    vmmMLEExists = Boolean.parseBoolean(mleVerifyStatus.substring(mleVerifyStatus.lastIndexOf(":")+1));                    
                } else {
                    log.error("Error during verification of MLE. Details: {}. So, defaulting to creation of new MLEs.", mleVerifyObj.errorMessage);
                }
                                
                // If either the BIOS or VMM or both already exists, then we should not be creating them again even if the user has requested for
                // as it will create duplicates and leads to confusion.
                
                // The reason we are updating the hostConfigObj flags is because when the attestation report is getting uploaded to the
                // DB, these flags are checked before making the DB transaction.
                if (hostConfigObj.addBiosWhiteList() && biosMLEExists)
                    hostConfigObj.setBiosWhiteList(false);
                if (hostConfigObj.addVmmWhiteList() && vmmMLEExists)
                    hostConfigObj.setVmmWhiteList(false);
                
                // Now that we have retrieved the details of the host, let us configure the BIOS MLE if needed
                if (hostConfigObj.addBiosWhiteList()) {
                    configureBIOSMLE(apiClient, hostConfigObj);
                }

                // Configure the VMM MLE if needed
                if (hostConfigObj.addVmmWhiteList()) {
                    // XXX UPDATE-VMM-MLE issue here
                    configureVMMMLE(apiClient, hostConfigObj);
                }

                log.debug("TIMETAKEN: for configuring MLES : " + (System.currentTimeMillis() - configWLStart));
                
                // Commenting out the below code as we are retrieving the attestation report in a separate thread above.
                /*try {

                    // Retrieve the attestation report from the host
                    attestationReport = agent.getHostAttestationReport(reqdManifestList);   // generic HostAgent interface but we know we are talking to a vmware host and we expect that format
                } catch (Throwable te) {
                    System.err.println("Unexpected error from getHostAttestationReport in registerHostFromCustomData: " + te.toString());
                    te.printStackTrace();
                    throw new MSException(te, ErrorCode.MS_HOST_COMMUNICATION_ERROR, te.getMessage());
                }*/

                // Finally store the attestation report by calling into the WhiteList REST APIs
                uploadToDB(hostConfigObj, attestationReport, apiClient);
                System.err.println("Successfully updated the white list database with the good known white list from host: " + gkvHost.HostName);

                log.debug("TIMETAKEN: for uploading to DB: " + (System.currentTimeMillis() - configWLStart));
                
                // Register host only if required.
                if (hostConfigObj.isRegisterHost() == true) {
                    // First let us check if the host is already configured. If yes, we will return back success
                    TblHosts hostSearchObj = hostsJpaController.findByName(gkvHost.HostName);
                    //if (hostSearchObj == null) {
                    //    System.err.println("Could not find the host using host name: " + gkvHost.HostName);
                    //    hostSearchObj = hostsJpaController.findByIPAddress(gkvHost.IPAddress);
                    //}

                    if (hostSearchObj == null) {
                        System.err.println("Could not find the host using host IP address: " + gkvHost.HostName);
                        System.err.println("Creating a new host.");

                        TxtHost hostObj = new TxtHost(gkvHost);
                        apiClient.addHost(hostObj);
                        System.err.println("Successfully registered the host : " + hostObj.getHostName());

                    } else {
                        System.err.println("Database already has the configuration details for host : " + hostSearchObj.getName());
                        // Since we might have changed the MLE configuration on the host, let us update the host
                        if (gkvHost.Port == null) {
                            gkvHost.Port = 0;
                        }
                        TxtHost newHostObj = new TxtHost(gkvHost);
                        apiClient.updateHost(newHostObj);
                        System.err.println(String.format("Successfully updated the host %s with the new MLE information.", gkvHost.HostName));
                    }
                }

                log.debug("TIMETAKEN: for registering host: " + (System.currentTimeMillis() - configWLStart));
                
                // Now we need to configure the MleSource table with the details of the host that was used for white listing the MLE.
                if (hostConfigObj.addBiosWhiteList()) {
                    configureMleSource(apiClient, gkvHost, true);
                    System.err.println("Successfully configured the details of the host that was used to white list the BIOS MLE - " + gkvHost.BIOS_Name);
                }

                if (hostConfigObj.addVmmWhiteList()) {
                    configureMleSource(apiClient, gkvHost, false);
                    System.err.println("Successfully configured the details of the host that was used to white list the VMM MLE - " + gkvHost.VMM_Name);
                }
                
                log.debug("TIMETAKEN: for configuring MLE source: " + (System.currentTimeMillis() - configWLStart));
                
                configStatus = true;
            }
        } catch (MSException me) {
            log.error("Error during white list configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (ApiException ae) {

            log.error("API Client error during white list configuration. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString()
                    + ": " + ae.getMessage());

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            log.error("Unexpected errror during white list configuration. " + ex.toString());
            ex.printStackTrace();
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during white list configuration." + ex.getMessage());
        }

        return configStatus;
    }
    
    /**
     * Author: Savino 11/13/2013
     *
     * Verifies that the attestation report is valid.
     *
     * @param attestationReport: String in XML format of the attestation report.
     * @param reqdPCRs: String of required PCR values separated by commas ",".
     * @return : True if all required PCRs and modules do not match the regular
     * expression format [0-9A-Fa-f]{40} or [0]{40}|[Ff]{40}.
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
                    
                    if (reqdManifestList.contains(pcrNum)) {
                        if (!isComponentValid(digVal)) {
                            log.error("Module '{0}' specified for '{1}' is not valid.", digVal, compName);
                            return false;
                        }
                    }
                } else if (reader.getLocalName().equalsIgnoreCase("PCRInfo")) {
                    String pcrNum = reader.getAttributeValue("", "ComponentName");
                    String digVal = reader.getAttributeValue(null, "DigestValue");
                    
                    if (reqdManifestList.contains(pcrNum)) {
                        if (!isComponentValid(digVal)) {
                            log.error("PCR '{0}' specified for '{1}' is not valid.", digVal, pcrNum);
                            return false;
                        }
                    }
                }
            }
            reader.next();
        }
        
        return true;
    }
    
    private Boolean isComponentValid(String modValue) {
        String hexadecimalRegEx = "[0-9A-Fa-f]{40}";
        String invalidWhiteList = "[0]{40}|[Ff]{40}";
        
        if (modValue == null || modValue.trim().isEmpty()) {
            return true;
        } // we allow empty values because in mtwilson 1.2 they are used to indicate dynamic information, for example vmware pcr 19, and the command line event that is extended into vmware pcr 19
        // Bug:775 & 802: If the TPM is reset we have seen that all the PCR values would be set to Fs. So, we need to disallow that since it is invalid. Also, all 0's are also invalid.
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
     * Verifies if all the MLE configuration exists for the host to be registered.
     *
     * @param hostObj: Host object having the details of the host to be registered.
     * @param wlTarget: Indicates whether the host to be registered has to use global white list or OEM specific white
     * list.
     * @return : True if all the backend configuration exists, if not throws an exception.
     */
    public boolean verifyMLEForHost(HostConfigData hostConfigObj) {
        boolean verifyStatus = false;

        try {

            TblOemJpaController oemJpa = My.jpa().mwOem(); //new TblOemJpaController(getASEntityManagerFactory());
            TblOsJpaController osJpa = My.jpa().mwOs(); //new TblOsJpaController((getASEntityManagerFactory()));
            TblMleJpaController mleJpa = My.jpa().mwMle(); // new TblMleJpaController(getASEntityManagerFactory());
            
            // Retrieve the host object.
            TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();

            
            // Need to do some data massaging. Firstly we need to change the White Spaces
            // in the OS name to underscores. In case of Intel's BIOS, need to trim it since it will be very long.
            hostObj.VMM_OSName = hostObj.VMM_OSName.replace(' ', '_');
            if (hostObj.BIOS_Oem.contains("Intel")) {
                hostObj.BIOS_Version = hostObj.BIOS_Version.split("\\.")[4].toString();
            }

            // Update the host object with the names of BIOS and VMM, which is needed during host registration.
            //hostObj.BIOS_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + hostObj.VMM_OSName.split("_")[0].toString();
            
            // Now that we know that the PCR 0 should be cosistent across the different hypervisors for the same BIOS version, we
            // need not append the OS name
            hostObj.BIOS_Name = hostObj.BIOS_Oem.replace(' ', '_');
            
            hostObj.VMM_Version = hostObj.VMM_OSVersion + "-" + hostObj.VMM_Version;

            // For VMware since there is no separate OS and VMM, we use the same name
            if (hostObj.VMM_OSName.contains("ESX")) {
                hostObj.VMM_Name = hostObj.VMM_OSName;
            }

            // We need to handle the case where the user might want to use the OEM specific White List. By default it is
            // the global value.
            // Bug 799 & 791: Need to append the platform name too
            String platformName = getPlatformName(hostObj.Processor_Info);
            if (hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_OEM) {
                if (!platformName.isEmpty()){
                    hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + platformName + "_" + hostObj.VMM_Name;
                }else {
                    hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" +  hostObj.VMM_Name;
                }
            }

            TblOem oemTblObj = oemJpa.findTblOemByName(hostObj.BIOS_Oem);
            if (oemTblObj == null) {

                throw new MSException(ErrorCode.MS_OEM_NOT_FOUND, hostObj.BIOS_Oem);
            }

            TblMle tblMleObj = mleJpa.findBiosMle(hostObj.BIOS_Name, hostObj.BIOS_Version, hostObj.BIOS_Oem);
            if (tblMleObj == null) {

                throw new MSException(ErrorCode.MS_BIOS_MLE_NOT_FOUND, hostObj.BIOS_Name + " - " + hostObj.BIOS_Version);
            }

            TblOs tblOsObj = osJpa.findTblOsByNameVersion(hostObj.VMM_OSName, hostObj.VMM_OSVersion);
            if (tblOsObj == null) {

                throw new MSException(ErrorCode.MS_OS_NOT_FOUND, hostObj.VMM_OSName + " - " + hostObj.VMM_OSVersion);
            }

            tblMleObj = mleJpa.findVmmMle(hostObj.VMM_Name, hostObj.VMM_Version, hostObj.VMM_OSName, hostObj.VMM_OSVersion);
            if (tblMleObj == null) {

                throw new MSException(ErrorCode.MS_VMM_MLE_NOT_FOUND, hostObj.VMM_Name + " - " + hostObj.VMM_Version);
            }

            verifyStatus = true;

        } catch (MSException me) {

            log.error("Host cannot be registered. " + me.getErrorMessage());
            throw me;

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            log.error("Unexpected errror during MLE verification of host " + hostConfigObj.getTxtHostRecord().HostName + ". " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Errror during MLE verification for host "
                    + hostConfigObj.getTxtHostRecord().HostName + ". " + ex.getMessage());
        }

        return verifyStatus;
    }

    /**
     * Author: Sudhir
     *
     * Configures the BIOS MLE using the Good known host specified in the WhiteListConfiguration object.
     *
     * @param apiClientObj : ApiClient object for making the ApiClient calls.
     * @param hostConfigObj : WhiteListConfiguration object having the details of the Host and how the white list should
     * be configured.
     * @return : A boolean indicating if the BIOS MLE was created or it already existed.
     */
    private boolean configureBIOSMLE(ApiClient apiClientObj, HostConfigData hostConfigObj) {
        boolean biosMLEAlreadyExists = false;

        try {
            // Extract the host object
            if (hostConfigObj != null) {
                TblOemJpaController oemJpa = My.jpa().mwOem();// new TblOemJpaController(getASEntityManagerFactory());
                TblMleJpaController mleJpa = My.jpa().mwMle(); //new TblMleJpaController(getASEntityManagerFactory());

                WhitelistService wlApiClient = (WhitelistService) apiClientObj;

                //hostConfigObj = calibrateMLENames(hostConfigObj, true);
                TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();  
                
                // Bug: 957 Need to change the MLE file name only if the user has requested for it. Otherwise we will update the
                // white list of the existing MLE
                if (!hostConfigObj.getOverWriteWhiteList())
                    hostObj.BIOS_Name = getNextAvailableBIOSMLEName(hostObj.BIOS_Name, hostObj.BIOS_Version, hostObj.BIOS_Oem);
                
                /*
                // Need to do some data massaging. Firstly we need to change the White Spaces
                // in the OS name to underscores. This is to ensure that it works correctly with
                // the WLM portal. In case of Intel's BIOS, need to trim it since it will be very long.
                //String tempVMMOSName = hostObj.VMM_OSName.replace(' ', '_');
                if (hostObj.BIOS_Oem.contains("Intel")) {
                    hostObj.BIOS_Version = hostObj.BIOS_Version.split("\\.")[4].toString();
                }

                // Update the host object with the names of BIOS. For the name we are using a combination of the OEM
                // and the hypervisor running on the host since we have seen different PCR 0 for the same
                // OEM having different hypervisors.
                //hostObj.BIOS_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + tempVMMOSName.split("_")[0].toString();
                
                // Now that we know that the PCR 0 should be cosistent across the different hypervisors for the same BIOS version, we
                // need not append the OS name
                hostObj.BIOS_Name = hostObj.BIOS_Oem.replace(' ', '_');
                
                // If we are setting host specific MLE, then we need to append the host name to the BIOS Name as well
                boolean value = (hostConfigObj.getBiosWLTarget() == HostWhiteListTarget.BIOS_HOST);
                if (hostConfigObj != null && value) {
                    hostObj.BIOS_Name = hostObj.HostName + "_" + hostObj.BIOS_Name;
                }*/

                TblOem oemTblObj = oemJpa.findTblOemByName(hostObj.BIOS_Oem);

                // Create the OEM if it does not exist
                if (oemTblObj == null) {

                    OemData oemObj = new OemData(hostObj.BIOS_Oem, hostObj.BIOS_Oem);
                    wlApiClient.addOEM(oemObj);
                    log.debug("Successfully created the OEM : " + hostObj.BIOS_Oem);

                } else {
                    log.debug("Database already has the configuration details for OEM : " + hostObj.BIOS_Oem);
                }

                // Create the BIOS MLE for the host. 
                MleData mleObj = new MleData();
                mleObj.setName(hostObj.BIOS_Name);
                mleObj.setVersion(hostObj.BIOS_Version);
                mleObj.setAttestationType("PCR");
                mleObj.setMleType("BIOS");
                mleObj.setDescription("");
                mleObj.setOsName("");
                mleObj.setOsVersion("");
                mleObj.setOemName(hostObj.BIOS_Oem);

                // Now we need to create empty manifests for all the BIOS PCRs that need to
                // be verified. 
                String biosPCRs = hostConfigObj.getBiosPCRs();
                if (biosPCRs.isEmpty()) {
                    biosPCRs = BIOS_PCRs;
                }
                String[] biosPCRList = biosPCRs.split(",");

                List<ManifestData> biosMFList = new ArrayList<ManifestData>();
                for (String biosPCR : biosPCRList) {
                    biosMFList.add(new ManifestData(biosPCR, ""));
                }

                mleObj.setManifestList(biosMFList);

                // If the MLE does not exist, then let us create it.
                TblMle tblMleObj = mleJpa.findBiosMle(hostObj.BIOS_Name, hostObj.BIOS_Version, hostObj.BIOS_Oem);
                if (tblMleObj == null) {

                    wlApiClient.addMLE(mleObj);
                    log.debug("Successfully created the BIOS MLE : " + hostObj.BIOS_Name);

                } else {
                    biosMLEAlreadyExists = true;
                    log.debug("Database already has the configuration details for BIOS MLE : " + hostObj.BIOS_Name);
                }
            }
        } catch (MSException me) {

            log.error("Error during OEM - BIOS MLE configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (ApiException ae) {

            log.error("API Client error during OEM - BIOS MLE configuration. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString()
                    + ": Error during OEM-BIOS MLE Configuration. " + ae.getMessage());

        } catch (Exception ex) {
            //System.err.println("JIM DEBUG"); 
            //ex.printStackTrace(System.err);
            log.error("Unexpected errror during OEM - BIOS MLE configuration. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during OEM - BIOS MLE configuration. " + ex.getMessage());
        }

        return biosMLEAlreadyExists;

    }

    /**
     * Author: Sudhir
     *
     * Configures the Hypervisor(VMM) MLE using the Good known host specified in the WhiteListConfiguration object.
     *
     * @param apiClientObj : ApiClient object for making the ApiClient calls.
     * @param hostConfigObj : WhiteListConfiguration object having the details of the Host and how the white list should
     * be configured.
     * @return : A boolean indicating if the VMM MLE was created or it already existed.
     */
    private boolean configureVMMMLE(ApiClient apiClientObj, HostConfigData hostConfigObj) {
        String attestationType = "";
        boolean vmmMLEAlreadyExists = false;
        try {
            if (hostConfigObj != null) {
                TblOsJpaController osJpa = My.jpa().mwOs(); //new TblOsJpaController((getASEntityManagerFactory()));
                TblMleJpaController mleJpa = My.jpa().mwMle(); //new TblMleJpaController(getASEntityManagerFactory());

                WhitelistService wlApiClient = (WhitelistService) apiClientObj;

                //hostConfigObj = calibrateMLENames(hostConfigObj, false);
                TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();
                
                // Bug: 957 Need to change the MLE file name only if the user has requested for it. Otherwise we will update the
                // white list of the existing MLE
                if (!hostConfigObj.getOverWriteWhiteList())
                    hostObj.VMM_Name = getNextAvailableVMMMLEName(hostObj.VMM_Name, hostObj.VMM_Version, hostObj.VMM_OSName, hostObj.VMM_OSVersion);
                
                /*
                // Need to do some data massaging. Firstly we need to change the White Spaces in the OS name to underscores. This is to ensure that it works correctly with
                // the WLM portal. In case of Intel's BIOS, need to trim it since it will be very long.
                hostObj.VMM_OSName = hostObj.VMM_OSName.replace(' ', '_');

                // Update the host object with the names of BIOS and VMM, which is needed during
                // host registration.
                hostObj.VMM_Version = hostObj.VMM_OSVersion + "-" + hostObj.VMM_Version;

                // For VMware since there is no separate OS and VMM, we use the same name
                if (hostObj.VMM_OSName.contains("ESX")) {
                    hostObj.VMM_Name = hostObj.VMM_OSName;
                }

                TblOs tblOsObj = osJpa.findTblOsByNameVersion(hostObj.VMM_OSName, hostObj.VMM_OSVersion);
                if (tblOsObj == null) {

                    // Now let us create the OS information corresponding to the host
                    OsData osObj = new OsData(hostObj.VMM_OSName, hostObj.VMM_OSVersion, "");
                    wlApiClient.addOS(osObj);
                    log.debug("Successfully created the OS : " + hostObj.VMM_OSName);

                } else {
                    log.debug("Database already has the configuration details for the OS : " + hostObj.VMM_OSName);
                }

                // If we are setting host specific MLE, then we need to append the host name to the VMM Name as well
                boolean value = (hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_HOST);
                boolean value2 = (hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_OEM);
                if (hostConfigObj != null && value) {
                    hostObj.VMM_Name = hostObj.HostName + "_" + hostObj.VMM_Name;
                } else if (hostConfigObj != null && value2) {
                    // Bug 799 & 791: Need to append the platform name too
                    String platformName = getPlatformName(hostObj.Processor_Info);
                    if (!platformName.isEmpty()){
                        hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + platformName + "_" + hostObj.VMM_Name;
                    } else{
                        hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" +  hostObj.VMM_Name;      
                    }
                } else if (hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_GLOBAL) {
                    // Bug #951 where in we need to append the platform name to the global white lists also.
                    String platformName = getPlatformName(hostObj.Processor_Info);
                    if (!platformName.isEmpty())
                        hostObj.VMM_Name = platformName + "_" + hostObj.VMM_Name;
                    else
                        hostObj.VMM_Name = hostObj.VMM_Name;
                } */

                //TODO: After the connectionString class integration change the below code to check for Citrix,which supports only PCR. Remaining host types support
                // module based attestation.
                ConnectionString connString = new ConnectionString(hostObj.AddOn_Connection_String);
                if (connString.getVendor() == Vendor.CITRIX) {
                    attestationType = "PCR";
                } else {
                    attestationType = "MODULE";
                }

                TblOs tblOsObj = osJpa.findTblOsByNameVersion(hostObj.VMM_OSName, hostObj.VMM_OSVersion);
                if (tblOsObj == null) {

                    // Now let us create the OS information corresponding to the host
                    OsData osObj = new OsData(hostObj.VMM_OSName, hostObj.VMM_OSVersion, "");
                    wlApiClient.addOS(osObj);
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

                // Let us create the dummy manifests now. We will update it later after
                // host registration. NOTE: We should not add PCR19 into the required manifest
                // list if the host is ESXi 5.0 as it will change. 
                String vmmPCRs = hostConfigObj.getVmmPCRs();
                String[] vmmPCRList = vmmPCRs.split(",");

                List<ManifestData> vmmMFList = new ArrayList<ManifestData>();
                for (String vmmPCR : vmmPCRList) {
                    vmmMFList.add(new ManifestData(vmmPCR, "")); // whitelist service now allows empty pcr's 
                }

                mleVMMObj.setManifestList(vmmMFList);

                TblMle tblMleObj = mleJpa.findVmmMle(hostObj.VMM_Name, hostObj.VMM_Version, hostObj.VMM_OSName, hostObj.VMM_OSVersion);
                if (tblMleObj == null) {

                    wlApiClient.addMLE(mleVMMObj);
                    log.debug("Successfully created the VMM MLE : " + hostObj.VMM_Name);
                    
                    // NOTE: In order to avoid the look up queries to decide whether to do a add or a update
                    // to the white lists, we will use the overwrite flag. If the overwrite flag is set to false
                    // then, we will do a "ADD" operation. If the overwrite flag is set to "TRUE", then we do an
                    // "UPDATE" operation. But users can set the overwrite flag to "TRUE" for MLEs that dont exist.
                    // So, here we know that the VMM MLE does not exist since we just created a new one. So, if
                    // the overwrite flag is set, we will unset it here.
                    if (hostConfigObj.getOverWriteWhiteList())
                        hostConfigObj.setOverWriteWhiteList(false);

                } else {
                    vmmMLEAlreadyExists = true;
                    log.debug("Database already has the configuration details for VMM MLE : " + hostObj.VMM_Name);
                }
            }
        } catch (MSException me) {

            log.error("Error during OS - VMM MLE configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;

        } catch (ApiException ae) {

            log.error("API Client error during OS - VMM MLE configuration. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString()
                    + ": Error during OEM-VMM MLE Configuration. " + ae.getMessage());

        } catch (Exception ex) {
            //System.err.println("JIM DEBUG"); 
            //ex.printStackTrace(System.err);
            log.error("Error during OS - VMM MLE configuration. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during OS - VMM MLE configuration. " + ex.getMessage());
        }

        return vmmMLEAlreadyExists;
    }

    /**
     *
     * @param apiClientObj
     * @param hostObj
     * @param isBIOSMLE
     */
    private void configureMleSource(ApiClient apiClientObj, TxtHostRecord hostObj, Boolean isBIOSMLE) {
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
                apiClientObj.addMleSource(mleSourceObj);
            } catch (ApiException iae) {
                if (iae.getErrorCode() == ErrorCode.WS_MLE_SOURCE_MAPPING_ALREADY_EXISTS.getErrorCode()) {
                    log.warn("Mapping already exists for the MLE white list host for MLE: " + mleSourceObj.getHostName());
                    // Since the mapping already exists, it means that the user is updating the white list. So, let us call the update method
                    apiClientObj.updateMleSource(mleSourceObj);
                } else {
                    throw new MSException(iae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(iae.getErrorCode()).toString()
                            + ": Error during MLE white list host mapping. " + iae.getMessage());
                }
            }

        } catch (ApiException ae) {
            log.error("API Client error during MLE white list host mapping. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString()
                    + ": Error during MLE white list host mapping. " + ae.getMessage());

        } catch (Exception ex) {
            //System.err.println("JIM DEBUG"); 
            //ex.printStackTrace(System.err);
            log.error("Error during MLE white list host mapping. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during MLE white list host mapping. " + ex.getMessage());

        }
    }

    /**
     * Author: Sudhir
     *
     * This is a helper function to delete the MLE.
     *
     * @param apiCLientObj : Handle to the ApiClient object
     * @param hostObj : TxtHostRecord object having the details of the MLE to be deleted.
     * @param isBIOSMLE : This flag will be set to true if the MLE being deleted is BIOS. For VMM MLEs it would be FALSE
     */
    private void deleteMLE(ApiClient apiClientObj, TxtHostRecord hostObj, Boolean isBIOSMLE) {
        MLESearchCriteria mleDetails = new MLESearchCriteria();
        try {

            if (isBIOSMLE) {
                // Process the deletion of the BIOS MLE                
                mleDetails.mleName = hostObj.BIOS_Name;
                mleDetails.mleVersion = hostObj.BIOS_Version;
                mleDetails.oemName = hostObj.BIOS_Oem;
                mleDetails.osName = "";
                mleDetails.osVersion = "";

            } else {
                // Process the deletion of the VMM MLE
                mleDetails.mleName = hostObj.VMM_Name;
                mleDetails.mleVersion = hostObj.VMM_Version;
                mleDetails.osName = hostObj.VMM_OSName;
                mleDetails.osVersion = hostObj.VMM_OSVersion;
                mleDetails.oemName = "";
            }

            apiClientObj.deleteMLE(mleDetails);

        } catch (ApiException ae) {
            log.error("API Client error during deletion of MLE. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString()
                    + ": Error during MLE Deletion. " + ae.getMessage());

        } catch (Exception ex) {
            //System.err.println("JIM DEBUG"); 
            //ex.printStackTrace(System.err);
            log.error("Error during MLE deletion. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during MLE configuration. " + ex.getMessage());

        }
    }

    /**
     * Author : Sudhir
     *
     * Uploads the attestation report (event info and pcr info) into the White List Database for the specified good
     * known host.
     *
     * @param hostConfigObj : WhiteList Configuration object having the details of the host, which would be used as the
     * good known host
     * @param attestationReport : XML report having all the details of the good known measurements that need to be
     * uploaded to the database.
     * @param apiClientObj: ApiClient object.
     *
     */
    private void uploadToDB(HostConfigData hostConfigObj, String attestationReport, ApiClient apiClientObj) throws IOException {

        //String vCenterVersion = "";
        //String esxHostVersion = "";
        TblPcrManifestJpaController pcrJpa = My.jpa().mwPcrManifest(); //new TblPcrManifestJpaController(getASEntityManagerFactory());
        TblModuleManifestJpaController moduleJpa = My.jpa().mwModuleManifest(); //new TblModuleManifestJpaController(getASEntityManagerFactory());
        TblEventTypeJpaController eventJpa = My.jpa().mwEventType(); //new TblEventTypeJpaController(getASEntityManagerFactory());
        TblMleJpaController mleJpa = My.jpa().mwMle(); //new TblMleJpaController(getASEntityManagerFactory());
        MleBO mleBO = new MleBO();
        
        // Bug:817: We need to refresh the trust status of all the hosts after the MLE update. 
        boolean isBiosMLEUpdated = false;
        boolean isVmmMLEUpdated = false;
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
        List<Integer> pcrsSupportedForEventLog = Arrays.asList(19);
        // Since the attestation report has all the PCRs we need to upload only the required PCR values into the white list tables.
        // Location PCR (22) is added by default. We will check if PCR 22 is configured or not. If the digest value for PCR 22 exists, then
        // we will configure the location table as well.
        List<String> pcrsToWhiteList = Arrays.asList((hostConfigObj.getBiosPCRs() + "," + hostConfigObj.getVmmPCRs() + "," + "22").split(","));
        //log.info("pcrs to whitelist: {}", pcrsToWhiteList.toString());
        try {

            TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();

            log.info("Starting the white list upload to database");
            WhitelistService wlsClient = (WhitelistService) apiClientObj;

            XMLInputFactory xif = XMLInputFactory.newInstance();
            //FileInputStream fis = new FileInputStream("c:\\temp\\nbtest.txt");
            StringReader sr = new StringReader(attestationReport);
            XMLStreamReader reader = xif.createXMLStreamReader(sr);

            TblMle mleSearchObj = mleJpa.findVmmMle(hostObj.VMM_Name, hostObj.VMM_Version, hostObj.VMM_OSName, hostObj.VMM_OSVersion);
            TblMle mleBiosSearchObj = mleJpa.findBiosMle(hostObj.BIOS_Name, hostObj.BIOS_Version, hostObj.BIOS_Oem);
            //log.info(String.format("found BIOs MLE: Name=%s Version=%s OEM=%s" , mleBiosSearchObj.getName(), mleBiosSearchObj.getVersion(), mleBiosSearchObj.getOemId().getName()));    
            // Process all the Event and PCR nodes in the attestation report.
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equalsIgnoreCase("Host_Attestation_Report")) {
                        //vCenterVersion = reader.getAttributeValue("", "vCenterVersion");
                        //esxHostVersion = reader.getAttributeValue("", "HostVersion");
                    } else if (reader.getLocalName().equalsIgnoreCase("EventDetails") && (hostConfigObj.addVmmWhiteList() == true)) {

                        // Check if the package is a dynamic package. If it is, then we should not be storing it in the database
                        if (reader.getAttributeValue("", "PackageName").length() == 0
                                && reader.getAttributeValue("", "EventName").equalsIgnoreCase("Vim25Api.HostTpmSoftwareComponentEventDetails")) {
                            reader.next();
                            continue;
                        }

                        // currently event details would be available for all host except for Citrix Xen. Also we should not process the event log information for all the PCRs. We just 
                        // need to do it for PCR 19
                        // Bug#: 768: We need to process the modules only if the user has requested for verifying that PCR. If not, we should not process PCR 19 at all.
                        if ((pcrsSupportedForEventLog.contains(Integer.parseInt(reader.getAttributeValue("", "ExtendedToPCR")))) && 
                                (pcrsToWhiteList.contains(reader.getAttributeValue("", "ExtendedToPCR")))) {
                            ModuleWhiteList moduleObj = new ModuleWhiteList();
                            // bug 2013-02-04 inserting the space here worked with mysql because mysql automatically trims spaces in queries but other database systems DO NOT;  it's OK for componentName to be empty string but somewhere else we have validation check and throw an error if it's empty
                            if (reader.getAttributeValue("", "ComponentName").isEmpty()) {
                                moduleObj.setComponentName(" ");
                                log.info("uploadToDB: component name set to single-space");
                            } else {
                                moduleObj.setComponentName(reader.getAttributeValue("", "ComponentName")); // it could be empty... see TestVmwareEsxi51.java in AttestationService/src/test/java to see how this can be easily handled using the vendor-specific classes, where the vmware implementation automatically sets component name to something appropriate
                            }
                            moduleObj.setDigestValue(reader.getAttributeValue("", "DigestValue"));
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

                            // For Open Source hypervisors, we do not want to prefix the event type field name. So, we need to check if the event name
                            // corresponds to VMware, then we will append the event type fieldName to the component name. Otherwise we won't
                            
                            // PLEASE SEE THE NOTE ABOVE at the start of the function. The following code has been commented out for performance improvements.
                            
                            /*String fullComponentName = "";                            
                            if (moduleObj.getEventName().contains("Vim25")) {
                                TblEventType eventSearchObj = eventJpa.findEventTypeByName(moduleObj.getEventName());
                                if (eventSearchObj != null) {
                                    fullComponentName = eventSearchObj.getFieldName() + "." + moduleObj.getComponentName();
                                }
                            } else {
                                fullComponentName = moduleObj.getComponentName();
                            }
                            
                            // fix for Bug #730 that affected postgres only because postgres does not automatically trim spaces on queries but mysql automatically trims
                            if( fullComponentName != null ) {
                                log.debug("trimming fullComponentName: " + fullComponentName);
                                fullComponentName = fullComponentName.trim(); 
                            }
                            log.debug("uploadToDB searching for module manifest with fullComponentName '" + fullComponentName + "'");*/
                            /*TblModuleManifest moduleSearchObj = moduleJpa.findByMleNameEventName(mleSearchObj.getId(),
                                    fullComponentName, moduleObj.getEventName());
                            if (moduleSearchObj == null) {
                                mleBO.addModuleWhiteList(moduleObj);
                                //wlsClient.addModuleWhiteList(moduleObj);
                                log.debug("Successfully created a new module manifest for : " + hostObj.VMM_Name + ":" + moduleObj.getComponentName());

                            } else {
                                mleBO.updateModuleWhiteList(moduleObj);
                                //wlsClient.updateModuleWhiteList(moduleObj);
                                log.debug("Successfully updated the module manifest for : " + hostObj.VMM_Name + ":" + moduleObj.getComponentName());
                                isVmmMLEUpdated = true;
                            }*/
                            if (!hostConfigObj.getOverWriteWhiteList()) {
                                if (isWhiteListServiceLocal())
                                    mleBO.addModuleWhiteList(moduleObj, emt);
                                else
                                    wlsClient.addModuleWhiteList(moduleObj);
                                log.debug("Successfully created a new module manifest for : " + hostObj.VMM_Name + ":" + moduleObj.getComponentName());

                            } else {
                                if (isWhiteListServiceLocal())
                                    mleBO.updateModuleWhiteList(moduleObj, emt);
                                else
                                    wlsClient.updateModuleWhiteList(moduleObj);
                                log.debug("Successfully updated the module manifest for : " + hostObj.VMM_Name + ":" + moduleObj.getComponentName());
                                isVmmMLEUpdated = true;
                            }
                        }
                    } else if (reader.getLocalName().equalsIgnoreCase("PCRInfo")) { // pcr information would be available for all the hosts.
                        //log.info(String.format("Reading PCRInfo node"));
                        // We need to white list only thos pcrs that were requested by the user. We will ignore the remaining ones
                        if (pcrsToWhiteList.contains(reader.getAttributeValue(null, "ComponentName"))) {
                            TblPcrManifest tblPCR;
                            PCRWhiteList pcrObj = new PCRWhiteList();
                            pcrObj.setPcrName(reader.getAttributeValue(null, "ComponentName"));
                            pcrObj.setPcrDigest(reader.getAttributeValue(null, "DigestValue"));
                            Integer mleID;

                            if (pcrObj.getPcrName() == null) {
                                log.error("uploadToDB: PCR name is null: " + hostObj.toString());
                            } else if ((Integer.parseInt(reader.getAttributeValue(null, "ComponentName")) <= MAX_BIOS_PCR)) {

                                if (hostConfigObj.addBiosWhiteList() == true) {
                                    pcrObj.setMleName(hostObj.BIOS_Name);
                                    pcrObj.setMleVersion(hostObj.BIOS_Version);
                                    pcrObj.setOsName("");
                                    pcrObj.setOsVersion("");
                                    pcrObj.setOemName(hostObj.BIOS_Oem);
                                    mleID = mleBiosSearchObj.getId();
                                    //log.info(String.format("Adding BiosWhiteList: Name=%s Version=%s OEM=%s mleID=%s",hostObj.BIOS_Name,hostObj.BIOS_Version,hostObj.BIOS_Oem,mleBiosSearchObj.getId().toString()));
                                    tblPCR = pcrJpa.findByMleIdName(mleID, pcrObj.getPcrName());
                                    if (tblPCR == null) {
                                        if (isWhiteListServiceLocal())
                                            mleBO.addPCRWhiteList(pcrObj, emt);
                                        else
                                            wlsClient.addPCRWhiteList(pcrObj);
                                        log.debug("Successfully created a new BIOS PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());

                                    } else {
                                        if (isWhiteListServiceLocal())
                                            mleBO.updatePCRWhiteList(pcrObj, emt);
                                        else
                                            wlsClient.updatePCRWhiteList(pcrObj);
                                        log.debug("Successfully updated the BIOS PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());
                                        isBiosMLEUpdated = true;
                                    }
                                }

                            } else if ((Integer.parseInt(reader.getAttributeValue(null, "ComponentName")) == LOCATION_PCR)) {
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
                            } else if (hostConfigObj.addVmmWhiteList() == true) {
                                //log.info(String.format("Adding VMM white list: Name=%s Version=%s OsName=%s OsVersion=%s mleID=%s", hostObj.VMM_Name,hostObj.VMM_Version,hostObj.VMM_OSName,hostObj.VMM_OSVersion,mleSearchObj.getId().toString()));
                                pcrObj.setMleName(hostObj.VMM_Name);
                                pcrObj.setMleVersion(hostObj.VMM_Version);
                                pcrObj.setOsName(hostObj.VMM_OSName);
                                pcrObj.setOsVersion(hostObj.VMM_OSVersion);
                                pcrObj.setOemName("");
                                mleID = mleSearchObj.getId();

                                // TODO : After the integration with the master branch, we need to use the ConnectionString class and determine the type of the vendor
                                // If the vendor is Citrix, then only we need to write the PCR 19. Otherwise we need to null it out. 
                                if (! hostObj.AddOn_Connection_String.toLowerCase().contains("citrix")) {
                                    if (pcrObj.getPcrName() != null && pcrObj.getPcrName().equalsIgnoreCase("19")) {
                                        pcrObj.setPcrDigest(""); // XXX hack, because the pcr value is dynamic / different across hosts and the whitelist service requires a value
                                    }
                                }

                                tblPCR = pcrJpa.findByMleIdName(mleID, pcrObj.getPcrName());
                                if (tblPCR == null) {
                                    if (isWhiteListServiceLocal())
                                        mleBO.addPCRWhiteList(pcrObj, emt);
                                    else
                                        wlsClient.addPCRWhiteList(pcrObj);
                                    log.debug("Successfully created a new VMM PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());
                                } else {
                                    if (isWhiteListServiceLocal())
                                        mleBO.updatePCRWhiteList(pcrObj, emt);
                                    else
                                        wlsClient.updatePCRWhiteList(pcrObj);
                                    log.debug("Successfully updated the VMM PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());
                                    isVmmMLEUpdated = true;
                                }
                            }
                        }
                    }
                }
                reader.next();
            }
            emt.getTransaction().commit();            
            emt.close();
            
            // Nov 14, 2013: Since we have the new feature where in if the host is untrusted, it will try to go and
            // map to a different MLE, we will comment out the below functionality since updateWhiteList would take a
            // huge performance impact if the # of servers associated with the MLE is large.
            
            /*
            // Now that we have uploaded all the whitelists, let us check if we updated/modified an existing one. If yes, then we need to retrieve 
            // the list of all the hosts for those MLEs and update their trust status.
            Collection<TblHosts> tblHostsCollection = null;
            if (isBiosMLEUpdated) {
                log.info("Retrieving the list of hosts to be updated because of BIOS update");
                Collection<TblHosts> biosHostCollection = mleBiosSearchObj.getTblHostsCollection1();
                if (biosHostCollection!= null && !biosHostCollection.isEmpty()) {
                    log.debug("Retrieved {} hosts for updates.", biosHostCollection.size());
                    tblHostsCollection = biosHostCollection;
                } else {
                    log.info("No hosts to be updated because of BIOS MLE update.");
                }
            }
            if (isVmmMLEUpdated) {
                log.info("Retrieving the list of hosts to be updated because of VMM update");                
                Collection<TblHosts> vmmHostCollection = mleSearchObj.getTblHostsCollection();
                if (vmmHostCollection != null && !vmmHostCollection.isEmpty()) {
                    log.debug("Retrieved {} hosts for updates.", vmmHostCollection.size());                    
                    if (tblHostsCollection == null){
                        tblHostsCollection = vmmHostCollection;
                    }
                    else{
                        tblHostsCollection.addAll(vmmHostCollection);
                    }
                } else {
                        log.info("No hosts to be updated because of VMM MLE update.");
                }
            }
            
            // Form the list of unique host names that needs to be attested
            //List<String> hostsToBeAttested = new ArrayList<String>();
            Set<Hostname> hostsToBeAttested = new HashSet<Hostname>();
            if (tblHostsCollection != null && !tblHostsCollection.isEmpty()) {
                for (TblHosts tblHosts : tblHostsCollection) {
                    if (!hostsToBeAttested.contains(new Hostname(tblHosts.getName()))) {
                        hostsToBeAttested.add(new Hostname(tblHosts.getName()));
                    }
                }
            }
            
            log.debug("Refreshing the trust status of the hosts : {}, since their MLE was updated", hostsToBeAttested.toString());
            //String hostNames = StringUtils.join(hostsToBeAttested, ",");
            // We don't need to process the output here as we refreshed the status to make sure that the SAML assertion table has the latest data
            // if and when the user requests.
            if(! hostsToBeAttested.isEmpty()) {
                //List<HostTrustXmlResponse> samlForMultipleHosts =
                apiClientObj.getSamlForMultipleHosts(hostsToBeAttested, true);
            }
            log.info("Successfully refreshed the status of all the hosts. ");
            */
        } catch (MSException me) {
            log.error("Error during white list upload to database. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
        } catch (ApiException ae) {
            log.error("API Client error during white list upload to database. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString()
                    + ": Error during White List upload to DB. " + ae.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            log.error("Error during white list upload to database. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during white list upload to database. " + ex.getMessage());
        }
    }
    
    /**
     * Finds the next available name that can be used for creation of MLE. If the default name is not used, then
     * the function returns back the same, otherwise it adds the next available numeric extension to the default
     * name and returns back to the caller.
     * 
     * @param biosName
     * @param biosVersion
     * @param oemName
     * @return 
     */
    public String getNextAvailableBIOSMLEName(String biosName, String biosVersion, String oemName) {
        TblMle tblMleObj = null;
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
            log.error("Unexpected errror during BIOS MLE name generation. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during BIOS MLE name generation. " + ex.getMessage());
        }        
    }

    /**
     * Finds the next available name that can be used for creation of MLE. If the default name is not used, then
     * the function returns back the same, otherwise it adds the next available numeric extension to the default
     * name and returns back to the caller.
     * 
     * @param vmmName
     * @param vmmVersion
     * @param osName
     * @param osVersion
     * @return 
     */
    public String getNextAvailableVMMMLEName(String vmmName, String vmmVersion, String osName, String osVersion) {
        TblMle tblMleObj = null;
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
            log.error("Unexpected errror during VMM MLE name generation. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during VMM MLE name generation. " + ex.getMessage());
        }   
    }

    /**
     * TODO: In 2.0 we need to add additional properties having the URL of attestation service.
     * If the attestation service is local, then we can instantiate the Business layer directly. If
     * not we need to use the REST API service.
     * @return 
     */
    private boolean isAttestationServiceLocal(){
        return true;
    }
        
    /**
     * TODO: In 2.0 we need to add additional properties having the URL of White List service.
     * If the white list service is local, then we can instantiate the Business layer directly. If
     * not we need to use the REST API service.
     * @return 
     */
    private boolean isWhiteListServiceLocal(){
        return true;
    }
        /*public void TestMLENameIssue() {
        try {

            TxtHostRecord hostObj = new TxtHostRecord();
            hostObj.HostName = "10.1.71.155";
            hostObj.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";

            HostAgentFactory hostAgentFactory = new HostAgentFactory();
            HostAgent hostAgent = hostAgentFactory.getHostAgent(new ConnectionString(hostObj.AddOn_Connection_String), new InsecureTlsPolicy());
            TxtHostRecord hostDetails = hostAgent.getHostDetails();
            hostObj.BIOS_Oem = hostDetails.BIOS_Oem;
            hostObj.BIOS_Version = hostDetails.BIOS_Version;
            hostObj.VMM_Name = hostDetails.VMM_Name;
            hostObj.VMM_Version = hostDetails.VMM_Version;
            hostObj.VMM_OSName = hostDetails.VMM_OSName;
            hostObj.VMM_OSVersion = hostDetails.VMM_OSVersion;
            hostObj.Processor_Info = hostDetails.Processor_Info;

            hostObj.VMM_OSName = hostObj.VMM_OSName.replace(' ', '_');
            if (hostObj.BIOS_Oem.contains("Intel")) {
                hostObj.BIOS_Version = hostObj.BIOS_Version.split("\\.")[4].toString();
            }

            hostObj.BIOS_Name = hostObj.BIOS_Oem.replace(' ', '_');           
            hostObj.VMM_Version = hostObj.VMM_OSVersion + "-" + hostObj.VMM_Version;

            if (hostObj.VMM_OSName.contains("ESX")) {
                hostObj.VMM_Name = hostObj.VMM_OSName;
            }

            String platformName = getPlatformName(hostObj.Processor_Info);
            hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + platformName + "_" + hostObj.VMM_Name;

            PcrManifest pcrManifest = hostAgent.getPcrManifest();
            HostReport hostReport = new HostReport();
            hostReport.pcrManifest = pcrManifest;
            hostReport.tpmQuote = null; // TODO
            hostReport.variables = new HashMap<String,String>(); // TODO
            if( hostAgent.isAikAvailable() ) {
                if( hostAgent.isAikCaAvailable() ) {
                    hostReport.aik = new Aik(hostAgent.getAikCertificate());
                    // TODO: if the host sends an aik cert, tthen it should ALSO send the privacy ca cert that signed it, and then we can add it to the report hre... instaead of having to contact the database, for exapmle, to try and finding a matching ca first and then add it here.
                }
                else {
                    hostReport.aik = new Aik(hostAgent.getAik()); 
                }
            }

            TblHosts tblHosts = new TblHosts();
            tblHosts.s
        } catch (MSException me) {
            log.error("Error during VMM MLE name generation. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
            
        } catch (Exception ex) {
            log.error("Unexpected errror during VMM MLE name generation. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during VMM MLE name generation. " + ex.getMessage());
        }        
        
    }*/
   
    /**
     * XXX TODO : THIS IS A DUPLICATE OF WHAT IS THERE IN ATTESTATION SERVICE HOSTBO.JAVA. IF YOU MAKE ANY CHANGE, PLEASE
     * CHANGE IT IN THE OTHER LOCATION AS WELL.
     * @param hostAikCert
     * @return 
     */
    private boolean isAikCertificateTrusted(X509Certificate hostAikCert) {
        log.debug("isAikCertificateTrusted {}", hostAikCert.getSubjectX500Principal().getName());
        // TODO read privacy ca certs from database and see if any one of them signed it. 
        // read privacy ca certificate.  if there is a privacy ca list file available (PrivacyCA.pem) we read the list from that. otherwise we just use the single certificate in PrivacyCA.cer (DER formatt)
        HashSet<X509Certificate> pcaList = new HashSet<X509Certificate>();
        try {
            InputStream privacyCaIn = new FileInputStream(ResourceFinder.getFile("PrivacyCA.p12.pem")); // may contain multiple trusted privacy CA certs
            List<X509Certificate> privacyCaCerts = X509Util.decodePemCertificates(IOUtils.toString(privacyCaIn));
            pcaList.addAll(privacyCaCerts);
            IOUtils.closeQuietly(privacyCaIn);
            log.debug("Added {} certificates from PrivacyCA.p12.pem", privacyCaCerts.size());
        }
        catch(Exception e) {
            // FileNotFoundException: cannot find PrivacyCA.pem
            // CertificateException: error while reading certificates from file
            log.error("Cannot load PrivacyCA.p12.pem");            
        }
        try {
            InputStream privacyCaIn = new FileInputStream(ResourceFinder.getFile("PrivacyCA.cer")); // may contain multiple trusted privacy CA certs
            X509Certificate privacyCaCert = X509Util.decodeDerCertificate(IOUtils.toByteArray(privacyCaIn));
            pcaList.add(privacyCaCert);
            IOUtils.closeQuietly(privacyCaIn);
            log.info("Added certificate from PrivacyCA.cer");
        }
        catch(Exception e) {
            // FileNotFoundException: cannot find PrivacyCA.cer
            // CertificateException: error while reading certificate from file
            log.error("Cannot load PrivacyCA.cer", e);            
        }
        boolean validCaSignature = false;
        for(X509Certificate pca : pcaList) {
            try {
                if( Arrays.equals(pca.getSubjectX500Principal().getEncoded(), hostAikCert.getIssuerX500Principal().getEncoded()) ) {
                    log.debug("Found matching CA: {}", pca.getSubjectX500Principal().getName());
                    pca.checkValidity(hostAikCert.getNotBefore()); // Privacy CA certificate must have been valid when it signed the AIK certificate
                    hostAikCert.verify(pca.getPublicKey()); // verify the trusted privacy ca signed this aik cert.  throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
                    // TODO read the CRL for this privacy ca and ensure this AIK cert has not been revoked
                    // TODO check if the privacy ca cert is self-signed... if it's not self-signed  we should check for a path leading to a known root ca in the root ca's file
                    validCaSignature = true;
                }
            }
            catch(Exception e) {
                log.error("Failed to verify AIK signature with CA", e); // but don't re-throw because maybe another cert in the list is a valid signer
            }
        }
        return validCaSignature;
    }
    
}
