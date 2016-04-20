/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util;

import com.intel.mtwilson.My;
import com.intel.mtwilson.jaxrs2.server.Util;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.commons.beanutils.PropertyUtils;
import org.stringtemplate.v4.*;

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
    protected HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        log.debug("ThrowableMapper toResponse", exception);
        Locale locale = Util.getAcceptableLocale(headers.getAcceptableLanguages(), My.configuration().getAvailableLocales());

        String localizedMessage;
        if( exception instanceof MWException ) {
            MWException mwe = (MWException)exception;
            mwe.setLocale(locale); // localizes output of getErrorMessage() below
            localizedMessage = mwe.getErrorMessage(); 
        }        
        else {
            localizedMessage = getLocalizedErrorMessage(exception, locale);
        }
        
        int status = 400; // assume bad request unless we find out otherwise
        if( exception instanceof WebApplicationException ) {
            status = ((WebApplicationException)exception).getResponse().getStatus();
        }
        
//        ResponseBuilder responseBuilder = Response.status(status).header("Error", localizedMessage);
        
         // setting empty entity to prevent web container from providing its own html error page wrapping our status message.
        // for example, if our localized message is "Bad argument" with status 400, Tomcat would wrap it with this html message:
        // <html><head><title>Apache Tomcat/7.0.34 - Error report</title><style><!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> </head><body><h1>HTTP Status 400 - Bad argument</h1><HR size="1" noshade="noshade"><p><b>type</b> Status report</p><p><b>message</b> <u>Bad argument</u></p><p><b>description</b> <u>The request sent by the client was syntactically incorrect.</u></p><HR size="1" noshade="noshade"><h3>Apache Tomcat/7.0.34</h3></body></html>
//        ResponseBuilder responseBuilder = Response.status(new CustomStatus(status, localizedMessage)).type("text/plain").entity(""); //.header("Error", localizedMessage).entity(Entity.text("").); // entity(Entity.text("")
        ResponseBuilder responseBuilder = Response.status(status).type("text/plain").entity(localizedMessage); //.header("Error", localizedMessage).entity(Entity.text("").); // entity(Entity.text("")
        /*
        if( exception instanceof MWException ) {
            ErrorCode code = ((MWException)exception).getErrorCode();
            responseBuilder.header("Error-Code", code.getErrorCode());
            responseBuilder.header("Error-Name", code.name());
        }
        else {
            responseBuilder.header("Error-Code", ErrorCode.SYSTEM_ERROR.getErrorCode());
            responseBuilder.header("Error-Name", ErrorCode.SYSTEM_ERROR.name());
        }
        */
        Response response = responseBuilder.build();
        return response;

    }
    
    public static class CustomStatus implements Response.StatusType {
        private int statusCode;
        private Response.Status.Family family;
        private String reasonPhrase;

        public CustomStatus(int statusCode, String reasonPhrase) {
            this.statusCode = statusCode;
            this.family = Response.Status.Family.familyOf(statusCode);
            this.reasonPhrase = reasonPhrase;
        }
        public CustomStatus(int statusCode, Response.Status.Family family, String reasonPhrase) {
            this.statusCode = statusCode;
            this.family = family;
            this.reasonPhrase = reasonPhrase;
        }
        
        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public Response.Status.Family getFamily() {
            return family;
        }

        @Override
        public String getReasonPhrase() {
            return reasonPhrase;
        }
        
    }
    
    protected String getLocalizedErrorMessage(Throwable exception, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("MtWilsonErrors", locale);
        log.debug("Message toString with locale: {}", locale.toString());
        log.debug("Message toString loaded resource bundle: {}", bundle.getLocale().toString());
        String key = exception.getClass().getName(); // for example "java.lang.IllegalArgumentException"
        try {
            String pattern = bundle.getString(key); // for example "Illegal argument: <message>" ; throws MissingResourceException
            ST template = new ST(pattern);
            Map<String, Object> sourceAttrs = PropertyUtils.describe(exception);// throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
            for (Map.Entry<String, Object> attr : sourceAttrs.entrySet()) {
                // there are attributes we skip, like "class" from getClass() 
//                    if( attr.getKey().equals("class") ) { continue; }  // let the template see the exception class so an author could write <class.name> to get the exception class name into the message
                Object value = PropertyUtils.getSimpleProperty(exception, attr.getKey());
                template.add(attr.getKey(), value);
            }
            String result = template.render();
            log.debug("Rendered template: {}", result);
            return result;
        } catch (MissingResourceException e) {
            log.error("No translation for key {} in bundle {}: {}", e.getKey(), e.getClassName(), e.getLocalizedMessage());
            return key;  // return just the message name with no parameters since we weren't able to find a localized translation, at least this will allow the recipient to maintain a local translation table for such untranslated constants
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Cannot describe exception object", e);
            return key;
        }
    }
}
