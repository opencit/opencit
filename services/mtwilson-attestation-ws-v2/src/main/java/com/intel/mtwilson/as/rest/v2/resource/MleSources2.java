/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleSourceLinks;
import com.intel.mtwilson.as.rest.v2.repository.MleSourceRepository;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ejb.Stateless;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.as.rest.v2.model.MleSourceLocator;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
/**
 *
 * @author ssbangal
 */
@V2
@Stateless
@Path("/mles/{mle}/sources")
public class MleSources2 extends AbstractJsonapiResource<MleSource, MleSourceCollection, MleSourceFilterCriteria, MleSourceLinks, MleSourceLocator>{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MleSources2.class);
    
    public MleSources2() {
        setRepository(new MleSourceRepository());
    }

    @Override
    protected MleSourceCollection createEmptyCollection() {
        return new MleSourceCollection();
    }
    
}
