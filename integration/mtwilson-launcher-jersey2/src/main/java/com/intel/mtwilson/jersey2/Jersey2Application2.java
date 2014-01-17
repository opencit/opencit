/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey2;

//import com.intel.dcsg.cpg.extensions.Extensions;
import org.glassfish.jersey.server.ResourceConfig;
//import com.intel.mtwilson.ws.jersey.util.*;
import java.util.List;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

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
public class Jersey2Application2 extends ResourceConfig {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Jersey2Application2.class);
    
    public Jersey2Application2() {
        log.debug("Registering YAML, XML, JSON providers");
register(com.intel.mtwilson.util.LocalizedExceptionMapper.class);
register(com.intel.mtwilson.jersey.provider.JacksonXmlMapperProvider.class); 
register(com.intel.mtwilson.jersey.provider.JacksonObjectMapperProvider.class);
register(com.intel.mtwilson.jersey.provider.JacksonYamlObjectMapperProvider.class);
register(com.intel.mtwilson.jersey.provider.ApplicationYamlProvider.class);
register(com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper.class);
register(com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper.class); 
register(com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider.class);
//register(com.fasterxml.jackson.jaxrs.json.JsonParseExceptionMapper.class);
register(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class);
//register(com.fasterxml.jackson.jaxrs.json.JsonMappingExceptionMapper.class);
register(com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider.class); 
//register(com.fasterxml.jackson.jaxrs.xml.JsonParseExceptionMapper.class); 
register(com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider.class); 
//register(com.fasterxml.jackson.jaxrs.xml.JsonMappingExceptionMapper.class);
register(org.glassfish.jersey.client.filter.HttpDigestAuthFilter.class); 
register(org.glassfish.jersey.server.wadl.internal.WadlResource.class);
register(com.intel.mtwilson.as.rest.v2.resource.Hosts.class);
register(com.intel.mtwilson.as.rest.v2.resource.Files.class);
register(com.intel.mtwilson.as.rest.v2.resource.Users.class);

        // XXX TODO LOW/NO PRIORITY  register(ApplicationWwwUrlFormEncodedProvider.class); // low priority for allowing html forms to create objects using POST ...  would go along with an ApplicationHtmlProvider.class which would implement message body writer and generate html for any obejct... basically a fields/values table for collections and a key/value table for a single object, with links from meta section rendered as <a> tags, etc.  it could load an html file template from configuration and use antlr, stringtemplate, or moustache plugins to render it with the given object. not intended for creating fully-featured web apps but it could be used for quick in browser testing and browsing of regular resources... 

            // XXX TODO tried to register the resource classses Hosts, Files, Users using the packages() directive below 
            // but this caused only the first resource Files to be registered and the others were missing; 
            // if they are registered explicitly as above then they are all registered.
//       packages("com.intel.mtwilson.as.rest.v2.resource");
       //        packages("com.intel.mtwilson.authz.shiro");
        
//        log.debug("listing all jersey provider classes");
//        List<Object> providers = Extensions.findAll(Provider.class.getName()); // this is the @Provider annotation so something must scan the classpath for these
//        for(Object provider : providers) {
//          register(provider); // or register(provider.getClass());
//        }

       // XXX TODO:
        // https://jersey.java.net/apidocs/2.5/jersey/org/glassfish/jersey/server/ResourceConfig.html
        // setClassLoadser( ... how are we going to know which one to use?   maybe just default to the current thread classloader and depend on someone else to set it ??? )
        // like this:   
        // setClassLoader( Thread.currentThread().getContextClassLoader() );
        // but need the mtwilson-launcher to call Thread.currentThread().setContextClassLoader(...)   with whatever classloader it wants us to use to load resources. 
        // this class loader will need to be able to see any resources that are loaded from all modules (their public apis) so if using a shared or fenced model it has to be the "commoN" classloader...
        // but XXX TODO  classloaderstrategy doesn't provide a method to get this "top" class loader so it might need an addition to the ClassLoadingStrategy interface...
        log.debug("v2resourceconfig classloader is: {}", getClassLoader().getClass().getName()); // sun.misc.Launcher$AppClassLoader   explains why the extensions can't be cast... this is the system classloader and the extensions are loaded by one of the strategies in cpg-classpath.    or you might see org.eclipse.jetty.webapp.WebAppClassLoader  when using the WebAppContext handler
        if( getClassLoader().getParent() != null ) {
            log.debug("and the parent classloader is: {}", getClassLoader().getParent().getClass().getName()); // sun.misc.Launcher$ExtClassLoader      //    or you would see sun.misc.Launcher$AppClassLoader  if the current classloader is  the WebAppClassLoader
        }
        /*
        List<HttpResource> rs = Extensions.findAll(HttpResource.class); 
        log.debug("Found {} HttpResource implmentations", rs.size());
        for(HttpResource r : rs) {
            log.debug("Adding resource: {}", r.getClass().getName());
            registerClasses(r.getClass());
//            registerResources(r);
        }
        */
        
    }

    // workaround for netbeans 7.3.1 bug (fixed in netbeans 7.4) https://netbeans.org/bugzilla/show_bug.cgi?id=234581
    // with this workaround netbeans will continue to auto-generate addRestResourceClasses but it's harmless
    // because it will never be called
    public void getClasses(int unused) {}

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically re-generated by NetBeans REST support to populate
     * given list with all resources defined in the project.
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.intel.mtwilson.as.rest.v2.resource.Files.class);
        resources.add(com.intel.mtwilson.as.rest.v2.resource.Hosts.class);
        resources.add(com.intel.mtwilson.as.rest.v2.resource.Users.class);
        resources.add(com.intel.mtwilson.jersey.provider.ApplicationYamlProvider.class);
        resources.add(com.intel.mtwilson.jersey.provider.JacksonObjectMapperProvider.class);
        resources.add(com.intel.mtwilson.jersey.provider.JacksonXmlMapperProvider.class);
        resources.add(com.intel.mtwilson.jersey.provider.JacksonYamlObjectMapperProvider.class);
        resources.add(com.intel.mtwilson.jersey.resource.AbstractResource.class);
        resources.add(com.intel.mtwilson.util.LocalizedExceptionMapper.class);
    }

}
