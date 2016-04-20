/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.model;

import com.intel.mtwilson.datatypes.IPAddress;
import java.io.File;
import java.net.URL;

/**
 *
 * @author jbuhacof
 */
public class AttestationService {
    private IPAddress hostAddress;
    private URL serviceUrl;
    private File dataFolder; // com.intel.mountwilson.as.home=/var/opt/intel/aikverifyhome
    // private String com.intel.mountwilson.as.aikqverify.cmd=aikqverify
    // private String com.intel.mountwilson.as.openssl.cmd=openssl.sh
    private String samlIssuer; // default to serviceUrl.toExternalString()
    private File samlKeystoreFile; // saml.keystore.file=SAML.jks
    private String samlKeystorePassword; // saml.keystore.password=changeit
    private String samlKeyAlias; // saml.key.alias=samlkey1
    private String samlKeyPassword; // saml.key.password=changeit;
    private Integer samlValiditySeconds; // saml.validity.seconds=3600
    private String databaseHost; // mountwilson.as.db.host=10.1.71.103
    private Integer databasePort; // mountwilson.as.db.port=3306
    private String databaseUsername; // mountwilson.as.db.user=root
    private String databasePassword; // mountwilson.as.db.password=password
    private IPAddress privacycaHost; // privacyca.server=10.1.71.103
}
