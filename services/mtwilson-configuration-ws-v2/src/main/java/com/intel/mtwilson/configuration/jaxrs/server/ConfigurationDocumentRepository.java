/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.jaxrs.server;

import com.intel.mtwilson.configuration.jaxrs.ConfigurationDocument;
import com.intel.mtwilson.configuration.jaxrs.ConfigurationDocumentCollection;
import com.intel.mtwilson.configuration.jaxrs.ConfigurationDocumentFilterCriteria;
import com.intel.mtwilson.configuration.jaxrs.Setting;
import com.intel.mtwilson.configuration.jaxrs.server.ConfigurationDocumentLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurationDocumentRepository implements DocumentRepository<ConfigurationDocument,ConfigurationDocumentCollection,ConfigurationDocumentFilterCriteria,ConfigurationDocumentLocator>{

    @Override
    public ConfigurationDocumentCollection search(ConfigurationDocumentFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(ConfigurationDocumentFilterCriteria criteria) {
        throw new UnsupportedOperationException("Cannot delete entire configurations; try removing individual settings");
    }

    @Override
    public ConfigurationDocument retrieve(ConfigurationDocumentLocator locator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void store(ConfigurationDocument item) {
        // currently assuming the only configuration is "main" ; but we need to be able to do this for features too or by "id" which would be subfolder of configuration 
        for(Setting setting : item.getSettings()) {
            
        }
    }

    @Override
    public void create(ConfigurationDocument item) {
        throw new UnsupportedOperationException("Cannot create new configuration; try adding individual settings");
    }

    @Override
    public void delete(ConfigurationDocumentLocator locator) {
        throw new UnsupportedOperationException("Cannot delete entire configuration; try removing individual settings");
    }
    
}
