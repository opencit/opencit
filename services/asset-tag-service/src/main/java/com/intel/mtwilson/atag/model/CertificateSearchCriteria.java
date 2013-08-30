/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import java.util.Date;

/**
 *
 * @author jbuhacoff
 */
public class CertificateSearchCriteria {
    public UUID id; // setting this implies equalTo 
    public String subjectEqualTo;
    public String subjectContains;
    public String issuerEqualTo;
    public String issuerContains;
    public String statusEqualTo; // XXX TODO need to add a status field to the Certificate table and object, maybe with 'active' or 'revoked'
    public Date validOn; // will select certificates where notBefore <= validOn <= notAfter
    public Date validBefore; // will select certificates where validBefore <= certifcate.notAfter
    public Date validAfter;  // will select certificates where validAfter >= certificate.notBefore
    public Sha256Digest sha256; // implies equalTo
    public Sha1Digest pcrEvent; // implies equalTo
    public Boolean revoked;
}
