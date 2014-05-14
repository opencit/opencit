/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.test.errors;

import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.util.MWException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/test/error")
public class ExceptionThrowingResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExceptionThrowingResource.class);
    
    @GET
    @Path("/runtime")
    public String throwRuntimeException() {
        log.debug("throwRuntimeException");
        throw new RuntimeException("deliberate non-internationalized exception");
    }

    @GET
    @Path("/runtime-localizable")
    public String throwLocalizableRuntimeException() {
        log.debug("throwLocalizableRuntimeException");
        throw new MWException(ErrorCode.UNKNOWN_ERROR, "deliberate localizable exception");
    }

    @GET
    @Path("/error")
    public String throwError() {
        log.debug("throwError");
        throw new Error("deliberate non-internationalized error");
    }
    
}
