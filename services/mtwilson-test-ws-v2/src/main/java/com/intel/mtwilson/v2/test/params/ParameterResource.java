/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.test.params;

import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.util.Date;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Example access:  GET /test/params?date=    would result in an exception
 * because the empty date string cannot be parsed into Date;  so a custom
 * parameter provider or a customer parameter class is needed which will 
 * handle empty string by returning a null value
 * 
 * In contrast, GET /test/params?isodate=  would result in a null value for
 * the isodate field because the Iso8601Date class has a valueOf static method
 * that handles null and empty string by returning null instead of throwing
 * a parse exception.
 * 
 * Example: 
 * 
 * http://localhost:8080/v2/test/params?isodate=
<Parameters>
<string/>
<date/>
<isodate/>
<integer/>
</Parameters>
 * 
 * 
 * http://localhost:8080/v2/test/params?isodate=2014-01-01T09:00
<Parameters>
<string/>
<date/>
<isodate>2014-01-01T09:00:00-0800</isodate>
<integer/>
</Parameters>
 *
 * 
 * 
 * @author jbuhacoff
 */
@V2
@Path("/test/params")
public class ParameterResource {
    
    public static class Parameters {
        @QueryParam("string")
        public String string;
        
        @QueryParam("date")
        public Date date;

        @QueryParam("isodate")
        public Iso8601Date isodate;
        
        
        @QueryParam("integer")
        public Integer integer;
        
        @QueryParam("boolean")
        public Boolean bool;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Parameters getParameterConversions(@BeanParam Parameters params) {
        return params;
    }
}
