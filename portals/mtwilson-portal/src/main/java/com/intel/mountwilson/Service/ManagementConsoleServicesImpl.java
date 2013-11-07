package com.intel.mountwilson.Service;

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
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.mtwilson.x500.DN;
import java.net.MalformedURLException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementConsoleServicesImpl implements IManagementConsoleServices {

    private static final Logger log = LoggerFactory.getLogger(ManagementConsoleServicesImpl.class.getName());
    private MCPersistenceManager mcManager = new MCPersistenceManager();
    private MwPortalUserJpaController keystoreJpa = new MwPortalUserJpaController(mcManager.getEntityManagerFactory("MSDataPU")); // fix bug 677,  MwPortalUser is in MSDataPU, not in ASDataPU
    private ApiClientX509JpaController apiClientJpa = new ApiClientX509JpaController(mcManager.getEntityManagerFactory("MSDataPU"));

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
        boolean result = false;
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
        ConnectionString connStr = null;
        if (hostDetailsObj.getHostType().equalsIgnoreCase(Vendor.INTEL.toString())) {
            connStr = ConnectionString.forIntel(hostDetailsObj.getHostName(), Integer.parseInt(hostDetailsObj.getHostPortNo())); //new ConnectionString(Vendor.INTEL, hostDetailsObj.getHostName(), Integer.parseInt(hostDetailsObj.getHostPortNo()));
        } else {
            // we need to handle both the VMware and Citrix connection strings in the same way. Since the user
            // will be providing the entire connection string, we do not need to create one similar to the Intel one.
            connStr = new ConnectionString(Vendor.valueOf(hostDetailsObj.getHostType().toUpperCase()), hostDetailsObj.getvCenterString());
        }
        hostRecord.AddOn_Connection_String = connStr.getConnectionStringWithPrefix();

        /* if (hostDetailsObj.isVmWareType()) {
         hostRecord.HostName = hostDetailsObj.getHostName();
         hostRecord.AddOn_Connection_String = hostDetailsObj.getvCenterString();
         } else {
         hostRecord.HostName = hostDetailsObj.getHostName();
         hostRecord.IPAddress = hostDetailsObj.getHostName();
         hostRecord.Port = Integer.parseInt(hostDetailsObj.getHostPortNo());
         } */

        hostConfigObj.setTxtHostRecord(hostRecord);

        try {
            result = msAPIObj.configureWhiteList(hostConfigObj);
        } catch (Exception e) {
            log.error("Failed to configure whitelist: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        return result;
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
        List<String> datacenters = null;

        try {
            datacenters = new ArrayList<String>();

            try {
                datacenters = client.getDatacenterNames();
            } catch (Exception e) {
                log.error("Failed to get datacenter information from vmware: {}", e.getMessage());
                throw ConnectionUtil.handleManagementConsoleException(e);
            }
        } catch (Exception e) {
            log.error("Failed to get datacenter information from vmware: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        return datacenters;
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
        List<String> clusters = null;

        try {
            clusters = new ArrayList<String>();

            try {
                clusters = client.getClusterNamesWithDC();
            } catch (Exception e) {
                log.error("Failed to get cluster information from vmware: {}", e.getMessage());
                throw ConnectionUtil.handleManagementConsoleException(e);
            }
        } catch (Exception e) {
            log.error("Failed to get cluster information from vmware: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        return clusters;
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
        List<HostDetails> hostVos = null;

        try {
            List<TxtHostRecord> hostList = null;
            hostVos = new ArrayList<HostDetails>();

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

        } catch (Exception e) {
            log.error("Failed to get host information from vmware cluster: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        return hostVos;
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
        boolean result = false;

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
            result = msAPIObj.updateApiClient(apiUpdateObj);
        } catch (Exception e) {
            log.error("Update failed: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        return result;
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
        boolean result = false;

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
                // XXX TODO it would be more efficient to add a fingerprint field to the keystore table, then we can look it up by fingerprint and have the right record immediately
//                            for(MwPortalUser portalUser : portalUsers) {
                // XXX TODO if we don't add teh fingerprintfield, then we need to looka t the cert in the keystore and compare the fingerprints
                keystoreJpa.destroy(portalUser.getId());
//                            }
//                            keystoreJpa.destroy(clientRecord.getId()); // actually deletes the user keystore w/ private key    bug #677 trying to delete a MwPortalUser keystore using the ID of an ApiClientX509 record
            }

            result = msAPIObj.deleteApiClient(decodedFP); // only marks it as deleted (must retain the record for audits)

        } catch (Exception e) {
            log.error("Delete failed: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        return result;
    }

    /**
     *
     * @param apiObj
     * @return
     * @throws ManagementConsolePortalException
     */
    public Role[] getAllRoles(ApiClient apiObj) throws ManagementConsolePortalException {
        log.info("ManagementConsoleServicesImpl.getAllRoles >>");
        Role[] roleList = null;

        try {
            ManagementService msAPIObj = (ManagementService) apiObj;
            roleList = msAPIObj.listAvailableRoles();
        } catch (Exception e) {
            log.error("Failed to get list of roles: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        return roleList;
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
        List<ApiClientDetails> apiClientList = new ArrayList<ApiClientDetails>();
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
                apiSearchObj.statusEqualTo = ApiClientStatus.CANCELLED.toString();
                apiListFromDB.addAll(msAPIObj.searchApiClients(apiSearchObj));

                apiSearchObj.enabledEqualTo = false;
                apiSearchObj.statusEqualTo = ApiClientStatus.REJECTED.toString();
                apiListFromDB.addAll(msAPIObj.searchApiClients(apiSearchObj));

                apiSearchObj.enabledEqualTo = false;
                apiSearchObj.statusEqualTo = ApiClientStatus.EXPIRED.toString();
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
                    apiClientDetailObj.setRequestedRoles(Arrays.asList(apiClientObj.roles));
                    apiClientDetailObj.setIssuer(apiClientObj.issuer);
                    apiClientDetailObj.setStatus(apiClientObj.status);
                    apiClientDetailObj.setComment(apiClientObj.comment);

                    apiClientList.add(apiClientDetailObj);
                }
            }
        } catch (Exception e) {
            log.error("Failed to compile list of API clients: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        return apiClientList;
    }

    /**
     *
     * @param apiObj
     * @return
     * @throws ManagementConsolePortalException
     */
    @Override
    public List<ApiClientDetails> getCADetails(ApiClient apiObj) throws ManagementConsolePortalException {
        // TODO Auto-generated method stub
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
        HostConfigResponseList results = null;
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
            ConnectionString connStr = null;
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
            HostConfigData configData = new HostConfigData();
            configData.setBiosWLTarget(HostWhiteListTarget.getBIOSWhiteListTarget(hostRecord.getBiosWLTarget()));
            configData.setVmmWLTarget(HostWhiteListTarget.getVMMWhiteListTarget(hostRecord.getVmmWLtarget()));
            configData.setTxtHostRecord(hostTxtObj);

            hostConfigList.add(configData);
        }

        hostList.setHostRecords(hostConfigList);
        try {
            results = msAPIObj.registerHosts(hostList);

        } catch (Exception e) {
            log.error("Failed to register hosts: {}", e.getMessage());
            throw ConnectionUtil.handleManagementConsoleException(e);
        }
        return results;
    }
}
