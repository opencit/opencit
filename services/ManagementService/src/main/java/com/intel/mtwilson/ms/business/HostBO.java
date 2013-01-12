/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.business;

import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.controller.TblEventTypeJpaController;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblEventType;
import com.intel.mtwilson.as.data.TblMle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.mtwilson.agent.*;
import com.intel.mtwilson.ms.helper.BaseBO;
import com.intel.mtwilson.ms.helper.HostInfoInterface;
import com.intel.mtwilson.ms.helper.OpenSourceVMMHelper;
import com.intel.mtwilson.ms.helper.VMWareHelper;
import com.intel.mtwilson.*;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.crypto.RsaCredential;
import com.intel.mtwilson.io.Filename;
import com.intel.mtwilson.util.MWException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.codehaus.plexus.util.StringUtils;
/**
 *
 * @author ssbangal
 */

public class HostBO extends BaseBO {
    private static int MAX_BIOS_PCR = 6;
    private static int LOCATION_PCR = 22;
    //private static String propertiesFile = "management-service.properties";
    //Configuration config = ConfigurationFactory.loadConfiguration(propertiesFile);
    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    private byte[] dataEncryptionKey;
    
    public void setDataEncryptionKey(byte[] key) {
        dataEncryptionKey = key;
    }
    
    public HostBO() {
    }
    
    private ApiClient createAPIObject() {
        ApiClient rsaApiClient = null;
        
        try {
            // Retrieve the required values from the configuration
            String keyAliasName = MSConfig.getConfiguration().getString("mtwilson.api.key.alias");
            String keyPassword = MSConfig.getConfiguration().getString("mtwilson.api.key.password");
            URL baseURL = new URL(MSConfig.getConfiguration().getString("mtwilson.api.baseurl"));
            
            String keystoreFilename = MSConfig.getConfiguration().getString("mtwilson.ms.keystore.dir") + File.separator + Filename.encode(keyAliasName) + ".jks";
            
            // Open the keystore with the password and retrieve the credentials
            File keyStoreFile = new File(keystoreFilename);
            SimpleKeystore keystore = new SimpleKeystore(keyStoreFile, keyPassword);
            RsaCredential credential = keystore.getRsaCredentialX509(keyAliasName, keyPassword);            

            Properties prop = new Properties();
            prop.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true"); // must be secure out of the box!
            prop.setProperty("mtwilson.api.ssl.verifyHostname", "true"); // must be secure out of the box!            

            rsaApiClient = new ApiClient(baseURL, credential, keystore, new MapConfiguration(prop));
            log.info("Successfully created the API object for Management Service");

        } catch (MSException me) {
            log.error("Error during Api Client registration. " + me.getErrorCode() + " :" + me.getErrorMessage());            
            throw me;
            
        } catch (Exception ex) {

            log.error("Error while creating the Api Client object. " + ex.getMessage());
            throw new MSException(ErrorCode.SYSTEM_ERROR, "Error while creating the Api Client object. " + ex.getMessage(), ex);

        }
        
        return rsaApiClient;            
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
                
        try {  
            
            if (hostObj != null) {
                
                hostConfigObj = new HostConfigData();
                hostConfigObj.setTxtHostRecord(hostObj);
                
                // Set the default parameters
                hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
                hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_GLOBAL);
            }
            
            registerStatus = registerHostFromCustomData(hostConfigObj);           
            
        } catch (MSException me) {
            log.error("Error during host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
            
        } catch (Exception ex) {
            
            log.error("Unexpected errror during host registration. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during host registration." + ex.getMessage());
        }        
        return registerStatus;
    }
    
