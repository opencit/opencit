/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import javax.ws.rs.QueryParam;

/**
 *
 * @author jbuhacoff
 */
public class RoleSearchCriteria {
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    @QueryParam("nameContains")
    public String nameContains;
}
