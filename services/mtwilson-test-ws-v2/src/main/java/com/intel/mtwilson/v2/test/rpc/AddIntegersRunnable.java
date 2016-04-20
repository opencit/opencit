/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.test.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.launcher.ws.ext.RPC;

/**
 * The simplest form of RPC is a POJO that implements Runnable. 
 * The framework will detect inputs as setters, outputs as getters. 
 * Exceptions thrown from run() will be caught and reported as
 * errors. 
 * The name passed to @RPC annotation is what will be used to access
 * the API, like /rpc/add_integers
 * 
 * Sample JSON input:
 * {x:1,y:1}
 * Sample XML input:
 * <add_integers><x>1</x><y>1</y></add_integers>
 * Sample YAML input:
 * ---
 * x: 1
 * y: 1
 * 
 * 
 * Sample JSON output:
 * {x:1,y:1,result:2}
 * Sample XML output:
 * <add_integers><x>1</x><y>1</y><result>2</result></add_integers>
 * Sample YAML output:
 * ---
 * x: 1
 * y: 1
 * result: 2
 * 
 * @author jbuhacoff
 */
@RPC("add_integers")
@JacksonXmlRootElement(localName="add_integers")
public class AddIntegersRunnable implements Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddIntegersRunnable.class);

    private Integer x;
    private Integer y;
    private Integer result;

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public void setResult(Integer result) {
        this.result = result;
    }
    

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getResult() {
        return result;
    }
    
    /**
     * Notice that we don't check for nulls here; if we throw a NullPointerException
     * the framework will catch it and report the error. 
     * RPCs can implement additional interfaces in order to report errors without
     * throwing exceptions.
     */
    @Override
    public void run() {
        log.debug("Adding {} + {}", x, y);
        result = x+y;
    }
    
}
