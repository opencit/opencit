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
public class AuditLogSearchCriteria {
    public Integer recordIdEqualTo;
    public String transactionIdEqualTo;
    public Integer entityIdEqualTo;
    public String entityTypeEqualTo;
    //public String[] entityTypeEqualToAny;
    public String fingerprintEqualTo;
    public Date createdNotAfter;
    public Date createdNotBefore;
    public String actionEqualTo;
    //public String[] actionEqualToAny;
    public String dataContains;
}
