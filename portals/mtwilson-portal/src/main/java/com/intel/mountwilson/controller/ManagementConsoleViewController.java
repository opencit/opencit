/**
 * 
 */
package com.intel.mountwilson.controller;

import com.intel.mountwilson.constant.HelperConstant;
import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.HostVMMType;
import com.intel.mtwilson.datatypes.HostWhiteListTarget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * @author yuvrajsx
 *
 */
@Controller
public class ManagementConsoleViewController extends MultiActionController{
	
	private static final Logger log = LoggerFactory.getLogger(ManagementConsoleViewController.class.getName());
	
	public ModelAndView getWhiteListConfigurationPage(HttpServletRequest req,HttpServletResponse res){
		ModelAndView modelAndView = new ModelAndView("WhiteListConfiguration");
		try {
			
			List<String> hostNameList = new ArrayList<>();
			
			HostVMMType[] hostVMMTypes = HostVMMType.values();
			for (HostVMMType hostVMMType : hostVMMTypes) {
				hostNameList.add(hostVMMType.getValue());
			}
					
			List<Map<String, Object>> hostList = new ArrayList<>(); 
			for (String hostName : hostNameList) {
				Map<String, Object> map = new HashMap<>();
				boolean isVmm = false;
				if (hostName.toLowerCase().contains(HelperConstant.VMWARE_TYPE.toLowerCase())) {
					isVmm = true;
				}
				map.put("hostName", hostName);
				map.put("isVMM", isVmm);
				map.put("pcrs", getHostVMMTypeFromValue(hostName));
				hostList.add(map);
			}
			modelAndView.addObject("hostTypeList",hostList);
			List<String> wlBiosList = new ArrayList<>();
			wlBiosList.add(HostWhiteListTarget.BIOS_OEM.getValue());
			wlBiosList.add(HostWhiteListTarget.BIOS_HOST.getValue());
			modelAndView.addObject("BIOSWhiteList",wlBiosList);
			
			List<String> wlVMMList = new ArrayList<>();
			wlVMMList.add(HostWhiteListTarget.VMM_OEM.getValue());
			wlVMMList.add(HostWhiteListTarget.VMM_HOST.getValue());
			wlVMMList.add(HostWhiteListTarget.VMM_GLOBAL.getValue());			
                        modelAndView.addObject("vmmWhiteList",wlVMMList);
			
		} catch (Exception e) {
			log.error("Error while Getting Host Entry from Config file."+e.getMessage());
			modelAndView.addObject("result",false);
			modelAndView.addObject("message","Error while Getting Host Entry from HostVMMType Class.");
			return modelAndView;
		}
		modelAndView.addObject("result",true);
		return modelAndView;
	}
	
	private String getHostVMMTypeFromValue(String hostName) {
		for (HostVMMType hostType : HostVMMType.values()) {
			if (hostType.getValue().equals(hostName)) {
				return hostType.getPcrs();
			}
		}
		return "";
	}

	public ModelAndView getAssetTagPage(HttpServletRequest req,HttpServletResponse res ) {
		return new ModelAndView("tag/index");
	}
    
	public ModelAndView getRegisterHostPage(HttpServletRequest req,HttpServletResponse res ) {
		return new ModelAndView("RegisterHost");
	}

	public ModelAndView getApproveRequestPage(HttpServletRequest req,HttpServletResponse res ) {
		return new ModelAndView("ApproveRequest");
	}
	public ModelAndView getApproveRejectPage(HttpServletRequest req,HttpServletResponse res ) {
		return new ModelAndView("ApproveReject");
	}
	public ModelAndView getViewExpiringPage(HttpServletRequest req,HttpServletResponse res ) {
		return new ModelAndView("ViewExpiring");
	}
	
	public ModelAndView getRekeyApiClient(HttpServletRequest req,HttpServletResponse res ) {
		return new ModelAndView("RekeyApiClient");
	}
	
	public ModelAndView getDeleteRegistrationPage(HttpServletRequest req,HttpServletResponse res ) {
		return new ModelAndView("DeleteRegistration");
	}
	
	public ModelAndView getViewRequestPage(HttpServletRequest req,HttpServletResponse res ) {
		return new ModelAndView("ViewRequest");
	}
    public ModelAndView getRegisterPage(HttpServletRequest req, HttpServletResponse res) {
        //log.info("WLMViewController.getRegisterPage >>");
        ModelAndView modelAndView = new ModelAndView("Register");
        try {
            List<Map<String, Object>> localeList = new ArrayList<Map<String, Object>>();
            for (String localeName : My.configuration().getAvailableLocales()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("localeName", localeName);
                localeList.add(map);
            }
            modelAndView.addObject("locales", localeList);
        } catch (Exception e) {
            log.error("Error while Getting locale selections." + e.getMessage());
            modelAndView.addObject("result", false);
            modelAndView.addObject("message", "Error while Getting locale selections.");
            return modelAndView;
        }
        return modelAndView;
        
        //return new ModelAndView("Register");
    }
   /*--Begin Added by Soni on 18/10/12 for New Screen for CA */
    public ModelAndView getCAStatusPage(HttpServletRequest req,HttpServletResponse res ) {
		return new ModelAndView("CAStatus");
	}
    
