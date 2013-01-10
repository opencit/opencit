package com.intel.mountwilson.Service;

import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.LoggerFactory;

import com.intel.mountwilson.common.MCPConfig;
import com.intel.mountwilson.common.ManagementConsolePortalException;
import com.intel.mountwilson.constant.HelperConstant;
import com.intel.mountwilson.datamodel.ApiClientDetails;
import com.intel.mountwilson.datamodel.ApiClientListType;
import com.intel.mountwilson.datamodel.HostDetails;
import com.intel.mountwilson.util.ConnectionUtil;
import com.intel.mtwilson.agent.vmware.VMwareClient;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.ApiException;
import com.intel.mtwilson.ManagementService;
import com.intel.mtwilson.datatypes.*;

public class ManagementConsoleServicesImpl implements IManagementConsoleServices {

	private static final Logger logger = Logger.getLogger(ManagementConsoleServicesImpl.class.getName());
	
		
	/**
     * 
     * @param hostDetailsObj
     * @param apiObj
     * @return
     * @throws ManagementConsolePortalException 
     */
    @Override
	public boolean saveWhiteListConfiguration(HostDetails hostDetailsObj, HostConfigData hostConfig, ApiClient apiObj)throws ManagementConsolePortalException {           
            logger.info("ManagementConsoleServicesImpl.saveWhiteListConfiguration >>");
//            logger.info("Data to save > "+hostDetailsObj);
            
            boolean result = false;
                            
            ManagementService msAPIObj = (ManagementService) apiObj;
           
            
            // Create the host config object to be sent to the Management API for white list configuration
            HostConfigData hostConfigObj = hostConfig;
            TxtHostRecord hostRecord = new TxtHostRecord();
            if (hostDetailsObj.isVmWareType()) {
            	hostRecord.HostName = hostDetailsObj.getHostName();
            	hostRecord.AddOn_Connection_String = hostDetailsObj.getvCenterString();
            } else {
            	hostRecord.HostName = hostDetailsObj.getHostName();
            	hostRecord.IPAddress = hostDetailsObj.getHostName();
            	hostRecord.Port = Integer.parseInt(hostDetailsObj.getHostPortNo());
            }
            
            hostConfigObj.setTxtHostRecord(hostRecord);
            
            try {
                result = msAPIObj.configureWhiteList(hostConfigObj);
            } 
            /* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */ 
            catch (Exception e) {
            	logger.info(e.getMessage());
    			throw ConnectionUtil.handleException(e);
    		}
            /* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
            
            /*catch (IOException ex) {
                throw new ManagementConsolePortalException("Network error occured. " + ex.getMessage());
            } catch (ApiException ex) {
            	 throw new ManagementConsolePortalException(ex.getErrorCode() + " : " + ex.getMessage());
            } catch (SignatureException ex) {
                throw new ManagementConsolePortalException("Authentication error occured. " + ex.getMessage());
            } catch (Exception ex) {
                throw new ManagementConsolePortalException("Unexpected error: " + ex.getMessage());
            }*/
            return result;
	}

