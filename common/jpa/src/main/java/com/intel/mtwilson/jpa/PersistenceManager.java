/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jpa;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class closes the EntityManagerFactory when when the application stops.
 *
 * It embodies on the fact that we need only one static EntityManagerFactory per
 * PersistenceUnit 
 * for the lifecycle of the application. If each Business Object maintained its
 * own factory we would have to obtain each one and close it to avoid a
 * resource leak.
 *
 * Closing of the EntityManagerFactory is done from this class because its
 * tied to the shutdown of the servlet container and we don't want to tie the
 * BaseBO to the servlet container.
 *
 * This needs to go in web.xml:
 *   <listener>
 *       <listener-class>com.intel.mtwilson.util.jpa.PersistenceManager</listener-class>
 *   </listener>
 *   <context-param>
 *       <param-name>mtwilson-jpa-units</param-name>
 *       <param-value>ASDataPU,MSDataPU</param-value>
 *   </context-param>
 *
 * @author jbuhacoff
 */
public abstract class PersistenceManager implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(PersistenceManager.class);
    
    /**
     * This map contains one static EntityManagerFactory for each Persistence Unit
     * used in the application
     */
    private static final ConcurrentHashMap<String,EntityManagerFactory> factories = new ConcurrentHashMap<String,EntityManagerFactory>();

    public EntityManagerFactory getEntityManagerFactory(String persistenceUnitName) {
        log.info("PersistenceManager is configured with {} factories in getEntityManagerFactory", factories.keySet().size());
        if( factories.keySet().isEmpty() ) {
            log.info("PersistenceManager factories is empty, calling configure()");
            configure();
            for(String factoryName : factories.keySet()) {
                EntityManagerFactory factory = factories.get(factoryName);
                if( factory != null && factory.isOpen() ) {
                    log.info("PersistenceManager is configured with factory {} in getEntityManagerFactory", factoryName);
                }
            }
        }
        
        if( factories.containsKey(persistenceUnitName) ) {
            return factories.get(persistenceUnitName);
        }
        throw new IllegalArgumentException("Cannot return EntityManagerFactory for unknown persistence unit: "+persistenceUnitName);
    }
    
    /**
     * Subclasses must implement this function and call addPersistenceUnit for
     * each persistence unit the application needs to use.
     * Here is an example implementation:
     * public void configure() {
     *   addPersistenceUnit("ASDataPU", ASConfig.getJpaProperties());
     *   addPersistenceUnit("MSDataPU", MSConfig.getJpaProperties());
     * }
     * Subclasses must call configure() from within their constructor.
     */
    public abstract void configure();
    
    public void addPersistenceUnit(String persistenceUnitName, Properties jpaProperties) {
        log.info("PersistenceManager adding PersistenceUnit {}", persistenceUnitName);
        if( factories.containsKey(persistenceUnitName) ) {
            EntityManagerFactory factory = factories.get(persistenceUnitName);
            if( factory != null && factory.isOpen() ) {
                //factory.close(); // XXX TODO maybe instead of closing... since it's already there and open, just keep it
                return;
            }
        }
        EntityManagerFactory factory = createFactory(persistenceUnitName, jpaProperties);
        log.warn("Created EntityManagerFactory for persistence unit {}", persistenceUnitName);
        factories.put(persistenceUnitName, factory);
    }
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("PersistenceManager initialized");
        
        // XXX can we get persistence unit names from web.xml to simplify configuration?
        Enumeration<String> attrs = sce.getServletContext().getAttributeNames();
        log.info("Servlet Context path {}",sce.getServletContext().getContextPath()); // like /WLMService
        while(attrs.hasMoreElements()) {
            String attr = attrs.nextElement();
//            log.info("Servlet Context attribute: {} = {}", new String[] { attr, sce.getServletContext().getAttribute(attr).toString() }); // attributes are not necessarily strings... some may be boolean or something else
            log.info("Servlet Context attribute: {}", attr);             
        }
        Enumeration<String> initparams = sce.getServletContext().getInitParameterNames();
        while(initparams.hasMoreElements()) {
            String param = initparams.nextElement();
//            log.info("Servlet Context init param: {} = {}", new String[] { param, sce.getServletContext().getInitParameter(param).toString() });
            log.info("Servlet Context init param: {}",  param);
        }
        
        /*
        // close any factories that may already be open...
        for(String factoryName : factories.keySet()) {
            EntityManagerFactory factory = factories.get(factoryName);
            if( factory != null && factory.isOpen() ) {
                log.info("PersistenceManager closing factory {} in contextInitialized", factoryName);
                factory.close();
            }
            factories.remove(factoryName);
        }
        // create factories according to the subclass implementation
        configure();
        */
//        System.out.println(String.format("PersistenceManager: Context initialized, EntityManagerFactory is %s", entityManagerFactory.isOpen() ? "open" : "closed"));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        for(String factoryName : factories.keySet()) {
            EntityManagerFactory factory = factories.get(factoryName);
            if( factory != null && factory.isOpen() ) {
                log.info("PersistenceManager closing factory {} in contextDestroyed", factoryName);
                factory.close();
            }
            factories.remove(factoryName);
        }
    }

    
    /**
     * 
     * @param persistenceUnitName as defined in the persistence.xml file, for example "ASDataPU" or "MSDataPU"
     * @param properties for initializing the persistence unit: javax.persistence.jdbc.driver, etc
     * @return 
     */
    public static EntityManagerFactory createFactory(String persistenceUnitName, Properties properties) {
        try {
            log.debug("Loading database driver {} for persistence unit {}", new String[] { properties.getProperty("javax.persistence.jdbc.driver"), persistenceUnitName });
            Class.forName(properties.getProperty("javax.persistence.jdbc.driver"));
            EntityManagerFactory factory = Persistence.createEntityManagerFactory(persistenceUnitName,properties);
            return factory;
        } catch (ClassNotFoundException ex) {
            log.error("Cannot load JDBC Driver for persistence unit", ex);
        }
        return null;
    }
    
}