    /*--End Added by Soni on 18/10/12 for New Screen for CA */
    
    /*--Begin Added by Soni on 18/10/12 for New Screen for CA */
    public ModelAndView getSAMLCertificatePage(HttpServletRequest req,HttpServletResponse res ) {
    	log.info("MGViewController.getSAMLCertificatePage >>");
		return new ModelAndView("SAMLDownload");
	
	}
    
    /*--End Added by Soni on 18/10/12 for New Screen for CA */
    
    /*--Begin Added by stdalex on 1/8/13 mc fingerprint */
    public ModelAndView getViewCertPage(HttpServletRequest req,HttpServletResponse res ) {
    	log.info("MGViewController.getViewCertPage >>");
        return new ModelAndView("CertDownload");
    }
    
    /*--End Added by stdale MC fingerprint 1/8/13 */
    
    /*public ModelAndView getDefineWhiteListConfig(HttpServletRequest req,HttpServletResponse res) {
    	log.info("WLMViewController.getDefineWhiteListConfig >>");
    	return new ModelAndView("DefineWhiteListConfig");
    }*/
    
    
    
    
    
    
    ///////////// from trust dashboard //////////////////////////
    
	//This method will return home page for TrustDashBoard.
	public ModelAndView getDashBoardPage(HttpServletRequest req,HttpServletResponse res) {
		log.info("DemoPortalViewController.getDashBoardPage");
		ModelAndView responseView = new ModelAndView("HostTrustStatus");
		return responseView;
	}
	
	//This method will return Add Host Page.
	public ModelAndView getAddHostPage(HttpServletRequest req , HttpServletResponse res){
		log.info("DemoPortalViewController.getAddHostPage");
		return new ModelAndView("AddHost");
	}
	
	public ModelAndView getViewHostPage(HttpServletRequest req , HttpServletResponse res){
		log.info("DemoPortalViewController.getViewHostPage");
		ModelAndView responseView = new ModelAndView("ViewHost");
		return responseView;
	}
	
	public ModelAndView getEditHostPage(HttpServletRequest req , HttpServletResponse res){
		log.info("DemoPortalViewController.getEditHostPage");
		ModelAndView responseView = new ModelAndView("EditHost");
		return responseView;
	}
        
        public ModelAndView showbulktrustUpdatePage(HttpServletRequest req , HttpServletResponse res){
        log.info("DemoPortalViewController.showbulktrustUpdatePage");
        ModelAndView responseView = new ModelAndView("BulktrustUpdate");
        return responseView;
	}
	
	public ModelAndView showReportsPage(HttpServletRequest req , HttpServletResponse res){
		log.info("DemoPortalViewController.showReportsPage");
		ModelAndView responseView = new ModelAndView("ShowReports");
		return responseView;
	}
	
	
	/**
	 * This method will return Page to show SAML Details for a host.
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public ModelAndView trustVerificationDetails(HttpServletRequest req,HttpServletResponse res) {
		log.info("DemoPortalDataController.trustVerificationDetails");
		ModelAndView responseView = new ModelAndView("TrustSamlDetails");
		String hostName = req.getParameter("hostName");
		responseView.addObject("hostName", hostName);
		return responseView;
	}
    
    
    
    ///////////////////////////// from whitelist portal ////////////////////////////
    

	/**
	 * Method to for OS Component
	 */
	public ModelAndView getEditOSPage(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMViewController.getEditOSPage >>");
		return new ModelAndView("EditOS");
	}
	
	public ModelAndView getViewOSPage(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMViewController.getViewOSPage >>");
		return new ModelAndView("ViewOS");
	}
	
	public ModelAndView getAddOSPage(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMViewController.getAddOSPage >>");
		return new ModelAndView("AddOS");
	}
	
	/**
	 * Method to for MLE Component
	 */
	public ModelAndView getAddMLEPage(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMViewController.getAddMLEPage >>");
		return new ModelAndView("AddMle");
	}
	
	public ModelAndView getViewMle(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMViewController.getViewMle >>");
		return new ModelAndView("ViewMle");
	}
	
	public ModelAndView getEditMle(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMViewController.getEditMle >>");
		return new ModelAndView("EditMle");
	}
	
	
	/**
	 * Method to for OEM Component
	 */
	public ModelAndView getViewOEMPage(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMViewController.getViewOEMPage >>");
		return new ModelAndView("ViewOEM");
	}
	
	//Method to get Edit page For OEM
	public ModelAndView getEditOEMPage(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMViewController.getEditOEMPage >>");
		return new ModelAndView("EditOEM");
	}
	
	public ModelAndView getAddOEMPage(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMViewController.getAddOEMPage >>");
		return new ModelAndView("AddOEM");
	}
	
	public ModelAndView getAboutPage(HttpServletRequest req,HttpServletResponse res) {
		log.info("WLMViewController.getAddOEMPage >>");
		return new ModelAndView("AboutWLM");
	}    
        
        public ModelAndView getAuthenticationPage(HttpServletRequest req,HttpServletResponse res) {
		log.info("DemoPortalViewController.getAuthenticationPage");
		return new ModelAndView("Authentication");
	}   
        
}
