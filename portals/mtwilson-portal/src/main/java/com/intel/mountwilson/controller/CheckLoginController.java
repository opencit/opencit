/**
 * 
 */
package com.intel.mountwilson.controller;

import com.intel.mountwilson.common.MCPersistenceManager;
import com.intel.mtwilson.My;
import com.intel.mtwilson.api.*;
import com.intel.dcsg.cpg.crypto.RsaCredential;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mountwilson.util.ProxyApiClient;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.Properties;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
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
	public static final String USERNAME_REGEX = "[a-zA-Z0-9,;.@ _-]+";
	// variable declaration used during Processing data. 
            
	private MCPersistenceManager mcManager = new MCPersistenceManager();
	
        private boolean isNullOrEmpty(String str) { return str == null || str.isEmpty(); }

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,HttpServletResponse res) throws MalformedURLException, IOException {
            log.debug("CheckLoginController >>");
            ModelAndView view = new ModelAndView("Login");
            
            
            String keyAliasName = req.getParameter("userNameTXT");
            String keyPassword = req.getParameter("passwordTXT");
            String locale;

            if (isNullOrEmpty(keyAliasName) || isNullOrEmpty(keyPassword)) {
                view.addObject("result", false);
                view.addObject("message", "User Name and Password cannot be empty");
                return view;
            }
            
            if( !ValidationUtil.isValidWithRegex(keyAliasName, USERNAME_REGEX) ) {
                view.addObject("result", false);
                view.addObject("message", "User Name is invalid");
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
            
            // workaround for connection pool returning stale connections after being idle too long; when the connection pool is fixed this will work on the first try
            MwPortalUser tblKeystore;
            try {
                tblKeystore = keystoreJpa.findMwPortalUserByUserName(keyAliasName);
            }
            catch(Exception e) {
                log.debug("Got exception [{}] while looking up user '{}', retrying", e.getMessage(), keyAliasName);
                tblKeystore = keystoreJpa.findMwPortalUserByUserName(keyAliasName);
            }
            
            
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
            RsaCredential credential;
            SimpleKeystore keystore;
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

                ProxyApiClient rsaApiClient = null;
                // Instantiate the API Client object and store it in the session. Otherwise either we need
                // to store the password in the session or the decrypted RSA key
                try {
                    // bug #1038 if mtwilson.api.baseurl is not configured or is invalid we get a MalformedURLException so it needs to be in a try block so we can catch it and respond appropriately
                    URL baseURL = new URL(My.configuration().getConfiguration().getString("mtwilson.api.baseurl"));
                    rsaApiClient = new ProxyApiClient(baseURL, credential, keystore, new MapConfiguration(p));
                    if (rsaApiClient == null) {
                        view.addObject("result", false);
                        view.addObject("message", "Failed to initialize the RSA API client object.");
                        return view;
                    }
                    /*  this was a temporary workaround for an authentication issue - delete when fixed:
                    Properties ptemp = new Properties();
                    ptemp.setProperty("mtwilson.api.baseurl", My.configuration().getConfiguration().getString("mtwilson.api.baseurl"));
                    ptemp.setProperty("mtwilson.api.ssl.policy", "INSECURE");
                    Configuration temp = new MapConfiguration(ptemp);
                    rsaApiClient = new ProxyApiClient(temp);
                    */
                } catch (ClientException e) {
                    log.error("Cannot create API client: "+e.toString(), e);
                    view.addObject("result", false);
                }
                
                // get locale
                try {
                    if (rsaApiClient != null) {
                        locale = rsaApiClient.getLocaleForUser(keyAliasName);
                        log.debug("Found locale {} for portal user: {}", locale, keyAliasName);
                    } else {
                        locale = null;
                    }
                } catch (Exception e) {
                    log.warn("Cannot retrieve locale for user: {}\r\n{}", keyAliasName, e.toString());
                    locale = null;
                }
                
                // set locale in apiclient
                try {
                    if (rsaApiClient != null) {
                        rsaApiClient.setLocale(LocaleUtil.forLanguageTag(locale));
                        log.debug("Set locale {} on apiclient", locale);
                    }
                } catch (Exception e) {
                    log.warn("Cannot set locale on API client object.\r\n{}", e.toString());
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
                
            } catch (MalformedURLException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException ex) {
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
