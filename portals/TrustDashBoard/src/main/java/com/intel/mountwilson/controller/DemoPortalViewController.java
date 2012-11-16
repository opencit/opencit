/**
 * This Class is use to return JSP's for each view present in TrustDashBoard.
 */
package com.intel.mountwilson.controller;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * @author yuvrajsx
 *
 */
public class DemoPortalViewController extends MultiActionController {
	
	// variable declaration used for Logging.  
	private static final Logger logger = Logger.getLogger(DemoPortalViewController.class.getName());
	
	//This method will return home page for TrustDashBoard.
	public ModelAndView getDashBoardPage(HttpServletRequest req,HttpServletResponse res) {
		logger.info("DemoPortalViewController.getDashBoardPage");
		ModelAndView responseView = new ModelAndView("HostTrustStatus");
		return responseView;
	}
	
	//This method will return Add Host Page.
	public ModelAndView getAddHostPage(HttpServletRequest req , HttpServletResponse res){
		logger.info("DemoPortalViewController.getAddHostPage");
		return new ModelAndView("AddHost");
	}
	
	public ModelAndView getViewHostPage(HttpServletRequest req , HttpServletResponse res){
		logger.info("DemoPortalViewController.getViewHostPage");
		ModelAndView responseView = new ModelAndView("ViewHost");
		return responseView;
	}
	
	public ModelAndView getEditHostPage(HttpServletRequest req , HttpServletResponse res){
		logger.info("DemoPortalViewController.getEditHostPage");
		ModelAndView responseView = new ModelAndView("EditHost");
		return responseView;
	}
        
        public ModelAndView showbulktrustUpdatePage(HttpServletRequest req , HttpServletResponse res){
        logger.info("DemoPortalViewController.showbulktrustUpdatePage");
        ModelAndView responseView = new ModelAndView("BulktrustUpdate");
        return responseView;
	}
	
	public ModelAndView showReportsPage(HttpServletRequest req , HttpServletResponse res){
		logger.info("DemoPortalViewController.showReportsPage");
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
		logger.info("DemoPortalDataController.trustVerificationDetails");
		ModelAndView responseView = new ModelAndView("TrustSamlDetails");
		String hostName = req.getParameter("hostName");
		responseView.addObject("hostName", hostName);
		return responseView;
	}
	
    //this method will return Page to register new user.
	public ModelAndView getRegisterPage(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getRegisterPage");
		return new ModelAndView("Register");
	}
}
