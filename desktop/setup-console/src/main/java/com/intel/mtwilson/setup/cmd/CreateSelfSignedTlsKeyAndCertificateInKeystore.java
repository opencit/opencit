/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.as.controller.MwKeystoreJpaController;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwKeystore;
import com.intel.mtwilson.crypto.RsaUtil;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.io.CopyResource;
import com.intel.mtwilson.io.FileResource;
import com.intel.mtwilson.setup.AbstractCommand;
import com.intel.mtwilson.setup.Command;
import com.intel.mtwilson.setup.SetupContext;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class CreateSelfSignedTlsKeyAndCertificateInKeystore extends AbstractCommand {
    
    private ByteArrayResource keystoreResource;
    private SimpleKeystore keystore = null;
    

    @Override
    public void execute(String[] args) throws Exception {
        // find and load glassfish keystore
//        String keystorePath = options.getString("keystore", System.getProperty("javax.net.ssl.keyStore"));
//        String keystorePath = options.getString("storepass", System.getProperty("javax.net.ssl.keyStore"));
        // create new private key and certificate (we COULD use the existing private key...)
        // save the glassfish keystore
    }
    

    
    /**
     * Precondition:  setupConfiguration() and setupDataAccess()
     * 
     * @throws KeyManagementException
     */
    private void openKeystore() throws KeyManagementException {
//        mwKeystore = keystoreJpa.findMwKeystoreByName(HostTrustBO.SAML_KEYSTORE_NAME);
//        if( mwKeystore != null && mwKeystore.getKeystore() != null ) {
//            keystoreResource = new ByteArrayResource(mwKeystore.getKeystore());
            keystoreResource = new ByteArrayResource(); // XXX TODO: should we be loading it from somewhere ????
            keystore = new SimpleKeystore(keystoreResource, "keystore password");
//            log.info("Loaded SAML Keystore from database");
//        }
    }

    @Override
    protected void validate() {
//        requireOptions("javax.net.ssl.keyStore", "javax.net.ssl.keyStorePassword"); // we're assuming javax.net.ssl.keyStoreType = jks   (we don't support other types right now)
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
 
    
}
