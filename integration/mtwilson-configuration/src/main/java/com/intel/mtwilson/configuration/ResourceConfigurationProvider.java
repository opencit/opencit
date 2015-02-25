/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.io.Resource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
//import org.apache.commons.configuration.Configuration;
//import org.apache.commons.configuration.ConfigurationException;
//import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author jbuhacoff
 */
public class ResourceConfigurationProvider implements ConfigurationProvider {
//    private String environmentVariableName = "";
//    private String systemPropertyName;
//    private String path;
    private Resource resource = null;
    public ResourceConfigurationProvider(Resource resource) {
        this.resource = resource;
    }
    
    public Resource getResource() { return resource; }
    
    @Override
    public Configuration load() throws IOException {
        Properties p = new Properties();
        p.load(getResource().getInputStream());
        return new PropertiesConfiguration(p);
    }

    @Override
    public void save(Configuration configuration) throws IOException {
        Properties p = new Properties();
        for(String key : configuration.keys()) {
            p.setProperty(key, configuration.get(key));
        }
        try (OutputStream out = getResource().getOutputStream()) {
            p.store(out, String.format("stored on %s", (new Date()).toString()));
        }
    }
    
}
