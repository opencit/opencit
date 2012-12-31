/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.model;

import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.setup.Timeout;

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
    
}
