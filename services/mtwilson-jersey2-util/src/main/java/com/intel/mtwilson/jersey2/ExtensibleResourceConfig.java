/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey2;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author jbuhacoff
 */
public class ExtensibleResourceConfig extends ResourceConfig {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtensibleResourceConfig.class);
    
    public ExtensibleResourceConfig() {
        log.debug("ExtensibleResourceConfig constructor");
//        register(JacksonFeature.class); // implements Feature
//        register(com.intel.mtwilson.shiro.AuthorizationExceptionMapper.class); // implements ExceptionMapper @Provider // catches shiro exceptions and converts them to http unauthorized responses
//        register(com.intel.mtwilson.util.ThrowableMapper.class); // implements ExceptionMapper @Provider
//        register(com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider.class); // implements ContextResolver @Provider
//        register(com.intel.mtwilson.jaxrs2.provider.JacksonXmlMapperProvider.class);  // implements ContextResolver @Provider
//        register(com.intel.mtwilson.jaxrs2.provider.JacksonYamlObjectMapperProvider.class); // implements ContextResolver @Provider
//        register(com.intel.mtwilson.jaxrs2.provider.X509CertificatePemProvider.class);// implements MessageBodyWriter/MessageBodyReader @Provider
//        register(com.intel.mtwilson.jaxrs2.provider.X509CertificateDerProvider.class);// implements MessageBodyWriter/MessageBodyReader @Provider
//        register(com.intel.mtwilson.jaxrs2.provider.X509CertificateArrayPemProvider.class); // implements MessageBodyWriter/MessageBodyReader @Provider
//        register(com.intel.mtwilson.jaxrs2.provider.ApplicationYamlProvider.class); // implements MessageBodyWriter/MessageBodyReader @Provider
//        register(com.intel.mtwilson.jaxrs2.server.filter.ErrorLogFilter.class); // implements ContainerResponseFilter
//        register(com.intel.mtwilson.jaxrs2.provider.DateParamConverterProvider.class); // implements ParamConverterProvider @Provider
        register(org.glassfish.jersey.server.filter.HttpMethodOverrideFilter.class); // jersey2 equivalent of com.sun.jersey.api.container.filter.PostReplaceFilter
        register(org.glassfish.jersey.server.wadl.internal.WadlResource.class);
        
        // register javax.ws.rs.core.Feature implementations
        List<Feature> features = Extensions.findAll(Feature.class);
        for(Feature feature : features) {
            log.debug("Registering javax.ws.rs.core.Feature: {}", feature.getClass().getName());
            register(feature.getClass());
        }
        
        // register javax.ws.rs.container.ContainerResponseFilter implementations
        List<ContainerResponseFilter> containerResponseFilters = Extensions.findAll(ContainerResponseFilter.class);
        for(ContainerResponseFilter containerResponseFilter : containerResponseFilters) {
            log.debug("Registering javax.ws.rs.container.ContainerResponseFilter: {}", containerResponseFilter.getClass().getName());
            register(containerResponseFilter.getClass());
        }

        // registers custom implementations of MessageBodyReader, MessageBodyWriter, ContextResolver, ExceptionMapper
        List<Object> providers = Extensions.findAllAnnotated(Provider.class);
        for(Object provider : providers) {
            log.debug("Registering javax.ws.rs.ext.Provider: {}", provider.getClass().getName());
            register(provider.getClass());
        }
        
        List<Object> resources = Extensions.findAllAnnotated(V2.class);
        for(Object resource : resources) {
            log.debug("Looking at resource class {}", resource.getClass().getName());
            if( resource.getClass().isAnnotationPresent(Path.class) ) {
                // for example, all the resource & rpc APIs have @V2 @Path("/...")  annotations
                String resourcePath = resource.getClass().getAnnotation(Path.class).value();
                log.debug("Found v2 class {} with @Path {}", resource.getClass().getName(), resourcePath);
                register(resource.getClass());
            }
        }
        
    }

}
