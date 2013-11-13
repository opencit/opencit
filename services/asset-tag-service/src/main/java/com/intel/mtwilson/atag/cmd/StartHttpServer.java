/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.cmd;

import com.intel.mtwilson.atag.AtagCommand;
import com.intel.mtwilson.atag.RestletApplication;
//import com.intel.mtwilson.atag.resource.TagResource;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;


import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 *
 * @author jbuhacoff
 */
public class StartHttpServer extends AtagCommand {
    private Component component;
    private int port = 1700;
    
    @Override
    public void execute(String[] args) throws Exception {
        if( getOptions().containsKey("port") ) {
            port = getOptions().getInt("port");
        }
        start();
        // we don't need component.stop() because user will kill process when they are done...
//        new Server(Protocol.HTTP, port, TagResource.class).start();
        // but junit tests call the stop method when they're done.
    }
    
    public void start() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, port);
        component.getClients().add(Protocol.FILE); // filesystem resources
        component.getClients().add(Protocol.CLAP); // classpath resources
        component.getDefaultHost().attach("", new RestletApplication()); // if the restlet attaches to "/fruit", this must be "", not "/";  but if the restlet attaches to "fruit", then this can be "/"
        component.start();
    }
    
    public void stop() throws Exception {
        component.stop();
    }
 
    
 
    public static void main(String args[]) throws Exception {
        StartHttpServer cmd = new StartHttpServer();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[0]);
        
    }    
}
