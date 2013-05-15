/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.helper;

import com.intel.mtwilson.plugin.api.Plugin;
import com.intel.mtwilson.plugin.api.PluginLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class PluginRegistry {
    private transient static Logger log = LoggerFactory.getLogger(PluginRegistry.class);
    private static final ArrayList<Plugin> plugins = new ArrayList<Plugin>();
    
    static {
        loadAvailablePlugins();
    }
    
    public static List<Plugin> getAvailablePlugins() {
        return plugins;
    }
    
    public static void loadAvailablePlugins() {
        //ArrayList<Plugin> plugins = new ArrayList<Plugin>();
        plugins.clear();
        Iterator<PluginLoader> pluginLoaders = ServiceLoader.load(PluginLoader.class).iterator();
        while(pluginLoaders.hasNext()) {
            try {
                PluginLoader pluginLoader = pluginLoaders.next();
                Plugin plugin = pluginLoader.loadPlugin();
                if( plugin != null ) {
                    plugins.add(plugin);
                }
            }
            catch(ServiceConfigurationError e) {
                log.error(e.toString());
            }
        }
    }
    
}
