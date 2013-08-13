/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.intel.dcsg.cpg.io.UUID;

/**
 *
 * @author jbuhacoff
 */
public class CertificateRequestSearchCriteria {
    public UUID id; // setting this implies equalTo 
    public String subjectEqualTo;
    public String subjectContains;
    public String tagNameEqualTo;
    public String tagNameContains;
    public String tagOidEqualTo;
    public String tagOidContains;
    public String tagValueEqualTo;
    public String tagValueContains;
    public String statusEqualTo;
}
