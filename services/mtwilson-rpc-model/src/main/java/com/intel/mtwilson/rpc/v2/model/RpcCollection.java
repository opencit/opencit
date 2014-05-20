/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.model;

import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Not clear yet if we'll offer a search capability that would possibly
 * return multiple RPC requests, but implementing for now to stay consistent
 * with the other resource APIs.
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="rpc_collection")
public class RpcCollection extends DocumentCollection<Rpc> {
    private final ArrayList<Rpc> rpcs = new ArrayList<Rpc>();
    
//    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="rpcs")
    @JacksonXmlProperty(localName="rpc")    
    public List<Rpc> getRpcs() { return rpcs; }
    
    @Override
    public List<Rpc> getDocuments() {
        return getRpcs();
    }
    
    
}
