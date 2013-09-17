/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util;

import com.intel.mtwilson.i18n.ErrorMessage;
import com.intel.mtwilson.datatypes.AuthResponse;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
@Provider
//@Component
public class LocalizedExceptionMapper implements ExceptionMapper<MWException> {
    private static Logger log = LoggerFactory.getLogger(LocalizedExceptionMapper.class);
    
    @Context private HttpServletRequest request;
    
    @Override
    public Response toResponse(MWException exception) {
        ArrayList<Locale> list = new ArrayList<Locale>();
        Locale locale = null;
        Enumeration<Locale> locales = request.getLocales(); // in priority order based on the accept language header in the request; if request doesn't specify then it contains the server's default locale (java-provided, not mtwilson-configured)
        while( locales.hasMoreElements() ) {
            list.add(locales.nextElement());
        }
        if( list.isEmpty() ) {
            list.add(Locale.getDefault()); // should never happen since the enumeration includes the platform default at the end
        }
        if( list.size() == 1 ) {
            // simple case: either user specified just one language or no language at all
            locale = list.get(0);
        }
        if( list.size() > 1 ) {
            // check each locale to see if we have a bundle for it
            int i;
            int max = list.size() - 1; // no need to evaluate the last locale, it will be our default
            for(i=0; i<max && locale==null; i++) {
                locale = list.get(i);
                ResourceBundle bundle = ResourceBundle.getBundle("mtwilson-strings", locale);
                if( bundle.getLocale().equals(Locale.getDefault())) {
                    locale = null; // reset for next iteration
                }
            }
            if( locale == null ) {
                locale = list.get(i); // last element
            }
        }
        // localize the error message using the selected locale
        String output = localize(exception, locale);
        // XXX if we want to set a different http status code for various errors, we could have different exception classes like NotFound extends MWException and sets a status code of 404
        Response response = Response.status(400).entity(new AuthResponse(exception.getErrorCode(), output)).type(MediaType.APPLICATION_JSON_TYPE).build();
        return response;
    }
    
    private String localize(MWException exception, Locale locale) {
        ErrorMessage message = new ErrorMessage(exception.getErrorCode(), exception.getParameters());
        return message.toString(locale);
    }
    
}
