/**
 * This class is used to check username and password while login.
 */
package com.intel.mountwilson.controller;

import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.MapConfiguration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.intel.mountwilson.common.WLMPConfig;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.crypto.RsaCredential;
import com.intel.mtwilson.io.Filename;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mountwilson.common.WLMPPersistenceManager;
import com.intel.mtwilson.as.controller.MwKeystoreJpaController;
import com.intel.mtwilson.as.data.MwKeystore;

/**
 * @author yuvrajsx
 *
 */
public class CheckLoginController extends AbstractController {

	
	// variable declaration used for logging. 
	private static final Logger logger = Logger.getLogger(CheckLoginController.class.getName());
	private WLMPPersistenceManager wlmManager = new WLMPPersistenceManager();
	private MwKeystoreJpaController keystoreJpa = new MwKeystoreJpaController(wlmManager.getEntityManagerFactory("ASDataPU"));
        
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res) throws Exception {
		logger.info("CheckLoginController >>");
		
		//Creating ModelAndView Object with Login page to return to user if Login is not successful.
		ModelAndView view = new ModelAndView("Login");
		RsaCredential credential = null;
                //File keystoreFile = null;
                SimpleKeystore keystore = null;
		
		String username,password;
		URL baseURL = new URL(WLMPConfig.getConfiguration().getString("mtwilson.api.baseurl"));  
                
		try {
			//getting username and password from request parameters.
			username = req.getParameter("userNameTXT");
			password = req.getParameter("passwordTXT");
		} catch (Exception e) {
			view.addObject("message", "username and password can't be blank.");
			return view;
		}
		/*
		try{
                    //this line will throw exception if file with username is not present in specific dir.
                    keystoreFile = new File(keystoreFilename);
                }catch(Exception e){
                    logger.severe("File Not found on server >> "+keystoreFilename);
                    view.addObject("message", "Key store is not configured/saved correctly in " + keystoreFilename + ".");
                    return view;
                }
                */
                //stdalex 1/15 jks2db!disk
                 MwKeystore tblKeystore = keystoreJpa.findMwKeystoreByName(username);
                 if(tblKeystore == null){
                    view.addObject("result", false);
                    view.addObject("message", "Username or Password does not match. Please try again.");
                    return view; 
                }
                ByteArrayResource keyResource = new ByteArrayResource(tblKeystore.getKeystore());		
		try {
                    keystore = new SimpleKeystore(keyResource, password);
                    credential = keystore.getRsaCredentialX509(username, password);
		} catch (Exception e) {
                        view.addObject("result", false);
			view.addObject("message", "Username or Password does not match. Please try again.");
			return view;
		}
		
		
		try {
                    Properties p = new Properties();
                    p.setProperty("mtwilson.api.ssl.policy", WLMPConfig.getConfiguration().getString("mtwilson.api.ssl.policy", "TRUST_CA_VERIFY_HOSTNAME")); // must be secure out of the box!
                    p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", WLMPConfig.getConfiguration().getString("mtwilson.api.ssl.requireTrustedCertificate", "true")); // must be secure out of the box!
                    p.setProperty("mtwilson.api.ssl.verifyHostname", WLMPConfig.getConfiguration().getString("mtwilson.api.ssl.verifyHostname", "true")); // must be secure out of the box!
	 
                    // Instantiate the API Client object and store it in the session. Otherwise either we need
                    // to store the password in the session or the decrypted RSA key
                    ApiClient rsaApiClient = new ApiClient(baseURL, credential, keystore, new MapConfiguration(p));
	      
                    //Storing variable into a session object used while calling into RESt Services.
                    HttpSession session = req.getSession();
                    session.setAttribute("logged-in", true);
                    session.setAttribute("username",username);
                    session.setAttribute("apiClientObject",rsaApiClient);
                    session.setMaxInactiveInterval(WLMPConfig.getConfiguration().getInt("mtwilson.wlmp.sessionTimeOut"));
	        
                    //Redirecting user to a home page after successful login.
                    res.sendRedirect("home.html");
             
		} catch (Exception e) {
			view.addObject("message", "The username or password you entered is incorrect.");
			return view;
		}
		return null;
	}
}
