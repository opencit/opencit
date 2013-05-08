/**
 * This Class contains methods used to communicate to REST Services.
 */
package com.intel.mountwilson.Service;

import com.intel.mountwilson.as.hosttrustreport.data.HostType;
import com.intel.mountwilson.as.hosttrustreport.data.HostsTrustReportType;
import com.intel.mountwilson.common.DemoPortalException;
import com.intel.mountwilson.common.ManagementConsolePortalException;
import com.intel.mountwilson.constant.HelperConstant;
import com.intel.mountwilson.datamodel.*;
import com.intel.mountwilson.util.ConnectionUtil;
import com.intel.mountwilson.util.ConverterUtil;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.AttestationService;
import com.intel.mtwilson.TrustAssertion;
import com.intel.mtwilson.WhitelistService;
import com.intel.mtwilson.datatypes.AttestationReport;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.model.Hostname;
import com.intel.mtwilson.datatypes.PcrLogReport;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.Vendor;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponse;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author yuvrajsx
 *
 */
public class DemoPortalServicesImpl implements IDemoPortalServices {
	
	//Variable used for logging. 
        Logger log = LoggerFactory.getLogger(getClass().getName());
	
	//variable used to change date into given format to display on screen.  
	private static final DateFormat formatter=  new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	
	
