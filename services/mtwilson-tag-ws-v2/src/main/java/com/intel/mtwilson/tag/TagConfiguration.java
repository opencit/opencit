/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.configuration.AbstractConfiguration;


/**
 * THIS CLASS IS TENTATIVE - NOT CURENTLY BEING USED OUTSIDE THIS PACKAGE
 * 
 * The setters are not defined because if some part of the application 
 * needs to change the tag keystore password, or the alias, etc. then
 * it cannot just change those settings and save the configuration because
 * things will cease to work. Those changes need to be done by a setup task
 * that knows to save the keystore with a new password, or to archive the
 * old certificate, etc. 
 * 
mtwilson.api.username=ATDemo
mtwilson.api.password=ATP@ssw0rd
mtwilson.atag.keystore=/root/AT-demo/serverAtag.jks            ; REMOVE because mtwilson ssl cert will replace it
mtwilson.atag.keystore.password=password            ; REMOVE because mtwilson ssl cert will replace it
mtwilson.atag.key.password=password                 ; REMOVE because mtwilson ssl cert will replace it
mtwilson.atag.api.username=admin                      ; REMOVE because shiro authentication will replace it
mtwilson.atag.api.password=password                 ; REMOVE because shiro authentication will replace it
mtwilson.atag.html5.dir=file:///root/AT-demo/html5/           ; REMOVE because mtwilson.portal.html5.dir will replace it
mtwilson.atag.certificate.import.auto=true   ; RENAME to tag.provision.autoimport
mtwilson.atag.mtwilson.baseurl=https://10.1.71.234:8181/mtwilson/v1   ;  REMOVE not needed because there is already mtwilson.api.baseurl
 * 
 * @author jbuhacoff
 */
public class TagConfiguration extends AbstractConfiguration {
//    public static final String TAG_KEYSTORE_FILE = "tag.keystore.file";
//    public static final String TAG_KEYSTORE_PASSWORD = "tag.keystore.password";
//    public static final String TAG_KEY_ALIAS = "tag.key.alias";
//    public static final String TAG_KEY_PASSWORD = "tag.key.password";
    public static final String TAG_ISSUER = "tag.issuer.dn"; // tag.certificate.dn 
    public static final String TAG_VALIDITY_SECONDS = "tag.validity.seconds";
    public static final String TAG_AUTO_IMPORT_TO_MTWILSON = "tag.provision.autoimport";
    public static final String TAG_PROVISION_EXTERNAL_CA = "tag.provision.external";
    public static final String TAG_PROVISION_SELECTION_DEFAULT = "tag.provision.selection.default";
    public static final String TAG_PROVISION_XML_ENCRYPTION_PASSWORD = "tag.provision.xml.encryption.password"; 
    public static final String TAG_PROVISION_XML_ENCRYPTION_REQUIRED = "tag.provision.xml.encryption.required";
    
    public TagConfiguration(Configuration configuration) {
        super();
        configure(configuration);
    }
    public TagConfiguration(org.apache.commons.configuration.Configuration configuration) {
        super();
        setConfiguration(configuration);
    }
    
    public boolean isTagProvisionAutoImport() {
        return getConfiguration().getBoolean(TAG_AUTO_IMPORT_TO_MTWILSON, true);
    }
    
    public boolean isTagProvisionExternal() {
        return getConfiguration().getBoolean(TAG_PROVISION_EXTERNAL_CA, false);
    }
    
    public int getTagValiditySeconds() {
        return getConfiguration().getInteger(TAG_VALIDITY_SECONDS, 60 * 60 * 24 * 365); // default one year
    }

    public String getTagIssuer() {
        return getConfiguration().getString(TAG_ISSUER, "CN=mtwilson-tag-ca");
    }

    public String getTagProvisionSelectionDefault() {
        return getConfiguration().getString(TAG_PROVISION_SELECTION_DEFAULT);  // intentionally not setting a default value; if there is no value set then a default selection will not be used.
    }
    
    public String getTagProvisionXmlEncryptionPassword() {
        return getConfiguration().getString(TAG_PROVISION_XML_ENCRYPTION_PASSWORD);  // intentionally not setting a default value; setup must generate a random password
    }

    public boolean isTagProvisionXmlEncryptionRequired() {
        return getConfiguration().getBoolean(TAG_PROVISION_XML_ENCRYPTION_REQUIRED, false);
    }
    
}
