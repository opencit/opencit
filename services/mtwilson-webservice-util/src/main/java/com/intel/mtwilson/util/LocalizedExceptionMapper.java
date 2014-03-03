/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util;

import com.intel.mtwilson.i18n.ErrorMessage;
//import com.intel.mtwilson.datatypes.ErrorResponse;
import java.util.Locale;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.mtwilson.jersey.http.Util;

/**
 *
 * @author jbuhacoff
 */
@Provider
//@Component
public class LocalizedExceptionMapper implements ExceptionMapper<MWException> {
    private static Logger log = LoggerFactory.getLogger(LocalizedExceptionMapper.class);
    
//    @Context private HttpServletRequest request;
//    @Context private HttpRequestContext request;
    @Context private HttpHeaders headers;
    
    @Override
    public Response toResponse(MWException exception) {
        log.debug("intercepted MWException; localizing response");
        Locale locale = Util.getAcceptableLocale(headers.getAcceptableLanguages());
        // localize the error message using the selected locale
        log.debug("localizing MWException to {}", locale.getDisplayName());
        ErrorMessage message = new ErrorMessage(exception.getErrorCode(), (Object)exception.getParameters());
//        return message.toString(locale);
//        String output = localize(exception, locale);
        ErrorResponse errorResponse = new ErrorResponse(message); // error_code and error_message  , can be serialized to json, xml, yaml...
        // XXX if we want to set a different http status code for various errors, we could have different exception classes like NotFound extends MWException and sets a status code of 404
//        Response response = Response.status(400).entity(new AuthResponse(exception.getErrorCode(), output)).type(MediaType.APPLICATION_JSON_TYPE).build();
//        Response response = Response.status(400).entity(errorResponse).type(Util.getAcceptableMediaType(headers.getAcceptableMediaTypes())).build();
        Response response = Response.status(400).entity(errorResponse).type(MediaType.APPLICATION_JSON).build(); // XXX when we use requestor's mediatype we get this error: java.lang.ClassCastException: com.intel.mtwilson.datatypes.ErrorResponse cannot be cast to java.util.Collection
        return response;
    }
    
    
    
}
