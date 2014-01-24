/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.client;

import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.io.UUID;
import java.io.IOException;
import org.restlet.data.MediaType;
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
    
    public static ClientResource tag(String anyUuidOidName) {
        return new ClientResource(baseurl() + "/tags/" + anyUuidOidName);
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
        return new ClientResource(baseurl() + "/certificates/" + uuid);
    }
    
    public static ClientResource selections() {
        return new ClientResource(baseurl() + "/selections");
    }
    
    public static ClientResource selection(UUID uuid) {
        return new ClientResource(baseurl() + "/selections/" + uuid);
    }

    public static ClientResource selections(String anyUuidName) {
        return new ClientResource(baseurl() + "/selections/" + anyUuidName);
    }

    public static ClientResource configurations() {
        return new ClientResource(baseurl() + "/configurations");
    }
    
    public static ClientResource configuration(UUID uuid) {
        return new ClientResource(baseurl() + "/configurations/" + uuid);
    }

    public static ClientResource configuration(String anyUuidName) {
        ClientResource client = new ClientResource(baseurl() + "/configurations/" + anyUuidName);
//        client.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML);
        return client;
    }
    
    public static ClientResource hostUuids() {
        return new ClientResource(baseurl() + "/host-uuids");
    }
    
}