/**
 * This class is used to return Login Page for TrustDashBoard.
 * This class will be called when user will not provide any specific root address other than context root.
 * This class will also get called if user will hit index.html or index.htm
 * 
 * For more clarification please check ManagementConsole-servlet.xml, for all address mapping.
 */
package com.intel.mountwilson.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class LoginController extends AbstractController {

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res)  {

		//setting a header value to true to tell ajax request that return page is login page.
		//this variable is used in commonUtil.js file while getting a AJAX response. If this variable is true it will show a pop-up to user to inform that session is expired. 
		res.setHeader("loginPage", "true");
		
		return new ModelAndView("Login");
	}

}
