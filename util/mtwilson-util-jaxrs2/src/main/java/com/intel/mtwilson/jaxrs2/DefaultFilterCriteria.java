/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class DefaultFilterCriteria {
    @QueryParam("filter")
    @DefaultValue("true") // default for use by the jaxrs framework
    public boolean filter = true; // default for use when creating a filter criteria instance from application code
    @DefaultValue("10") // default for use by the jaxrs framework
    @QueryParam("limit") 
    public Integer limit = 10; 
    @QueryParam("page") 
    public Integer page; 
}
