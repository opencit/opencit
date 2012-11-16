/**
 * 
 */
package com.intel.mountwilson.controller;

import com.intel.mountwilson.common.MCPConfig;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.ClientException;
import com.intel.mtwilson.crypto.RsaCredential;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.io.Filename;
import com.mysql.jdbc.StringUtils;
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
            ModelAndView view = new ModelAndView("Login");

            String keyAliasName = req.getParameter("userNameTXT");
            String keyPassword = req.getParameter("passwordTXT");

            if (StringUtils.isNullOrEmpty(keyAliasName) || StringUtils.isNullOrEmpty(keyPassword)) {
                view.addObject("result", false);
                view.addObject("message", "User Name and Password cannot be empty");
                return view;
            }

//            String keyStore = MCPConfig.getConfiguration().getString("mtwilson.mc.keyStoreFileName");
            String keystoreFilename = MCPConfig.getConfiguration().getString("mtwilson.mc.keystore.dir") + File.separator + Filename.encode(keyAliasName) + ".jks";
            URL baseURL = new URL(MCPConfig.getConfiguration().getString("mtwilson.api.baseurl"));
            File keyStoreFile = null;

            try {

                keyStoreFile = new File(keystoreFilename);

            } catch (Exception ex) {
                view.addObject("result", false);
                view.addObject("message", "Key store is not configured/saved correctly in " + keystoreFilename + ".");
                return view;                    
            }

            RsaCredential credential = null;
            SimpleKeystore keystore = null;
            try {
//                KeyStore keystore = KeystoreUtil.open(new FileInputStream(keyStoreFile), keyPassword);
//                credential = KeystoreUtil.loadX509(keystore, keyAliasName, keyPassword);
                keystore = new SimpleKeystore(keyStoreFile, keyPassword);
                credential = keystore.getRsaCredentialX509(keyAliasName, keyPassword);

            } catch (Exception ex) {
                view.addObject("result", false);
                view.addObject("message", "Username or Password does not match. Please try again.");
                return view;                    
            } 

            try {
                HttpSession session = req.getSession();
                session.setAttribute("logged-in", true);
                if (keyAliasName != null) {
                	session.setAttribute("username", keyAliasName);
				}
                session.setMaxInactiveInterval(MCPConfig.getConfiguration().getInt("mtwilson.mc.sessionTimeOut"));
                
                Properties p = new Properties();
//                p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "false");
//                p.setProperty("mtwilson.api.ssl.verifyHostname", "false");
                p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true"); // must be secure out of the box!
                p.setProperty("mtwilson.api.ssl.verifyHostname", "true"); // must be secure out of the box!

                ApiClient rsaApiClient = null;
                // Instantiate the API Client object and store it in the session. Otherwise either we need
                // to store the password in the session or the decrypted RSA key
                try {
                    rsaApiClient = new ApiClient(baseURL, credential, keystore, new MapConfiguration(p));
                } catch (ClientException e) {
                    view.addObject("result", false);
                }
                session.setAttribute("api-object", rsaApiClient);
                
            } catch (Exception ex) {

                view.addObject("message", "Error during user authentication. " + ex.getMessage());
                return view;                    
                
            } 
                
            res.sendRedirect("home.html");
		
            return null;
	}

}
