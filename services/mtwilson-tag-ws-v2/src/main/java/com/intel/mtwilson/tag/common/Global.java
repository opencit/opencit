/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.common;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.ApiClientFactory;
import com.intel.mtwilson.My;
import com.intel.mtwilson.api.MtWilson;
import com.intel.mtwilson.tag.dao.jdbi.*;
import com.intel.mtwilson.tag.model.*;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.jaxrs2.client.PropertiesTlsPolicyFactory;
import com.intel.mtwilson.tag.dao.TagJdbi;
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
    private static Logger log = LoggerFactory.getLogger(Global.class);
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
                configurationDao = TagJdbi.configurationDao();
                currentConfiguration = configurationDao.findByName("main");
                if( currentConfiguration == null ) {
                    log.debug("Cannot find 'main' configuration, using default");
                    currentConfiguration = new Configuration(); 
                }
            }
            catch(Exception e) {
                log.error("Cannot load configuration, using default", e);
                currentConfiguration = new Configuration(); 
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
            try(FileDAO fileDao = TagJdbi.fileDao()) {
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
                fileDao = TagJdbi.fileDao();
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
                fileDao = TagJdbi.fileDao();
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
            String keystoreUsername = My.configuration().getTagKeystoreUsername(); //configuration().getMtWilsonClientKeystoreUsername();
            String keystorePassword = My.configuration().getTagKeystorePassword(); //configuration().getMtWilsonClientKeystorePassword();
            
            URL url = My.configuration().getMtWilsonURL();  //configuration().getMtWilsonURL();
            ApiClientFactory factory = new ApiClientFactory();
            //TlsPolicy tlsPolicy = V1TlsPolicyFactory.getInstance().getTlsPolicyWithKeystore(keystore);
            TlsPolicy tlsPolicy = PropertiesTlsPolicyFactory.createTlsPolicy(My.configuration().getClientProperties());
            mtwilson = factory.clientForUserInResource(keystoreResource, keystoreUsername, keystorePassword, url, tlsPolicy);
            }
            catch(Exception e) {
                log.error("Cannot create MtWilson client", e);
            }
            
        }
        return mtwilson;
    }
}
