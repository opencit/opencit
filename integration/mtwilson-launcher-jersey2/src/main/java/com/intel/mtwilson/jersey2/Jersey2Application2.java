/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey2;

//import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.AnnotationRegistrar;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.mtwilson.My;
import com.intel.mtwilson.launcher.DirectoryLauncher;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.File;
//import com.intel.mtwilson.ws.jersey.util.*;
import java.util.List;
import javax.ws.rs.Path;
import com.intel.mtwilson.Version;
import java.io.IOException;

/**
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
        // now get the list of classes that implement @V2 and @Path
        List<Object> resources = Extensions.findAll(V2.class.getName()); // we could search for @Path but then we'd find v1 and v2 classes as well as utility classes for both such as the application.wadl generator ; we use .class.getName() and not just .class because we want the object instances, not the annotation itself as <T>
        for(Object resource : resources) {
            if( resource.getClass().isAnnotationPresent(Path.class) ) {
                String resourcePath = resource.getClass().getAnnotation(Path.class).value();
                log.debug("Found v2 class {} with @Path {}", resource.getClass().getName(), resourcePath);
                register(resource.getClass());
            }
        }
    }

    @Override
    protected File[] getJars() {
        try {
            // find the plugin directory, try My.configuration() first then the maven target folder
            File javaPath = new File(My.configuration().getMtWilsonJava());
            if( !javaPath.exists() ) {
                // try target/.../WEB-INF/lib in case we are running on a developer laptop
                javaPath = new File("target"+File.separator+"mtwilson-launcher-jersey2-"+Version.VERSION+File.separator+"WEB-INF"+File.separator+"lib");
            }
            log.debug("Relative path for testing: {}", javaPath.getAbsolutePath());
            if(javaPath.exists()) {
                log.debug("Found relative WEB-INF/lib");
            }
            else {
    //            targetWebinfLib = new File("C:\\Users\\jbuhacof\\workspace\\dcg_security-mtwilson\\integration\\mtwilson-launcher-jersey2\\target\\mtwilson-launcher-jersey2-1.2.3-SNAPSHOT\\WEB-INF\\lib");
                // check mtwilson home ... or maybe reverse the order?
            }
            DirectoryLauncher.JarFilter jarfilter = new DirectoryLauncher.JarFilter();
            return javaPath.listFiles(jarfilter);
        }
        catch(IOException e) {
            throw new RuntimeException("Cannot load configuration", e);
        }
    }

    @Override
    protected Registrar[] getRegistrars() {
        // scan the jar files for mtwilson plugins that contain classes annotated with @Path from javax.ws.rs
        AnnotationRegistrar registrar = new AnnotationRegistrar(V2.class);
        return new Registrar[] { registrar };
    }

}
