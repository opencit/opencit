/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.client;

import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.io.UUID;
import java.io.IOException;
import org.restlet.resource.ClientResource;

/**
 * Convenient factory methods that mirror the routed resources. So you can write Tag[] results = My.tags().post(new
 * Tag[] { ... });
 *
 * @author jbuhacoff
 */
public class At {
    
    private static String baseurl() {
        try {
            return My.configuration().getAssetTagServerURL().toExternalForm();
        }
        catch(IOException e) {
            return "";
        }
    }
    
    public static ClientResource tags() {
        return new ClientResource(baseurl() + "/tags");
    }

    public static ClientResource tag(UUID uuid) {
        return new ClientResource(baseurl() + "/tags/" + uuid);
    }

    public static ClientResource tagValues(UUID uuid) {
        return new ClientResource(baseurl() + "/tags/" + uuid + "/values");
    }

    public static ClientResource rdfTriples() {
        return new ClientResource(baseurl() + "/rdf-triples");
    }

    public static ClientResource rdfTriple(UUID uuid) {
        return new ClientResource(baseurl() + "/rdf-triples/" + uuid);
    }

    public static ClientResource certificateRequests() {
        return new ClientResource(baseurl() + "/certificate-requests");
    }

    public static ClientResource certificateRequest(UUID uuid) {
        return new ClientResource(baseurl() + "/certificate-requests/" + uuid);
    }

    public static ClientResource certificateRequestApproval(UUID uuid) {
        return new ClientResource(baseurl() + "/certificate-requests/" + uuid + "/certificate");
    }

    public static ClientResource certificates(UUID uuid) {
        return new ClientResource(baseurl() + "/certificate/" + uuid);
    }
}