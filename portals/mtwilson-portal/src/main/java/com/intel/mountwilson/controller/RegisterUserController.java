/**
 * 
 */
package com.intel.mountwilson.controller;

import com.intel.mountwilson.common.MCPConfig;
import com.intel.mountwilson.common.MCPersistenceManager;
import com.intel.mountwilson.util.JSONView;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.Role;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import static com.intel.mountwilson.controller.CheckLoginController.USERNAME_REGEX;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author yuvrajsx
 *
 */
public class RegisterUserController extends AbstractController {

	
	// variable declaration used during Processing data. 
        private static final Logger log = LoggerFactory.getLogger(RegisterUserController.class.getName());
	private MCPersistenceManager mcManager = new MCPersistenceManager();
	private MwPortalUserJpaController keystoreJpa = new MwPortalUserJpaController(mcManager.getEntityManagerFactory("MSDataPU"));
        
        private boolean isNullOrEmpty(String str) { return str == null || str.isEmpty(); }
        
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res)  {
		log.info("RegisterUserController >>");
		ModelAndView view = new ModelAndView(new JSONView());
		
            String username;
	    String password;
            String locale;
	    final String baseURL = MCPConfig.getConfiguration().getString("mtwilson.api.baseurl");
		
            // Get username and password from js
            if (isNullOrEmpty(req.getParameter("userNameTXT")) || isNullOrEmpty(req.getParameter("passwordTXT"))) {
                view.addObject("result", false);
                view.addObject("message", "username and password can't be Blank.");
                return view;
            } else {
                //Getting User Name and Password from request object.
                username = req.getParameter("userNameTXT");
                password = req.getParameter("passwordTXT");
            }
            
            if( !ValidationUtil.isValidWithRegex(username, USERNAME_REGEX) ) {
                view.addObject("result", false);
                view.addObject("message", "User Name is invalid");
                return view;
            }

            // Get locale from js
            if (isNullOrEmpty(req.getParameter("localeTXT"))) {
                view.addObject("result", false);
                view.addObject("message", "locale cannot be blank.");
                return view;
            } else {
                locale = req.getParameter("localeTXT");
            }
                
		//stdalex 1/15 jks2db!disk
                //Checking for duplicate user registration by seeing if there is already a cert in table for user
                MwPortalUser keyTest = keystoreJpa.findMwPortalUserByUserName(username);
                if(keyTest != null) {
                    // Bug: 1038: Hiding the error message so that existing user names cannot be found.
                  log.error("An user already exists with the specified User Name: {}. Different user name needs to be choosen.", username);
		  view.addObject("result",true);
		  //view.addObject("message","An user already exists with the specified User Name. Please select different User Name.");
		  return view;      
                }
                /*
		File[] files = new File(dirName).listFiles();
		
		if (files != null) {
			for (File keystoreName : files) {
			    if (keystoreName.isFile()) {
			        if (keystoreName.getName().equalsIgnoreCase(username+".jks")) {
					log.info("An user already exists with the specified User Name. Please select different User Name.");
						view.addObject("result",false);
			            view.addObject("message","An user already exists with the specified User Name. Please select different User Name.");
			            return view;
					}
			    }
			}
		}
                */
                

        try {
                // stdalex 1/15 jks2db!disk
                //SimpleKeystore response = KeystoreUtil.createUserInDirectory(new File(dirName), username, password, new URL(baseURL), new String[] { Role.Whitelist.toString(),Role.Attestation.toString(),Role.Security.toString()});
                
                ByteArrayResource certResource = new ByteArrayResource();
                log.info("registerusercontroller calling createUserInResource");
        	SimpleKeystore response = KeystoreUtil.createUserInResource(certResource, username, password, new URL(baseURL),new String[] { Role.Whitelist.toString(),Role.Attestation.toString()});
                // Feb 12, 2014: Sudhir: Since the portal user would be created by the above call, we just need to update with the keystore.                
                MwPortalUser keyTable = keystoreJpa.findMwPortalUserByUserName(username);
                if (keyTable != null) {
                    keyTable.setLocale(locale);
                    keyTable.setKeystore(certResource.toByteArray());
                    log.info("registerusercontroller updating the portal user table with the new keystore.");
                    keystoreJpa.edit(keyTable);
                } else {
                    view.addObject("result",false);
                    view.addObject("message", "Error during user registration. Please see the server log for more details.");                    
                }
        	
            if (response == null) {
                view.addObject("result",false);
                view.addObject("message", "Server Side Error. Could not register the user. Keystore is null.");
                return view;
            }
        } catch (Exception e) {
            view.addObject("result",false);   
            view.addObject("message", "Server Side Error. Could not register the user. " + StringEscapeUtils.escapeHtml(e.getMessage()));
               e.printStackTrace();
               return view;
        }
		view.addObject("result",true);
		return view;
	}
	

}
