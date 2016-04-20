/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util;

import com.intel.mtwilson.My;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.jaxrs2.server.Util;
import java.util.Locale;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author jbuhacoff
 */
public class ThrowableMapperV1 extends ThrowableMapper {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThrowableMapperV1.class);
    
    @Override
    public Response toResponse(Throwable exception) {
        log.debug("ThrowableMapperV1 toResponse", exception);
        Locale locale = Util.getAcceptableLocale(headers.getAcceptableLanguages(), My.configuration().getAvailableLocales());
        
        /*
        MediaType mediaType = Util.getAcceptableMediaType(headers.getAcceptableMediaTypes()); 
        
        // we respect client's media type selection only if it's json or xml,  if it's anything else we return the error in json
        if( !mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE) && !mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE) ) {
            mediaType = MediaType.APPLICATION_JSON_TYPE;
        }
        */
        MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

        ErrorResponse errorResponse;
        if( exception instanceof MWException ) {
            MWException mwe = (MWException)exception;
            mwe.setLocale(locale); // localizes error message obtained below with getErrorMessage()
            ErrorCode code = mwe.getErrorCode();
            String localizedErrorMessage = mwe.getErrorMessage();
            errorResponse = new ErrorResponse(code, localizedErrorMessage);
        }
        else {
            String localizedMessage = getLocalizedErrorMessage(exception, locale);
            errorResponse = new ErrorResponse(ErrorCode.SYSTEM_ERROR, localizedMessage);
        }
        
        Response response = Response.status(400).entity(errorResponse).type(mediaType).build();
        return response;
        
    }
}
