/**
 * 
 */
package com.intel.mountwilson.Service;

import com.intel.mountwilson.common.ManagementConsolePortalException;
import com.intel.mountwilson.datamodel.ApiClientDetails;
import com.intel.mountwilson.datamodel.ApiClientListType;
import com.intel.mountwilson.datamodel.HostDetails;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.agent.vmware.VMwareClient;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostConfigResponseList;
import com.intel.mtwilson.datatypes.Role;
import java.net.MalformedURLException;
import java.util.List;

/**
 * @author yuvrajsx
 *
 */
public interface IManagementConsoleServices {
	
        public boolean saveWhiteListConfiguration(HostDetails hostDetailsObj,HostConfigData hostConfig, ApiClient apiObj) throws ManagementConsolePortalException, MalformedURLException;

        public List<String> getDatacenterNames(VMwareClient client)throws ManagementConsolePortalException;
        
        public List<String> getClusterNamesWithDC(VMwareClient client)throws ManagementConsolePortalException;
        
        public List<HostDetails> getHostNamesForCluster(VMwareClient client, String clusterName)throws ManagementConsolePortalException;

        public HostDetails registerNewHost(HostDetails hostDetailList, ApiClient apiObj)throws ManagementConsolePortalException;

        //public HostDetails updateRegisteredHost(HostDetails hostDetailList, ApiClient apiObj)throws ManagementConsolePortalException;

        public boolean deleteSelectedRequest(String fingerprint, ApiClient apiObj)throws ManagementConsolePortalException;

        public Role[] getAllRoles(ApiClient apiObj) throws ManagementConsolePortalException;
                
        public List<ApiClientDetails> getApiClients(ApiClient apiObj, ApiClientListType apiType )throws ManagementConsolePortalException;

        public List<ApiClientDetails> getCADetails(ApiClient apiObj)throws ManagementConsolePortalException;
    
        public boolean updateRequest(ApiClientDetails apiClientDetailsObj, ApiClient apiObj, boolean approve) throws ManagementConsolePortalException;
        
        public HostConfigResponseList registerHosts(ApiClient apiObj, List<HostDetails> hostRecords) throws ManagementConsolePortalException, MalformedURLException;

}
