/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.jaxrs.server;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.configuration.jaxrs.ConfigurationDocument;
import com.intel.mtwilson.configuration.jaxrs.Setting;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/configuration-settings") 
public class ConfigurationSettingsResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationSettingsResource.class);
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissions("configuration_settings:retrieve")        
    public ConfigurationDocument retrieveConfigurationSettings() {
        try {
            ConfigurationDocument document = new ConfigurationDocument();
            Configuration configuration = ConfigurationFactory.getConfiguration();
            document.copyFrom(configuration);
            return document;
        }
        catch(IOException e) {
            log.error("Cannot retrieve configuration settings", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissions("configuration_settings:store")        
    public ConfigurationDocument storeConfigurationSettings(ConfigurationDocument update) {
        try {
            ConfigurationProvider provider = ConfigurationFactory.getConfigurationProvider();
            Configuration configuration = provider.load();
            if( !configuration.isEditable() ) {
                throw new IllegalStateException("Configuration not editable");
            }
            for(Setting setting : update.getSettings()) {
                configuration.set(setting.getName(), setting.getValue());
            }
            provider.save(configuration);
            // now return an updated document
            ConfigurationDocument document = new ConfigurationDocument();
            document.copyFrom(configuration);
            return document;
        }
        catch(IOException | IllegalStateException e) {
            log.error("Cannot store configuration settings", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
}
