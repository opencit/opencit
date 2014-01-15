/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.as.rest.v2.model.HostLinks;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

/**
 *
 * @author jbuhacoff
 */
@Stateless
@Path("/v2/hosts")
public class Hosts extends AbstractResource<Host,HostCollection,HostFilterCriteria,HostLinks> {

    @Override
    protected HostCollection search(HostFilterCriteria criteria) {
        HostCollection hosts = new HostCollection();
        Host host = new Host();
        host.setId(new UUID()); // id
        host.setName("hostabc"); // name
        host.setDescription("test host"); // description
        host.setConnectionUrl("http://1.2.3.4"); // connection_url
        host.setBiosMLE("bios-4.3.2"); // bios_mle
        host.setIPAddress("1.2.2.3"); // ipaddress   ;  so if you want it to be ip_address you need to call it setIpAddress
        hosts.getHosts().add(host);
        return hosts;
    }

    @Override
    protected Host retrieve(String id) {
        Host tmp = new Host();
        tmp.setId(new UUID());
        tmp.setName("hostxyz");
        tmp.setDescription("test host");
        tmp.setConnectionUrl("http://1.2.3.4");
        tmp.setBiosMLE("bios-4.3.2");
        return tmp;
    }

    @Override
    protected void store(Host item) {
        // store it...
    }

    @Override
    protected void delete(String id) {
    }

    @Override
    protected HostFilterCriteria createFilterCriteriaWithId(String id) {
        HostFilterCriteria criteria = new HostFilterCriteria();
        criteria.id = UUID.valueOf(id);
        return criteria;
    }
    
}
