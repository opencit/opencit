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
public class FileSearchCriteria {
    public UUID id; // setting this implies equalTo 
    public String nameEqualTo;
    public String nameContains;
    public String contentTypeEqualTo;
    public String contentTypeStartsWith;
}
