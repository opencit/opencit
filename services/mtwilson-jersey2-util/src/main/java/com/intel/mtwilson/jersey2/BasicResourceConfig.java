/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey2;

import com.intel.mtwilson.jaxrs2.feature.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author jbuhacoff
 */
public class BasicResourceConfig extends ResourceConfig {

    public BasicResourceConfig() {
        register(JacksonFeature.class);
        register(org.glassfish.jersey.server.filter.HttpMethodOverrideFilter.class); // jersey2 equivalent of com.sun.jersey.api.container.filter.PostReplaceFilter
        register(com.intel.mtwilson.jaxrs2.provider.X509CertificatePemProvider.class);
        register(com.intel.mtwilson.jaxrs2.provider.X509CertificateDerProvider.class);
        register(com.intel.mtwilson.jaxrs2.provider.X509CertificateArrayPemProvider.class);
        register(com.intel.mtwilson.jaxrs2.provider.DateParamConverterProvider.class);
        register(com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider.class);
        //register(com.intel.mtwilson.jaxrs2.server.filter.ErrorLogFilter.class);
        //register(org.glassfish.jersey.server.wadl.internal.WadlResource.class);
        //register(com.intel.mtwilson.shiro.AuthorizationExceptionMapper.class); // catches shiro exceptions and converts them to http unauthorized responses
        //register(com.intel.mtwilson.util.ThrowableMapper.class);
    }

    // workaround for netbeans 7.3.1 bug (fixed in netbeans 7.4) https://netbeans.org/bugzilla/show_bug.cgi?id=234581
    // with this workaround netbeans will continue to auto-generate addRestResourceClasses but it's harmless
    // because it will never be called
    public void getClasses(int unused) {
    }
}
