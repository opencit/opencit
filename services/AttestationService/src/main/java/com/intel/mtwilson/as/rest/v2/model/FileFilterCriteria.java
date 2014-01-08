/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import javax.ws.rs.QueryParam;

/**
 *
 * @author jbuhacoff
 */
public class FileFilterCriteria {
    
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    @QueryParam("nameContains")
    public String nameContains;
    @QueryParam("contentTypeEquals")
    public String contentTypeEquals;
}
