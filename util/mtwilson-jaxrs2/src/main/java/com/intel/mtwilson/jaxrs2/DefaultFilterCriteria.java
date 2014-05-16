/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.jaxrs2;

import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class DefaultFilterCriteria {
    @QueryParam("filter") 
    public boolean filter = true; 
    @QueryParam("limit") 
    public Integer limit = null; 
    @QueryParam("page") 
    public Integer page = null; 
}
