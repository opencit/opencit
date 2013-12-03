/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag;

import com.intel.mtwilson.atag.dao.Derby;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.ApiClientFactory;
import com.intel.mtwilson.My;
import com.intel.mtwilson.api.MtWilson;
import com.intel.mtwilson.atag.dao.jdbi.*;
import com.intel.mtwilson.atag.model.*;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.io.FileResource;
import com.intel.mtwilson.io.Resource;
import com.intel.mtwilson.tls.KeystoreCertificateRepository;
import com.intel.mtwilson.tls.TlsPolicy;
import com.intel.mtwilson.tls.TrustFirstCertificateTlsPolicy;
import java.io.IOException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class Global {
    private static Logger log = LoggerFactory.getLogger(RestletApplication.class);
    private static Configuration currentConfiguration = null;
    private static PrivateKey cakey = null; // private key to use for automatically signing new certificates
    private static X509Certificate cakeyCert = null; // the specific certificate corresponding to the private key
    private static List<X509Certificate> cacerts = null; // the list of all approved certificates (including cakeyCert)
    private static MtWilson mtwilson = null;
    
    public static Configuration configuration() throws IOException {
        if( currentConfiguration == null ) {
            log.debug("Loading global configuration...");
            ConfigurationDAO configurationDao = null;
            try {
                configurationDao = Derby.configurationDao();
                currentConfiguration = configurationDao.findByName("main");
                if( currentConfiguration == null ) {
                    log.debug("Cannot find 'main' configuration, using default");
                    currentConfiguration = new Configuration(); // XXX using default config because cannot find main config
                }
            }
            catch(Exception e) {
                log.error("Cannot load configuration, using default", e);
                currentConfiguration = new Configuration(); // XXX using default config because of database error 
            }
            finally {
                if( configurationDao != null ) { configurationDao.close(); }
            }
        }
        log.debug("Loaded configuration: {}", currentConfiguration.getXmlContent());
        return currentConfiguration;
    }
    
    public static void reset() {
        currentConfiguration = null;
        cakey = null;
        cacerts = null;
    }
    
    public static PrivateKey cakey() {
        if( cakey == null ) {
            log.debug("Loading CA key...");
            FileDAO fileDao = null;
            try {
                fileDao = Derby.fileDao();
                File cakeyFile = fileDao.findByName("cakey");
                if( cakeyFile == null ) {
                    log.debug("Cannot find 'cakey' file");
                }
                else {
                    String content = new String(cakeyFile.getContent(), "UTF-8");
                    cakey = RsaUtil.decodePemPrivateKey(content);
                    cakeyCert = X509Util.decodePemCertificate(content);
                }
            }
            catch(Exception e) {
                log.error("Cannot load cakey", e);
                cakey = null;
                cakeyCert = null;
            }
            finally {
                if( fileDao != null ) { fileDao.close(); }
            }
        }
        return cakey;
    }
    
    public static X509Certificate cakeyCert() {
        cakey(); // loads the private key AND the certificate and initializes cakeyCert
        return cakeyCert; // either X509Certificate object or null if there was an error
    }
    
    public static List<X509Certificate> cacerts() {
        if( cacerts == null ) {
            log.debug("Loading CA cert...");
            FileDAO fileDao = null;
            try {
                fileDao = Derby.fileDao();
                File cacertFile = fileDao.findByName("cacerts");
                if( cacertFile == null ) {
                    log.debug("Cannot find 'cacert' file");
                }
                else {
                    cacerts = X509Util.decodePemCertificates(new String(cacertFile.getContent(), "UTF-8"));
                }
            }
            catch(Exception e) {
                log.error("Cannot load cacerts", e);
                cacerts = null;
            }
            finally {
                if( fileDao != null ) { fileDao.close(); }
            }
        }
        return cacerts;
    }
        
    public static MtWilson mtwilson() {
        if( mtwilson == null ) {
            // the mtwilson api client keystore is stored in our database as a file
            log.debug("Preparing Mt Wilson Web Service API Client...");
            FileDAO fileDao = null;
            ByteArrayResource keystoreResource = null;
            try {
                fileDao = Derby.fileDao();
                File mtwilsonKeystoreFile = fileDao.findByName("mtwilson-client-keystore");
                if( mtwilsonKeystoreFile == null ) {
                    log.debug("Cannot find 'mtwilson-client-keystore' file");
                }
                else {
                    keystoreResource = new ByteArrayResource(mtwilsonKeystoreFile.getContent());
                }
            }
            catch(Exception e) {
                log.error("Cannot load mtwilson-client-keystore", e);
                
            }
            finally {
                if( fileDao != null ) { fileDao.close(); }
            }
            
            try {
            String keystoreUsername = My.configuration().getKeystoreUsername(); //configuration().getMtWilsonClientKeystoreUsername();
            String keystorePassword = My.configuration().getKeystorePassword(); //configuration().getMtWilsonClientKeystorePassword();
            
            SimpleKeystore keystore = new SimpleKeystore(keystoreResource, keystorePassword);
            KeystoreCertificateRepository respository = new KeystoreCertificateRepository(keystore);
            URL url = My.configuration().getMtWilsonURL();  //configuration().getMtWilsonURL();
            ApiClientFactory factory = new ApiClientFactory();
            mtwilson = factory.clientForUserInResource(keystoreResource, keystoreUsername, keystorePassword, url, new TrustFirstCertificateTlsPolicy(respository));
            }
            catch(Exception e) {
                log.error("Cannot create MtWilson client", e);
            }
            
        }
        return mtwilson;
    }
}