	/**
	 * This method is used to get host trust status from REST services and convert that data into TrustedHostVO Object.
	 * 
	 * @param hostList (List contains all Host information for which trust status is needed.)
	 * @param apiClientServices
	 * @param trustedCertificates
	 * @return List of TrustedHostVO Objects.
	 * @throws DemoPortalException
	 */
	@Override
	public List<TrustedHostVO> getTrustStatusForHost(List<HostDetailsEntityVO> hostList, AttestationService apiClientServices,X509Certificate[] trustedCertificates) throws DemoPortalException {
		//List contains data to be return.
		List<TrustedHostVO> hostVOs = new ArrayList<TrustedHostVO>();
		Map<String,HostDetailsEntityVO> hostTempMap = new HashMap<String, HostDetailsEntityVO>();
		
		//check size of List of Host for which Trust is required if its empty Throw Exception with specific message to Controller.
		if (hostList!=null && hostList.size() > 0) {
			Set<Hostname> listOfHostName = new HashSet<Hostname>();
			for (HostDetailsEntityVO hostDetailsEntityVO : hostList) {
                listOfHostName.add(new Hostname(hostDetailsEntityVO.getHostName()));
                hostTempMap.put(hostDetailsEntityVO.getHostName(), hostDetailsEntityVO);
			}
			
            try {
            	
            	//call to REST Services, for trust status data by passing Set of all Host Name.
                List<HostTrustXmlResponse> trust = apiClientServices.getSamlForMultipleHosts(listOfHostName, false);
                for (HostTrustXmlResponse hostTrustXmlResponse : trust) {
                	
                	//get HostDetailsEntityVO for current host for which we are checking Trust Status.
                	HostDetailsEntityVO hostDetails = hostTempMap.get(hostTrustXmlResponse.getName());
	                try {
	                	log.info("Getting trust Information for Host "+hostTrustXmlResponse.getName());
	                	if (hostTrustXmlResponse.getAssertion() != null) {
	                		TrustAssertion trustAssertion = new TrustAssertion(trustedCertificates, hostTrustXmlResponse.getAssertion());
	                		if( trustAssertion.isValid() ) {
	                			hostVOs.add(ConverterUtil.getTrustedHostVoFromTrustAssertion(hostDetails, trustAssertion,null));
	                		}
	                		else {
	                			log.error("Trust Assertion is NOT valid "+hostTrustXmlResponse.getName()+". "+ trustAssertion.error().getMessage());
	                			hostVOs.add(ConverterUtil.getTrustedHostVoFromTrustAssertion(hostDetails, null,trustAssertion.error().getMessage()));
	                		}
	                	}else {
	                		log.error("Trust Assertion is NOT valid "+hostTrustXmlResponse.getName()+". "+ hostTrustXmlResponse.getErrorCode()+". "+hostTrustXmlResponse.getErrorMessage());
	                		hostVOs.add(ConverterUtil.getTrustedHostVoFromTrustAssertion(hostDetails, null,hostTrustXmlResponse.getErrorCode()+". "+hostTrustXmlResponse.getErrorMessage()));
	                	}
	                } catch (Exception e) {
	                	hostVOs.add(ConverterUtil.getTrustedHostVoFromTrustAssertion(hostDetails, null,StringEscapeUtils.escapeHtml(e.getMessage())));
	                	log.error("Exception while getting trust status "+hostTrustXmlResponse.getName()+". "+ e.getMessage());
	                	throw ConnectionUtil.handleDemoPortalException(e);
	                }
                 }
                
            } catch (Exception e) {
                    log.error("Exception while getting trust status All Host."+ e.getMessage());
                    throw ConnectionUtil.handleDemoPortalException(e);
            }
		}else {
			throw new DemoPortalException("Currently there are no hosts configured in the system.");
		}
       
       return hostVOs;
	}
	
	
	/**
	 * This Method will get all configured Host Details from REST Services.
	 * 
	 * @param service (Object of AttestationService, used to call into REST Services)
	 * @return List of HostDetailsEntityVO Objects
	 * @throws DemoPortalException
	 */
	@Override
	public List<HostDetailsEntityVO> getHostListFromDB(AttestationService service) throws DemoPortalException{
		List<HostDetailsEntityVO> hostList  = null;
		try{
			//Call into REST Services for getting all HOST information by passing empty String.
			hostList = ConverterUtil.getHostVOListFromTxtHostRecord(service.queryForHosts("")); 
		} catch (Exception e) {
			log.error("Error While getting data from DataBase."+e.getMessage());	
			 throw ConnectionUtil.handleDemoPortalException(e);
		}
		
		//Check if Return list is empty or Not, if empty throw Exception to controller with Specific message. 
		if (hostList==null || hostList.size() < 0) {
			throw new DemoPortalException("Currently there are no hosts configured in the system.");
		}
		
		//Statements to change data according to need, replace all null values with Empty String so it will not shown up on screen.
		for (HostDetailsEntityVO hostDetailsEntityVO : hostList) {
			System.out.println(hostDetailsEntityVO);
			
			if (hostDetailsEntityVO.getvCenterDetails() == null || hostDetailsEntityVO.getvCenterDetails().equals("null")) {
				hostDetailsEntityVO.setvCenterDetails("");
				//If vCenter String is present then remove Administrator and password from it while returning.
            	}else if(hostDetailsEntityVO.getvCenterDetails().indexOf(";") >= 0){
                      String vCenterString =  hostDetailsEntityVO.getvCenterDetails().substring(0, hostDetailsEntityVO.getvCenterDetails().indexOf(";"));
                      hostDetailsEntityVO.setvCenterDetails(vCenterString);
                  }
				
			if(hostDetailsEntityVO.getHostIPAddress() == null || hostDetailsEntityVO.getHostIPAddress().equals("null")){
				hostDetailsEntityVO.setHostIPAddress("");
			}
			if(hostDetailsEntityVO.getHostPort() == null || hostDetailsEntityVO.getHostPort().equals("null")){
				hostDetailsEntityVO.setHostPort("");
			}
			if(hostDetailsEntityVO.getEmailAddress() == null || hostDetailsEntityVO.getEmailAddress().equals("null")){
				hostDetailsEntityVO.setEmailAddress("");
			}
			if(hostDetailsEntityVO.getvCenterDetails() == null || hostDetailsEntityVO.getvCenterDetails().equals("null")){
				hostDetailsEntityVO.setvCenterDetails("");
			}
			if(hostDetailsEntityVO.getHostDescription() == null || hostDetailsEntityVO.getHostDescription().equals("null")){
				hostDetailsEntityVO.setHostDescription("");
			}
		}
		return hostList;
	}
	
