/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.v2.resource;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.setup.v2.model.*;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractResource;
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

/**
 *
 * @author jbuhacoff
 */
@V2
//@Stateless
@Path("/setup-tasks") 
public class SetupTaskResource extends AbstractResource<SetupTask,SetupTaskCollection,SetupTaskFilterCriteria,NoLinks<SetupTask>> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetupTaskResource.class);
    private static final Pattern xmlTagName = Pattern.compile("(?:^[a-z].*)");
    
    @Override
    protected SetupTaskCollection search(SetupTaskFilterCriteria criteria) {
        SetupTaskCollection setupTasks = new SetupTaskCollection();
        // TODO  collect all available setupTasks... local and database
        SetupTask setupTask = retrieve("local");
        setupTasks.getSetupTasks().add(setupTask);
        return setupTasks;
    }

    @Override
    protected SetupTask retrieve(String id) {
        SetupTask setupTask = null;
            // assume uuid or other name so find out which it is so we can use the right query
            if( UUID.isValid(id) ) {
                // query by uuid
            }
            else {
                // query by name
            }
        return setupTask;
    }

    @Override
    protected void create(SetupTask item) {
        // store it...
    }
    @Override
    protected void store(SetupTask item) {
        // store it...
    }

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
    protected SetupTaskCollection createEmptyCollection() {
        return new SetupTaskCollection();
    }
    
}
