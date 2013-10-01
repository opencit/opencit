/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import java.io.Console;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.LogManager;
import org.apache.commons.configuration.Configuration;

/**
 * TODO:  should deprecate this in favor of com.intel.mtwilson.setup.ui.console.Main
 * @author jbuhacof
 */
public class TextConsole extends com.intel.dcg.console.Main {
}
/* 
public class TextConsole {
    public static final Console console = System.console();
    public static final SetupContext ctx = new SetupContext();
    public static void main(String[] args) {
        
        if( args.length == 0 ) {
            System.err.println("Usage: <command> [args]");
            System.exit(1);
        }
        // turn off jdk logging because sshj logs to console
        LogManager.getLogManager().reset();
//        Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);  
//        globalLogger.setLevel(java.util.logging.Level.OFF);          

        try {
            if( args.length > 0 ) {
                String commandName = args[0];
                Class commandClass = Class.forName("com.intel.mtwilson.setup.cmd."+commandName);
                Object commandObject = commandClass.newInstance();
                Command command = (Command)commandObject;
                String[] subargs = Arrays.copyOfRange(args, 1, args.length);
                ExtendedOptions getopt = new ExtendedOptions(subargs);
                Configuration options = getopt.getOptions();
                subargs = getopt.getArguments();
                command.setContext(ctx);
                command.setOptions(options);
                command.execute(subargs);
            }
        }
        catch(ClassNotFoundException e) {
            System.err.println("Unrecognized command");
        }
        catch(SetupException e) {
            e.printStackTrace(System.err);
        }
        catch(IOException e){
            System.err.println("No console.");
            e.printStackTrace();   
        }
        catch(Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
}
*/