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
public class TagSearchCriteria {
    public UUID id; // setting this implies equalTo 
    public String nameEqualTo;
    public String nameContains;
    public String oidEqualTo;
    public String oidStartsWith;
    public String valueEqualTo;
    public String valueContains;
}
