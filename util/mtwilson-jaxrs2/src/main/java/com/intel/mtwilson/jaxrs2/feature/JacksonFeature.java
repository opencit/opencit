/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.feature;

import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 *
 * @author jbuhacoff
 */
public class JacksonFeature implements Feature {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonFeature.class);
    
    @Override
    public boolean configure( final FeatureContext context ) {
        log.debug("JacksonFeature configure");
//            String postfix = '.' + context.getConfiguration().getRuntimeType().name().toLowerCase();  // this just evaluates to ".server"  ... we don't

            // using the literal configuration property name instead of the constant in jersey's CommonProperties to avoid having jersey2 as a dependency of this project
            context.property( "jersey.config.disableMoxyJson", true ); // CommonProperties.MOXY_JSON_FEATURE_DISABLE
//            context.property( "jersey.config.disableMoxyJson.server", true ); // CommonProperties.MOXY_JSON_FEATURE_DISABLE

            context.register( JsonParseExceptionMapper.class );
            context.register( JsonMappingExceptionMapper.class );
            context.register( JacksonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class );

            // should this go in a separate jacksonxmlfeature?
            context.register(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class);
            context.register(com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider.class); 
            context.register(com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider.class); 

            return true;
        }
}
