/**
 * 
 */
package com.intel.mountwilson.controller;

import com.intel.mountwilson.common.MCPConfig;
import com.intel.mountwilson.common.MCPersistenceManager;
import com.intel.mountwilson.common.TDPConfig;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.My;
import com.intel.mtwilson.api.*;
import com.intel.dcsg.cpg.crypto.RsaCredential;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuvrajsx
 *
 */
public class CheckLoginController extends AbstractController {
    private Logger log = LoggerFactory.getLogger(getClass());
	
	// variable declaration used during Processing data. 
            
	private MCPersistenceManager mcManager = new MCPersistenceManager();
	
        private boolean isNullOrEmpty(String str) { return str == null || str.isEmpty(); }

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res) throws MalformedURLException, IOException {
            log.debug("CheckLoginController >>");
            ModelAndView view = new ModelAndView("Login");
            
            
            String keyAliasName = req.getParameter("userNameTXT");
            String keyPassword = req.getParameter("passwordTXT");
            String locale = "en";

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
            MwPortalUserJpaController keystoreJpa = My.jpa().mwPortalUser();
            MwPortalUser tblKeystore = keystoreJpa.findMwPortalUserByUserName(keyAliasName);
            if(tblKeystore == null){
                view.addObject("message", "Username or Password does not match or the user is not enabled.");                
                view.addObject("result", false);
                return view; 
            }

            // Partial fix for Bug 965 to prevent users in rejected/pending status from logging into the portal.
            if (!tblKeystore.getEnabled()) {
                view.addObject("message", "Username or Password does not match or the user is not enabled.");                
                view.addObject("result", false);
                return view;                 
            }
            
            ByteArrayResource keyResource = new ByteArrayResource(tblKeystore.getKeystore());
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
                view.addObject("message", "Username or Password does not match or the user is not enabled.");
                return view;                    
            } 

            try {
                
                Properties p = new Properties();
//                p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "false");
//                p.setProperty("mtwilson.api.ssl.verifyHostname", "false");
                p.setProperty("mtwilson.api.ssl.policy", My.configuration().getConfiguration().getString("mtwilson.api.ssl.policy", "TRUST_CA_VERIFY_HOSTNAME")); // must be secure out of the box!
                p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", My.configuration().getConfiguration().getString("mtwilson.api.ssl.requireTrustedCertificate", "true")); // must be secure out of the box!
                p.setProperty("mtwilson.api.ssl.verifyHostname",My.configuration().getConfiguration().getString("mtwilson.api.ssl.verifyHostname", "true")); // must be secure out of the box!

                ApiClient rsaApiClient = null;
                // Instantiate the API Client object and store it in the session. Otherwise either we need
                // to store the password in the session or the decrypted RSA key
                try {
                    // bug #1038 if mtwilson.api.baseurl is not configured or is invalid we get a MalformedURLException so it needs to be in a try block so we can catch it and respond appropriately
                    URL baseURL = new URL(My.configuration().getConfiguration().getString("mtwilson.api.baseurl"));
                    rsaApiClient = new ApiClient(baseURL, credential, keystore, new MapConfiguration(p));
                } catch (ClientException e) {
                    log.error("Cannot create API client: "+e.toString(), e);
                    view.addObject("result", false);
                }
                
                // get locale
                try {
                    locale = rsaApiClient.getLocale(keyAliasName);
                    log.debug("Found locale {} for portal user: {}", locale, keyAliasName);
                } catch (Exception e) {
                    log.error("Cannot retrieve locale for user: {}\r\n{}", keyAliasName, e.toString());
                    locale = null;
                }
                
                
                HttpSession session = req.getSession();
                session.setAttribute("logged-in", true);
                	session.setAttribute("username", keyAliasName);
                session.setMaxInactiveInterval(My.configuration().getConfiguration().getInt("mtwilson.portal.sessionTimeOut", 1800));
                

                        X509Certificate[] trustedCertificates = keystore.getTrustedCertificates(SimpleKeystore.SAML);
	     
                        //Storing information into a request session. These variables are used in DemoPortalDataController.
			session.setAttribute("logged-in", true);
            session.setAttribute("api-object", rsaApiClient);
			session.setAttribute("apiClientObject",rsaApiClient);
			session.setAttribute("trustedCertificates",trustedCertificates);
                
            } catch (Exception ex) {
                log.error("Login failed", ex);
                view.addObject("message", "Error during user authentication. " + StringEscapeUtils.escapeHtml(ex.getMessage()));
                return view;                    
                
            }
            
            Cookie langCookie = new Cookie("lang", locale);
            res.addCookie(langCookie);
            
            res.sendRedirect("home.html");
		
            return null;
	}

}
