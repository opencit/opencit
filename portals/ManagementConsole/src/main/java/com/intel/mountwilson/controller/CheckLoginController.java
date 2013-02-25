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
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mountwilson.common.MCPersistenceManager;
import com.intel.mtwilson.as.controller.MwKeystoreJpaController;
import com.intel.mtwilson.as.data.MwKeystore;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.data.MwPortalUser;
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
	private MCPersistenceManager mcManager = new MCPersistenceManager();
	private MwPortalUserJpaController keystoreJpa = new MwPortalUserJpaController(mcManager.getEntityManagerFactory("MSDataPU"));
        private boolean isNullOrEmpty(String str) { return str == null || str.isEmpty(); }

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res) throws Exception {
            logger.info("CheckLoginController >>");
            ModelAndView view = new ModelAndView("Login");
            String keystoreFilename = "";
            
            String keyAliasName = req.getParameter("userNameTXT");
            String keyPassword = req.getParameter("passwordTXT");

            if (isNullOrEmpty(keyAliasName) || isNullOrEmpty(keyPassword)) {
                view.addObject("result", false);
                view.addObject("message", "User Name and Password cannot be empty");
                return view;
            }

            // String keyStore = MCPConfig.getConfiguration().getString("mtwilson.mc.keyStoreFileName");
            //String keystoreFilename = MCPConfig.getConfiguration().getString("mtwilson.mc.keystore.dir") + File.separator + Filename.encode(keyAliasName) + ".jks";
            
            //File keyStoreFile = null;
            //try {
            // keyStoreFile = new File(keystoreFilename);
            //} catch (Exception ex) {
            //    view.addObject("result", false);
            //    view.addObject("message", "Key store is not configured/saved correctly in " + keystoreFilename + ".");
            //    return view;                    
            //}
            //stdalex 1/15 jks2db!disk
            MwPortalUser tblKeystore = keystoreJpa.findMwPortalUserByUserName(keyAliasName);
            if(tblKeystore == null){
                view.addObject("message", "Unable to retrieve the user details for authentication. Please enter again.");                
                view.addObject("result", false);
                return view; 
            }
            ByteArrayResource keyResource = new ByteArrayResource(tblKeystore.getKeystore());
            URL baseURL = new URL(MCPConfig.getConfiguration().getString("mtwilson.api.baseurl"));
            RsaCredential credential = null;
            SimpleKeystore keystore = null;
            try {
//                KeyStore keystore = KeystoreUtil.open(new FileInputStream(keyStoreFile), keyPassword);
//                credential = KeystoreUtil.loadX509(keystore, keyAliasName, keyPassword);
                keystore = new SimpleKeystore(keyResource, keyPassword);
                //new SimpleKeystore(keyStoreFile, keyPassword);
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
                p.setProperty("mtwilson.api.ssl.policy", MCPConfig.getConfiguration().getString("mtwilson.api.ssl.policy", "TRUST_CA_VERIFY_HOSTNAME")); // must be secure out of the box!
                p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", MCPConfig.getConfiguration().getString("mtwilson.api.ssl.requireTrustedCertificate", "true")); // must be secure out of the box!
                p.setProperty("mtwilson.api.ssl.verifyHostname", MCPConfig.getConfiguration().getString("mtwilson.api.ssl.verifyHostname", "true")); // must be secure out of the box!

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
