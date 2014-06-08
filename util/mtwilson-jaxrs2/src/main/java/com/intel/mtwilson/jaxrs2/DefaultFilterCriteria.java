/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    @DefaultValue("true")
    public boolean filter; 
    @QueryParam("limit") 
    public Integer limit; 
    @QueryParam("page") 
    public Integer page; 
}