	/**
	 * This Method is used to get Trust Status for Single Host.
	 * 
	 * @param hostName
	 * @param apiClientServices
	 * @param trustedCertificates
	 * @return
	 * @throws DemoPortalException
	 */
	@Override
	public TrustedHostVO getSingleHostTrust(String hostName,AttestationService apiClientServices,X509Certificate[] trustedCertificates)throws DemoPortalException {
		
		TrustedHostVO hostVO = null;
		HostDetailsEntityVO hostDetailsEntityVO = new HostDetailsEntityVO();
		hostDetailsEntityVO.setHostName(hostName);
		String xmloutput = null;
		try {
			log.info("Getting trust Information for Host "+hostName);
			
			//call to REST Services to get Host Trust status.
			xmloutput = apiClientServices.getSamlForHost(new Hostname(hostName));
			TrustAssertion trustAssertion = new TrustAssertion(trustedCertificates, xmloutput);
			if( trustAssertion.isValid() ) {
                                                	             hostVO = ConverterUtil.getTrustedHostVoFromTrustAssertion(hostDetailsEntityVO, trustAssertion,null);
                                                                        }  else {
                                                                                        log.error("Trust Assertion is NOT valid "+hostName+". "+ trustAssertion.error());
                                                                                        throw new DemoPortalException("Trust Assertion is NOT valid "+hostName+". "+ trustAssertion.error());
                                                                        }
		} catch (Exception e) {
                                                                        // Bug: 445 - We should not be throwing the exception. Instead return the object with data filled in.                    
                                                                        hostVO = ConverterUtil.getTrustedHostVoFromTrustAssertion(hostDetailsEntityVO, null,StringEscapeUtils.escapeHtml(e.getMessage()));
		}
		return hostVO;
	}
	
	/**
	 * This method is used to get all OEM details from REST Services.
	 * Call to searchMLE method present in ApiClient class by passing empty string as parameter. this Method will return all MLE from services.
	 * 
	 * @param client (Object of ApiClient)
	 * @return
	 * @throws DemoPortalException
	 */
	@Override
	public Map<String, List<Map<String, String>>> getAllOemInfo(ApiClient client)throws DemoPortalException {
		
		Map<String, List<Map<String, String>>> map = new HashMap<String, List<Map<String,String>>>();
		List<MleDetailsEntityVO> mleList = null;
        List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		
		try {
			WhitelistService service = (WhitelistService) client;
			//This statement will get all MLE information from REST services, will get only OEM information from that list.
			 mleList = ConverterUtil.getMleVOListWhereOEMNotNull(service.searchMLE(""));
			
			 //convert data into a MAP of Strings which is used in UI (JQuery) to display on screen.
			 if (mleList != null && mleList.size() > 0) {
				for (MleDetailsEntityVO mleDetailsEntityVO : mleList) {
					if (map.get(mleDetailsEntityVO.getOemName()) == null) {
						list = new ArrayList<Map<String,String>>();
						map.put(mleDetailsEntityVO.getOemName(), list);
					}else {
						list = map.get(mleDetailsEntityVO.getOemName());
					}
					Map<String, String> oemInfo = new HashMap<String, String>();
					oemInfo.put(mleDetailsEntityVO.getMleName(), mleDetailsEntityVO.getMleVersion());
					list.add(oemInfo);
				}
			}else {
				// throw new DemoPortalException("No OEM & OS Information is present in Database. Please check Database Configuration.");
                                                                                                // Bug:575. Providing a better error message for the user.
				throw new DemoPortalException("Currently no MLEs are configured in the system.  Please make sure you have created both a BIOS and VMM mle");                                
			}
		}catch (Exception e) {
			throw ConnectionUtil.handleDemoPortalException(e);
		}
		return map;
	}
	
