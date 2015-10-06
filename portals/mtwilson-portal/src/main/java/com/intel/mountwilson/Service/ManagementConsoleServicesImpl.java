package com.intel.mountwilson.Service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.intel.mountwilson.common.MCPConfig;
import com.intel.mountwilson.common.MCPersistenceManager;
import com.intel.mountwilson.common.ManagementConsolePortalException;
import com.intel.mountwilson.datamodel.ApiClientDetails;
import com.intel.mountwilson.datamodel.ApiClientListType;
import com.intel.mountwilson.datamodel.HostDetails;
import com.intel.mountwilson.util.ConnectionUtil;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.agent.vmware.VMwareClient;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.dcsg.cpg.x500.DN;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.user.management.rest.v2.model.UserComment;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementConsoleServicesImpl implements IManagementConsoleServices {

    private static final Logger log = LoggerFactory.getLogger(ManagementConsoleServicesImpl.class.getName());
    private MCPersistenceManager mcManager;
    private MwPortalUserJpaController keystoreJpa;
    private ApiClientX509JpaController apiClientJpa;
    private final ObjectMapper yaml;

    
    public ManagementConsoleServicesImpl() {
        mcManager = new MCPersistenceManager();
        keystoreJpa = new MwPortalUserJpaController(mcManager.getEntityManagerFactory("MSDataPU")); // fix bug 677,  MwPortalUser is in MSDataPU, not in ASDataPU
        apiClientJpa = new ApiClientX509JpaController(mcManager.getEntityManagerFactory("MSDataPU"));
        yaml = createYamlMapper();
    }
    
    /**
     *
     * @param hostDetailsObj
     * @param apiObj
     * @return
     * @throws ManagementConsolePortalException
     */
    @Override
    public boolean saveWhiteListConfiguration(HostDetails hostDetailsObj, HostConfigData hostConfig, ApiClient apiObj)
            throws ManagementConsolePortalException, MalformedURLException {
        log.info("ManagementConsoleServicesImpl.saveWhiteListConfiguration >>");
        ManagementService msAPIObj = (ManagementService) apiObj;

        // Create the host config object to be sent to the Management API for white list configuration
        HostConfigData hostConfigObj = hostConfig;
        TxtHostRecord hostRecord = new TxtHostRecord();
        hostRecord.HostName = hostDetailsObj.getHostName();
        // Bug:726: Port number was not being displayed in the UI since it was not being set in the Port field. Because of this issue, the port # was not getting stored in the DB.
        if (hostDetailsObj.getHostPortNo() != null && !hostDetailsObj.getHostPortNo().isEmpty()) {
            hostRecord.Port = Integer.parseInt(hostDetailsObj.getHostPortNo());
        }

        // Bug 614: Using connection strings for all kinds of hosts.
        ConnectionString connStr;
        connStr = new ConnectionString(Vendor.valueOf(hostDetailsObj.getHostType().toUpperCase()), hostDetailsObj.getvCenterString()); 
        /*
        if (hostDetailsObj.getHostType().equalsIgnoreCase(Vendor.INTEL.toString())) {
            connStr = ConnectionString.forIntel(hostDetailsObj.getHostName(), Integer.parseInt(hostDetailsObj.getHostPortNo())); //new ConnectionString(Vendor.INTEL, hostDetailsObj.getHostName(), Integer.parseInt(hostDetailsObj.getHostPortNo()));
        } else {
            // we need to handle both the VMware and Citrix connection strings in the same way. Since the user
            // will be providing the entire connection string, we do not need to create one similar to the Intel one.
            connStr = new ConnectionString(Vendor.valueOf(hostDetailsObj.getHostType().toUpperCase()), hostDetailsObj.getvCenterString());
        }*/
        hostRecord.AddOn_Connection_String = connStr.getConnectionStringWithPrefix();
//        hostRecord.tlsPolicyChoice = hostDetailsObj.?????
        
        /* if (hostDetailsObj.isVmWareType()) {
         hostRecord.HostName = hostDetailsObj.getHostName();
         hostRecord.AddOn_Connection_String = hostDetailsObj.getvCenterString();
         } else {
         hostRecord.HostName = hostDetailsObj.getHostName();
         hostRecord.IPAddress = hostDetailsObj.getHostName();
         hostRecord.Port = Integer.parseInt(hostDetailsObj.getHostPortNo());
         } */
        
        if( hostDetailsObj.getTlsPolicyId() != null ) {
            log.debug("saveWhiteListConfiguration tls policy id {}",  hostDetailsObj.getTlsPolicyId());
            hostRecord.tlsPolicyChoice = new TlsPolicyChoice();
            hostRecord.tlsPolicyChoice.setTlsPolicyId(hostDetailsObj.getTlsPolicyId());
        }
        else if( hostDetailsObj.getTlsPolicyType() != null ) {
            log.debug("saveWhiteListConfiguration tls policy type {}",  hostDetailsObj.getTlsPolicyType());
            ArrayList<String> data = new ArrayList<>();
            data.add(hostDetailsObj.getTlsPolicyData());
            hostRecord.tlsPolicyChoice = new TlsPolicyChoice();
            hostRecord.tlsPolicyChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
            hostRecord.tlsPolicyChoice.getTlsPolicyDescriptor().setPolicyType(hostDetailsObj.getTlsPolicyType());
            hostRecord.tlsPolicyChoice.getTlsPolicyDescriptor().setData(data);
        }

        hostConfigObj.setTxtHostRecord(hostRecord);

        try {
            boolean result = msAPIObj.configureWhiteList(hostConfigObj);
            return result;
        } catch (Exception e) {
            log.error("Failed to configure whitelist: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
    }

    /**
     *
     * @param vCenterConnection
     * @return
     * @throws ManagementConsolePortalException
     */
    @Override
    public List<String> getDatacenterNames(VMwareClient client) throws ManagementConsolePortalException {
        log.info("ManagementConsoleServicesImpl.getDatacenters >>");
        //logger.info("ClusterName : "+clusterName +", vCenter Connection String : "+vCenterConnection);
        try {
            List<String> datacenters = client.getDatacenterNames();
            return datacenters;
        } catch (Exception e) {
            log.error("Failed to get datacenter information from vmware: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
    }

    /**
     *
     * @param datacenter
     * @param vCenterConnection
     * @return
     * @throws ManagementConsolePortalException
     */
    @Override
    public List<String> getClusterNamesWithDC(VMwareClient client) throws ManagementConsolePortalException {
        log.info("ManagementConsoleServicesImpl.getClusters >>");
        //logger.info("ClusterName : "+clusterName +", vCenter Connection String : "+vCenterConnection);
        try {
            List<String> clusters = client.getClusterNamesWithDC();
            return clusters;
        } catch (Exception e) {
            log.error("Failed to get cluster information from vmware: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        
    }

    /**
     *
     * @param clusterName
     * @param vCenterConnection
     * @return
     * @throws ManagementConsolePortalException
     */
    @Override
    public List<HostDetails> getHostNamesForCluster(VMwareClient client, String clusterName) throws ManagementConsolePortalException {
        log.info("ManagementConsoleServicesImpl.getHostEntryFromVMWareCluster >>");
        //logger.info("ClusterName : "+clusterName +", vCenter Connection String : "+vCenterConnection);
        try {
            List<TxtHostRecord> hostList;
            List<HostDetails>hostVos = new ArrayList<HostDetails>();

            try {
                hostList = client.getHostNamesForCluster(clusterName);

            } catch (Exception e) {
                log.error("Failed to get host information from vmware cluster: {}", e.getMessage());
                throw ConnectionUtil.handleManagementConsoleException(e);
            }

            for (TxtHostRecord hostObj : hostList) {
                log.debug("TxtHostRecord found in hostList: " + hostObj.HostName);

                HostDetails mcObj = new HostDetails();
                mcObj.setHostName(hostObj.HostName);
                mcObj.setvCenterString(hostObj.AddOn_Connection_String);
                mcObj.setVmWareType(true);
                hostVos.add(mcObj);
            }
            return hostVos;
        } catch (Exception e) {
            log.error("Failed to get host information from vmware cluster: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
    }

    /**
     *
     * @param dataVOList
     * @param apiObj
     * @return
     * @throws ManagementConsolePortalException
     */
    @Override
    public HostDetails registerNewHost(HostDetails hostDetailList, ApiClient apiObj) throws ManagementConsolePortalException {
        log.info("ManagementConsoleServicesImpl.registerNewHost >>");
        log.debug("Host To Be Register >>" + hostDetailList);
        ManagementService msAPIObj = (ManagementService) apiObj;

        // Create the host object to be sent to the Management API for host registration
        TxtHostRecord hostObj = new TxtHostRecord();
        if (hostDetailList.isVmWareType()) {
            hostObj.HostName = hostDetailList.getHostName();
            hostObj.AddOn_Connection_String = hostDetailList.getvCenterString();
        } else {
            hostObj.HostName = hostDetailList.getHostName();
            hostObj.IPAddress = hostDetailList.getHostName();
            hostObj.Port = Integer.parseInt(hostDetailList.getHostPortNo());
        }

        HostConfigData configData = new HostConfigData();
        configData.setBiosWLTarget(HostWhiteListTarget.valueOf(hostDetailList.getBiosWLTarget()));
        configData.setVmmWLTarget(HostWhiteListTarget.valueOf(hostDetailList.getVmmWLtarget()));
        configData.setTxtHostRecord(hostObj);

        try {
            boolean result = msAPIObj.registerHost(configData);
            if (result) {
                hostDetailList.setStatus("Successfully registered the host.");
            }
        } catch (Exception e) {
            log.error("Failed to register the host: {}", e.getMessage());
            // Bug: 441 - We should not be throwing exception here. Instead setting the error correctly

            hostDetailList.setStatus(StringEscapeUtils.escapeHtml(e.getMessage()));
        }

        return hostDetailList;
    }

    /**
     *
     * @param dataVO
     * @param apiObj
     * @param approve
     * @return
     * @throws ManagementConsolePortalException
     */
    @Override
    public boolean updateRequest(ApiClientDetails apiClientDetailsObj, ApiClient apiObj, boolean approve) throws ManagementConsolePortalException {
        log.info("ManagementConsoleServicesImpl.updateRequest >>");
        try {
            ManagementService msAPIObj = (ManagementService) apiObj;
            ApiClientUpdateRequest apiUpdateObj = new ApiClientUpdateRequest();

            try {
                apiUpdateObj.fingerprint = Hex.decodeHex(apiClientDetailsObj.getFingerprint().toCharArray());
            } catch (DecoderException ex) {
                throw ex;
            }

            if (approve) {
                apiUpdateObj.enabled = true;
                apiUpdateObj.roles = (String[]) apiClientDetailsObj.getRequestedRoles().toArray(new String[0]);
                apiUpdateObj.status = "APPROVED";
                if (apiClientDetailsObj.getComment() != null) {
                    apiUpdateObj.comment = apiClientDetailsObj.getComment();
                }
            } else {
                apiUpdateObj.enabled = false;
                apiUpdateObj.roles = (String[]) apiClientDetailsObj.getRequestedRoles().toArray(new String[0]);
                apiUpdateObj.status = "REJECTED";
                apiUpdateObj.comment = apiClientDetailsObj.getComment();
            }
            boolean result = msAPIObj.updateApiClient(apiUpdateObj);
            return result;
        } catch (Exception e) {
            log.error("Update failed: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
    }

    /**
     *
     * @param fingerprint
     * @param apiObj
     * @return
     * @throws ManagementConsolePortalException
     */
    @Override
    public boolean deleteSelectedRequest(String fingerprint, ApiClient apiObj) throws ManagementConsolePortalException {
        log.info("ManagementConsoleServicesImpl.deleteSelectedRequest >>");
        log.debug("API Client being deleted >> " + fingerprint);
        try {
            ManagementService msAPIObj = (ManagementService) apiObj;
            byte[] decodedFP;

            try {
                decodedFP = Hex.decodeHex(fingerprint.toCharArray());
            } catch (DecoderException ex) {
                throw ex;
            }

            // fix bug #677
            ApiClientX509 clientRecord = apiClientJpa.findApiClientX509ByFingerprint(decodedFP);
            if (clientRecord != null) {
                X509Certificate clientCert = X509Util.decodeDerCertificate(clientRecord.getCertificate());
                DN dn = new DN(clientCert.getSubjectX500Principal().getName());
                String username = dn.getCommonName();
                MwPortalUser portalUser = keystoreJpa.findMwPortalUserByUserName(username);
//                            List<MwPortalUser> portalUsers = keystoreJpa.findMwPortalUserByUsernameEnabled(username);
                // in case there was more than one (shouldn't happen!!) with the same username who is ENABLED, identify the right one via fingerprint
                keystoreJpa.destroy(portalUser.getId());
//                            }
//                            keystoreJpa.destroy(clientRecord.getId()); // actually deletes the user keystore w/ private key    bug #677 trying to delete a MwPortalUser keystore using the ID of an ApiClientX509 record
            }
            boolean result = msAPIObj.deleteApiClient(decodedFP); // only marks it as deleted (must retain the record for audits)
            return result;
        } catch (Exception e) {
            log.error("Delete failed: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
    }

    /**
     *
     * @param apiObj
     * @return
     * @throws ManagementConsolePortalException
     */
    public Role[] getAllRoles(ApiClient apiObj) throws ManagementConsolePortalException {
        log.info("ManagementConsoleServicesImpl.getAllRoles >>");
        try {
            ManagementService msAPIObj = (ManagementService) apiObj;
            Role[] roleList = msAPIObj.listAvailableRoles();
            return roleList;
        } catch (Exception e) {
            log.error("Failed to get list of roles: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
    }

    /**
     *
     * @param apiObj
     * @param apiType
     * @return
     * @throws ManagementConsolePortalException
     */
    @Override
    public List<ApiClientDetails> getApiClients(ApiClient apiObj, ApiClientListType apiType) throws ManagementConsolePortalException {
        log.info("ManagementConsoleServicesImpl.getApprovedRequest >>");
        List<ApiClientDetails> apiClientList = new ArrayList<>();
        List<ApiClientInfo> apiListFromDB = null;

        try {
            // Retrieve the pending approvals
            ManagementService msAPIObj = (ManagementService) apiObj;
            ApiClientSearchCriteria apiSearchObj = new ApiClientSearchCriteria();

            if (apiType == ApiClientListType.ALL) {
                apiSearchObj.enabledEqualTo = true;
                apiSearchObj.statusEqualTo = ApiClientStatus.APPROVED.toString();
                apiListFromDB = msAPIObj.searchApiClients(apiSearchObj);

                apiSearchObj.enabledEqualTo = false;
                apiSearchObj.statusEqualTo = ApiClientStatus.REJECTED.toString();
                apiListFromDB.addAll(msAPIObj.searchApiClients(apiSearchObj));

            } else if (apiType == ApiClientListType.DELETE) {
                apiSearchObj.enabledEqualTo = true;
                apiSearchObj.statusEqualTo = "APPROVED";
                apiListFromDB = msAPIObj.searchApiClients(apiSearchObj);

            } else if (apiType == ApiClientListType.EXPIRING) {
                int expirationMonths = MCPConfig.getConfiguration().getInt("mtwilson.mc.apiKeyExpirationNoticeInMonths", 3);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, expirationMonths);
                apiSearchObj.expiresBefore = cal.getTime();
                apiListFromDB = msAPIObj.searchApiClients(apiSearchObj);

            } else if (apiType == ApiClientListType.PENDING) {
                apiSearchObj.enabledEqualTo = false;
                apiSearchObj.statusEqualTo = "PENDING";
                apiListFromDB = msAPIObj.searchApiClients(apiSearchObj);

            }

        } catch (Exception e) {
            log.error("Failed to list API clients: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        try {
            if (apiListFromDB != null) {
                for (ApiClientInfo apiClientObj : apiListFromDB) {
                    ApiClientDetails apiClientDetailObj = new ApiClientDetails();
                    apiClientDetailObj.setName(apiClientObj.name);
                    apiClientDetailObj.setFingerprint(new String(Hex.encodeHex(apiClientObj.fingerprint)));
                    apiClientDetailObj.setExpires(apiClientObj.expires);
                    apiClientDetailObj.setRoles(Arrays.asList(apiClientObj.roles));
                    apiClientDetailObj.setIssuer(apiClientObj.issuer);
                    apiClientDetailObj.setStatus(apiClientObj.status);
//                    apiClientDetailObj.setComment(apiClientObj.comment);
                    try {
                        if( apiClientObj.comment != null ) {
                            UserComment comment = yaml.readValue(apiClientObj.comment, UserComment.class);
                            if( comment.roles != null ) {
                                apiClientDetailObj.setRequestedRoles(new ArrayList<String>(comment.roles));
                            }
                            apiClientDetailObj.setComment(""); 
                        }
                    }
                    catch(Exception e) {
                        log.error("Cannot parse user comment: {}", apiClientObj.comment, e);
                        apiClientDetailObj.setRequestedRoles(new ArrayList<String>());
                        apiClientDetailObj.setComment(apiClientObj.comment);
                    }

                    apiClientList.add(apiClientDetailObj);
                }
            }
        } catch (Exception e) {
            log.error("Failed to compile list of API clients: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        return apiClientList;
    }
    
    // SEE ALSO: ApiClientBO.creatYamlMapper() in mtwilson-management
    private ObjectMapper createYamlMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        yamlFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        return mapper;
    }

    /**
     *
     * @param apiObj
     * @return
     * @throws ManagementConsolePortalException
     */
    @Override
    public List<ApiClientDetails> getCADetails(ApiClient apiObj) throws ManagementConsolePortalException {
        return null;
    }

    /**
     *
     * @param apiObj
     * @param hostRecords
     * @return
     * @throws ManagementConsolePortalException
     */
    @Override
    public HostConfigResponseList registerHosts(ApiClient apiObj, List<HostDetails> hostRecords) throws ManagementConsolePortalException, MalformedURLException {
        log.info("ManagementConsoleServicesImpl.registerHosts >>");
        log.debug("# of hosts to be registered >> " + hostRecords.size());
        List<HostConfigData> hostConfigList = new ArrayList<HostConfigData>();
        HostConfigDataList hostList = new HostConfigDataList();
        ManagementService msAPIObj = (ManagementService) apiObj;

        // We now need to create the actual HostConfigData objects using the HostDetail object 
        for (HostDetails hostRecord : hostRecords) {
            TxtHostRecord hostTxtObj = new TxtHostRecord();
            hostTxtObj.HostName = hostRecord.getHostName();
            // Bug:726: Port number was not being displayed in the UI since it was not being set in the Port field. Because of this issue, the port # was not getting stored in the DB.
            if (hostRecord.getHostPortNo() != null && !hostRecord.getHostPortNo().isEmpty()) {
                hostTxtObj.Port = Integer.parseInt(hostRecord.getHostPortNo());
            }

            // Bug 614: Using connection strings for all kinds of hosts.
            ConnectionString connStr;
            if (hostRecord.getHostType().equalsIgnoreCase(Vendor.INTEL.toString())) {
                connStr = ConnectionString.forIntel(hostRecord.getHostName(), Integer.parseInt(hostRecord.getHostPortNo())); //new ConnectionString(Vendor.INTEL, hostRecord.getHostName(), Integer.parseInt(hostRecord.getHostPortNo()));
            } else {
                // we need to handle both the VMware and Citrix connection strings in the same way. Since the user
                // will be providing the entire connection string, we do not need to create one similar to the Intel one.
                connStr = new ConnectionString(Vendor.valueOf(hostRecord.getHostType().toUpperCase()), hostRecord.getvCenterString());
            }
            hostTxtObj.AddOn_Connection_String = connStr.getConnectionStringWithPrefix();
            /*if (hostRecord.isVmWareType()) {
             hostTxtObj.HostName = hostRecord.getHostName();
             hostTxtObj.AddOn_Connection_String = hostRecord.getvCenterString();
             } else {
             hostTxtObj.HostName = hostRecord.getHostName();
             hostTxtObj.IPAddress = hostRecord.getHostName();
             hostTxtObj.Port = Integer.parseInt(hostRecord.getHostPortNo());
             }*/
            hostTxtObj.tlsPolicyChoice = getTlsPolicyChoice(hostRecord);
            
            HostConfigData configData = new HostConfigData();
            configData.setBiosWLTarget(HostWhiteListTarget.getBIOSWhiteListTarget(hostRecord.getBiosWLTarget()));
            configData.setVmmWLTarget(HostWhiteListTarget.getVMMWhiteListTarget(hostRecord.getVmmWLtarget()));
            configData.setTxtHostRecord(hostTxtObj);

            hostConfigList.add(configData);
        }

        hostList.setHostRecords(hostConfigList);
        try {
            HostConfigResponseList results = msAPIObj.registerHosts(hostList);
            return results;
        } catch (Exception e) {
            log.error("Failed to register hosts: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
    }
    
    private TlsPolicyChoice getTlsPolicyChoice(HostDetails hostRecord) {
        TlsPolicyChoice tlsPolicyChoice =  new TlsPolicyChoice();
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        if( hostRecord.getTlsPolicyId() != null && !hostRecord.getTlsPolicyId().isEmpty() ) {
            tlsPolicyChoice.setTlsPolicyId(hostRecord.getTlsPolicyId());
        }
        if( hostRecord.getTlsPolicyType() != null && !hostRecord.getTlsPolicyType().isEmpty() ) {
            tlsPolicyDescriptor.setPolicyType(hostRecord.getTlsPolicyType());
        }
        if( hostRecord.getTlsPolicyData() != null && !hostRecord.getTlsPolicyData().isEmpty() ) {
            tlsPolicyDescriptor.setData(new ArrayList<String>());
            tlsPolicyDescriptor.getData().add(hostRecord.getTlsPolicyData());
        }
        tlsPolicyChoice.setTlsPolicyDescriptor(tlsPolicyDescriptor);
        return tlsPolicyChoice;
    }
}
