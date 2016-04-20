/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.selection.json;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The commented out block applies only to xml serialization; currently
 * only json serialization is used.
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class AttributeTypeMixIn {
    /*
    // this section wouldn't be necessary if the JaxbAnnotationIntrospector worked properly and used the XmlAttribute annotations...
    protected String oid;
    */
}
