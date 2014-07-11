/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.v2.resource;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.configuration.v2.resource.*;
import com.intel.mtwilson.configuration.v2.model.Configuration;
import com.intel.mtwilson.configuration.v2.model.ConfigurationFilterCriteria;
import com.intel.mtwilson.configuration.v2.model.ConfigurationCollection;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
//import javax.ejb.Stateless;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
@V2
//@Stateless
@Path("/configurations_unused") 
public class ConfigurationResource extends AbstractResource<Configuration,ConfigurationCollection,ConfigurationFilterCriteria,NoLinks<Configuration>> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationResource.class);
    private static final Pattern xmlTagName = Pattern.compile("(?:^[a-z].*)");
    
    @RequiresPermissions("configurations:search")        
    @Override
    protected ConfigurationCollection search(ConfigurationFilterCriteria criteria) {
        ConfigurationCollection configurations = new ConfigurationCollection();
        Configuration configuration = retrieve("local");
        configurations.getConfigurations().add(configuration);
        return configurations;
    }

    @RequiresPermissions("configurations:retrieve")        
    @Override
    protected Configuration retrieve(String id) {
        Configuration configuration = null;
        if( "local".equals(id) ) {
            // get mtwilson.properties etc.
            configuration = new Configuration();
            configuration.setName("local");
            try {
                org.apache.commons.configuration.Configuration conf = My.configuration().getConfiguration(); 
                Iterator<String> it = conf.getKeys();
                while(it.hasNext()) {
                    String key = it.next();
                    String value = conf.getString(key);
                    if( !xmlTagName.matcher(value).matches() ) {
                        log.debug("Skipping poperty {}", key);
                        continue;
                    }
                    configuration.getProperties().put(key, value);
                }
            }
            catch(Exception e) {
                throw new WebApplicationException("Cannot load configuration"); 
            }
        }
        else if( "my".equals(id) ) {
            try {
            configuration = new Configuration();
            configuration.setName("my");
            // find all the properties in MyConfiguration that are either String, Boolean, or String[] 
            PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy namingStrategy = new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy();
//            Map<String,Object> result = new HashMap<>();
            Map<String,Object> attrs = PropertyUtils.describe(My.configuration());// throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
            for(Map.Entry<String,Object> attr : attrs.entrySet()) {
                String translatedKey = namingStrategy.translate(attr.getKey());
                // there are attributes we skip, like "class" from getClass() 
                if( attr.getKey().equals("class") ) { continue; }
                // for now allow only String, Boolean, and String[] properties 
                Object a1 = PropertyUtils.getSimpleProperty(My.configuration(), attr.getKey());
                if( a1 == null ) { 
                    configuration.getProperties().put(translatedKey, null);
                }
                else if( a1 instanceof String ) {
                    configuration.getProperties().put(translatedKey, (String)a1);
                }
                else if(  a1 instanceof Boolean  ) {
                    configuration.getProperties().put(translatedKey, (String)a1.toString());
                }
                else if( a1 instanceof String[] ) {
                    configuration.getProperties().put(translatedKey, StringUtils.join((String[])a1));
                }
            }
            }
            catch(Exception e) {
                throw new WebApplicationException("Cannot load configuration"); 
            }            
        }
        else if( "cluster".equals(id) ) {
            // load from database ...
        }
        else {
            // assume uuid or other name so find out which it is so we can use the right query
            if( UUID.isValid(id) ) {
                // query by uuid
            }
            else {
                // query by name
            }
        }
        return configuration;
    }

    @RequiresPermissions("configurations:create")        
    @Override
    protected void create(Configuration item) {
        // store it...
    }
    @RequiresPermissions("configurations:store")        
    @Override
    protected void store(Configuration item) {
        // store it...
    }

    @RequiresPermissions("configurations:delete")        
    @Override
    protected void delete(String id) {
    }

    /*
    @Override
    protected RpcFilterCriteria createFilterCriteriaWithId(String id) {
        RpcFilterCriteria criteria = new RpcFilterCriteria();
        criteria.id = UUID.valueOf(id);
        return criteria;
    }
    */
    @Override
    protected ConfigurationCollection createEmptyCollection() {
        return new ConfigurationCollection();
    }
    
}