    /**
     * Author: Sudhir
     * 
     * Registers the host using the additional details like which MLE to use etc for registration.
     * 
     * @param hostConfigObj : Host Configuration object having the details of the host to be registered
     * along with the details of the MLE that needs to be used for registration.
     * 
     * @return : True if success or else an exception.
     */
    public boolean registerHostFromCustomData(HostConfigData hostConfigObj) {
        
        boolean registerStatus = false;
        HostInfoInterface vmmHelperObj = null;
        TxtHost txtHost = null;
        
        try {  

            ApiClient apiClient = createAPIObject();
            
            // extract the host object
            TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();
            log.debug("Starting to process the registration for host: " + hostObj.HostName);            
            
            TblHostsJpaController hostsJpaController = new TblHostsJpaController(getASEntityManagerFactory(), dataEncryptionKey);
            
            // First let us check if the host is already configured. If yes, we will return back success
            TblHosts hostSearchObj = hostsJpaController.findByName(hostObj.HostName);
            if (hostSearchObj == null) {
                hostSearchObj = hostsJpaController.findByIPAddress(hostObj.IPAddress);
            }
            
            // If the host already exists in the Mt.Wilson, all we will do is check what is the new MLE
            // that the user has opted for, update the host with the corresponding MLEs.
            if (hostSearchObj != null) {
                
                log.info(String.format("Since '%s' is already configured, we will update the host with the new MLEs.",
                        hostSearchObj.getName()));
                boolean updateHostStatus = updateHost(apiClient, hostSearchObj, hostConfigObj);
                return updateHostStatus;
            }
                
            // bug #497   this should be a different object than TblHosts  
            TblHosts tblHosts = new TblHosts();
            tblHosts.setSSLPolicy("TRUST_FIRST_CERTIFICATE");  // XXX  we are assuming that the host is in an initial trusted state and that no attackers are executing a man-in-the-middle attack against us at the moment.  TODO maybe we need an option for a global default policy (including global default trusted certs or ca's) to choose here and that way instead of us making this assumption, it's the operator who knows the environment.
            tblHosts.setSSLCertificate(new byte[0]); 
            tblHosts.setName(hostObj.HostName);
            tblHosts.setAddOnConnectionInfo(hostObj.AddOn_Connection_String);
            tblHosts.setIPAddress(hostObj.IPAddress);
            if( hostObj.Port != null ) { tblHosts.setPort(hostObj.Port); }
            
                        
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
            
            /*
            // Create the appropriate interface object based on the type of the host
            if (hostObj.AddOn_Connection_String == null || hostObj.AddOn_Connection_String.isEmpty()) {
                vmmHelperObj = (HostInfoInterface) new OpenSourceVMMHelper();
            }
            else {
                vmmHelperObj = (HostInfoInterface) new VMWareHelper();
            }
                        
            try {
                
                hostObj = vmmHelperObj.getHostDetails(hostObj);
                
            } catch (Throwable te) {
                throw new MSException(ErrorCode.MS_HOST_COMMUNICATION_ERROR, te.getMessage());
            }
            * */
            
            // Let us verify if we got all the data back correctly or not (Bug: 442)
            if (hostObj.BIOS_Oem == null || hostObj.BIOS_Version == null || hostObj.VMM_OSName == null || hostObj.VMM_OSVersion == null || hostObj.VMM_Version == null) {
                throw new MSException(ErrorCode.MS_HOST_CONFIGURATION_ERROR);
            }
            
            hostConfigObj.setTxtHostRecord(hostObj);
            log.info("Successfully retrieved the host information. Details: " + hostObj.BIOS_Oem + ":" + 
                    hostObj.BIOS_Version  + ":" + hostObj.VMM_OSName + ":" + hostObj.VMM_OSVersion + 
                    ":" + hostObj.VMM_Version);

            // Let us first verify if all the configuration details required for host registration already exists 
            boolean verifyStatus = verifyMLEForHost(hostConfigObj);
            
            if (verifyStatus == true) {
             
                // Finally register the host.
                txtHost = new TxtHost(hostObj);
                apiClient.addHost(txtHost); 
            }
                       
            // If everything is successful, set the status flag to true
            registerStatus = true;
            log.debug("Successfully registered the host: " + hostObj.HostName);
            
        } catch (MSException me) {
            log.error("Error during host registration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
            
        } catch (ApiException ae) {
            
            log.error("API Client error during host registration. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString() + ":" + ae.getMessage());

        } catch (Exception ex) {
            
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
        HostWhiteListTarget hostVMMWLTargetObj = null, hostBIOSWLTargetObj=null;
        TxtHostRecord hostObj = new TxtHostRecord();

        try {  

            // Get the new WL target for the host
            hostBIOSWLTargetObj = hostConfigObj.getBiosWLTarget();
            hostVMMWLTargetObj = hostConfigObj.getVmmWLTarget();
            
            // Get the current values of the host
            hostObj.HostName = tblHostObj.getName();
            hostObj.IPAddress = tblHostObj.getIPAddress();
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
            
            // Find out what is the current White List target that the host is configured. For
            // white list target of "Specified Good Known host", we will have the host name appended
            // to the VMM_Name. For "OEM specific ones" we will have the OEM name appended to it. This
            // is what we will look out for now.
            
            // First we will process the BIOS MLE
            if (hostObj.BIOS_Name.startsWith(String.format("%s_", hostObj.HostName))) {
                
                log.info(String.format("'%s' is currently configured to use '%s' BIOS MLE '%s'.", 
                        hostObj.HostName, HostWhiteListTarget.BIOS_HOST.getValue(), hostObj.BIOS_Name));

                // The host is currently configured for BIOS_HOST option. 
                // We need to check the new MLE that the user wants to use. Accordingly we will update the BIOS name.
                if (hostBIOSWLTargetObj == HostWhiteListTarget.BIOS_HOST) {
                    
                    // Then we do not need to do anything. We are set. 
                    log.info(String.format("'%s' is being updated to use '%s' BIOS MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.BIOS_HOST.getValue(), hostObj.BIOS_Name));

                } else if (hostBIOSWLTargetObj == HostWhiteListTarget.BIOS_OEM) {
                    
                    // Now the user wants to change from HOST SPECIFIC option to OEM SPECIFIC option. So,
                    // We need to change the name of BIOS_Name to remove the Host Name                    
                    hostObj.BIOS_Name = hostObj.BIOS_Name.substring(String.format("%s_", hostObj.HostName).length());                    
                    
                    log.info(String.format("'%s' is being updated to use '%s' BIOS MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.BIOS_OEM.getValue(), hostObj.BIOS_Name));

                } else {
                    
                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostBIOSWLTargetObj.toString());               
                }
                
            } else {
                
                log.info(String.format("'%s' is currently configured to use '%s' BIOS MLE '%s'.", 
                        hostObj.HostName, HostWhiteListTarget.BIOS_OEM.getValue(), hostObj.BIOS_Name));

                // The host is currently configured for BIOS_OEM option. 
                // We need to check the new MLE that the user wants to use. Accordingly we will
                // update the BIOS and VMM names.
                if (hostBIOSWLTargetObj == HostWhiteListTarget.BIOS_HOST) {
                    
                    // NOTE: This condition is there only if people use the API directly. If the user uses UI
                    // This option will not be allowed. You can only change from HOST SPECIFIC option but not to
                    // HOST SPECIFIC.  
                    log.info(String.format("'%s' is being updated to use '%s' BIOS MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.BIOS_HOST.getValue(), hostObj.BIOS_Name));
                    
                   throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostBIOSWLTargetObj.toString() + "." +
                           " Use WhiteList configuration option to register with Host specific white list values.");

                } else if (hostBIOSWLTargetObj == HostWhiteListTarget.BIOS_OEM) {
                    
                    // We do not need to do anything;
                    log.info(String.format("'%s' is being updated to use '%s' BIOS MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.BIOS_OEM.getValue(), hostObj.BIOS_Name));
                    
                } else {
                    
                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostBIOSWLTargetObj.toString());               
                }
            }
                        

            // Now let us process the VMM MLE and modify the MLE Names if needed
            if (hostObj.VMM_Name.startsWith(String.format("%s_", hostObj.HostName))) {
                
                log.info(String.format("'%s' is currently configured to use '%s' VMM MLE '%s'.", 
                        hostObj.HostName, HostWhiteListTarget.VMM_HOST.getValue(), hostObj.VMM_Name));

                // The host is currently configured for VMM_HOST option. 
                // We need to check the new MLE that the user wants to use. Accordingly we will update the VMM name.
                if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_HOST) {
                    
                    // Then we do not need to do anything. We are set. Just ignore the register host
                    // request and send back success.
                    log.info(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.VMM_HOST.getValue(), hostObj.VMM_Name));

                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_OEM) {
                    
                    // Now the user wants to change from SPECIFIC HOST option to OEM specific option. So,
                    // We need to change the name of VMM_Name to remove the Host Name                    
                    hostObj.VMM_Name = hostObj.VMM_Name.substring(String.format("%s_", hostObj.HostName).length());
                    
                    // Also we need to add back the OEM name to the VMM_Name. We do not need to add it for
                    // BIOS as BIOS_Name is always OEM specific.                    
                    hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + hostObj.VMM_Name;
                    
                    log.info(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.VMM_OEM.getValue(), hostObj.VMM_Name));

                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_GLOBAL) {
                    
                    // Now the user wants to change from SPECIFIC HOST option to Global option. So,
                    // We need to change the name of VMM_Name to remove the Host Name                    
                    hostObj.VMM_Name = hostObj.VMM_Name.substring(String.format("%s_", hostObj.HostName).length());

                    log.info(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.VMM_GLOBAL.getValue(), hostObj.VMM_Name));
                } else {
                    
                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostVMMWLTargetObj.toString());               
               
                }
                
            } else if (hostObj.VMM_Name.startsWith(String.format("%s_", hostObj.BIOS_Oem.split(" ")[0].toString()))) {
                
                log.info(String.format("'%s' is currently configured to use '%s' VMM MLE '%s'.", 
                        hostObj.HostName, HostWhiteListTarget.VMM_OEM.getValue(), hostObj.VMM_Name));

                // The host is currently configured for VMM_OEM option. 
                // We need to check the new MLE that the user wants to use. Accordingly we will update VMM name.
                if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_HOST) {
                    
                    // NOTE: This condition is there only if people use the API directly. If the user uses UI
                    // This option will not be allowed. You can only change from HOST SPECIFIC option but not to
                    // HOST SPECIFIC.  
                    
                    log.info(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.VMM_HOST.getValue(), hostObj.VMM_Name));

                   throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostVMMWLTargetObj.toString() + "." +
                           " Use WhiteList configuration option to register with Host specific white list values.");

                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_OEM) {
                    
                    // We do not need to do anything;
                    log.info(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.VMM_OEM.getValue(), hostObj.VMM_Name));
                    
                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_GLOBAL) {
                    
                    // Now the user wants to change from VMM_OEM option to Global 
                    // option. So,we need to change the names of VMM_Name 
                    hostObj.VMM_Name = hostObj.VMM_Name.substring(String.format("%s_", hostObj.BIOS_Oem.split(" ")[0].toString()).length());

                    log.info(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.VMM_GLOBAL.getValue(), hostObj.VMM_Name));
                } else {
                    
                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostVMMWLTargetObj.toString());               
                }
                
            } else {

                log.info(String.format("'%s' is currently configured to use '%s' VMM MLE '%s'.", 
                        hostObj.HostName, HostWhiteListTarget.VMM_GLOBAL.getValue(), hostObj.VMM_Name));

                // The host is currently configured for VMM_GLOBAL option. 
                // We need to check the new MLE that the user wants to use. Accordingly we will update the VMM name.
                if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_HOST) {
                    
                    // NOTE: This condition is there only if people use the API directly. If the user uses UI
                    // This option will not be allowed. You can only change from HOST SPECIFIC option but not to
                    // HOST SPECIFIC.  

                    log.info(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.VMM_HOST.getValue(), hostObj.VMM_Name));
                   
                   throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostVMMWLTargetObj + "." +
                           " Use WhiteList configuration option to register with Host specific white list values.");

                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_OEM) {
                    
                    // We need to add OEM name to the VMM_Name.                    
                    hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + hostObj.VMM_Name;

                    log.info(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.", 
                            hostObj.HostName, HostWhiteListTarget.VMM_OEM.getValue(), hostObj.VMM_Name));
                                        
                } else if (hostVMMWLTargetObj == HostWhiteListTarget.VMM_GLOBAL) {

                    // We do not need to do anything here.
                    log.info(String.format("'%s' is being updated to use '%s' VMM MLE '%s'.", 
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
            log.info(String.format("'%s' has been successfully updated to use the '%s' BIOS MLE and '%s' VMM MLE.", 
                    hostObj.HostName, hostObj.BIOS_Name, hostObj.VMM_Name));            
            
            updateStatus = true;
            
        } catch (MSException me) {
            log.error("Error during host update. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
            
        } catch (ApiException ae) {
            
            log.error("API Client exception during host update. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString() + ":" + ae.getMessage());

        } catch (Exception ex) {
            
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
     * @param gkvHost: User need to provide just the Host Name and vCenter Connection string in case of
     * VMware hosts. For Open Source hypervisors, user need to provide the IP Address and port number.
     * @return : true if the white list is configured successfully.
     */
    public boolean configureWhiteListFromHost(TxtHostRecord gkvHost) throws ApiException {
        HostConfigData hostConfigObj = null;
        boolean configStatus = false;
        
        try {  
            
            if (gkvHost != null) {
                
                hostConfigObj = new HostConfigData();
                hostConfigObj.setTxtHostRecord(gkvHost);
                
                // Set the default parameters
                hostConfigObj.setBiosWhiteList(true);
                hostConfigObj.setVmmWhiteList(true);
                hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
                hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_GLOBAL);
                hostConfigObj.setRegisterHost(true);
                hostConfigObj.setBiosPCRs(MSConfig.getConfiguration().getString("mtwilson.ms.biosPCRs").replace(';', ','));
                hostConfigObj.setVmmPCRs(MSConfig.getConfiguration().getString("mtwilson.ms.vmmPCRs").replace(';', ','));
            }
            
            configStatus = configureWhiteListFromCustomData(hostConfigObj);           
            
        } catch (MSException me) {
            log.error("Error during white list configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
            
        } catch (Exception ex) {
            
            log.error("Unexpected errror during white list configuration. " + ex.toString());
            ex.printStackTrace();
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during white list configuration." + ex.getMessage());
        }        
        
        return configStatus;
    }
    
       
    /**
     * Author: Sudhir
     * 
     * This function using the white list configuration settings including pcr details, whether the whitelist is
     * for an individual host/for OEM specific host/global white list, etc, configures the DB with the whitelist from
     * the specified good known host.
     * 
     * @param hostConfigObj : White List configuration object having all the details.
     * @return : true on success.
     */
    public boolean configureWhiteListFromCustomData(HostConfigData hostConfigObj) {
        String errorMessage = "";
        boolean configStatus = false;
        String attestationReport;
//        HostInfoInterface vmmHelperObj = null;
        boolean hostAlreadyConfigured = false;
        boolean biosMLEAlreadyExists = false;
        boolean vmmMLEAlreadyExists = false;
        
        try {
                        
            // Let us ensure that the user has specified the PCRs to be used
            if (hostConfigObj != null) {
                
                if ((hostConfigObj.addBiosWhiteList() == true) && (hostConfigObj.getBiosPCRs() == null || 
                        hostConfigObj.getBiosPCRs().isEmpty())) {
                    
                    throw new MSException(ErrorCode.MS_INVALID_PCRS);
                }
                
                if ((hostConfigObj.addVmmWhiteList() == true) && (hostConfigObj.getVmmPCRs() == null || 
                        hostConfigObj.getVmmPCRs().isEmpty())) {
                    
                    throw new MSException(ErrorCode.MS_INVALID_PCRS);
                }
                
                if ((hostConfigObj.addBiosWhiteList() == true) && (hostConfigObj.getBiosWLTarget() == null || 
                        hostConfigObj.getBiosWLTarget().getValue().isEmpty())) {
                    
                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostConfigObj.getBiosWLTarget().toString());
                }
                
                if ((hostConfigObj.addVmmWhiteList() == true) && (hostConfigObj.getVmmWLTarget() == null || 
                        hostConfigObj.getVmmWLTarget().getValue().isEmpty())) {
                    
                    throw new MSException(ErrorCode.MS_INVALID_WHITELIST_TARGET, hostConfigObj.getBiosWLTarget().toString());
                }                 
            }
            
            
            TxtHostRecord gkvHost = hostConfigObj.getTxtHostRecord();
            
            
            // bug #497   this should be a different object than TblHosts  
            TblHosts tblHosts = new TblHosts();
            tblHosts.setSSLPolicy("TRUST_FIRST_CERTIFICATE");  // XXX  we are assuming that the host is in an initial trusted state and that no attackers are executing a man-in-the-middle attack against us at the moment.  TODO maybe we need an option for a global default policy (including global default trusted certs or ca's) to choose here and that way instead of us making this assumption, it's the operator who knows the environment.
            tblHosts.setSSLCertificate(new byte[0]); 
            tblHosts.setName(gkvHost.HostName);
            tblHosts.setAddOnConnectionInfo(gkvHost.AddOn_Connection_String);
            tblHosts.setIPAddress(gkvHost.IPAddress);
            if( gkvHost.Port != null ) { tblHosts.setPort(gkvHost.Port); }

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
            } catch (Throwable te) {
                log.error("Unexpected error in configureWhiteListFromCustomData: {}", te.toString());
                te.printStackTrace();
                throw new MSException(te, ErrorCode.MS_HOST_COMMUNICATION_ERROR, te.getMessage());
            }
            
            /*
            // Create the appropriate interface object based on the type of the host   // BUG #497  this should be replaced with HostAgentFactory.getHostAgent()
            if (gkvHost.AddOn_Connection_String == null || gkvHost.AddOn_Connection_String.isEmpty()) {
                vmmHelperObj = (HostInfoInterface) new OpenSourceVMMHelper();
            }
            else {
                vmmHelperObj = (HostInfoInterface) new VMWareHelper();
            }
            
            try {
                
                gkvHost = vmmHelperObj.getHostDetails(gkvHost);
            } catch(MWException e) {
                log.error("Mt Wilson error: "+e.toString());
                throw new MSException(e, ErrorCode.MS_HOST_COMMUNICATION_ERROR, e.toString());
            } catch (Throwable e) {
                log.error("Unknown error: "+e.toString());
                throw new MSException(ErrorCode.MS_HOST_COMMUNICATION_ERROR, e.getMessage());
            }
            */ 
            log.info("Starting to process the white list configuration from host: " + gkvHost.HostName);                    
            

            // Let us verify if we got all the data back correctly or not (Bug: 442)
            if (gkvHost.BIOS_Oem == null || gkvHost.BIOS_Version == null || gkvHost.VMM_OSName == null || gkvHost.VMM_OSVersion == null || gkvHost.VMM_Version == null) {
                throw new MSException(ErrorCode.MS_HOST_CONFIGURATION_ERROR);
            }
            
            hostConfigObj.setTxtHostRecord(gkvHost);
            log.info("Successfully retrieved the host information. Details: " + gkvHost.BIOS_Oem + ":" + 
                    gkvHost.BIOS_Version  + ":" + gkvHost.VMM_OSName + ":" + gkvHost.VMM_OSVersion + 
                    ":" + gkvHost.VMM_Version);

            String reqdManifestList = "";

            TblHostsJpaController hostsJpaController = new TblHostsJpaController(getASEntityManagerFactory(), dataEncryptionKey);
            ApiClient apiClient = createAPIObject();
            
            // For OpenSource hosts, the host has to be registered first before we can extract the
            // white list measurements by taking the TPM ownership. But for VMware host there is no 
            // such constraint. But for VMware ESXi 5.1 hosts we cannot register the host without
            // configuring the white list first. So, we need to handle these two scenarios separately.
            if (gkvHost.AddOn_Connection_String == null || gkvHost.AddOn_Connection_String.isEmpty()) {

                // If in case the user wants to just add the BIOS or VMM white list, then also we need to add both first
                // then add the host and finally delete the MLEs that the user didn't want to configure.
                
                biosMLEAlreadyExists = configureBIOSMLE(apiClient, hostConfigObj);
                reqdManifestList = hostConfigObj.getBiosPCRs();

                vmmMLEAlreadyExists = configureVMMMLE(apiClient, hostConfigObj);
                reqdManifestList = reqdManifestList+ "," + hostConfigObj.getVmmPCRs();
                
                // First let us check if the host is already configured. If yes, we will return back success
                TblHosts hostSearchObj = hostsJpaController.findByName(gkvHost.HostName);
                if (hostSearchObj == null) {
                    log.info("Could not find the host using host name: " + gkvHost.HostName);
                    hostSearchObj = hostsJpaController.findByIPAddress(gkvHost.IPAddress);
                }
                
                if (hostSearchObj == null) {
                    log.info("Could not find the host using host IP address: " + gkvHost.IPAddress);
                    log.info("Creating a new host.");
                    
                    TxtHost hostObj = new TxtHost(gkvHost);
                    apiClient.addHost(hostObj);                    
                    log.info("Successfully registered the host : " + hostObj.getHostName());
                    
                } else {
                    hostAlreadyConfigured = true;
                    log.info("Database already has the configuration details for host : " + hostSearchObj.getName());
                }
                    
                
                try {

                    // Retrieve the attestation report from the host
//                    attestationReport = vmmHelperObj.getHostAttestationReport(gkvHost, reqdManifestList);
                    attestationReport = agent.getHostAttestationReport(reqdManifestList);  // generic HostAgent interface but we know we are talking to a trust agent host and we expect that format
                } catch (Throwable te) {
                    
                    // Bug# 467: We have seen cases where in because of an error with TrustAgent we may not
                    // get the attestation report. In such cases, we would have created the MLEs with empty values,
                    // which has to be deleted.
                    
                    // Delete the BIOS MLE
                    try {
                        if (biosMLEAlreadyExists == false)
                            deleteMLE(apiClient, gkvHost, true);
                    } catch (Exception ex) {
                        // ignore the error. It is already logged in the function
                    }
                    
                    // Delete the VMM MLE
                    try {
                        if (vmmMLEAlreadyExists == false)
                            deleteMLE(apiClient, gkvHost, false);
                    } catch (Exception ex) {
                        // ignore the error. It is already logged in the function
                    }

                    // Also note that we are ignoring any exception coming from the deleteMLE call as we do not
                    // want to lose the original error                    
                    log.error("Unexpected error while deleting MLE in registerHostFromCustomData: {}", te.toString());
                    te.printStackTrace();
                    throw new MSException(te, ErrorCode.MS_HOST_COMMUNICATION_ERROR, te.getMessage());
                }

                if (attestationReport != null && !attestationReport.isEmpty()) {
                    // Bug# 467: We do not want to delete the MLEs here since we were able to successfully make a call to 
                    // get the attestation report but it is not having the data. So, we are ok to leave the MLEs
                    // with empty white lists.
                    if (!attestationReport.contains("ComponentName"))
                        throw new MSException(ErrorCode.MS_INVALID_ATTESTATION_REPORT);
                }
                
                log.info("Successfully retrieved the attestation report from host: " + gkvHost.HostName);
                                   
                // Finally store the attestation report by calling into the WhiteList REST APIs
                uploadToDB(hostConfigObj, attestationReport, apiClient);
                log.info("Successfully updated the white list database with the good known white list from host: " + gkvHost.HostName);
                
                // Now that we have configured the white list, if the user did not want the host to be registered, delete it
                if (hostConfigObj.isRegisterHost() == false && hostAlreadyConfigured == false) {
                    apiClient.deleteHost(new Hostname((gkvHost.HostName)));
                    log.info(String.format("Successfully deleted the host '%s' as the user did not want the host to be registered.", gkvHost.HostName));
                    
                    // Also delete the MLEs that the user didn't intend to add. This is the fix for Bug# 478.
                    if ((hostConfigObj.addBiosWhiteList()== false) && (biosMLEAlreadyExists == false)) {
                        deleteMLE(apiClient, gkvHost, true);
                    } else {
                        // Since either a new BIOS MLE was created or an existing one was updated, we need to store the 
                        // mapping of the white list host that was used to configure the MLE in the DB.
                        configureMleSource(apiClient, gkvHost, true);
                        log.info("Successfully configured the details of the host that was used to white list the BIOS MLE - " + gkvHost.BIOS_Name);
                    }
                    
                    if ((hostConfigObj.addVmmWhiteList() == false) && (vmmMLEAlreadyExists == false)){
                        deleteMLE(apiClient, gkvHost, false);
                    } else {
                        // Since either a new VMM MLE was created or an existing one was updated, we need to store the 
                        // mapping of the white list host that was used to configure the MLE in the DB.
                        configureMleSource(apiClient, gkvHost, false);
                        log.info("Successfully configured the details of the host that was used to white list the VMM MLE - " + gkvHost.VMM_Name);
                    }                   
                }
                
                // If in case the host is already configured, let us update it with the latest MLE configuration
                if (hostAlreadyConfigured == true) {
                    apiClient.updateHost(new TxtHost(gkvHost));
                    log.info(String.format("Successfully updated the host '%s' with the new MLE information.", gkvHost.HostName));                    
                }
                                    
            } else {
                // Handle the VMware host

                // Now that we have retrieved the details of the host, let us configure the BIOS MLE if needed
                if (hostConfigObj.addBiosWhiteList()) {
                    configureBIOSMLE(apiClient, hostConfigObj);
                    reqdManifestList = hostConfigObj.getBiosPCRs();
                }

                // Configure the VMM MLE if needed
                if (hostConfigObj.addVmmWhiteList()) {
                    configureVMMMLE(apiClient, hostConfigObj);
                    if (reqdManifestList.isEmpty())
                        reqdManifestList = hostConfigObj.getVmmPCRs();
                    else
                        reqdManifestList = reqdManifestList+ "," + hostConfigObj.getVmmPCRs();
                }
                
                try {

                    // Retrieve the attestation report from the host
//                    attestationReport = vmmHelperObj.getHostAttestationReport(gkvHost, reqdManifestList);
                    attestationReport = agent.getHostAttestationReport(reqdManifestList);   // generic HostAgent interface but we know we are talking to a vmware host and we expect that format
                    log.info("Successfully retrieved the attestation report from host: " + gkvHost.HostName);
                } catch (Throwable te) {
                    log.error("Unexpected error from getHostAttestationReport in registerHostFromCustomData: {}", te.toString());
                    te.printStackTrace();
                    throw new MSException(te, ErrorCode.MS_HOST_COMMUNICATION_ERROR, te.getMessage());
                }
                                                    
                if (attestationReport != null && !attestationReport.isEmpty()) {
                    if (!attestationReport.contains("ComponentName")) {
                        log.info("Attestation report content: "+attestationReport);
                        throw new MSException(ErrorCode.MS_INVALID_ATTESTATION_REPORT);
                    }
                }
                    
                
                log.info("Successfully retrieved the attestation report from host: " + gkvHost.HostName);

                // Finally store the attestation report by calling into the WhiteList REST APIs
                uploadToDB(hostConfigObj, attestationReport, apiClient);
                log.info("Successfully updated the white list database with the good known white list from host: " + gkvHost.HostName);
                    

                // Register host only if required.
                if (hostConfigObj.isRegisterHost() == true) {
                    
                    // First let us check if the host is already configured. If yes, we will return back success
                    TblHosts hostSearchObj = hostsJpaController.findByName(gkvHost.HostName);
                    if (hostSearchObj == null) {
                        log.info("Could not find the host using host name: " + gkvHost.HostName);
                        hostSearchObj = hostsJpaController.findByIPAddress(gkvHost.IPAddress);
                    }

                    if (hostSearchObj == null) {
                        log.info("Could not find the host using host IP address: " + gkvHost.IPAddress);
                        log.info("Creating a new host.");

                        TxtHost hostObj = new TxtHost(gkvHost);
                        apiClient.addHost(hostObj);

                        log.info("Successfully registered the host : " + hostObj.getHostName());

                    } else {
                        log.info("Database already has the configuration details for host : " + hostSearchObj.getName());

                        // Since we might have changed the MLE configuration on the host, let us update the host
                        if (gkvHost.Port == null) {
                            gkvHost.Port = 0;
                        }
                        TxtHost newHostObj = new TxtHost(gkvHost);
                        apiClient.updateHost(newHostObj);
                        log.info(String.format("Successfully updated the host %s with the new MLE information.", gkvHost.HostName));                                            
                    }
                }
                
                // Now we need to configure the MleSource table with the details of the host that was used for white listing the MLE.
                if (hostConfigObj.addBiosWhiteList()) {
                    configureMleSource(apiClient, gkvHost, true);
                    log.info("Successfully configured the details of the host that was used to white list the BIOS MLE - " + gkvHost.BIOS_Name);
                }
                
                if (hostConfigObj.addVmmWhiteList()) {
                    configureMleSource(apiClient, gkvHost, false);
                    log.info("Successfully configured the details of the host that was used to white list the VMM MLE - " + gkvHost.VMM_Name);
                }
            }            

            configStatus = true;
            
        } catch (MSException me) {
            log.error("Error during white list configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
            
        } catch (ApiException ae) {
            
            log.error("API Client error during white list configuration. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString() + 
                    ": " + ae.getMessage());

        } catch (Exception ex) {
            
            log.error("Unexpected errror during white list configuration. " + ex.toString());
            ex.printStackTrace();
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during white list configuration." + ex.getMessage());
        }     
        
        return configStatus;
    }
    
    /**
     * Author: Sudhir
     * 
     * Verifies if all the MLE configuration exists for the host to be registered.
     * 
     * @param hostObj: Host object having the details of the  host to be registered.
     * @param wlTarget: Indicates whether the host to be registered has to use global white list or
     * OEM specific white list.
     * @return : True if all the backend configuration exists, if not throws an exception.
     */
    public boolean verifyMLEForHost(HostConfigData hostConfigObj) {
        boolean verifyStatus = false;
        String errorMessage = "";
        
        try {
            
            TblOemJpaController oemJpa = new TblOemJpaController(getASEntityManagerFactory());
            TblOsJpaController osJpa = new TblOsJpaController((getASEntityManagerFactory()));
            TblMleJpaController mleJpa = new TblMleJpaController(getASEntityManagerFactory());
            
            // Retrieve the host object.
            TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();
            
            // Need to do some data massaging. Firstly we need to change the White Spaces
            // in the OS name to underscores. In case of Intel's BIOS, need to trim it since it will be very long.
            hostObj.VMM_OSName = hostObj.VMM_OSName.replace(' ', '_');
            if (hostObj.BIOS_Oem.contains("Intel"))
                hostObj.BIOS_Version = hostObj.BIOS_Version.split("\\.")[4].toString();          

            // Update the host object with the names of BIOS and VMM, which is needed during host registration.
            hostObj.BIOS_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + hostObj.VMM_OSName.split("_")[0].toString();
            hostObj.VMM_Version = hostObj.VMM_OSVersion + "-" + hostObj.VMM_Version;
            
            // For VMware since there is no separate OS and VMM, we use the same name
            if (hostObj.VMM_OSName.contains("ESX"))
                hostObj.VMM_Name = hostObj.VMM_OSName;
            
            // We need to handle the case where the user might want to use the OEM specific White List. By default it is
            // the global value.
            if (hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_OEM)
                hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + hostObj.VMM_Name;            
            
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
            
            log.error("Unexpected errror during MLE verification of host " + hostConfigObj.getTxtHostRecord().HostName + ". " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Errror during MLE verification for host " + 
                    hostConfigObj.getTxtHostRecord().HostName + ". " + ex.getMessage());
        }        
        
        return verifyStatus;
    }


    /**
     * Author: Sudhir
     * 
     * Configures the BIOS MLE using the Good known host specified in the WhiteListConfiguration object.
     * 
     * @param apiClientObj : ApiClient object for making the ApiClient calls.
     * @param hostConfigObj : WhiteListConfiguration object having the details of the Host and how the white list
     * should be configured.
     * @return : A boolean indicating if the BIOS MLE was created or it already existed.
     */
    private boolean configureBIOSMLE(ApiClient apiClientObj, HostConfigData hostConfigObj) {
        boolean biosMLEAlreadyExists = false;

        try {
            // Extract the host object
            TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();
            
            TblOemJpaController oemJpa = new TblOemJpaController(getASEntityManagerFactory());
            TblMleJpaController mleJpa = new TblMleJpaController(getASEntityManagerFactory());
            
            WhitelistService wlApiClient = (WhitelistService) apiClientObj;

            // Need to do some data massaging. Firstly we need to change the White Spaces
            // in the OS name to underscores. This is to ensure that it works correctly with
            // the WLM portal. In case of Intel's BIOS, need to trim it since it will be very long.
            String tempVMMOSName = hostObj.VMM_OSName.replace(' ', '_');
            if (hostObj.BIOS_Oem.contains("Intel"))
                hostObj.BIOS_Version = hostObj.BIOS_Version.split("\\.")[4].toString();           

            // Update the host object with the names of BIOS. For the name we are using a combination of the OEM
            // and the hypervisor running on the host since we have seen different PCR 0 for the same
            // OEM having different hypervisors.
            hostObj.BIOS_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + tempVMMOSName.split("_")[0].toString();

            // If we are setting host specific MLE, then we need to append the host name to the BIOS Name as well
            if (hostConfigObj != null && hostConfigObj.getBiosWLTarget() == HostWhiteListTarget.BIOS_HOST)
                hostObj.BIOS_Name = hostObj.HostName + "_" + hostObj.BIOS_Name;
            
            TblOem oemTblObj = oemJpa.findTblOemByName(hostObj.BIOS_Oem);
            
            // Create the OEM if it does not exist
            if (oemTblObj == null) {
                
                OemData oemObj = new OemData(hostObj.BIOS_Oem, hostObj.BIOS_Oem);
                wlApiClient.addOEM(oemObj);                
                log.info("Successfully created the OEM : " + hostObj.BIOS_Oem);
                
            } else
                log.info("Database already has the configuration details for OEM : " + hostObj.BIOS_Oem);
                      
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
            if (biosPCRs.isEmpty())
                biosPCRs = "0";            
            String[] biosPCRList = biosPCRs.split(",");
            
            List<ManifestData> biosMFList = new ArrayList<ManifestData>();            
            for (String biosPCR : biosPCRList){
                biosMFList.add(new ManifestData(biosPCR, " "));
            }
            
            mleObj.setManifestList(biosMFList);
            
            // If the MLE does not exist, then let us create it.
            TblMle tblMleObj = mleJpa.findBiosMle(hostObj.BIOS_Name, hostObj.BIOS_Version, hostObj.BIOS_Oem);
            if (tblMleObj == null ) {
                
                wlApiClient.addMLE(mleObj);
                log.info("Successfully created the BIOS MLE : " + hostObj.BIOS_Name);
                
            } else {
                biosMLEAlreadyExists = true;
                log.info("Database already has the configuration details for BIOS MLE : " + hostObj.BIOS_Name);
            }

        } catch (MSException me) {
            
            log.error("Error during OEM - BIOS MLE configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
            
        } catch (ApiException ae) {
            
            log.error("API Client error during OEM - BIOS MLE configuration. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString() +
                    ": Error during OEM-BIOS MLE Configuration. " + ae.getMessage());

        } catch (Exception ex) {
            
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
     * @param hostConfigObj : WhiteListConfiguration object having the details of the Host and how the white list
     * should be configured.
     * @return : A boolean indicating if the VMM MLE was created or it already existed.
     */
    private boolean configureVMMMLE(ApiClient apiClientObj, HostConfigData hostConfigObj) {
        String attestationType = "";
        boolean vmmMLEAlreadyExists = false;
        try {
            
            TxtHostRecord hostObj = hostConfigObj.getTxtHostRecord();
            
            TblOsJpaController osJpa = new TblOsJpaController((getASEntityManagerFactory()));
            TblMleJpaController mleJpa = new TblMleJpaController(getASEntityManagerFactory());
            
            WhitelistService wlApiClient = (WhitelistService) apiClientObj;

            // Need to do some data massaging. Firstly we need to change the White Spaces
            // in the OS name to underscores. This is to ensure that it works correctly with
            // the WLM portal. In case of Intel's BIOS, need to trim it since it will be very long.
            hostObj.VMM_OSName = hostObj.VMM_OSName.replace(' ', '_');
            if ((hostObj.VMM_OSName.contains("ESX")) && hostObj.VMM_OSVersion.contains("5.1"))
                attestationType = "MODULE";
            else
                attestationType = "PCR";
            
            // Update the host object with the names of BIOS and VMM, which is needed during
            // host registration.
            hostObj.VMM_Version = hostObj.VMM_OSVersion + "-" + hostObj.VMM_Version;
            
            // For VMware since there is no separate OS and VMM, we use the same name
            if (hostObj.VMM_OSName.contains("ESX"))
                hostObj.VMM_Name = hostObj.VMM_OSName;
                        
            TblOs tblOsObj = osJpa.findTblOsByNameVersion(hostObj.VMM_OSName, hostObj.VMM_OSVersion);
            if (tblOsObj == null) {
            
                // Now let us create the OS information corresponding to the host
                OsData osObj = new OsData(hostObj.VMM_OSName, hostObj.VMM_OSVersion, "");
                wlApiClient.addOS(osObj);
                log.info("Successfully created the OS : " + hostObj.VMM_OSName);
                
            } else
                log.info("Database already has the configuration details for the OS : " + hostObj.VMM_OSName);
            
            // If we are setting host specific MLE, then we need to append the host name to the VMM Name as well
            if (hostConfigObj != null && hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_HOST)
                hostObj.VMM_Name = hostObj.HostName + "_" + hostObj.VMM_Name;
            else if (hostConfigObj != null && hostConfigObj.getVmmWLTarget() == HostWhiteListTarget.VMM_OEM)
                hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0].toString() + "_" + hostObj.VMM_Name;
            
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
            for (String vmmPCR : vmmPCRList){
                vmmMFList.add(new ManifestData(vmmPCR, " "));
            }

            mleVMMObj.setManifestList(vmmMFList);
            
            TblMle tblMleObj = mleJpa.findVmmMle(hostObj.VMM_Name, hostObj.VMM_Version, hostObj.VMM_OSName, hostObj.VMM_OSVersion);
            if (tblMleObj == null) {
    
                wlApiClient.addMLE(mleVMMObj); 
                log.info("Successfully created the VMM MLE : " + hostObj.VMM_Name);
                
            } else {
                vmmMLEAlreadyExists = true;
                log.info("Database already has the configuration details for VMM MLE : " + hostObj.VMM_Name);
            }

        } catch (MSException me) {
            
            log.error("Error during OS - VMM MLE configuration. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
            
        } catch (ApiException ae) {
            
            log.error("API Client error during OS - VMM MLE configuration. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString() +
                    ": Error during OEM-VMM MLE Configuration. " + ae.getMessage());

        } catch (Exception ex) {
            
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
    private void configureMleSource(ApiClient apiClientObj, TxtHostRecord hostObj, Boolean isBIOSMLE){
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
            mleSourceObj.setHostName(hostObj.HostName);
            
            // Since this function would be called during both creation and updation, we need to handle both the scenarios.
            try {
                apiClientObj.addMleSource(mleSourceObj);
            } catch (ApiException iae) {
                if (iae.getErrorCode() == ErrorCode.WS_MLE_SOURCE_MAPPING_ALREADY_EXISTS.getErrorCode()) {
                    // Since the mapping already exists, it means that the user is updating the white list. So, let us call the update method
                    apiClientObj.updateMleSource(mleSourceObj);
                }
                else {
                    throw new MSException(iae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(iae.getErrorCode()).toString() +
                        ": Error during MLE white list host mapping. " + iae.getMessage());                    
                }
            }
                        
        } catch (ApiException ae) {
            log.error("API Client error during deletion of MLE. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString() +
                    ": Error during MLE white list host mapping. " + ae.getMessage());
            
        } catch (Exception ex) {
            log.error("Error during MLE deletion. " + ex.getMessage());
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
    private void deleteMLE(ApiClient apiClientObj, TxtHostRecord hostObj, Boolean isBIOSMLE){
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
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString() +
                    ": Error during MLE Deletion. " + ae.getMessage());
            
        } catch (Exception ex) {
            log.error("Error during MLE deletion. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during MLE configuration. " + ex.getMessage());
            
        }
    }
    
    /**
     * Author : Sudhir
     * 
     * Uploads the attestation report (event info and pcr info) into the White List Database for the
     * specified good known host.
     * 
     * @param hostConfigObj : WhiteList Configuration object having the details of the host, which would be used
     * as the good known host
     * @param attestationReport : XML report having all the details of the good known measurements that need to
     * be uploaded to the database.
     * @param apiClientObj: ApiClient object.
     * 
     */
    private void uploadToDB(HostConfigData hostConfigObj, String attestationReport, ApiClient apiClientObj) {
        
        String vCenterVersion = "";
        String esxHostVersion = "";
        TblPcrManifestJpaController pcrJpa = new TblPcrManifestJpaController(getASEntityManagerFactory());
        TblModuleManifestJpaController moduleJpa = new TblModuleManifestJpaController(getASEntityManagerFactory());
        TblEventTypeJpaController eventJpa = new TblEventTypeJpaController(getASEntityManagerFactory());
        TblMleJpaController mleJpa = new TblMleJpaController(getASEntityManagerFactory());

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
            
            // Process all the Event and PCR nodes in the attestation report.
            while (reader.hasNext()){
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT){
                    if (reader.getLocalName().equalsIgnoreCase("Host_Attestation_Report")){
                        vCenterVersion = reader.getAttributeValue("", "vCenterVersion");
                        esxHostVersion = reader.getAttributeValue("", "HostVersion");
                    } else if (reader.getLocalName().equalsIgnoreCase("EventDetails") && (hostConfigObj.addVmmWhiteList() == true)){
                        
                        // Check if the package is a dynamic package. If it is, then we should not be storing it in the database
                        if (reader.getAttributeValue("", "PackageName").equals("") && 
                                reader.getAttributeValue("", "EventName").equalsIgnoreCase("Vim25Api.HostTpmSoftwareComponentEventDetails")) {
                            reader.next();
                            continue;
                        }
                        
                        // Event Details will be available only for ESXi 5.1 hosts. 
                        ModuleWhiteList moduleObj = new ModuleWhiteList();
                        if (reader.getAttributeValue("", "ComponentName").isEmpty())
                            moduleObj.setComponentName(" ");
                        else
                            moduleObj.setComponentName(reader.getAttributeValue("", "ComponentName"));
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
                        
                        // System.out.println(moduleObj.getComponentName() + ":::" + moduleObj.getDigestValue());
                        TblEventType eventSearchObj = eventJpa.findEventTypeByName(moduleObj.getEventName());
                        String fullComponentName = "";
                        if (eventSearchObj != null)
                            fullComponentName = eventSearchObj.getFieldName() + "." + moduleObj.getComponentName();
                        TblModuleManifest moduleSearchObj = moduleJpa.findByMleNameEventName(mleSearchObj.getId(), 
                                fullComponentName, moduleObj.getEventName());
                        if (moduleSearchObj == null) {
                            
                            wlsClient.addModuleWhiteList(moduleObj);
                            log.debug("Successfully created a new module manifest for : " + hostObj.VMM_Name + ":" + moduleObj.getComponentName());
                            
                        } else {
                            
                            wlsClient.updateModuleWhiteList(moduleObj);
                            log.debug("Successfully updated the module manifest for : " + hostObj.VMM_Name + ":" + moduleObj.getComponentName());
                        }
                        
                    } else if (reader.getLocalName().equalsIgnoreCase("PCRInfo")) {
                        
                        // PCR information will be available for both ESXi 5.0 & 5.1 hosts
                        TblPcrManifest tblPCR = null;
                        PCRWhiteList pcrObj = new PCRWhiteList();
                        pcrObj.setPcrName(reader.getAttributeValue(null, "ComponentName"));
                        pcrObj.setPcrDigest(reader.getAttributeValue(null, "DigestValue"));
                        Integer mleID = 0;
                        
                        // was: if (Integer.parseInt(reader.getAttributeValue(null, "ComponentName")) <= MAX_BIOS_PCR)
                        if( pcrObj.getPcrName() == null ) {
                            log.error("uploadToDB: PCR name is null: "+hostObj.toString());
                        }
                        else if ((Integer.parseInt(reader.getAttributeValue(null, "ComponentName")) <= MAX_BIOS_PCR)) {
                                
                            if (hostConfigObj.addBiosWhiteList() == true) {
                                pcrObj.setMleName(hostObj.BIOS_Name);
                                pcrObj.setMleVersion(hostObj.BIOS_Version);
                                pcrObj.setOsName("");
                                pcrObj.setOsVersion("");
                                pcrObj.setOemName(hostObj.BIOS_Oem);
                                mleID = mleBiosSearchObj.getId();

                                tblPCR = pcrJpa.findByMleIdName(mleID, pcrObj.getPcrName());
                                if (tblPCR == null) {

                                    wlsClient.addPCRWhiteList(pcrObj);                        
                                    log.debug("Successfully created a new BIOS PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());

                                } else {

                                    wlsClient.updatePCRWhiteList(pcrObj);
                                    log.debug("Successfully updated the BIOS PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());
                                }
                            }
                                                    
                        } else if (hostConfigObj.addVmmWhiteList() == true){
                            
                            pcrObj.setMleName(hostObj.VMM_Name);
                            pcrObj.setMleVersion(hostObj.VMM_Version);
                            pcrObj.setOsName(hostObj.VMM_OSName);
                            pcrObj.setOsVersion(hostObj.VMM_OSVersion);
                            pcrObj.setOemName("");
                            mleID = mleSearchObj.getId();

                            // If in case the vCenter is 5.1 and host is 5.1, then we should not be
                            // storing anything for the PCR 19 digest value. So, we need to null it out.
                            if (vCenterVersion != null && esxHostVersion != null && vCenterVersion.contains("5.1") 
                                            &&  esxHostVersion.contains("5.1") &&  pcrObj.getPcrName() != null &&
                                    pcrObj.getPcrName().equalsIgnoreCase("19"))
                                pcrObj.setPcrDigest("");
                            // System.out.println(pcrObj.getPcrName() + ":::" + pcrObj.getPcrDigest());
                            
                            tblPCR = pcrJpa.findByMleIdName(mleID, pcrObj.getPcrName());
                            if (tblPCR == null) {

                                wlsClient.addPCRWhiteList(pcrObj);                        
                                log.debug("Successfully created a new VMM PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());

                            } else {

                                wlsClient.updatePCRWhiteList(pcrObj);
                                log.debug("Successfully updated the VMM PCR manifest for : " + pcrObj.getMleName() + ":" + pcrObj.getPcrName());
                            }                           
                                                        
                        }
                                                   
                    }
                }
                reader.next();
            }
        } catch (MSException me) {
            
            log.error("Error during white list upload to database. " + me.getErrorCode() + " :" + me.getErrorMessage());
            throw me;
            
        } catch (ApiException ae) {
            
            log.error("API Client error during white list upload to database. " + ae.getErrorCode() + " :" + ae.getMessage());
            throw new MSException(ae, ErrorCode.MS_API_EXCEPTION, ErrorCode.getErrorCode(ae.getErrorCode()).toString() +
                    ": Error during White List upload to DB. " + ae.getMessage());

        } catch (Exception ex) {
            
            log.error("Error during white list upload to database. " + ex.getMessage());
            throw new MSException(ex, ErrorCode.SYSTEM_ERROR, "Error during white list upload to database. " + ex.getMessage());
        }                    
    }    
}
