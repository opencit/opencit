/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.helper;

import com.intel.mtwilson.plugin.api.Plugin;
import java.util.List;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Look for required configuration settings. 
 * Generate default settings for missing configuration, if possible.
 * Write the generated settings back to the configuration file. 
 * 
 * For database, first look in mtwilson.as.db.* and then in mtwilson.db.* FOR EACH proeprty separately.
 * So admin can define common database server in mtwilson.db.host and then different database names
 * for each service in mtwilson.as.db.name and mtwilson.ms.db.name etc. 
 * 
 * @author jbuhacoff
 */
public class ASConfiguration implements ServletContextListener {
    private transient static Logger log = LoggerFactory.getLogger(ASConfiguration.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("ASConfiguration: context initialized");
        /*
        PluginRegistry.loadAvailablePlugins();
        List<Plugin> plugins = PluginRegistry.getAvailablePlugins();
        log.info("Loaded {} Mt Wilson Plugins", plugins.size());
        for(Plugin plugin : plugins) {
            log.info("Loaded Mt Wilson Plugin: {}", plugin.getClass().getName());
        }
        */
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("ASConfiguration: context destroyed");
    }
    
}