	/**
	 * This method is used to Get All OS details from REST Services.
	 * 
	 * @param client (Object of ApiClient)
	 * @return
	 * @throws DemoPortalException
	 */
	@Override
	public Map<String, Boolean> getOSAndVMMInfo(ApiClient client)throws DemoPortalException {
		 List<MleDetailsEntityVO> mleList = null;
		 //This is a MAP of OS/VMM name and boolean variable which denote about current os/vmm info is VMWare type or not. 
		 Map<String,Boolean> maps = new HashMap<String, Boolean>();
		 WhitelistService service = (WhitelistService) client;
		try {
			//Call to REST Services to get all details of MLE, will extract all MLE from that data where OEM info is null.
			mleList = ConverterUtil.getMleVOListWhereOEMIsNull(service.searchMLE(""));
	        for (MleDetailsEntityVO mleDetailsEntityVO : mleList) {
	        	maps.put(ConverterUtil.getOSAndVMMInfoString(mleDetailsEntityVO), mleDetailsEntityVO.getOsName().toLowerCase().contains(HelperConstant.OS_IMAGE_VMWARE.toLowerCase()) ? true : false);
			}
		}catch (Exception e) {
			 throw ConnectionUtil.handleDemoPortalException(e);
		}
		return maps;
	}

	/**
	 * This method is used to add/configure new Host to REST services.
	 * 
	 * @param dataVO
	 * @param apiClientServices
	 * @return
	 * @throws DemoPortalException
	 */
	@Override
	public boolean saveNewHostData(HostDetailsEntityVO dataVO,AttestationService apiClientServices)throws DemoPortalException {
		boolean result = false;
		try {
                                    ConnectionString connStr = null;
                                    if (dataVO.getvCenterDetails() == null && dataVO.getHostIPAddress() != null && dataVO.getHostPort() != null) {
                                        connStr = new ConnectionString(Vendor.INTEL, dataVO.getHostIPAddress(), Integer.parseInt(dataVO.getHostPort()));
                                    } else if (dataVO.getVmmName().toLowerCase().contains("vmware")) {
                                        connStr = new ConnectionString(Vendor.VMWARE, dataVO.getvCenterDetails());
                                    } else if (dataVO.getVmmName().toLowerCase().contains("citrix")) {
                                        connStr = new ConnectionString(Vendor.CITRIX, dataVO.getvCenterDetails());
                                    } else {
                                        connStr = new ConnectionString(Vendor.INTEL, dataVO.getvCenterDetails());
                                    }
                                    dataVO.setvCenterDetails(connStr.getConnectionStringWithPrefix());
                                    TxtHost hostObj = ConverterUtil.getTxtHostFromHostVO(dataVO);
                                    
			//Call to REST Services to add host information.                                    
			apiClientServices.addHost(hostObj);
			result = true;
		} catch (Exception e) {
			log.error("Errror While Adding New Host."+e.getMessage());
			throw ConnectionUtil.handleDemoPortalException(e); 
		}
		return result;
	}
	
	/**
	 * This method is used to Update Host information, which was already configure.
	 * 
	 * @param dataVO
	 * @param apiClientServices
	 * @return
	 * @throws DemoPortalException
	 */
	@Override
	public boolean updateHostData(HostDetailsEntityVO dataVO,AttestationService apiClientServices)throws DemoPortalException {
		boolean result = false;
		try {
                                    ConnectionString connStr = null;
                                    if (dataVO.getvCenterDetails() == null && dataVO.getHostIPAddress() != null && dataVO.getHostPort() != null) {
                                        connStr = new ConnectionString(Vendor.INTEL, dataVO.getHostIPAddress(), Integer.parseInt(dataVO.getHostPort()));
                                    } else if (dataVO.getVmmName().toLowerCase().contains("vmware")) {
                                        connStr = new ConnectionString(Vendor.VMWARE, dataVO.getvCenterDetails());
                                    } else if (dataVO.getVmmName().toLowerCase().contains("citrix")) {
                                        connStr = new ConnectionString(Vendor.CITRIX, dataVO.getvCenterDetails());
                                    } else {
                                        connStr = new ConnectionString(Vendor.INTEL, dataVO.getvCenterDetails());
                                    }
                                    dataVO.setvCenterDetails(connStr.getConnectionStringWithPrefix());
                                    TxtHost hostObj = ConverterUtil.getTxtHostFromHostVO(dataVO);
            
			//Call to Services to Update pre-configure host information.
			apiClientServices.updateHost(hostObj);
			result = true;
		} catch (Exception e) {
			log.error("Errror While Updating Host.");
			throw ConnectionUtil.handleDemoPortalException(e);
		}
		
		return result;
	}

