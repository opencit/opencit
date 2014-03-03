/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util;

import com.intel.dcsg.cpg.i18n.Localizable;
import com.intel.mtwilson.datatypes.ErrorCode;
//import com.intel.mtwilson.datatypes.ErrorResponse;
import com.intel.mtwilson.i18n.ErrorMessage;
import com.intel.mtwilson.jersey.http.Util;
import java.util.Locale;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * If the throwable is localizable, it sets the locale and uses the localized
 * message directly. Otherwise, it attempts to use the throwable's class name as
 * a localization key for a localized message with no parameters. If that
 * doesn't work either, a localized "internal server error" message is returned.
 *
 * @author jbuhacoff
 */
@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThrowableMapper.class);
    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        log.debug("ThrowableMapper toResponse {}", exception.getClass().getName());
        Locale locale = Util.getAcceptableLocale(headers.getAcceptableLanguages());
        MediaType mediaType = Util.getAcceptableMediaType(headers.getAcceptableMediaTypes());

        if (exception instanceof Localizable) {
            log.debug("intercepted Localizable Throwable; localizing response to {}", locale.getDisplayName());
            ((Localizable) exception).setLocale(locale);
        }
        
        // TODO:  we probably don't need separate MWException and Throwable mappers; 
        // combine them both here by checking if( exception instanceof MWException ) and
        // then get the error code and message that way, otherwise continue with
        // the generic processing

        // TODO:   need another facility besides ErrorMessage to look up the
        // throwable's class name as a key ... because ErrorMessage is tied to
        // our ErrorCode enum and that's a constant thing... need something that
        // can be configured via properties files... so probably use the Java
        // Bundle API here directly...

        // If a better message was not found, use the generic system error
        ErrorMessage message = new ErrorMessage(ErrorCode.SYSTEM_ERROR, exception.getLocalizedMessage());
        ErrorResponse errorResponse = new ErrorResponse(message); // error_code and error_message  , can be serialized to json, xml, yaml...
        // XXX if we want to set a different http status code for various errors, we could have different exception classes like NotFound extends MWException and sets a status code of 404
//        Response response = Response.status(400).entity(new AuthResponse(exception.getErrorCode(), output)).type(MediaType.APPLICATION_JSON_TYPE).build();
//        Response response = Response.status(400).entity(errorResponse).type(mediaType).build();
        Response response = Response.status(400).entity(errorResponse).type(MediaType.APPLICATION_JSON).build(); // XXX when we use requestor's mediatype we get this error: java.lang.ClassCastException: com.intel.mtwilson.datatypes.ErrorResponse cannot be cast to java.util.Collection
        return response;

    }
}
