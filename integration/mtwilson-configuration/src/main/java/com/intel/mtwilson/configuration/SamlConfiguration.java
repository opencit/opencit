/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;


/**
 * THIS CLASS IS TENTATIVE - NOT CURENTLY BEING USED OUTSIDE THIS PACKAGE
 * @author jbuhacoff
 */
public class SamlConfiguration extends AbstractConfiguration {
    public static final String SAML_KEYSTORE_FILE = "saml.keystore.file";
    public static final String SAML_KEYSTORE_PASSWORD = "saml.keystore.password";
    public static final String SAML_KEY_ALIAS = "saml.key.alias";
    public static final String SAML_KEY_PASSWORD = "saml.key.password";
    
//    private String samlKeystoreFile;
//    private String samlKeystorePassword;
//    private String samlKeyAlias;
//    private String samlKeyPassword;

    public String getSamlKeyAlias() {
        return getConfiguration().getString(SAML_KEY_ALIAS);
    }
    public void setSamlKeyAlias(String samlKeyAlias) {
        getConfiguration().setString(SAML_KEY_ALIAS, samlKeyAlias);
    }
    
    public String getSamlKeyPassword() {
        return getConfiguration().getString(SAML_KEY_PASSWORD);
    }

    public void setSamlKeyPassword(String samlKeyPassword) {
        getConfiguration().setString(SAML_KEY_PASSWORD, samlKeyPassword);
    }

    public String getSamlKeystoreFile() {
        return getConfiguration().getString(SAML_KEYSTORE_FILE);
    }

    public void setSamlKeystoreFile(String samlKeystoreFile) {
        getConfiguration().setString(SAML_KEYSTORE_FILE, samlKeystoreFile);
    }

    public String getSamlKeystorePassword() {
        return getConfiguration().getString(SAML_KEYSTORE_PASSWORD);
    }

    public void setSamlKeystorePassword(String samlKeystorePassword) {
        getConfiguration().setString(SAML_KEYSTORE_PASSWORD, samlKeystorePassword);
    }


}
