/**
 * This Class is use to check login Credential. If login is successful it will redirect user to home page otherwise it will return Login page. 
 */
package com.intel.mountwilson.controller;

import com.intel.mountwilson.common.TDPConfig;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.crypto.RsaCredential;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.io.Filename;
import java.io.File;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.configuration.MapConfiguration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author yuvrajsx
 *
 */
public class CheckLoginController extends AbstractController {

	
	// variable declaration used during Processing data. 
	private static final Logger logger = Logger.getLogger(CheckLoginController.class.getName());
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res) throws Exception {
		logger.info("CheckLoginController >>");
		
		//Creating ModelAndView Object with Login page to return to user if Login is not successful.
		ModelAndView view = new ModelAndView("Login");
		RsaCredential credential = null;
		File keystoreFile = null;
        SimpleKeystore keystore = null;
		
		String username, password;
        
        URL baseURL = new URL(TDPConfig.getConfiguration().getString("mtwilson.api.baseurl")); 
		
        try {
        	//getting username and password from request parameters.
			username = req.getParameter("userNameTXT");
			password = req.getParameter("passwordTXT");
		} catch (Exception e) {
			view.addObject("message", "username and password can't be Blank.");
			return view;
		}
		
        final String keystoreFilename = TDPConfig.getConfiguration().getString("mtwilson.tdbp.keystore.dir") + File.separator + Filename.encode(username) + ".jks";
		
		try{
			keystoreFile = new File(keystoreFilename);
        }catch(Exception e){
            logger.severe("File Not found on server >> "+keystoreFilename);
            e.printStackTrace();
            view.addObject("message", "Private key file is missing on server.");
            return view;
        }
		
		try {
            keystore = new SimpleKeystore(keystoreFile, password);
            credential = keystore.getRsaCredentialX509(username, password);
		} catch (Exception e) {
			view.addObject("message", "The username or password you entered is incorrect.");
			return view;
		}
		
		
		try {
			Properties p = new Properties();
	        p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true"); // must be secure out of the box!
	        p.setProperty("mtwilson.api.ssl.verifyHostname", "true"); // must be secure out of the box!
	     
	        ApiClient rsaApiClient = new ApiClient(baseURL, credential, keystore, new MapConfiguration(p));
	        X509Certificate[] trustedCertificates = keystore.getTrustedCertificates(SimpleKeystore.SAML);
	     
	        //Storing information into a request session. These variables are used in DemoPortalDataController.
	        HttpSession session = req.getSession();
			session.setAttribute("logged-in", true);
			session.setAttribute("username",username);
			session.setAttribute("apiClientObject",rsaApiClient);
			session.setAttribute("trustedCertificates",trustedCertificates);
			session.setMaxInactiveInterval(Integer.parseInt(TDPConfig.getConfiguration().getString("mtwilson.tdbp.sessionTimeOut")));
			
			//if Login is successful, redirecting user to first/Home page.
			res.sendRedirect("home.html");
		} catch (Exception e) {
			view.addObject("message", "The username or password you entered is incorrect.");
			return view;
		}
		return null;
	}
}