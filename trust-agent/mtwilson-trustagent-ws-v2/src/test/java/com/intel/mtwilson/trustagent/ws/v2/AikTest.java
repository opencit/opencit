package com.intel.mtwilson.trustagent.ws.v2;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.TrustagentRepository;

/*
 * before you run this test you need export TRUSTAGENT_PASSWORD=<your password> in linux cmd line
 */

/**
 * 
 * @author  zjj
 * 
 */

public class AikTest {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Aik.class);
		
	@Before
    public void setUp() {
    	
    	System.setProperty("mtwilson.application.id", "trustagent"); //set your install directory
    	System.setProperty("mtwilson.environment.prefix", "TRUSTAGENT_");
    	    	
    }
    
    @After
    public void tearDown() {
    	
    }
        
    /*
     * 
     * Test methods of getIdentity use, of class Aik.
     */	  
        
    @Test
    public void testGetIdentity() throws IOException, CertificateException{
    	X509Certificate identity = null;
    	if (identity == null) {
            TrustagentConfiguration configuration = TrustagentConfiguration.loadConfiguration();
            if (configuration.isDaaEnabled()) {
                log.debug("daa is currently not supported");
                                
            } else {
                TrustagentRepository repository = new TrustagentRepository(configuration);
                X509Certificate aikCertificate = repository.getAikCertificate();
                if (aikCertificate == null) {
                    throw new WebApplicationException(Response.serverError().header("Error", "Cannot load AIK certificate file").build());
                }
                identity = aikCertificate;
            }
        }
        System.out.println( "aik is: " + identity);
    }
    
    /*
     * 
     * Test methods of getIdentityCA use, of class Aik.
     */	
    
    @Test
    public void testGetIdentityCA() throws IOException, CertificateException{
    	X509Certificate identityIssuer = null;
    	if (identityIssuer == null) {
            TrustagentConfiguration configuration = TrustagentConfiguration.loadConfiguration();
            File keystoreFile = configuration.getTrustagentKeystoreFile();
            if (!keystoreFile.exists()) {
                log.debug("Missing keystore file: {}", keystoreFile.getAbsolutePath());
                throw new WebApplicationException(Response.serverError().header("Error", "Missing CA keystore file").build());
            }
            try {
                SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), configuration.getTrustagentKeystorePassword());
                X509Certificate privacyCACertificate = keystore.getX509Certificate("privacy", SimpleKeystore.CA);
                identityIssuer = privacyCACertificate;
            } catch (KeyManagementException | NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
                log.debug("Unable to load Privacy CA certificate from keystore file");
                log.debug("Unable to load Privacy CA certificate from keystore file", e);
                throw new WebApplicationException(Response.serverError().header("Error", "Cannot load Privacy CA certificate file").build());
            }
        }
        System.out.println( "ca is: " + identityIssuer);
    }
}
