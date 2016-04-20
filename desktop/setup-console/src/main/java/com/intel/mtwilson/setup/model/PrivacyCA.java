/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.model;

import com.intel.dcsg.cpg.crypto.Pkcs12;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.setup.Timeout;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 *
 * @author jbuhacoff
 */
public class PrivacyCA  {
    public InternetAddress hostname;
    public String ekSigningKeyFilename;
    public String ekSigningKeyPassword;
    public String ekSigningKeyDownloadUsername;
    public String ekSigningKeyDownloadPassword;
    public Timeout pcaCertificateValidity; // usually configured in number of days
    public Pkcs12 keystore;
    public KeyPair ekSigningKeyPair;
    public X509Certificate ekSigningKeyCertificate;
}
