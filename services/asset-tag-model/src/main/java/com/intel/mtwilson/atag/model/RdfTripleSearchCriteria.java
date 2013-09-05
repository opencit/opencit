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
public class RdfTripleSearchCriteria {
    public UUID id; // setting this implies equalTo 
    public String subjectEqualTo;
    public String predicateEqualTo;
    public String objectEqualTo;
    public String subjectContains;
    public String predicateContains;
    public String objectContains;
}
