/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.mediatype;

/**
 *
 * @author jbuhacoff
 */
public class DataMediaType {
    // patch formats
    public static final String APPLICATION_JSON_PATCH = "application/json-patch+json"; // RFC 6902
    // yaml formats
    public static final String APPLICATION_YAML = "application/yaml";
    public static final String APPLICATION_RELATIONAL_PATCH_JSON = "application/relational-patch+json";
    public static final String TEXT_YAML = "text/yaml";
    // jsonapi format, see http://jsonapi.org
    public static final String APPLICATION_VND_API_JSON = "application/vnd.api+json";
    
}
