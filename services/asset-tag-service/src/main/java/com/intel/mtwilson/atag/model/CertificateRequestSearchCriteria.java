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
    public String subjectContains; // only makes sense if someone is using something other than UUIDs as the subject... 
    public String selectionEqualTo;
    public String selectionContains; // only makes sense if selections are being referenced by name nstead of uuid... uuid seems better.
    public String statusEqualTo;
}
