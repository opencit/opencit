/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.rest.v2.model.PollHost;
import com.intel.mtwilson.as.rest.v2.model.PollHostCollection;
import com.intel.mtwilson.as.rest.v2.model.PollHostFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.PollHostLocator;
import com.intel.mtwilson.as.rest.v2.repository.PollHostRepository;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelQuery;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/integrations/openstack/PollHosts")
public class PollHosts /*extends AbstractJsonapiResource<PollHost, PollHostCollection, PollHostFilterCriteria, NoLinks<PollHost>, PollHostLocator>*/ {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    /*
    private PollHostRepository repository;

    public PollHosts(PollHostRepository repository) {
        this.repository = new PollHostRepository();
    }
    */
    /*
    @Override
    protected PollHostCollection createEmptyCollection() {
        return new PollHostCollection();
    }

    @Override
    protected PollHostRepository getRepository() {
        return repository;
    }
    */
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public OpenStackHostTrustLevelReport getOpenStackHostTrustReport(OpenStackHostTrustLevelQuery request) {
        ValidationUtil.validate(request);
        OpenStackHostTrustLevelReport pollHosts = new HostTrustBO().getPollHosts(request);
        return pollHosts;
    }
            
}
