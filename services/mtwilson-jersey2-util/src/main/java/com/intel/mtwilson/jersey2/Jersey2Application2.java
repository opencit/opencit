/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey2;

import com.intel.dcsg.cpg.extensions.AnnotationRegistrar;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.util.List;
import javax.ws.rs.Path;
import com.intel.mtwilson.launcher.ws.ext.RPC;

/**
 * Configures Mt Wilson 2.x APIs
 * 
 * See also:
 * https://jersey.java.net/apidocs/2.5/jersey/org/glassfish/jersey/server/ResourceConfig.html (especially about what can be configured from our constructor here like providers and setting the class loader)
 * 
 * https://jersey.java.net/apidocs/latest/jersey/
 * 
 * https://jersey.java.net/documentation/latest/index.html
 * https://jersey.java.net/documentation/latest/modules-and-dependencies.html
 * https://jersey.java.net/documentation/latest/deployment.html
 * https://jersey.java.net/documentation/latest/message-body-workers.html
 * https://jersey.java.net/documentation/latest/resource-builder.html
 * https://jersey.java.net/documentation/latest/uris-and-links.html
 * https://jersey.java.net/documentation/latest/ioc.html
 * https://jersey.java.net/documentation/latest/appendix-properties.html#appendix-properties-common
 * https://jersey.java.net/documentation/latest/appendix-properties.html#appendix-properties-server
 * 
 * 
 * https://jersey.java.net/apidocs/1.8/jersey/com/sun/jersey/spi/service/ServiceFinder.html (similar to Java's SPI and our Extensions...  cpg-extensions separates classpath scanning from the class registry itself which is nice )
 * https://jersey.java.net/apidocs/1.11/jersey/com/sun/jersey/core/spi/scanning/PackageNamesScanner.html
 * 
 * http://grepcode.com/file/repo1.maven.org/maven2/org.glassfish.jersey.containers/jersey-container-servlet-core/2.1/org/glassfish/jersey/servlet/WebAppResourcesScanner.java?av=f
 * 
 * http://grepcode.com/file/repo1.maven.org/maven2/org.glassfish.jersey.core/jersey-server/2.1/org/glassfish/jersey/server/internal/scanning/JarFileScanner.java#JarFileScanner
 * 
 * 
 * @author jbuhacoff
 */
//@ApplicationPath("/v2")
public class Jersey2Application2 extends AbstractJerseyPluginApplication {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Jersey2Application2.class);
    
    public Jersey2Application2() {
        super();
register(com.intel.mtwilson.util.ThrowableMapper.class); // returns localized error messages in an "Error" header;  api's that have detailed error reporting would explicitly set a status code and output format which the client would expect, whereas the Error header is for unexpected errors (like client expecting x509 certificate but getting error instead)
        
register(com.intel.mtwilson.jaxrs2.provider.JacksonXmlMapperProvider.class); 
register(com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider.class);

register(com.intel.mtwilson.jaxrs2.provider.JacksonYamlObjectMapperProvider.class);
register(com.intel.mtwilson.jaxrs2.provider.ApplicationYamlProvider.class);
register(com.intel.mtwilson.jaxrs2.provider.X509CertificatePemProvider.class);
register(com.intel.mtwilson.jaxrs2.provider.X509CertificateDerProvider.class);
register(com.intel.mtwilson.jaxrs2.provider.X509CertificateArrayPemProvider.class);
register(com.intel.mtwilson.jaxrs2.provider.DateParamConverterProvider.class);
        
        // now get the list of classes that implement @V2 and @Path
        List<Object> resources = Extensions.findAllAnnotated(V2.class); // we could search for @Path but then we'd find v1 and v2 classes as well as utility classes for both such as the application.wadl generator
        for(Object resource : resources) {
            log.debug("Looking at resource class {}", resource.getClass().getName());
            if( resource.getClass().isAnnotationPresent(Path.class) ) {
                String resourcePath = resource.getClass().getAnnotation(Path.class).value();
                log.debug("Found v2 class {} with @Path {}", resource.getClass().getName(), resourcePath);
                register(resource.getClass());
            }
        }
    }

    @Override
    protected Registrar[] getRegistrars() {
        AnnotationRegistrar v2 = new AnnotationRegistrar(V2.class); // Mt Wilson 2.0 Resource APIs 
        AnnotationRegistrar rpc = new AnnotationRegistrar(RPC.class); // Mt Wilson 2.0 Remote Procedure Call APIs 
        ImplementationRegistrar runnables = new ImplementationRegistrar(); //  backgroudn tasks 
        return new Registrar[] { v2, rpc, runnables };
    }

}