	/**
         * 
         * @param clusterName
         * @param vCenterConnection
         * @return
         * @throws ManagementConsolePortalException 
         */
        @Override
	public List<HostDetails> getHostEntryFromVMWareCluster(String clusterName, String vCenterConnection)throws ManagementConsolePortalException {
            logger.info("ManagementConsoleServicesImpl.getHostEntryFromVMWareCluster >>");
            logger.info("ClusterName : "+clusterName +", vCenter Connection String : "+vCenterConnection);
		
            List<HostDetails> hostVos = null;
            
            try {
                
                List<TxtHostRecord> hostList = null;
                hostVos = new ArrayList<HostDetails>();
                VMwareClient vmHelperObj = new VMwareClient();
                
                try {
                    
                    hostList = vmHelperObj.getHostDetailsForCluster(clusterName, vCenterConnection);
                    
                } 
                /* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
                catch (Exception e) {
                	logger.info(e.getMessage());
        			throw ConnectionUtil.handleException(e);
        		}
                /* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
                /*catch (Exception ex) {
                    
                    throw new ManagementConsolePortalException("Error during retrieval of host information from vCenter. " + ex.getMessage());
                }*/
               
                for (TxtHostRecord hostObj : hostList) {
                    
                    HostDetails mcObj = new HostDetails();
                    mcObj.setHostName(hostObj.HostName);
                    mcObj.setvCenterString(hostObj.AddOn_Connection_String);
                    mcObj.setVmWareType(true);
                    hostVos.add(mcObj);
                }
                
            } 
            /* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
            catch (Exception e) {
            	logger.info(e.getMessage());
    			throw ConnectionUtil.handleException(e);
    		}
            /* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
            /*catch (Exception ex ) {
                
                throw new ManagementConsolePortalException("Error during processing of hosts retrieved from vCenter. " + ex.getMessage());
            }*/
             
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
	public HostDetails registerNewHost(HostDetails hostDetailList, ApiClient apiObj)throws ManagementConsolePortalException {
            logger.info("ManagementConsoleServicesImpl.registerNewHost >>");
            logger.info("Host To Be Register >>" + hostDetailList);

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
            configData.setBiosWLTarget(hostDetailList.getBiosWLTarget());
            configData.setVmmWLTarget(hostDetailList.getVmmWLtarget());
            configData.setTxtHostRecord(hostObj);
            try {

                boolean result = false;
                result = msAPIObj.registerHost(configData);
                if (result)
                	hostDetailList.setStatus("Successfully registered the host.");

            } 
            /* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
            catch (Exception e) {
            	logger.info(e.getMessage());
                // Bug: 441 - We should not be throwing exception here. Instead setting the error correctly
                // throw ConnectionUtil.handleException(e);
                hostDetailList.setStatus(e.getMessage());
            }
            /* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
            /*catch (IOException ex) {
            	hostDetailList.setStatus("Network error: " + ex.getMessage());
            } catch (ApiException ex) {
            	hostDetailList.setStatus(ex.getErrorCode() + ": " + ex.getMessage());
            } catch (SignatureException ex) {
            	hostDetailList.setStatus("Authentication error: " + ex.getMessage());
            } catch (Exception ex) {
            	hostDetailList.setStatus("Unexpected error: " + ex.getMessage());
            }*/
            

            return hostDetailList;
	}
        
       /* @Override
		public HostDetails updateRegisteredHost(HostDetails hostDetailObj,ApiClient apiObj) throws ManagementConsolePortalException {
        	logger.info("ManagementConsoleServicesImpl.updateRegisteredHost >>");
            logger.info("Host To Be Register >>" + hostDetailObj);

            // Create the host object to be sent to the API CLient to Update Host Info
            TxtHostRecord hostObj = null;
			try {
				hostObj = apiObj.queryForHosts(hostDetailObj.getHostName()).get(0);
			} catch (Exception ex) {
				hostDetailObj.setStatus("Error While Getting register Host Info : " + ex.getMessage());
			}
            
            String vmmName = getOriginalVMMOrBiosName(hostObj,hostObj.VMM_Name);
            
            //check if Previously host has chosen with “Specified Good Known Host”
            boolean isGooDKnownHost = false;
            if (hostObj.VMM_Name.toLowerCase().contains(hostObj.HostName.toLowerCase()) || hostObj.BIOS_Name.toLowerCase().contains(hostObj.HostName.toLowerCase())) {
            	isGooDKnownHost = true;
            }
            
            if (hostDetailObj.getVmmWLtarget() == HostWhiteListTarget.HOSTS_WITH_SAME_OS_VMM_BUILD) {
            	// if previously it is "Specified good known host" then remove any extra part from BIOS Name.
            	if (isGooDKnownHost) {
					hostObj.BIOS_Name = getOriginalVMMOrBiosName(hostObj,hostObj.BIOS_Name);
				}
            	hostObj.VMM_Name = vmmName;
			}else if (hostDetailObj.getVmmWLtarget() == HostWhiteListTarget.OEM_SPECIFIC_HOSTS_WITH_SAME_OS_VMM_BUILD) {
            	// if previously it is "Specified good known host" then remove any extra part from BIOS Name.
				if (isGooDKnownHost) {
					hostObj.BIOS_Name = getOriginalVMMOrBiosName(hostObj,hostObj.BIOS_Name);
				}
				if (hostObj.BIOS_Oem.indexOf(" ") > 0) {
					hostObj.VMM_Name = hostObj.BIOS_Oem.split(" ")[0]+HelperConstant.VMM_NAME_SEPARATOR_TXTHOSTRECORD+vmmName;
				}else {
					hostObj.VMM_Name = hostObj.BIOS_Oem+HelperConstant.VMM_NAME_SEPARATOR_TXTHOSTRECORD+vmmName;
				}
			}
            
            try {

               // HostResponse result = apiObj.updateHost(new TxtHost(hostObj));
                hostDetailObj.setStatus("Successfully registered the host.");

//            } catch (IOException ex) {
//            	hostDetailObj.setStatus("Network error: " + ex.getMessage());
//            } catch (ApiException ex) {
//            	hostDetailObj.setStatus(ex.getErrorCode() + ": " + ex.getMessage());
//            } catch (SignatureException ex) {
//            	hostDetailObj.setStatus("Authentication error: " + ex.getMessage());
            } catch (Exception ex) {
            	hostDetailObj.setStatus("Unexpected error: " + ex.getMessage());
            }

            return hostDetailObj;
		}*/

	
        private String getOriginalVMMOrBiosName(TxtHostRecord hostObj,String vmmName) {
			if (vmmName.toLowerCase().contains(hostObj.BIOS_Oem.toLowerCase()) || vmmName.toLowerCase().contains(hostObj.HostName.toLowerCase())) {
				if (vmmName.contains(Pattern.quote(HelperConstant.VMM_NAME_SEPARATOR_TXTHOSTRECORD))) {
					return vmmName.substring(vmmName.indexOf(HelperConstant.VMM_NAME_SEPARATOR_TXTHOSTRECORD));
				}
			}
			
        	return vmmName;
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
	public boolean updateRequest(ApiClientDetails apiClientDetailsObj, ApiClient apiObj, boolean approve)	throws ManagementConsolePortalException {
            logger.info("ManagementConsoleServicesImpl.updateRequest >>");
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
                    if (apiClientDetailsObj.getComment() != null)
                        apiUpdateObj.comment = apiClientDetailsObj.getComment();
                    
                } else {

                    apiUpdateObj.enabled = false;
                    apiUpdateObj.roles = (String[]) apiClientDetailsObj.getRequestedRoles().toArray(new String[0]);
                    apiUpdateObj.status = "REJECTED";
                    apiUpdateObj.comment = apiClientDetailsObj.getComment();
                    
                }
                
                result = msAPIObj.updateApiClient(apiUpdateObj);

            } 
            /* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
            catch (Exception e) {
            	logger.info(e.getMessage());
    			throw ConnectionUtil.handleException(e);
    		}
            /* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
            /*catch (IOException ex) {

                throw new ManagementConsolePortalException("Network error occured. " + ex.getMessage());

            } catch (ApiException ex) {

                throw new ManagementConsolePortalException(ex.getErrorCode() + " : " + ex.getMessage());

            } catch (SignatureException ex) {

                throw new ManagementConsolePortalException("Authentication error occured. " + ex.getMessage());
                
            } catch (Exception ex) {
                
                throw new ManagementConsolePortalException("Unexpected error. " + ex.getMessage());
            }*/
           
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
            logger.info("ManagementConsoleServicesImpl.deleteSelectedRequest >>");
            logger.info("API Client being deleted >> " + fingerprint);
            boolean result = false;

            try {

                ManagementService msAPIObj = (ManagementService) apiObj;
                byte[] decodedFP;
                
                try {
                    
                    decodedFP = Hex.decodeHex(fingerprint.toCharArray());
                    
                } catch (DecoderException ex) {
                    
                    throw ex;
                }
                                
                result = msAPIObj.deleteApiClient(decodedFP);

            }
            
            
            catch (Exception e) {
    			logger.info(e.getMessage());
    			throw ConnectionUtil.handleException(e);
    		}
            /* catch (IOException ex) {

                throw new ManagementConsolePortalException("Network error occured. " + ex.getMessage());

            } catch (ApiException ex) {
            	
                throw new ManagementConsolePortalException(ex.getErrorCode() + " : " + ex.getMessage());

            } catch (SignatureException ex) {

                throw new ManagementConsolePortalException("Authentication error occured. " + ex.getMessage());
                
            } catch (Exception ex) {
                
                throw new ManagementConsolePortalException("Unexpected error. " + ex.getMessage());
            }*/

            return result;
        }

