/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

/**
 * just a draft
 * @author jbuhacoff
 */
public enum MtWilsonProperty {
    AS_DB_USERNAME("mountwilson.as.db.user", "username", "", "Database credential specific to Attestation Service"),
    AS_DB_PASSWORD("mountwilson.as.db.password", "password", "", "Database credential specific to Attestation Service"),
    AS_AIKQVERIFY_CMD("com.intel.mountwilson.as.aikqverify.cmd", "file", "aikqverify", "Filename; aikqverify on linux, aikqverify.exe on windows"), 
    AS_SAML_KEY_ALIAS("saml.key.alias", "keystore-alias", "samlkey1", "Alias of the SAML private key"),
    AS_SAML_KEYSTORE_FILE("saml.keystore.file", "file", "SAML.jks", "Filename of SAML keystore");
    
    private String propertyName;
    private String description;
    private String format;
    private String defaultValue;
    MtWilsonProperty(String propertyName, String format, String defaultValue, String description) {
        this.propertyName = propertyName;
        this.format = format;
        this.defaultValue = defaultValue;
        this.description = description;
    }
    
    public String propertyName() { return propertyName; }
    public String format() { return format; }
    public String description() { return description; }
    public String defaultValue() { return defaultValue; }
}
