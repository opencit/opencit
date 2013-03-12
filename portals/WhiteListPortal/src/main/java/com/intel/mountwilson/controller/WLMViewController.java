/**
 * Class use to return Web Pages for each MLE,OEM and OS Component.
 */
package com.intel.mountwilson.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
/**
 * @author yuvrajsx
 *
 */
@Controller
public class WLMViewController extends MultiActionController{
	
	private static final Logger logger = LoggerFactory.getLogger(WLMViewController.class.getName()); 

	/**
	 * Method to for OS Component
	 */
	public ModelAndView getEditOSPage(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getEditOSPage >>");
		return new ModelAndView("EditOS");
	}
	
	public ModelAndView getViewOSPage(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getViewOSPage >>");
		return new ModelAndView("ViewOS");
	}
	
	public ModelAndView getAddOSPage(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getAddOSPage >>");
		return new ModelAndView("AddOS");
	}
	
	/**
	 * Method to for MLE Component
	 */
	public ModelAndView getAddMLEPage(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getAddMLEPage >>");
		return new ModelAndView("AddMle");
	}
	
	public ModelAndView getViewMle(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getViewMle >>");
		return new ModelAndView("ViewMle");
	}
	
	public ModelAndView getEditMle(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getEditMle >>");
		return new ModelAndView("EditMle");
	}
	
	
	/**
	 * Method to for OEM Component
	 */
	public ModelAndView getViewOEMPage(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getViewOEMPage >>");
		return new ModelAndView("ViewOEM");
	}
	
	//Method to get Edit page For OEM
	public ModelAndView getEditOEMPage(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getEditOEMPage >>");
		return new ModelAndView("EditOEM");
	}
	
	public ModelAndView getAddOEMPage(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getAddOEMPage >>");
		return new ModelAndView("AddOEM");
	}
	
	public ModelAndView getAboutPage(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getAddOEMPage >>");
		return new ModelAndView("AboutWLM");
	}
	public ModelAndView getRegisterPage(HttpServletRequest req,HttpServletResponse res) {
		logger.info("WLMViewController.getRegisterPage >>");
		return new ModelAndView("Register");
	}
	
}
