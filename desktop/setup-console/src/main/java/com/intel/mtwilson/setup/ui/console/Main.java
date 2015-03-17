/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.ui.console;

import com.intel.dcsg.cpg.console.ExtendedOptions;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.*;
import java.io.Console;
import java.util.Arrays;
import java.util.logging.LogManager;
import org.apache.commons.configuration.Configuration;

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
 * @author jbuhacoff
 * @deprecated use mtwilson-util-console
 */
public class Main {
    public static final Console console = System.console();
    public static final SetupContext ctx = new SetupContext();

    /**
     * Argument 1:  "local" or "remote"  to indicate if we are setting up the local host or if we are setting up a cluster remotely;  case-insensitive
     * @param args 
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Usage: <command> [args]");
                System.exit(1);
            }
            // turn off jdk logging because sshj logs to console
            LogManager.getLogManager().reset();
//        Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);          

            String commandName = args[0];
            try {
                Class commandClass = Class.forName("com.intel.mtwilson.setup.cmd." + commandName);
                Object commandObject = commandClass.newInstance();
                Command command = (Command) commandObject;
                String[] subargs = Arrays.copyOfRange(args, 1, args.length);
//            command.setContext(ctx);
                ExtendedOptions getopt = new ExtendedOptions(subargs);
                Configuration options = getopt.getOptions();
                subargs = getopt.getArguments();
//            command.setContext(ctx);
                command.setOptions(options);
                command.execute(subargs);
            } catch (ClassNotFoundException e) {
                System.err.println("Unrecognized command: " + commandName);
            } /*
             catch(IOException e){
             System.err.println("No console.");
             e.printStackTrace();   
             }
             * */ catch (java.lang.SecurityException e) {
                System.err.println("Securty Exception: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        } catch (SecurityException se) {
            System.err.println(String.format("Security exception while running command %s", (args.length>0?args[0]:"")));
        }
    }
    

}