	/**
	 * This method will delete HOST information from Services.
	 * Also it will delete all entry from HOST VM Mapping information for that host, which is used to store Policy of VM.  
	 * 
	 * @param hostID
	 * @param hostName
	 * @param apiClientServices
	 * @param vmMappingData
	 * @return
	 * @throws DemoPortalException
	 */
	@Override
	public boolean deleteHostDetails(String hostID, String hostName,AttestationService apiClientServices,Map<String, HostVmMappingVO> vmMappingData) throws DemoPortalException {
		boolean result = false;
		try {
			//Call to Services to delete HOST.
			apiClientServices.deleteHost(new Hostname(hostName));
			
			//Delete all entries from HostVMMapping table, which store policy for VM.
			for (Entry<String, HostVmMappingVO> vmMap : vmMappingData.entrySet()) {
				HostVmMappingVO hostVmMappingVO = vmMap.getValue();
				if (hostVmMappingVO.getHostId().equals(hostID)) {
					vmMappingData.remove(vmMap.getKey());
				}
			}
			result = true;
		} catch (Exception e) {
			log.error("Errror While Deleting Host.");
			 throw ConnectionUtil.handleDemoPortalException(e);
		}
		return result;
	}

	/**
	 * This method will get all VM associated with HOST. 
	 * Also it will update HOST VM Mapping table according. If there are no VM present for that host then delete all entries from VM Mapping for corresponding host.
	 * 
	 * @param hostName
	 * @param hostID
	 * @param vmMappingData
	 * @param service
	 * @return
	 * @throws DemoPortalException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<HostVmMappingVO> getVMsForHost(String hostName,String hostID,Map<String, HostVmMappingVO> vmMappingData,AttestationService service)throws DemoPortalException {
		log.info("DemoPortalServicesImpl.getVMsForHost >>");
        List<String> vms = null;
		String vCenterString;
		try {
			//get vCenterString of Host.
			vCenterString = service.queryForHosts(hostName).get(0).AddOn_Connection_String.replaceAll("vmware:","");
		} catch (Exception e) {
			log.error("Error while getting vCenterString for host ID, cause is "+e.getMessage());
			 throw ConnectionUtil.handleDemoPortalException(e);
		}
		log.info("Connecting to VM Client.");
		try {
			//Call to get all VM associated with that HOST.
			vms = VMwareClient.getVMsForHost(hostName, vCenterString);
			
			//check for response from Services if its Empty. throw Exception back to controller with specific message.
			if (vms.isEmpty()) {
				log.error("Host currently does not have any virtual machines configured.");
                
				//Delete all entries from HOST VM Mapping for corresponding HOST
            	for (Entry<String, HostVmMappingVO> vmMap : vmMappingData.entrySet()) {
					HostVmMappingVO hostVmMappingVO = vmMap.getValue();
					if (hostVmMappingVO.getHostId().equals(hostID)) {
						vmMappingData.remove(vmMap.getKey());
					}
				}
            	
            	//throw Exception back to controller.
				throw new DemoPortalException("Host currently does not have any virtual machines configured.");
			}
			
			try{
				//If Response is  not null then add that VM information  into Host VM Mapping table.
				addVMDetailsToHostVmMapping(hostID,vms,vmMappingData);
			}catch(Exception e){
				log.error("Error while getting data from Host mapping table, cause is "+e.getMessage());
				/* Soni_Begin_18/09/2012_issue_for_consistent_Error_Message  */
				 throw ConnectionUtil.handleDemoPortalException(e);
				/* Soni_End_18/09/2012_issue_for_consistent_Error_Message  */
				//throw new DemoPortalException("Error while getting data from Host mapping table, cause is "+e.getMessage(),e);
			}
			
		} catch (Exception e) {
			log.error("Error while getting data from VMCLient, cause is "+e.getMessage());
			 throw ConnectionUtil.handleDemoPortalException(e);
		}
		//Get the list of All VM with all predefine policy information.
		return getVMFromHostVmMapping(hostID,vms,vmMappingData);
	}

	/**
	 * This method will Start/Stop VM for a Host.
	 * 
	 * @param hostName (host name on which VM is running)
	 * @param vmName (Name of a VM which you want to Start/Stop)
	 * @param hostID
	 * @param isPowerOnCommand
	 * @param service
	 * @return
	 * @throws DemoPortalException
	 */
	@Override
	public boolean powerOnOffHostVMs(String hostName, String vmName,String hostID, boolean isPowerOnCommand,AttestationService service) throws DemoPortalException {
		log.info("DemoPortalServicesImpl.powerOnOffHostVMs >>");
		String vCenterString;
		try {
			//get vCenterString from Services for host.
			vCenterString = service.queryForHosts(hostName).get(0).AddOn_Connection_String;
		} catch (Exception e) {
			log.error("Error while getting vCenterString for host ID, cause is "+e.getMessage());
			 throw ConnectionUtil.handleDemoPortalException(e);
		}
		
		try {
			//Call to Services for Starting/Stopping VM.
			VMwareClient.powerOnOffVM(vmName, hostName, isPowerOnCommand, vCenterString);
		} catch (Exception e) {
			log.error(e.getMessage());
			 throw ConnectionUtil.handleDemoPortalException(e);
		}
		return true;
	}

	/**
	 * This method is used to Migrated VM from one host to another. 
	 * 
	 * @param vmName (VM Name to be migrated)
	 * @param sourceHost (Current Host Name)
	 * @param hostToTransfer (Target Host Name)
	 * @param hostID
	 * @param service
	 * @return
	 * @throws DemoPortalException
	 */
	@Override
	public boolean migrateVMToHost(String vmName,String sourceHost, String hostToTransfer, String hostID,AttestationService service)throws DemoPortalException {
		log.info("DemoPortalServicesImpl.migrateVMToHost >>");
		String vCenterString;
		try {
			//Get vCenterString for a Host.
			vCenterString = service.queryForHosts(sourceHost).get(0).AddOn_Connection_String;
		} catch (Exception e) {
			log.error("Error while getting vCenterString for host ID, cause is "+e.getMessage());
			 throw ConnectionUtil.handleDemoPortalException(e);
		}
		
		try {
			//Call to Services to migrate VM from Sources Host to Target Host.
			VMwareClient.migrateVM(vmName,hostToTransfer,vCenterString);
		} catch (Exception e) {
			log.error(e.getMessage());
			 throw ConnectionUtil.handleDemoPortalException(e);
		}
		return true;
	}

	/**
	 * This method is used to get SAML Assertion data for a Host.
	 * This data is shown in pop-up window when user click on trust details button in Home page to TrustDashBoard.
	 * 
	 * @param hostName
	 * @param apiClientServices
	 * @param trustedCertificates
	 * @return
	 * @throws DemoPortalException
	 */
	@Override
	public String trustVerificationDetails(String hostName,AttestationService apiClientServices,X509Certificate[] trustedCertificates)throws DemoPortalException {
		log.info("DemoPortalServicesImpl.trustVerificationDetails >>");
		String xmloutput  = null;
        Set<Hostname> hostnames = new HashSet<Hostname>();
        hostnames.add(new Hostname(hostName));
		try {
			//calling into Services to get SAML for a Host.
			List<HostTrustXmlResponse> trust = apiClientServices.getSamlForMultipleHosts(hostnames, false);

            for (HostTrustXmlResponse hostTrustXmlResponse : trust) {
                TrustAssertion trustAssertion = new TrustAssertion(trustedCertificates, hostTrustXmlResponse.getAssertion());
                if( trustAssertion.isValid() ) {
                	//Store SAML Assertion into a String.
                    xmloutput = hostTrustXmlResponse.getAssertion();
                    }
                else {
                    log.error("Error While Getting SAML ."+hostTrustXmlResponse.getErrorCode()+". "+hostTrustXmlResponse.getErrorMessage());
                    throw new DemoPortalException("Error While Getting SAML. "+hostTrustXmlResponse.getErrorCode()+". "+hostTrustXmlResponse.getErrorMessage());
                }
            }
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			 throw ConnectionUtil.handleDemoPortalException(e);
		} 
		//format a SAML String into a XML type using helper Function.
		return ConverterUtil.formateXMLString(xmloutput);
	}
	
	@Override
	public boolean getBlukTrustUpdatedForHost(List<String> hostNames,AttestationService apiClientServices,X509Certificate[] trustedCertificates)throws DemoPortalException {
		try {
			Set<Hostname> listOfHostName = new HashSet<Hostname>();
			for (String host : hostNames) {
				listOfHostName.add(new Hostname(host));
			}
			apiClientServices.getSamlForMultipleHosts(listOfHostName, true);
				
		} catch (Exception e) {
			log.error(e.getMessage());
			 throw ConnectionUtil.handleDemoPortalException(e);
		}
		return true;
	}
        
        
   	@Override
	public List<HostReportTypeVO> getHostTrustReport(List<String> hostNames,ApiClient client)throws DemoPortalException {
		
            AttestationService service = (AttestationService) client;
            HostsTrustReportType report = null;
            List<HostReportTypeVO> hostReportTypeVO = new ArrayList<HostReportTypeVO>();
		try {
            List<Hostname> hostList = new ArrayList<Hostname>();
            for (String host : hostNames) {
                hostList.add(new Hostname(host));
            }
            report = service.getHostTrustReport(hostList);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw  ConnectionUtil.handleDemoPortalException(e);
		}
                
                List<HostType> list = report.getHost();
                
                for (HostType hostType : list) {
                    HostReportTypeVO vo = new HostReportTypeVO();
                    vo.setHostName(hostType.getHostName());
                    vo.setMleInfo(hostType.getMLEInfo());
                    // Since the created and updated data for the host will be in the Audit DB from 1.1 release,
                    // we will not have this data here.
                    vo.setCreatedOn("");
                    vo.setTrustStatus(hostType.getTrustStatus());
                    vo.setVerifiedOn(formatter.format(hostType.getVerifiedOn().toGregorianCalendar().getTime()));
                    hostReportTypeVO.add(vo);
                }
		return hostReportTypeVO;
	}

   	//Method to add/update VM Mapping map after getting all VM info for a Host.
    private void addVMDetailsToHostVmMapping(String hostID, List<String> listVMDetails, Map<String, HostVmMappingVO> vmMappingData) throws Exception {
        for (String vmDetails : listVMDetails) {
        	HostVmMappingVO hostVmMappingVO = new HostVmMappingVO();
                hostVmMappingVO.setHostId(hostID);
                String vmName = vmDetails.split(HelperConstant.SEPARATOR_VMCLIENT)[0];
                hostVmMappingVO.setVmName(vmName);
                if ((vmDetails.split(HelperConstant.SEPARATOR_VMCLIENT)[1]).equalsIgnoreCase(HelperConstant.VM_POWER_STATE_ON)) {
                     hostVmMappingVO.setVmStatus((short) 1);
                }else{
                     hostVmMappingVO.setVmStatus((short) 0);
                }
                hostVmMappingVO.setLocationPolicy((short) 0);
                hostVmMappingVO.setTrustedHostPolicy((short) 0);
                
                if(!vmMappingData.isEmpty()){
                    boolean vmFound = false;
                    for (Entry<String, HostVmMappingVO> entry : vmMappingData.entrySet()) {
                        HostVmMappingVO vo = entry.getValue();
                        //Check for VM Mapping data for all VM and if found then update map with old policy.
                    	if(vo.getVmName().equals(hostVmMappingVO.getVmName())){
                            hostVmMappingVO.setLocationPolicy(vo.getLocationPolicy());
                            hostVmMappingVO.setTrustedHostPolicy(vo.getTrustedHostPolicy());
                            vmMappingData.put(entry.getKey(), hostVmMappingVO);
                            vmFound = true;
                        }
                    }
                    if (!vmFound) {
                    	//If VM Mapping data is not empty and also it does not have any entry for current Host then add one with default value.
                    	vmMappingData.put(hostVmMappingVO.getHostId()+HelperConstant.VM_HOST_MAPPING_SEPERATOR+hostVmMappingVO.getVmName(), hostVmMappingVO);
                        }
                }else {
                	//If VM Mapping data is empty, then directly add Host-vm-mapping data into map with default value.
                	vmMappingData.put(hostVmMappingVO.getHostId()+HelperConstant.VM_HOST_MAPPING_SEPERATOR+hostVmMappingVO.getVmName(), hostVmMappingVO);
                }
         }
    }

    private List<HostVmMappingVO> getVMFromHostVmMapping(String hostID, List<String> vms, Map<String, HostVmMappingVO> vmMappingData) throws DemoPortalException {
    	List<HostVmMappingVO> list = new ArrayList<HostVmMappingVO>();
    	Set<String> hostToDelete = new HashSet<String>();
    	
    	//Statements to check HOst and VM mapping in VM Mapping map, if found then add it list which will get return to controller.
    	for (Entry<String, HostVmMappingVO> entry : vmMappingData.entrySet()) {
			HostVmMappingVO hostVmMappingVO = entry.getValue();
			boolean gotKey = false;
			if (hostVmMappingVO.getHostId().equals(hostID)) {
				for (String vmDetails : vms) {
		            String vmName = vmDetails.split(HelperConstant.SEPARATOR_VMCLIENT)[0];
		            if (hostVmMappingVO.getVmName().equals(vmName)) {
		            	list.add(hostVmMappingVO);
		            	gotKey = true;
					}
		        }
				if (!gotKey) {
					hostToDelete.add(entry.getKey());
				}
				
			}
		}
    	
    	//delete all un-wanted host entry from VM Mapping map.
        for (String hostKey : hostToDelete) {
        	vmMappingData.remove(hostKey);
		}
        
        return list;
    }
    
    
	@Override
	public HostDetailsEntityVO getSingleHostDetailFromDB(String hostName,AttestationService service) throws DemoPortalException {
		HostDetailsEntityVO hostDetailsEntityVO  = new HostDetailsEntityVO();
		try{
			hostDetailsEntityVO = ConverterUtil.getHostVOObjectFromTxtHostRecord(service.queryForHosts(hostName).get(0));
		} catch (Exception e) {
			log.error("Error While getting data from DataBase."+e.getMessage());
			 throw ConnectionUtil.handleDemoPortalException(e);
		}
    	
		
       if(hostDetailsEntityVO.getHostIPAddress() == null || hostDetailsEntityVO.getHostIPAddress().equals("null")){
           hostDetailsEntityVO.setHostIPAddress("");
       }
       if(hostDetailsEntityVO.getHostPort() == null || hostDetailsEntityVO.getHostPort().equals("null")){
           hostDetailsEntityVO.setHostPort("");
       }
       if(hostDetailsEntityVO.getEmailAddress() == null || hostDetailsEntityVO.getEmailAddress().equals("null")){
           hostDetailsEntityVO.setEmailAddress("");
       }
       if(hostDetailsEntityVO.getvCenterDetails() == null || hostDetailsEntityVO.getvCenterDetails().equals("null")){
           hostDetailsEntityVO.setvCenterDetails("");
       }
       if(hostDetailsEntityVO.getHostDescription() == null || hostDetailsEntityVO.getHostDescription().equals("null")){
           hostDetailsEntityVO.setHostDescription("");
       }
        
            return hostDetailsEntityVO;
	}
	
	/**
	 * This method is used to get failure report for Host. 
	 * 
	 * @param hostName
	 * @param attestationService
	 * @return
	 * @throws DemoPortalException
	 * @throws Exception
	 */
	@Override
	public List<PcrLogReport> getFailureReportData(String hostName,ApiClient attestationService) throws Exception {
		log.info("DemoPortalServicesImpl.getFailureReportData >>");
		
			AttestationReport report;
			try {
				report = attestationService.getAttestationReport(new Hostname(hostName));
				
			} catch (Exception e) {
				log.error(e.getMessage());
				 throw ConnectionUtil.handleDemoPortalException(e);
			}
		
		return report.getPcrLogs();
	}
}
