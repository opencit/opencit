/**
 * This class is used to return Login Page for TrustDashBoard.
 * This class will be called when user will not provide any specific root address other than context root.
 * This class will also get called if user will hit index.html or index.htm
 * 
 * For more clarification please check TrustDashboard-servlet.xml, for all address mapping.
 */

package com.intel.mountwilson.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class LoginController extends AbstractController {

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res) throws Exception {
		res.setHeader("loginPage", "true");
		return new ModelAndView("Login");
	}

}
