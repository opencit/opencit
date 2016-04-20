/**
 * 
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
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res)  {
		return new ModelAndView("Home");
	}
	
	

}
