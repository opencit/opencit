/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

//import com.intel.mtwilson.setup.*;
//import com.intel.mtwilson.setup.cmd.*;
//import com.intel.mtwilson.setup.model.*;
import java.io.Console;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.LogManager;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple console program to obtain user preferences for either a local or
 * remote Mt Wilson configuration. 
 * 
 * The purpose of this program is to obtain and validate user input only - 
 * network access is allowed for input validation (server addresses, database
 * connections, etc) but not to effect any changes. Also this program should not
 * effect any changes on the local host.
 * 
 * The output of this program is a complete "mtwilson.properties" file in
 * the current directory. It can then be used to configure the local instance
 * (using another command) or copied to another server and used there.
 * 
 * How to run it for local setup:
 * java -cp setup-console-0.5.4-SNAPSHOT-with-dependencies.jar com.intel.mtwilson.setup.ui.console.Main local  (will not be used?? setup tool is premium and if it's going to be used it's likely to be used in remote mode, since existing installer already does local setup)
 * (the local argument is optional - default is local)
 * 
 * How to run it for remote setup of installed instance:
 * java -cp setup-console-0.5.4-SNAPSHOT-with-dependencies.jar com.intel.mtwilson.setup.ui.console.Main remote
 * 
 * How to run it for creating a configuration template to apply to selected remote instances:
 * java -cp setup-console-0.5.4-SNAPSHOT-with-dependencies.jar com.intel.mtwilson.setup.ui.console.Main cluster   (not implemented yet)
 * 
 * TODO: Currently the program prompts user to confirm the host key. This is ok, but
 * can be improved by allowing user to set known hosts file (maybe through environment variable)
 * or to check the windows registry (when running on windows) for the Putty known_hosts information.
 * See also: 
 * http://www.davidc.net/programming/java/reading-windows-registry-java-without-jni
 * http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 * http://superuser.com/questions/197489/where-does-putty-store-known-hosts-information-on-windows
 * http://kobowi.co.uk/blog/2011/08/convert-winscpputty-ssh-host-keys-to-known_hosts-format/
 * https://bitbucket.org/kobowi/reg2kh/  (python tool that exports putty and winscp registry keys to a known hosts file format)
 * 
 * @author jbuhacoff
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static final Console console = System.console();
//    public static final SetupContext ctx = new SetupContext();
    
    public static Command findCommand(String commandName) {
        Iterator<CommandFinder> finders = ServiceLoader.load(CommandFinder.class).iterator();
        while(finders.hasNext()) {
            try {
                CommandFinder finder = finders.next();
                Command command = finder.forName(commandName);
                if( command != null ) {
                    return command;
                }
            }
            catch(ServiceConfigurationError e) {
                log.error(e.toString());
            }
        }
        return null;
    }

    /**
     * Argument 1:  "local" or "remote"  to indicate if we are setting up the local host or if we are setting up a cluster remotely;  case-insensitive
     * @param args 
     */
    public static void main(String[] args) {

        if( args.length == 0 ) {
            System.err.println("Usage: <command> [args]");
            System.exit(1);
        }
        // turn off jdk logging because sshj logs to console
        LogManager.getLogManager().reset();
//        Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);  
//        globalLogger.setLevel(java.util.logging.Level.OFF);          
        
        String commandName = args[0];
        try {
            Command command = findCommand(commandName);
            if( command == null ) {
                System.err.println("Unrecognized command: "+commandName);                
            }
            else {
                String[] subargs = Arrays.copyOfRange(args, 1, args.length);
    //            command.setContext(ctx);
                ExtendedOptions getopt = new ExtendedOptions(subargs);
                Configuration options = getopt.getOptions();
                subargs = getopt.getArguments();
    //            command.setContext(ctx);
                command.setOptions(options);
                command.execute(subargs);
            }
        }
        catch(ClassNotFoundException e) {
        }
        catch(IOException e){
            System.err.println("No console.");
            e.printStackTrace(System.err);   
        }
        catch(Exception e) {
            e.printStackTrace(System.err);
        }
        
    }
    

}
