/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import java.util.Date;

/**
 *
 * @author jbuhacoff
 */
public class ApiClientSearchCriteria {
    public String nameEqualTo;
    public String nameContains;
    public byte[] fingerprintEqualTo;
    public Date expiresAfter;
    public Date expiresBefore;
    public String issuerEqualTo;
    public Integer serialNumberEqualTo;
    public String statusEqualTo;
    public Boolean enabledEqualTo;
    public String commentContains;
}
