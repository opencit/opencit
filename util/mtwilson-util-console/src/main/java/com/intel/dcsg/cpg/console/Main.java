/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

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
 * Use the Main class in mtwilson-launcher instead.
 * @author jbuhacoff
 */
@Deprecated
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static final Console console = System.console();

    public static Command findCommand(String commandName) {
        Iterator<CommandFinder> finders = ServiceLoader.load(CommandFinder.class).iterator();
        while (finders.hasNext()) {
            try {
                CommandFinder finder = finders.next();
                log.trace("Querying CommandFinder: {}", finder.getClass().getName());
                Command command = finder.forName(commandName);
                if (command != null) {
                    return command;
                }
            } catch (ServiceConfigurationError e) {
                log.error(e.toString());
            }
        }
        return null;
    }
    
    private String[] args;
    
    public void setArgs(String[] args) {
        this.args = args;
        if (log.isTraceEnabled()) {
            log.trace("Number of args: {}", args.length);
            for (String arg : args) {
                log.trace("Arg: {}", arg);
            }
        }
    }

    public String[] getArgs() {
        return args;
    }
    
    
    
    public String getDefaultCommand() { return null; }
    
    public String getCommandName() {
        if( args == null || args.length == 0 ) {
            return getDefaultCommand();
        }
        return args[0];
    }

    /**
     * @param args comprised of command name followed by arguments for that
     * command
     */
    public static void main(String[] args) {
        try {
            Main main = new Main();
            main.setArgs(args);
            String commandName = main.getCommandName();
            
            if (commandName == null) {
                log.error("Usage: <command> [args]");
                System.exit(1);
            }
            // turn off jdk logging because sshj logs to console
            LogManager.getLogManager().reset();
//        Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);  
//        globalLogger.setLevel(java.util.logging.Level.OFF);          

            try {
                Command command = findCommand(commandName);
                if (command == null) {
                    log.error("Unrecognized command: " + commandName);
                    System.exit(1);
                } else {
                    String[] subargs = Arrays.copyOfRange(args, 1, args.length);
                    //            command.setContext(ctx);
                    ExtendedOptions getopt = new ExtendedOptions(subargs);
                    Configuration options = getopt.getOptions();
                    subargs = getopt.getArguments();
                    //            command.setContext(ctx);
                    command.setOptions(options);
                    command.execute(subargs);
                    System.exit(0);
                }
            } catch (Exception e) {
                if (e.getMessage() == null) {
                    log.error("Error while executing {}: {}", commandName, e.getClass().getName());
                } else {
                    log.error("Error while executing {}: {}", commandName, e.getMessage());
                }
                log.debug("Error while executing {}", commandName, e);
                System.exit(2);
            }
        } catch (Throwable t) {
            log.error("Error while executing command: {}", (t.getMessage() == null ? t.getMessage() : t.getClass().getName()));
            log.debug("Error while executing command", t);
            System.exit(3);
        }

    }
}
