/**
 * 
 */
package com.intel.mountwilson.controller;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.intel.mountwilson.Service.IManagementConsoleServices;
import com.intel.mountwilson.common.MCPConfig;
import com.intel.mountwilson.common.ManagementConsolePortalException;
import com.intel.mountwilson.constant.HelperConstant;
import com.intel.mountwilson.datamodel.ApiClientDetails;
import com.intel.mountwilson.datamodel.ApiClientListType;
import com.intel.mountwilson.datamodel.HostDetails;
import com.intel.mountwilson.util.JSONView;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostVMMType;
import com.intel.mtwilson.datatypes.HostWhiteListTarget;
import com.intel.mtwilson.datatypes.TxtHostRecord;

/**
 * @author yuvrajsx
 *
 */
@Controller
public class ManagementConsoleDataController extends MultiActionController{
	
	//private static final Logger log = Logger.getLogger(ManagementConsoleDataController.class.getName());
        Logger log = LoggerFactory.getLogger(getClass().getName());
	private IManagementConsoleServices services;
	
	/**
	 * @param HttpServletRequest
	 * @param HttpServletResponse
	 * @return
	 */
	public ModelAndView uploadFlatFileRegisterHost(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.uploadFlatFileRegisterHost >>");
		req.getSession().removeAttribute("hostVO");
		ModelAndView responseView = new ModelAndView(new JSONView());
		List<HostDetails> listOfRegisterHost = new ArrayList<HostDetails>();
		
		// Check that we have a file upload request
		boolean isRequestMultiType = ServletFileUpload.isMultipartContent(req);
		System.out.println(isRequestMultiType);
		if (!isRequestMultiType) {
			responseView.addObject("result",false);
			log.error("File Upload is not MultiPart. Please check you File Uploaded Plugin.");
			return responseView;
		}
		
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		try {
			@SuppressWarnings("unchecked")
			List<FileItem>  items = upload.parseRequest(req);
			
			// Process the uploaded items
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
			    FileItem item = (FileItem) iter.next();

			    if (!item.isFormField()) {
			    	String lines[] = item.getString().split("\\r?\\n");
			    	for (String values : lines) {
			    		//Split host name and host value with Separator e.g. |
			    		if (values.indexOf(HelperConstant.SEPARATOR_REGISTER_HOST) > 0) {
			    			String val[] = values.split(Pattern.quote(HelperConstant.SEPARATOR_REGISTER_HOST));
				    		if (val.length > 1) {
				    			HostDetails host = new HostDetails();
				    			host.setHostName(val[0]);
				    			String portOrVCenter = val[1];
	                                                
				    			if (portOrVCenter.toLowerCase().contains(HelperConstant.HINT_FOR_VCENTERSTRING.toLowerCase())) {
                                        host.setvCenterString(portOrVCenter);
                                        host.setVmWareType(true);
							}else {
                                    host.setHostPortNo(portOrVCenter);
                                    host.setVmWareType(false);
							}
				    			listOfRegisterHost.add(host);
						}
					}else {
						responseView.addObject("result",false);
						log.info("Please Provide Host name and port/vCenterString seperated by | symbol.");
						return responseView;
					}
				}
			    }
			}
		log.info("Uploaded Content :: "+listOfRegisterHost.toString());
		req.getSession().setAttribute("hostVO",listOfRegisterHost);
		responseView.addObject("result",listOfRegisterHost.size() > 0 ? true : false);
			
		} catch (FileUploadException e) {
			e.printStackTrace();
			responseView.addObject("result",false);
		}catch (Exception e) {
			e.printStackTrace();
			responseView.addObject("result",false);
		}
                
