/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.extensions.jaxrs;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
//import javax.ejb.Stateless;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * See also  com.intel.mtwilson.v2.test.extensions.ExtensionsResource   in  mtwilson-test-ws-v2
 * @author jbuhacoff
 */
@V2
//@Stateless
@Path("/extensions") 
public class ExtensionsResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtensionsResource.class);
    private static final Pattern extensionClassName = Pattern.compile("^(?:[a-zA-Z0-9$.]*)$");
    
    @GET
    @RequiresPermissions("extensions:search")
    @Produces(MediaType.TEXT_PLAIN)
    public String search(@BeanParam ExtensionFilterCriteria criteria) {
        Map<String,List<Class<?>>> map = Extensions.getWhiteboard();
        HashSet<String> resultset = new HashSet<>();
        for(String registeredInterface : map.keySet()) {
            if( criteria.isInterface != null && criteria.isInterface.booleanValue() ) {
                // list of extension points
                if( acceptInterface(registeredInterface, criteria)) {
                    resultset.add(registeredInterface);
                }
            }
            else {
                // list of implementations
                List<Class<?>> list = map.get(registeredInterface);
                for(Class<?> clazz : list) {
                    String className = clazz.getName();
                    if( acceptExtension(className, registeredInterface, criteria)) {
                        resultset.add(className);
                    }
                }
            }
        }
        return StringUtils.join(resultset, "\n");
    }
    
    private boolean acceptExtension(String extensionName, String registeredInterface, ExtensionFilterCriteria criteria) {
        if( criteria.filter ) {
            return 
                   (criteria.interfaceEqualTo == null || criteria.interfaceEqualTo.isEmpty() || criteria.interfaceEqualTo.equals(registeredInterface))
                    &&
                   (criteria.nameEqualTo == null || criteria.nameEqualTo.isEmpty() || extensionName.equals(criteria.nameEqualTo))
                    &&
                   (criteria.nameContains == null || criteria.nameContains.isEmpty() || extensionName.contains(criteria.nameContains))
                    ;
        }
        else {
            return true;
        }
    }
    

    private boolean acceptInterface(String registeredInterface, ExtensionFilterCriteria criteria) {
        if( criteria.filter ) {
            return 
                   (criteria.interfaceEqualTo == null || criteria.interfaceEqualTo.isEmpty() || registeredInterface.equals(criteria.interfaceEqualTo))
                    ;
        }
        else {
            return true;
        }
    }
    
}
