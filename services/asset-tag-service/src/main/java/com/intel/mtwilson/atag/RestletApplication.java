/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag;

import com.intel.mtwilson.atag.resource.TagListResource;
import com.intel.mtwilson.atag.resource.CertificateRequestApprovalResource;
import com.intel.mtwilson.atag.resource.CertificateRequestListResource;
import com.intel.mtwilson.atag.resource.CertificateRequestResource;
import com.intel.mtwilson.atag.resource.RdfTripleListResource;
import com.intel.mtwilson.atag.resource.CertificateResource;
import com.intel.mtwilson.atag.resource.RdfTripleResource;
import com.intel.mtwilson.atag.resource.TagValueResource;
import com.intel.mtwilson.atag.resource.TagResource;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References:
 * 
 * http://restlet.org/learn/guide/2.2/core/resource/
 * 
 * http://restlet.org/learn/javadocs/2.1/jse/api/org/restlet/data/Reference.html
 *
 * @author jbuhacoff
 */
public class RestletApplication extends Application {
 
    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach("/tags", TagListResource.class); // create tag, create multiple tags, search tags
        router.attach("/tags/{id}", TagResource.class); // update tag, delete tag, read tag
        router.attach("/tags/{id}/values", TagValueResource.class);
        router.attach("/rdf-triples", RdfTripleListResource.class); 
        router.attach("/rdf-triples/{id}", RdfTripleResource.class);
        router.attach("/certificate-requests", CertificateRequestListResource.class); 
        router.attach("/certificate-requests/{id}", CertificateRequestResource.class); 
//        router.attach("certificate-requests/{id}/approval", CertificateRequestApprovalResource.class); 
        router.attach("/certificate-requests/{id}/certificate", CertificateRequestApprovalResource.class); 
        router.attach("/certificates/{id}", CertificateResource.class); 
        Directory directory = new Directory(getContext(), "file:///c:/users/jbuhacof/workspace/mtwilson-dev/services/asset-tag-service/src/main/resources/html5/");
//        Directory directory = new Directory(getContext(), "clap:///html5/");
        directory.setIndexName("index.html");
        router.attach("/", directory);
        
        return router;
    }
    
}
