package com.intel.mtwilson.tag.setup.cmd;

///*
// * Copyright (C) 2013 Intel Corporation
// * All rights reserved.
// */
//package com.intel.mtwilson.tag.setup;
//
//import com.intel.mtwilson.My;
////import com.intel.mtwilson.atag.RestletApplication;
////import com.intel.mtwilson.atag.resource.TagResource;
//import java.util.Properties;
//import org.apache.commons.configuration.MapConfiguration;
//
//
//import org.restlet.Component;
//import org.restlet.Server;
//import org.restlet.data.ChallengeScheme;
//import org.restlet.data.Protocol;
//import org.restlet.data.Parameter;
//import org.restlet.security.ChallengeAuthenticator;
//import org.restlet.security.MapVerifier;
//import org.restlet.util.Series;
//
///**
// *
// * @author jbuhacoff
// */
//public class StartHttpServer extends TagCommand {
//    private Component component;
//    private int port = 1700;
//    
//    @Override
//    public void execute(String[] args) throws Exception {
//        if( getOptions().containsKey("port") ) {
//            port = getOptions().getInt("port");
//        }
//        start();
//        // we don't need component.stop() because user will kill process when they are done...
////        new Server(Protocol.HTTP, port, TagResource.class).start();
//        // but junit tests call the stop method when they're done.
//    }
//    
//    public void start() throws Exception {
//        component = new Component();
//        component.getClients().add(Protocol.FILE); // filesystem resources
//        component.getClients().add(Protocol.CLAP); // classpath resources
//        
//        Server server = component.getServers().add(Protocol.HTTPS, port);
//        Series<Parameter> parameters = server.getContext().getParameters();
//        
//        parameters.add("sslContextFactory", "org.restlet.ext.ssl.PkixSslContextFactory");
//        parameters.add("keystorePath", My.configuration().getAssetTagKeyStorePath());
//        parameters.add("keystorePassword", My.configuration().getAssetTagKeyStorePassword());
//        parameters.add("keyPassword", My.configuration().getAssetTagKeyPassword());
//        parameters.add("keystoreType", "JKS");
//        
//        /*
//        // setup the http-basic stuff
//        // Guard the restlet with BASIC authentication.
//        ChallengeAuthenticator guard = new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "AssetTagDemo");
//        // Instantiates a Verifier of identifier/secret couples based on a simple Map.
//        MapVerifier mapVerifier = new MapVerifier();
//        // Load a single static login/secret pair.
//        mapVerifier.getLocalSecrets().put(My.configuration().getAssetTagApiUsername(),My.configuration().getAssetTagApiPassword().toCharArray());
//        guard.setVerifier(mapVerifier);
//        RestletApplication restlet = new RestletApplication();
//        // this sets the restlet that is called once authentication is passed.
//        guard.setNext(restlet);
//        component.getDefaultHost().attach("",guard);
//        */
//        RestletApplication restlet = new RestletApplication();
//        component.getDefaultHost().attach("",restlet);
//        
//        component.start();
//        
//    }
//    
//    public void stop() throws Exception {
//        component.stop();
//    }
// 
//    
// 
//    public static void main(String args[]) throws Exception {
//        StartHttpServer cmd = new StartHttpServer();
//        cmd.setOptions(new MapConfiguration(new Properties()));
//        cmd.execute(new String[0]);
//        
//    }    
//}
