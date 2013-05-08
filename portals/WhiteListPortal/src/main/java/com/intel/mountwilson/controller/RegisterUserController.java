/**
 * This class is use to register new user for WhiteListPortal
 */
package com.intel.mountwilson.controller;

import com.intel.mountwilson.common.WLMPConfig;
import com.intel.mountwilson.common.WLMPPersistenceManager;
import com.intel.mountwilson.util.JSONView;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.Role;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author yuvrajsx
 *
 */
public class RegisterUserController extends AbstractController {

	
	// variable declaration used for logging. 
	private static final Logger logger = LoggerFactory.getLogger(RegisterUserController.class.getName());
	private WLMPPersistenceManager wlmManager = new WLMPPersistenceManager();
	private MwPortalUserJpaController keystoreJpa = new MwPortalUserJpaController(wlmManager.getEntityManagerFactory("MSDataPU"));
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
		
                //Checking for duplicate user registration by seeing if there is already a cert in table for user
                MwPortalUser keyTest = keystoreJpa.findMwPortalUserByUserName(username);
                if(keyTest != null) {
                  logger.info("An user already exists with the specified User Name. Please select different User Name.");
		  view.addObject("result",false);
		  view.addObject("message","An user already exists with the specified User Name. Please select different User Name.");
		  return view;      
                }
                /*
		//Taking all files from a Directory using directory name mention in WLMPConfig.
		File[] files = new File(dirName).listFiles();
			
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
                */

        try {
        	//calling into REST services to register a user.
                // stdalex 1/15 jks2db!disk
                //SimpleKeystore response = KeystoreUtil.createUserInDirectory(new File(dirName), username, password, new URL(baseURL), new String[] { Role.Whitelist.toString() });
                ByteArrayResource certResource = new ByteArrayResource();
        	SimpleKeystore response = KeystoreUtil.createUserInResource(certResource, username, password, new URL(baseURL), new String[] { Role.Whitelist.toString() });
                MwPortalUser keyTable = new MwPortalUser();
                keyTable.setUsername(username);
                keyTable.setStatus("PENDING");
                keyTable.setKeystore(certResource.toByteArray());
                keystoreJpa.create(keyTable);
        	
	        if (response == null) {
	            view.addObject("result",false);
	            view.addObject("message", "Server Side Error. Could not register the user. Keystore is null.");
	            return view;
	        }
        } catch (Exception e) {
            view.addObject("result",false);   
            view.addObject("message", "Server Side Error. Could not register the user. "+ StringEscapeUtils.escapeHtml(e.getMessage()));
               return view;
        }
		view.addObject("result",true);
		return view;
	}
}
