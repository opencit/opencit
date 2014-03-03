/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.MutableConfiguration;


/**
 * THIS CLASS IS TENTATIVE - NOT CURENTLY BEING USED OUTSIDE THIS PACKAGE
 * 
 * The setters are not defined because if some part of the application 
 * needs to change the saml keystore password, or the alias, etc. then
 * it cannot just change those settings and save the configuration because
 * things will cease to work. Those changes need to be done by a setup task
 * that knows to save the keystore with a new password, or to archive the
 * old certificate, etc. 
 * 
 * @author jbuhacoff
 */
public class SamlConfiguration extends AbstractConfiguration {
    public static final String SAML_KEYSTORE_FILE = "saml.keystore.file";
    public static final String SAML_KEYSTORE_PASSWORD = "saml.keystore.password";
    public static final String SAML_KEY_ALIAS = "saml.key.alias";
    public static final String SAML_KEY_PASSWORD = "saml.key.password";
    public static final String SAML_ISSUER = "saml.issuer"; // saml.certificate.dn 
    public static final String SAML_VALIDITY_SECONDS = "saml.validity.seconds";
    
    public SamlConfiguration(Configuration configuration) {
        super();
        setConfiguration(configuration);
    }

    public String getSamlIssuer() { 
        return getConfiguration().getString(SAML_ISSUER);
    }
    
    public Long getSamlValiditySeconds() {
        return getConfiguration().getLong(SAML_VALIDITY_SECONDS);
    }
    
    public String getSamlKeyAlias() {
        return getConfiguration().getString(SAML_KEY_ALIAS);
    }
    
    public String getSamlKeyPassword() {
        return getConfiguration().getString(SAML_KEY_PASSWORD);
    }

    public String getSamlKeystoreFile() {
        return getConfiguration().getString(SAML_KEYSTORE_FILE);
    }

    public String getSamlKeystorePassword() {
        return getConfiguration().getString(SAML_KEYSTORE_PASSWORD);
    }

}
