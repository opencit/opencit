/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag;

import com.intel.mtwilson.atag.resource.*;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import com.intel.mtwilson.My;
import java.io.IOException;
import org.restlet.data.ChallengeScheme;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References:
 * 
 * http://restlet.org/discover/features
 * 
 * http://restlet.org/learn/2.2/
 * 
 * http://restlet.org/learn/guide/2.2/core/resource/
 * 
 * http://restlet.org/learn/javadocs/2.1/jse/api/org/restlet/data/Reference.html
 * 
 * Using X-HTTP-Method-Override and filename extensions and other parameters in the URL:
 * http://restlet.org/learn/javadocs/snapshot/jse/api/org/restlet/service/TunnelService.html
 *
 * @author jbuhacoff
 */
public class RestletApplication extends Application {
    private static Logger log = LoggerFactory.getLogger(RestletApplication.class);
    
    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach("/tags", TagListResource.class); // create tag, create multiple tags, search tags
        router.attach("/tags/{id}", TagResource.class); // update tag, delete tag, read tag
        router.attach("/tags/{id}/values", TagValueResource.class);
        router.attach("/rdf-triples", RdfTripleListResource.class); 
        router.attach("/rdf-triples/{id}", RdfTripleResource.class);
        router.attach("/selections", SelectionListResource.class); 
        router.attach("/selections/{id}", SelectionResource.class); 
        router.attach("/certificate-requests", CertificateRequestListResource.class); 
        router.attach("/certificate-requests/{id}", CertificateRequestResource.class); 
//        router.attach("certificate-requests/{id}/approval", CertificateRequestApprovalResource.class); 
        router.attach("/certificate-requests/{id}/certificate", CertificateRequestApprovalResource.class); 
        router.attach("/certificates", CertificateListResource.class); 
        router.attach("/certificates/{id}", CertificateResource.class);
        router.attach("/automation", AutomationResource.class);
        router.attach("/configurations", ConfigurationListResource.class); 
        router.attach("/configurations/{id}", ConfigurationResource.class); 
        router.attach("/files", FileListResource.class); 
        router.attach("/files/{id}", FileResource.class); 
        // allows instant editing of html5 resources... set mtwilson.atag.html5.dir in ~/.mtwilson/mtwilson.properties to make this work on your development laptop
//        Directory directory = new Directory(getContext(), "file:///c:/users/jbuhacof/workspace/mtwilson-dev/services/asset-tag-service/src/main/resources/html5/");
        // uses the resources on the classpath (packaged with the application) -- this is the default from MyConfiguration
//        Directory directory = new Directory(getContext(), "clap:///html5/");
        try {
            Directory directory = new Directory(getContext(), My.configuration().getAssetTagHtml5Dir());
            directory.setIndexName("index.html");
            router.attach("/", directory);
        }
        catch(IOException e) {
            log.error("Cannot load configuration", e);
        }
        
        /*
         * TODO:  support X-HTTP-Method-Override header, method=XXXX in the URL, and media type in URL:
getMetadataService().addCommonExtensions(); 
getTunnelService().setEnabled(true); 
getTunnelService().setQueryTunnel(true); 
...
         * 
         */
        ChallengeAuthenticator guard = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "AssetTagDemo");
        // Instantiates a Verifier of identifier/secret couples based on a simple Map.
        MapVerifier mapVerifier = new MapVerifier();
        // Load a single static login/secret pair.
        try {
        mapVerifier.getLocalSecrets().put(My.configuration().getAssetTagApiUsername(),My.configuration().getAssetTagApiPassword().toCharArray());
        }
        catch(IOException e) {
            throw new IllegalStateException("Cannot initialize guard",e);
        }
        guard.setVerifier(mapVerifier);
        // this sets the restlet that is called once authentication is passed.
        guard.setNext(router);
/*        
        return router;
        */
        return guard;
    }
    
}
