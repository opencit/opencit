/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey2;

import com.intel.mtwilson.jaxrs2.feature.JacksonFeature;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.mtwilson.launcher.ExtensionCacheLauncher;
import org.glassfish.jersey.server.ResourceConfig;

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
public abstract class AbstractJerseyPluginApplication extends ResourceConfig {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractJerseyPluginApplication.class);
    
//    protected abstract File[] getJars();
    
    protected abstract Registrar[] getRegistrars();
    
    public AbstractJerseyPluginApplication() {
//        ExtensionDirectoryLauncher launcher = new ExtensionDirectoryLauncher();
        ExtensionCacheLauncher launcher = new ExtensionCacheLauncher();
        launcher.setRegistrars(getRegistrars());
        launcher.run(); // loads application jars, scans extension jars for the plugins as specified by getRegistrars()
//        Util.scanJars(Util.findAllJars(),getRegistrars());
register(JacksonFeature.class);        
        log.debug("Registering YAML, XML, JSON providers");
        
        // THESE WERE ENABLED:
//register(com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper.class);
//register(com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper.class); 
//register(com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider.class);
//register(com.fasterxml.jackson.jaxrs.json.JsonParseExceptionMapper.class);

/*        // THESE WERE ENABLED:
register(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class);
register(com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider.class); 
register(com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider.class); 
*/
        
//register(com.fasterxml.jackson.jaxrs.json.JsonMappingExceptionMapper.class);
//register(com.fasterxml.jackson.jaxrs.xml.JsonParseExceptionMapper.class); 
//register(com.fasterxml.jackson.jaxrs.xml.JsonMappingExceptionMapper.class);
        // we can register javax.ws.rs filters here that implement
        // javax.ws.rs.container.ContainerRequestFilter or
        // javax.ws.rs.container.ContainerResponseFilter,
        // but registering servlet filters will not work;
        // that is why the LocalizableResponseFilter (implements 
        // ContainerResponseFilter) can be registered here but
        // the ShiroFilter must be declared in web.xml instead.
        log.debug("Registering exception mappers and filters");
//register(com.intel.mtwilson.util.LocalizedExceptionMapper.class);
register(org.glassfish.jersey.server.filter.HttpMethodOverrideFilter.class); // jersey2 equivalent of com.sun.jersey.api.container.filter.PostReplaceFilter
//register(org.glassfish.jersey.client.filter.HttpDigestAuthFilter.class); 
//register(com.intel.mtwilson.as.helper.ASAuthenticationFilter.class); 
//register(org.apache.shiro.web.servlet.ShiroFilter.class); // must be in web.xml because it's a servlet filter, not a javax.ws.rs filter
//register(com.intel.mtwilson.audit.helper.AuditJerseyRequestFilter.class);
//register(com.intel.mtwilson.audit.helper.AuditJerseyResponseFilter.class);
register(com.intel.mtwilson.shiro.AuthorizationExceptionMapper.class);
//register(com.intel.mtwilson.util.ASLocalizationFilter.class);
register(com.intel.mtwilson.jaxrs2.server.filter.ErrorLogFilter.class);
        log.debug("Registering other resources");
register(org.glassfish.jersey.server.wadl.internal.WadlResource.class);

//       packages("com.intel.mtwilson.as.rest.v2.resource");
       //        packages("com.intel.mtwilson.authz.shiro");
        
//        log.debug("listing all jersey provider classes");
//        List<Object> providers = Extensions.findAll(Provider.class.getName()); // this is the @Provider annotation so something must scan the classpath for these
//        for(Object provider : providers) {
//          register(provider); // or register(provider.getClass());
//        }

        log.debug("resourceconfig classloader is: {}", getClassLoader().getClass().getName()); // sun.misc.Launcher$AppClassLoader   explains why the extensions can't be cast... this is the system classloader and the extensions are loaded by one of the strategies in cpg-classpath.    or you might see org.eclipse.jetty.webapp.WebAppClassLoader  when using the WebAppContext handler
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

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically re-generated by NetBeans REST support to populate
     * given list with all resources defined in the project.
     */
    /***** UNUSED
    private void addRestResourceClasses(Set<Class<?>> resources) {
//        resources.add(com.intel.mtwilson.as.rest.v2.resource.Files.class);
//        resources.add(com.intel.mtwilson.as.rest.v2.resource.Hosts.class);
//        resources.add(com.intel.mtwilson.as.rest.v2.resource.Users.class);
//        resources.add(com.intel.mtwilson.jersey.provider.ApplicationYamlProvider.class);
//        resources.add(com.intel.mtwilson.jersey.provider.JacksonObjectMapperProvider.class);
//        resources.add(com.intel.mtwilson.jersey.provider.JacksonXmlMapperProvider.class);
//        resources.add(com.intel.mtwilson.jersey.provider.JacksonYamlObjectMapperProvider.class);
//        resources.add(com.intel.mtwilson.jersey.resource.AbstractResource.class);
//        resources.add(com.intel.mtwilson.util.LocalizedExceptionMapper.class);
    }*/

}
