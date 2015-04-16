/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.extensions.jaxrs;

//import com.intel.mtwilson.configuration.v2.model.Configuration;
//import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
//import com.intel.mtwilson.repository.FilterCriteria;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

/**
 *
 * @author jbuhacoff
 */
public class ExtensionFilterCriteria /*extends DefaultFilterCriteria*/ /*implements FilterCriteria<Configuration>*/ {
    @QueryParam("filter")
    @DefaultValue("true")
    public Boolean filter = false;
    
    /**
     * The extension fully-qualified class name 
     */
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    /**
     * The extension fully-qualified class name 
     */
    @QueryParam("nameContains")
    public String nameContains;
    /**
     * The fully-qualified class or interface name the extension implements
     */
    @QueryParam("interfaceEqualTo")
    public String interfaceEqualTo;
    /**
     * True if the result should only include names of extension points
     */
    @QueryParam("isInterface")
    public Boolean isInterface;
}
