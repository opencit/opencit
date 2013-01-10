/**
 * This class is use to register new user for TrustDashBoard
 */
package com.intel.mountwilson.controller;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.intel.mountwilson.common.TDPConfig;
import com.intel.mountwilson.util.JSONView;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.Role;

/**
 * @author yuvrajsx
 *
 */
public class RegisterUserController extends AbstractController {
	
	// variable declaration used for Logging. 
    private static final Logger logger = Logger.getLogger(RegisterUserController.class.getName());
	
        private boolean isNullOrEmpty(String str) { return str == null || str.isEmpty(); }
    
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res) throws Exception {
		logger.info("RegisterUserController >>");
		ModelAndView view = new ModelAndView(new JSONView());
		
		String username,password;
        final String dirName = TDPConfig.getConfiguration().getString("mtwilson.tdbp.keystore.dir");
        final String baseURL = TDPConfig.getConfiguration().getString("mtwilson.api.baseurl");
		
		try {
			username = req.getParameter("userNameTXT");
			password = req.getParameter("passwordTXT");
		} catch (Exception e) {
			view.addObject("result",false);
			view.addObject("message", "username and password can't be Blank.");
			return view;
		}
		
		//Checking for null value against Username and password. 
		if (isNullOrEmpty(username) || isNullOrEmpty(password)) {
            view.addObject("result",false);
            view.addObject("message", "username and password can't be Blank.");
            return view;
		 }

		
		//Taking all files from a Directory using directory name mention in TDPConfig.
		File[] files = new File(dirName).listFiles();
		
		//Checking for duplicate user registration by comparing file name with username. If equals then user is already register.  
		if (files != null) {
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
        	SimpleKeystore response = KeystoreUtil.createUserInDirectory(new File(dirName), username, password, new URL(baseURL), new String[] { Role.Whitelist.toString(),Role.Attestation.toString()});
            if (response == null) {
                view.addObject("result",false);
                view.addObject("message", "Server Side Error. Could not register the user. Keystore is null.");
                return view;
            }
        } catch (Exception e) {
            view.addObject("result",false);   
            view.addObject("message", "Server Side Error. Could not register the user. " + e.getMessage());
               e.printStackTrace();
               return view;
        }
        
		view.addObject("result",true);
		return view;
	}
	
}
