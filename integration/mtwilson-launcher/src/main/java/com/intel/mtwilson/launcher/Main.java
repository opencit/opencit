/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.dcsg.cpg.io.Platform;
import com.intel.dcsg.cpg.performance.AlarmClock;
import com.intel.dcsg.cpg.module.Container;
import com.intel.dcsg.cpg.module.Module;
import com.intel.mtwilson.MyConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * The purpose of the launcher is to provide a deployment-specific environment for the
 * Mt Wilson container. This launcher assumes all Mt Wilson jar files are in a single
 * directory to be loaded by a single classloader. When the container is able to handle
 * multiple classloaders better this would be changed here. 
 * 
 * The launcher requires the following environment variables to be set:
 * MTWILSON_HOME     default /opt/mtwilson on Linux, or user.home\mtwilson on Windows
 * MTWILSON_PASSWORD (if mtwilson.properties is encrypted)
 * MTWILSON_CONF   (probably /etc/intel/cloudsecurity , or /etc/mtwilson)
 * 
 * @author jbuhacoff
 */
public class Main {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Main.class);

    private static final ModuleDirectoryLauncher launcher = new ModuleDirectoryLauncher();
    
    public static void main(String[] args) throws IOException {
        // read environment variable so we know where to find our plugins
        // XXX TODO:  we need to know a few things from environment like MTWILSON_HOME
        // so that we can go and read our core files and extensions using our own
        // classloader... so we can't assume that ANYTHING is on the classpath besides
        // this mtwilson-launcher.jar .... yet that's not realistic because we need
        // the cpg-classpath module on our classpath in order to use the custom
        // classloaders... and mtwilson-config in order to read the encrypted configuration...
        // so what we really need to do is the caller has to set our classpath as
        // everything under /opt/mtwilson/java  and then we'll load extensionsa nd
        // modules etc.  from /opt/mtwilson/java.ext.d 
        // that way we can put all the cpg-* modules and our core mtwilson modules
        // in /opt/mtwilson/java to bootstrap the application.
        MyConfiguration conf = new MyConfiguration();
        String mtwilsonHomePath = conf.getMtWilsonHome();
        
        // make sure MTWILSON_HOME exists
        File mtwilsonHome = new File(mtwilsonHomePath);
        if( !mtwilsonHome.exists() ) {
            if(!mtwilsonHome.mkdirs()) {
                throw new IllegalStateException("Cannot create directory: "+mtwilsonHomePath);
            }
        }
        
        // create directory jar file resolver
        launcher.setDirectory(new File(mtwilsonHomePath + File.separator + "java"));
        // use the only container we have right now
//        launcher.setContainer(new Container()); // don't need to set it unless we develop more than one... right now each launcher instantiates a container by default
        // create container and load modules
        // XXX TODO ... we don't necessarily want to start an event loop here....  only if the command is start-http-server or some other thing that needs
        // to have an event loop.... 
        // what we really need here is 1) use the selected launcher to load all extensions  (core libs already loaded by jvm because of invocation classpath)
        // and 2) a tie in to cpg-console using cpg-extensions to load commands 
        // XXX TODO  to make the console commands quicker (and avoid starting up a jvm and loading extensions on every single command invoked from the console)
        //    we should have a start-console-daemon (or something) command which starts up a service that listens only for requests from 127.0.0.1 
        //    and implements a very simple HTTP protocol which is essnetially an RPC in order for the "mtwilson" command to send requests to it to be executed
        //    immediately;  the "mtwilson" command should start the daemon automatically when a command is run if the daemon is not up yet;  on startup the
        //    daemon should output two things:  1) the random port number it's listening on, and 2) the authorization token to use with every request.  the
        //    "mtwilson" command can then store the token so that it can authenticate to the daemon for every command.  (otherwise it would be a vulnerability
        //    where any local user can execute commands on behalf of the mtwilson admin). the token should have an expiration (use cpg-authz-token) and 
        //    the daemon should automatically issue a new one before it expires. the daemon can exit automatically if it has been idle for some time (maybe
        //    an hour or more as a default).  daemon can auto-generate an internal-use-only ssl certificate or use an existing configured mtwilson ssl cert if available 
        //    
        try {
            launcher.launch();
            // start event loop (block in foreground so http module etc can listen for connections)
            launcher.startEventLoop();
        }
        catch(Exception e) {
            log.error("Cannot launch container", e);
        }
    }
    
    // XXX should this be moved to "mtwilson-my/config" ?
  
}
