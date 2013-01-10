/**
 * This class is use to register new user for WhiteListPortal
 */
package com.intel.mountwilson.controller;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.intel.mountwilson.common.WLMPConfig;
import com.intel.mountwilson.util.JSONView;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.Role;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yuvrajsx
 *
 */
public class RegisterUserController extends AbstractController {

	
	// variable declaration used for logging. 
	private static final Logger logger = Logger.getLogger(RegisterUserController.class.getName());
	
        private boolean isNullOrEmpty(String str) { return str == null || str.isEmpty(); }
        
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res) throws Exception {
		logger.info("RegisterUserController >>");
		ModelAndView view = new ModelAndView(new JSONView());
		
		String username,password;
		
		//Getting variables from configuration file or from WLMPConfig.java
        final String dirName = WLMPConfig.getConfiguration().getString("mtwilson.wlmp.keystore.dir");
        final String baseURL = WLMPConfig.getConfiguration().getString("mtwilson.api.baseurl");
		
		if (isNullOrEmpty(req.getParameter("userNameTXT")) || isNullOrEmpty(req.getParameter("passwordTXT"))) {
			view.addObject("result",false);
			view.addObject("message", "username and password can't be Blank.");
			return view;
		}else {
			//Getting User Name and Password from request object.
			username = req.getParameter("userNameTXT");
			password = req.getParameter("passwordTXT");
		}
		
		//Taking all files from a Directory using directory name mention in WLMPConfig.
		File[] files = new File(dirName).listFiles();
		
		//Checking for duplicate user registration by comparing file name with username. If equals then user is already register.
		if (files != null && files.length > 0) {
			for (File keystoreName : files) {
			    if (keystoreName.isFile()) {
			        if (keystoreName.getName().equalsIgnoreCase(username+".jks")) {
						logger.info("An user already exists with the specified User Name. Please select different User Name.");
						view.addObject("result",false);
			            view.addObject("message","An user already exists with the specified User Name. Please select different User Name.");
			            return view;
					}
			    }
			}
		}
                

        try {
        	//calling into REST services to register a user.
        	SimpleKeystore response = KeystoreUtil.createUserInDirectory(new File(dirName), username, password, new URL(baseURL), new String[] { Role.Whitelist.toString() });
	        if (response == null) {
	            view.addObject("result",false);
	            view.addObject("message", "Server Side Error. Could not register the user. Keystore is null.");
	            return view;
	        }
        } catch (Exception e) {
            view.addObject("result",false);   
            view.addObject("message", "Server Side Error. Could not register the user. "+ e.getMessage());
               e.printStackTrace();
               return view;
        }
		view.addObject("result",true);
		return view;
	}
}