        /**
         * 
         * @param apiObj
         * @return
         * @throws ManagementConsolePortalException 
         */
        public Role[] getAllRoles(ApiClient apiObj) throws ManagementConsolePortalException {
            logger.info("ManagementConsoleServicesImpl.getAllRoles >>");
            Role[] roleList = null;
            
            try {

                ManagementService msAPIObj = (ManagementService) apiObj;
                                
                roleList = msAPIObj.listAvailableRoles();

            } 
            /* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
            
            catch (Exception e) {
    			logger.info(e.getMessage());
    			throw ConnectionUtil.handleException(e);
    		}
            /* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
            
            /*catch (IOException ex) {

                throw new ManagementConsolePortalException("Network error occured. " + ex.getMessage());

            } catch (ApiException ex) {
            	
                throw new ManagementConsolePortalException(ex.getErrorCode() + " : " + ex.getMessage());

            } catch (SignatureException ex) {

                throw new ManagementConsolePortalException("Authentication error occured. " + ex.getMessage());
                
            } catch (Exception ex) {
                
                throw new ManagementConsolePortalException("Unexpected error. " + ex.getMessage());
            }*/

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
	public List<ApiClientDetails> getApiClients(ApiClient apiObj, ApiClientListType apiType )throws ManagementConsolePortalException {
            logger.info("ManagementConsoleServicesImpl.getApprovedRequest >>");

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
                    int expirationMonths = MCPConfig.getConfiguration().getInt("mtwilson.mc.apiKeyExpirationNoticeInMonths");
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, expirationMonths);
                    apiSearchObj.expiresBefore = cal.getTime();
                    apiListFromDB = msAPIObj.searchApiClients(apiSearchObj);

                } else if (apiType == ApiClientListType.PENDING) {
                    apiSearchObj.enabledEqualTo = false;
                    apiSearchObj.statusEqualTo = "PENDING";
                    apiListFromDB = msAPIObj.searchApiClients(apiSearchObj);
                
                }

            }
            /* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
            catch (Exception e) {
    			logger.info(e.getMessage());
    			throw ConnectionUtil.handleException(e);
    		}
            /* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
            
            
            
            /*catch (IOException ex) {
                throw new ManagementConsolePortalException("Network error: " + ex.getMessage());

            } catch (ApiException ex) {
            	
             throw new ManagementConsolePortalException(ex.getErrorCode() + ": " + ex.getMessage());

            } catch (SignatureException ex) {
                throw new ManagementConsolePortalException("Authentication error: " + ex.getMessage());
                
            } catch (Exception ex) {
                throw new ManagementConsolePortalException("Unexpected error: " + ex.getMessage());
            }*/

            try {

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
            /* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
            catch (Exception e) {
    			logger.info(e.getMessage());
    			throw ConnectionUtil.handleException(e);
    		}
            /* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
            /*catch (Exception ex) {
                throw new ManagementConsolePortalException("Error during processing of ApiClients retrieved from DB. " + ex.getMessage());
            }*/
            return apiClientList;
	}
	  //Begin_Added by Soni-Function for CA
		@Override
		public List<ApiClientDetails> getCADetails(ApiClient apiObj)
				throws ManagementConsolePortalException {
			
			// TODO Auto-generated method stub
			return null;
		}
		//End_Added by Soni-Function for CA
}
