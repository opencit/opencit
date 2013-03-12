/**
 * This class is used to return Home Page for TrustDashBoard.
 * This class will get called if user will hit /home.html
 * 
 * For more clarification please check TrustDashboard-servlet.xml, for all address mapping.
 */

package com.intel.mountwilson.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author yuvrajsx
 *
 */
public class HomeController extends AbstractController {

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res) throws Exception {
		return new ModelAndView("Home");
	}
}
