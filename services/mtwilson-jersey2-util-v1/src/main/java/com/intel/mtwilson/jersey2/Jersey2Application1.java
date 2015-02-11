/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey2;

import com.intel.dcsg.cpg.extensions.AnnotationRegistrar;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.mtwilson.launcher.ws.ext.V1;
import java.util.List;
import javax.ws.rs.Path;

/**
 * Configures Mt Wilson 1.x APIs
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
//@ApplicationPath("/v1")
public class Jersey2Application1 extends AbstractJerseyPluginApplication {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Jersey2Application1.class);
    
    public Jersey2Application1() {
        super();
register(com.intel.mtwilson.util.ThrowableMapperV1.class); // returns an application/json structure like { "error_code": 0, "error_message": "OK" }
        
//register(com.intel.mtwilson.jersey.provider.JacksonXmlMapperProvider.class); 
register(com.intel.mtwilson.jaxrs2.provider.V1JacksonObjectMapperProvider.class);
register(com.intel.mtwilson.util.ASLocalizationFilter.class);
        
        // now get the list of classes that implement @V1 and @Path
        List<Object> resources = Extensions.findAllAnnotated(V1.class); // we could search for @Path but then we'd find v1 and v2 classes as well as utility classes for both such as the application.wadl generator
        for(Object resource : resources) {
            if( resource.getClass().isAnnotationPresent(Path.class) ) {
                String resourcePath = resource.getClass().getAnnotation(Path.class).value();
                log.debug("Found v1 class {} with @Path {}", resource.getClass().getName(), resourcePath);
                register(resource.getClass());
            }
        }
/*
// Attestation Service
register(com.intel.mtwilson.as.rest.AsStatus.class);
register(com.intel.mtwilson.as.rest.AssetTagCert.class);
register(com.intel.mtwilson.as.rest.BulkHostTrust.class);
register(com.intel.mtwilson.as.rest.CA.class);
register(com.intel.mtwilson.as.rest.Host.class);
register(com.intel.mtwilson.as.rest.PollHosts.class);
register(com.intel.mtwilson.as.rest.Reports.class);
register(com.intel.mtwilson.as.rest.SAML.class);
register(com.intel.mtwilson.as.rest.Test.class);
// Management Service
register(com.intel.mtwilson.ms.rest.APIClient.class);
register(com.intel.mtwilson.ms.rest.CA.class);
register(com.intel.mtwilson.ms.rest.Host.class);
register(com.intel.mtwilson.ms.rest.MsStatus.class);
register(com.intel.mtwilson.ms.rest.SamlCertificate.class);
// Whitelist Service
register(com.intel.mtwilson.wlm.rest.Mle.class);
register(com.intel.mtwilson.wlm.rest.Oem.class);
register(com.intel.mtwilson.wlm.rest.Os.class);
register(com.intel.mtwilson.wlm.rest.WlmStatus.class);
*/
    }


    @Override
    protected Registrar[] getRegistrars() {
        // scan the jar files for mtwilson plugins that contain classes annotated with @Path from javax.ws.rs
        AnnotationRegistrar registrar = new AnnotationRegistrar(V1.class);
        return new Registrar[] { registrar };
    }
    
}