		log.info("ManagementConsoleDataController.uploadFlatFileRegisterHost <<<");
		return responseView;		
	}
	
	/**
	 * This Method will use to Retrieve the pre-fetched Host to be Register, uploaded with flat file.
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public ModelAndView getUploadedRegisterHostValues(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.getUploadedRegisterHostValues >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		@SuppressWarnings("unchecked")
		List<HostDetails> listOfRegisterHost = (List<HostDetails>)req.getSession().getAttribute("hostVO");
		boolean result = false;
		
		if ( listOfRegisterHost != null) {
			try {
				responseView = getListofRegisteredHost(listOfRegisterHost,responseView,getApiClientService(req, ApiClient.class));
				result = true;
			} catch (Exception ex) {
				log.error("Exception Checking for already register host. " + ex.getMessage());
				responseView.addObject("message","Exception Checking for already register host. " + ex.getMessage());
				responseView.addObject("result",result);
				return responseView;
			}
		}
		responseView.addObject("result",result);
		log.info("ManagementConsoleDataController.getUploadedRegisterHostValues <<<");
		return responseView;
		
	}

	/**
	 * This Method will use to get Data of white List Configuration.
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public ModelAndView uploadWhiteListConfiguration(HttpServletRequest req, HttpServletResponse res) {
		log.info("ManagementConsoleDataController.uploadWhiteListConfiguration >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		String hostVOString = req.getParameter("registerHostVo");
		boolean result = false;
		HostDetails hostDetailsObj;
		
		try {
			String whiteListConfigVOString = req.getParameter("whiteListConfigVO");
			HostConfigData hostConfig;
			
			@SuppressWarnings("serial")
			Type whiteListType = new TypeToken<HostConfigData>() {}.getType();
			hostConfig = new Gson().fromJson(whiteListConfigVOString, whiteListType);
			//hostConfig = getObjectFromJSONString(whiteListConfigVOString, HostConfigData.class);
			
			hostConfig.setBiosWLTarget(getBiosWhiteListTarget(req.getParameter("biosWLTagrget")));
			hostConfig.setVmmWLTarget(getVmmWhiteListTarget(req.getParameter("vmmWLTarget")));
			System.err.println("whiteListConfigVO>>"+hostConfig);
			
			@SuppressWarnings("serial")
			Type hostInfo = new TypeToken<HostDetails>() {}.getType();
			hostDetailsObj = new Gson().fromJson(hostVOString, hostInfo);
			//hostDetailsObj = getObjectFromJSONString(hostVOString, HostDetails.class);
			hostConfig.setHostVmmType(getHostVmmTypeTarget(hostDetailsObj.getHostType()));
			
                        
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
                        
			result = services.saveWhiteListConfiguration(hostDetailsObj,hostConfig, apiObj);
			result = true;            
		} catch (Exception ex) {
			log.error("Exception during whitelist configuration. " + ex.getMessage());
			responseView.addObject("message", ex.getMessage());
			responseView.addObject("result",result);
			return responseView;
		}
		responseView.addObject("result",result);
		log.info("ManagementConsoleDataController.uploadWhiteListConfiguration <<<");
		return responseView;
		
	}
	
	/**
	 * This Method will use to get Data of white List Configuration, using Automatic  tools.
	 * 
	 * @param req
	 * @param res
	 * @return
	 *//*
	public ModelAndView defineWhiteListConfig(HttpServletRequest req, HttpServletResponse res) {
		log.info("ManagementConsoleDataController.defineWhiteListConfig >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		String whiteListConfigVOString = req.getParameter("whiteListConfigVO");
		System.out.println(whiteListConfigVOString);
		boolean result = false;
		HostConfigData whiteList;
		
		try {
			@SuppressWarnings("serial")
			Type hostInfo = new TypeToken<HostConfigData>() {}.getType();
			whiteList = new Gson().fromJson(whiteListConfigVOString, hostInfo);
			whiteList = getObjectFromJSONString(whiteListConfigVOString, HostConfigData.class);
			
			whiteList.setBiosWLTarget(getWhiteListTarget(req.getParameter("biosWLTagrget")));
			whiteList.setVmmWLTarget(getWhiteListTarget(req.getParameter("vmmWLTarget")));
			
			System.err.println(whiteList);
			
			
			// Now save the whiteListConfig object to the session
			HttpSession httpSession = req.getSession(false);
			httpSession.setAttribute("White-List", whiteList);
			result = true;
			
		} catch (Exception ex) {
			log.severe("Exception during whitelist configuration. " + ex.getMessage());
			responseView.addObject("message","Error during white list configuration. " + ex.getMessage());
			responseView.addObject("result",result);
			ex.printStackTrace();
			return responseView;
		}
		responseView.addObject("result",result);
		log.info("ManagementConsoleDataController.defineWhiteListConfig <<<");
		return responseView;
		
	}*/
	
	private HostWhiteListTarget getBiosWhiteListTarget(String target) throws Exception{
		List<HostWhiteListTarget> biosTargetList = new ArrayList<HostWhiteListTarget>();
		biosTargetList.add(HostWhiteListTarget.BIOS_HOST);
		biosTargetList.add(HostWhiteListTarget.BIOS_OEM);
		for (HostWhiteListTarget whiteListTargets : biosTargetList){
			if (whiteListTargets.getValue().equals(target)){	
				return whiteListTargets; 
			}
		}
		return null;
	}
	
	private HostWhiteListTarget getVmmWhiteListTarget(String target) throws Exception{
		List<HostWhiteListTarget> vmmTargetList = new ArrayList<HostWhiteListTarget>();
		vmmTargetList.add(HostWhiteListTarget.VMM_OEM);
		vmmTargetList.add(HostWhiteListTarget.VMM_HOST);
		vmmTargetList.add(HostWhiteListTarget.VMM_GLOBAL);
		for (HostWhiteListTarget whiteListTargets : vmmTargetList){
			if (whiteListTargets.getValue().equals(target)){	
				return whiteListTargets; 
			}
		}
		return null;
	}
	
	private HostVMMType getHostVmmTypeTarget(String target) throws Exception{
		for (HostVMMType hostVmmType : HostVMMType.values()){
			if (hostVmmType.getValue().equals(target)){	
				return hostVmmType; 
			}
		}
		return null;
	}
	
	/**
	 * This Method will use to get Data for pre configured  white List Configuration.
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public ModelAndView getDefinedWhiteListConfig(HttpServletRequest req, HttpServletResponse res) {
		log.info("ManagementConsoleDataController.getDefinedWhiteListConfig >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		boolean result = false;
		HostConfigData whiteList;
		
		try {
			// Now get the whiteListConfig object from the session
			HttpSession httpSession = req.getSession(false);
			whiteList = (HostConfigData) httpSession.getAttribute("White-List");
			System.err.println(whiteList);
			responseView.addObject("whiteListConfig",whiteList);
			responseView.addObject("biosWLTarget",whiteList.getBiosWLTarget().getValue() == null ? "" : whiteList.getBiosWLTarget().getValue());
			responseView.addObject("vmmWLTarget",whiteList.getVmmWLTarget().getValue() == null ? "" : whiteList.getVmmWLTarget().getValue());
			result = true;
			
		} catch (Exception ex) {
			log.error("Exception during whitelist configuration. " + ex.getMessage());
			responseView.addObject("message", ex.getMessage());
			responseView.addObject("result",result);
			return responseView;
		}
		responseView.addObject("result",result);
		log.info("ManagementConsoleDataController.getDefinedWhiteListConfig <<<");
		return responseView;
		
	}
	
	/**
	 * This Method is use to get Retrive Host data from VMWare Cluster. using Cluster name and vCenterConnection String.
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public ModelAndView retriveHostFromCluster(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.retriveHostFromCluster >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		String clusterName;
		String vCenterConnection ;
                
		try {
			clusterName = req.getParameter("clusterName");
			vCenterConnection = req.getParameter("vCentertConnection");
		} catch (Exception e) {
			log.error("Error while getting Input parameter from request."+e.getMessage());
			responseView.addObject("message","Input Parameters are NUll.");
			responseView.addObject("result",false);
			return responseView;
		}
		
		try {
			List<HostDetails> listOfRegisterHost = services.getHostEntryFromVMWareCluster(clusterName,vCenterConnection);
			if (listOfRegisterHost != null) {
				responseView = getListofRegisteredHost(listOfRegisterHost,responseView,getApiClientService(req, ApiClient.class));
			}
		} catch (Exception e) {
            log.error("Error while getting data from VMware vCeneter. " + e.getMessage());
            responseView.addObject("message",e.getMessage());
            responseView.addObject("result",false);
            return responseView;
		}

                responseView.addObject("result",true);
		log.info("ManagementConsoleDataController.retriveHostFromCluster <<<");
		return responseView;
		
	}
	
	/**
	 * This Method is use to register multiple host on server..
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public ModelAndView registerMultipleHost(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.registerMultipleHost >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		
		String hostListString;
		
		if(req.getParameter("hostToBeRegister") != null){
			hostListString = req.getParameter("hostToBeRegister");
		} else {
            log.info("hostToBeRegister parameter is Null");
            responseView.addObject("result",false);
            responseView.addObject("message","Input Parameters are NULL.");
            return responseView;
		}
		
		HostDetails hostDetailsObj;
		
                        
		try {
			// Now get the API object from the session
			ApiClient apiObj = getApiClientService(req, ApiClient.class);
			@SuppressWarnings("serial")
			Type hostDetail = new TypeToken<HostDetails>() {}.getType();
			hostDetailsObj = new Gson().fromJson(hostListString, hostDetail);
			
			hostDetailsObj.setBiosWLTarget(getBiosWhiteListTarget(req.getParameter("biosWLTarget")));
			hostDetailsObj.setVmmWLtarget(getVmmWhiteListTarget(req.getParameter("vmmWLTarget")));
			
			//if (hostDetailsObj.isRegistered()) {
				//If host is already register Update host info
				//responseView.addObject("hostVOs", services.updateRegisteredHost(hostDetailsObj, apiObj));
			//}else {
				// register New host
				responseView.addObject("hostVOs", services.registerNewHost(hostDetailsObj, apiObj));
			//}
                        
		} catch (Exception e) {
                    
			log.error("Error while registering the hosts. " + e.getMessage());
			/* Soni_Begin_26/09/2012_issue_for_consistent_Error_Message  */
			responseView.addObject("message",e.getMessage());
			/* Soni_Begin_26/09/2012_issue_for_consistent_Error_Message  */
			responseView.addObject("result",false);
			return responseView;
		}
		
		responseView.addObject("result",true);
		log.info("ManagementConsoleDataController.registerMultipleHost <<<");
		return responseView;
		
	}
	
	/**
	 * This Method is use to get All pending registration request.
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public ModelAndView getAllPendingRegistrationRequest(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.getAllPendingRegistrationRequest >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		
		try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            
            responseView.addObject("pendingRequest", services.getApiClients(apiObj, ApiClientListType.PENDING));
            responseView.addObject("allRoles", services.getAllRoles(apiObj));
		} catch (Exception e) {
                    
			log.error("Error While getting pending requests. "+e.getMessage());
			responseView.addObject("message",e.getMessage());
			responseView.addObject("result",false);
			return responseView;
                        
		}
		responseView.addObject("result",true);
		log.info("ManagementConsoleDataController.getAllPendingRegistrationRequest <<<");
		return responseView;
	}
	
        /**
         * 
         * @param req
         * @param res
         * @return 
         */
	public ModelAndView getAllApprovedRequests(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.getAllApprovedRequests >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		
		try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            responseView.addObject("approvedRequests", services.getApiClients(apiObj, ApiClientListType.ALL));
		} catch (Exception e) {
			log.error("Error While getting all approved requests. "+e.getMessage());
			responseView.addObject("message",e.getMessage());
			responseView.addObject("result",false);
			return responseView;
                        
		}
		responseView.addObject("result",true);
		log.info("ManagementConsoleDataController.getAllApprovedRequests <<<");
		return responseView;
	}

        /**
         * 
         * @param req
         * @param res
         * @return 
         */
	public ModelAndView getAllExpiringApiClients(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.getAllExpiringApiClients >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		
		try {
	        // Now get the API object from the session
	        ApiClient apiObj = getApiClientService(req, ApiClient.class);
	        
	        responseView.addObject("expiringApiClients", services.getApiClients(apiObj, ApiClientListType.EXPIRING));
	        responseView.addObject("expirationMonths", MCPConfig.getConfiguration().getString("mtwilson.mc.apiKeyExpirationNoticeInMonths"));
                    
		} catch (Exception e) {
                    
			log.error("Error While getting all expiring API clients. "+e.getMessage());
			responseView.addObject("message",e.getMessage());
			responseView.addObject("result",false);
			return responseView;
                        
		}
		responseView.addObject("result",true);
		log.info("ManagementConsoleDataController.getAllExpiringApiClients <<<");
		return responseView;
	}

        /**
         * 
         * @param req
         * @param res
         * @return 
         */
	public ModelAndView getApiClientsForDelete(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.getApiClientsForDelete >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		
		try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            
            responseView.addObject("apiClientList", services.getApiClients(apiObj, ApiClientListType.DELETE));
		} catch (Exception e) {
			log.error("Error while getting Api clients. "+e.getMessage());
			responseView.addObject("message",e.getMessage());
			responseView.addObject("result",false);
			return responseView;
		}
		responseView.addObject("result",true);
		log.info("ManagementConsoleDataController.getApiClientsForDelete <<<");
		return responseView;
	}

        /**
	 * This Method is use Approve selected request from Approve request page.
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public ModelAndView approveSelectedRequest(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.approveSelectedRequest >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		String hostDetailsString;
		ApiClientDetails apiClientDetailsObj;
                boolean result = false;
                
		try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            
            hostDetailsString = req.getParameter("requestVO");
            apiClientDetailsObj = getObjectFromJSONString(hostDetailsString,ApiClientDetails.class);
            result = services.updateRequest(apiClientDetailsObj, apiObj, true);
		}catch (Exception ex) {
			log.error(ex.getMessage());
			responseView.addObject("result", false);
			responseView.addObject("message", ex.getMessage());
			return responseView;
		}
		
                responseView.addObject("result", result);
		log.info("ManagementConsoleDataController.approveSelectedRequest <<<");
		return responseView;
	}
	

	/**
	 * This Method is use Reject selected request from Approve request page.
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public ModelAndView rejectSelectedRequest(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.rejectSelectedRequest >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		String hostDetailsString;
		ApiClientDetails apiClientDetailsObj;
                boolean result = false;
                
		try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
            
            hostDetailsString = req.getParameter("requestVO");
            apiClientDetailsObj = getObjectFromJSONString(hostDetailsString,ApiClientDetails.class);
            result = services.updateRequest(apiClientDetailsObj, apiObj, false);
		}catch (Exception ex) {
                    
			log.error(ex.getMessage());
			responseView.addObject("result", result);
			responseView.addObject("message", ex.getMessage());
			return responseView;
                        
		}

        responseView.addObject("result",result);
		log.info("ManagementConsoleDataController.rejectSelectedRequest <<<");
		return responseView;
	}
		
	/**
	 * This Method is use to delete selected request from Delete request page.
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public ModelAndView deleteSelectedRequest(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.deleteSelectedRequest >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
                String fingerprint;
                boolean result = false;
                
		try {
                    
                    // Now get the API object from the session
                    ApiClient apiObj = getApiClientService(req, ApiClient.class);
                    
                    fingerprint = req.getParameter("fingerprint");
                    result = services.deleteSelectedRequest(fingerprint, apiObj);
                    
		}catch (Exception ex) {
                    
			log.error(ex.getMessage());
			responseView.addObject("result", result);
			responseView.addObject("message", ex.getMessage());
			return responseView;
                        
		}
                
		responseView.addObject("result",result);
		log.info("ManagementConsoleDataController.deleteSelectedRequest <<<");
		return responseView;
	}
        
	public ModelAndView logOutUser(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.logOutUser >>");
		ModelAndView responseView = new ModelAndView("Login");
		try {
			HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		log.info("ManagementConsoleDataController.logOutUser <<");
		return responseView;
	}
	
	/*
	 * Method to provide Services Object while calling servies methods. used by Spring Conatiner.
	 * 
	 */
	public void setServices(IManagementConsoleServices services){
		this.services = services;
	}
	

	@SuppressWarnings("unchecked")
	private <T> T getObjectFromJSONString(String objectString,Class<T> type) {
		Type apiClientDetailsObj = TypeToken.of(type).getType();
		Gson gson = new Gson();
		return (T)gson.fromJson(objectString, apiClientDetailsObj);
	}
	
	/**
     * This method will return a AttestationService/ApiCLient Object from a Session.
     * This object is stored into Session at time of user login.
     * Check CheckLoginController.java for more Clarification.
     * 
     * @param req
     * @return 
     * @return AttestationService
	 * @throws ManagementConsolePortalException 
     * @throws DemoPortalException
     */
    @SuppressWarnings("unchecked")
	private <T> T getApiClientService(HttpServletRequest req,Class<T> type) throws ManagementConsolePortalException{
        
    	//getting already created session object by passing false while calling into getSession();
    	HttpSession session = req.getSession(false);
        T service = null;
        if(session !=null){
            try{
            	//getting ApiClient Object from Session and downcast that object to Type T.  
                service = (T) session.getAttribute("api-object");    
            } catch (Exception e) {
				log.error("Error while creating ApiCliennt Object. "+e.getMessage());
				throw new ManagementConsolePortalException("Error while creating ApiClient Object. "+e.getMessage(),e);
            }
            
        }
        return service;
     }
    
    private ModelAndView getListofRegisteredHost(List<HostDetails> listOfRegisterHost, ModelAndView responseView,ApiClient apiClient) {
		List<HostDetails> listToSend = new ArrayList<HostDetails>();
			
		for (HostDetails hostDetails : listOfRegisterHost) {
			HostDetails details = hostDetails;
			try {
				List<TxtHostRecord> list = apiClient.queryForHosts(hostDetails.getHostName());
				if (list.size() <= 0) {
					details.setRegistered(false);
				}else{
					TxtHostRecord response = list.get(0);
					details.setRegistered(true);
					details.setStatus(HelperConstant.ALREADY_REGISTER);
					System.err.println("response.VMM_Name >>"+response.VMM_Name);
					
					String biosOem = response.BIOS_Oem;
					if (biosOem.contains(" ")) {
						biosOem = biosOem.split(" ")[0];
					}
					
					if (response.VMM_Name.toLowerCase().contains(hostDetails.getHostName().toLowerCase())) {
						responseView.addObject("vmmConfigValue_"+hostDetails.getHostName(),HostWhiteListTarget.VMM_HOST.getValue());
					}else if (response.VMM_Name.toLowerCase().contains(biosOem.toLowerCase())) {
						responseView.addObject("vmmConfigValue_"+hostDetails.getHostName(),HostWhiteListTarget.VMM_OEM.getValue());
					}else {
						responseView.addObject("vmmConfigValue_"+hostDetails.getHostName(),HostWhiteListTarget.VMM_GLOBAL.getValue());
					}
					
					System.err.println("response.biosWLTarget >>"+response.VMM_Name);
					if (response.VMM_Name.toLowerCase().contains(hostDetails.getHostName().toLowerCase())) {
						responseView.addObject("biosConfigValue_"+hostDetails.getHostName(),HostWhiteListTarget.BIOS_HOST.getValue());
					}else {
						responseView.addObject("biosConfigValue_"+hostDetails.getHostName(),HostWhiteListTarget.BIOS_OEM.getValue());
					}
				}
				
			} catch (Exception e) {
				log.error("Error While getting host info using QueryForHost method, cause : "+e.getMessage());
				details.setRegistered(false);
			}
			listToSend.add(details);
		}
	
		responseView.addObject("hostVO",listToSend);
		List<String> wlBios = new ArrayList<String>();
		wlBios.add(HostWhiteListTarget.BIOS_OEM.getValue());
		wlBios.add(HostWhiteListTarget.BIOS_HOST.getValue());
		responseView.addObject("wlBiosList",wlBios);
		
		List<String> wlVMM = new ArrayList<String>();
		wlVMM.add(HostWhiteListTarget.VMM_OEM.getValue());
		wlVMM.add(HostWhiteListTarget.VMM_HOST.getValue());
		wlVMM.add(HostWhiteListTarget.VMM_GLOBAL.getValue());
		responseView.addObject("wlVMMList",wlVMM);
		responseView.addObject("SpecificHostValue",HostWhiteListTarget.VMM_HOST.getValue());
		return responseView;
	}
    
    //Begin_Added by Soni-Function for CA
    public ModelAndView getAllCAStatus(HttpServletRequest req,HttpServletResponse res) {
		log.info("ManagementConsoleDataController.getAllCAStatus >>");
		ModelAndView responseView = new ModelAndView(new JSONView());
		
		try {
            // Now get the API object from the session
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
         
            responseView.addObject("caStatus", services.getCADetails(apiObj));
                        
		} catch (Exception e) {
			log.error("Error While getting ca status. "+e.getMessage());
			responseView.addObject("message",e.getMessage());
			responseView.addObject("result",false);
			return responseView;
                        
		}
		responseView.addObject("result",true);
		
		log.info("ManagementConsoleDataController.getAllCAStatus <<<");
		return responseView;
	}
  //End_Added by Soni-Function for CA

    //Begin_Added by Soni-Function to download SAML certificate
    public ModelAndView getSAMLCertificate(HttpServletRequest req,HttpServletResponse res) {
		log.info("In Data Contoller ManagementConsoleDataController.getSAMLCertificate  >>");
		//ModelAndView responseView = new ModelAndView("SAMLDownload");
		ModelAndView responseView = new ModelAndView(new JSONView());
		 res.setContentType("application/octet-stream ");
         res.setHeader("Content-Disposition",
         "attachment;filename=mtwilson-saml.crt");
		
		try {
            // Now get the API object from the session
			
            ApiClient apiObj = getApiClientService(req, ApiClient.class);
          // InputStream in = new ByteArrayInputStream(apiObj.getSamlCertificate().getEncoded());
           //System.err.println("SAMLcertificate     "+apiObj.getSamlCertificate().getEncoded());
           //IOUtils.copy(in, res.getOutputStream());
            //ServletOutputStream out = res.getOutputStream();
        /*  FileOutputStream out = new FileOutputStream("C:/mtwilson-saml.crt");
            System.err.println("ServletOutputStream"+out);
            byte[] outputByte = new byte[4096];
          //copy binary contect to output stream
          while(in.read(outputByte, 0, 4096) != -1)
          {System.err.println("outputByte           "+outputByte);
          	out.write(outputByte, 0, 4096);
            System.err.println("ServletOutputStream"+out);
          }
      
          System.err.println("ServletOutputStream"+out);*/
            responseView.addObject("SAMLcertificate", apiObj.getSamlCertificate().getEncoded());
            responseView.addObject("result",true);
        log.info("ManagementConsoleDataController.getSAMLCertificate <<<");
  		
  	/*	 in.close();
         out.flush();
         out.close();
  		*/
                        
		} catch (Exception e) {
			log.error("Error While getting Downlaoding Certificate. "+e.getMessage());			
			responseView.addObject("message",e.getMessage());
			responseView.addObject("result",false);
			return responseView;
			
                        
		}
		
		return responseView;
		
	}
    //End_Added by Soni-Function to download SAML certificate
    
}
