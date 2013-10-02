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
public class SelectionSearchCriteria {
    public UUID id; // setting this implies equalTo 
    public String nameEqualTo; // the name of the selection
    public String nameContains; // the name of the selection
    public String subjectEqualTo; // one or more subjects (hosts) included in the selection
    public String subjectContains; // one or more subjects (hosts) included in the selection
    public String tagNameEqualTo; // one or more tags (attribute name and value pairs) included in the selection
    public String tagNameContains; // one or more tags (attribute name and value pairs) included in the selection
    public String tagOidEqualTo;
    public String tagOidContains;
    public String tagValueEqualTo; // one or more tags (attribute name and value pairs) included in the selection
    public String tagValueContains; // one or more tags (attribute name and value pairs) included in the selection
}
