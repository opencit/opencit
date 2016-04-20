/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.dcsg.cpg.console.ExtendedOptions;
import com.intel.dcsg.cpg.console.Command;
import java.io.Console;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.LogManager;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jbuhacof
 */
public class TextConsole {
    private static Logger log = LoggerFactory.getLogger(TextConsole.class);
    public static final Console console = System.console();
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Usage: <command> [args]");
                System.exit(1);
            }
            // turn off jdk logging because sshj logs to console
            LogManager.getLogManager().reset();
//        Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);        

            try {
                if (args.length > 0) {
                    String commandName = args[0];
                    Class commandClass = Class.forName("com.intel.mtwilson.setup.cmd." + commandName);
                    Object commandObject = commandClass.newInstance();
                    Command command = (Command) commandObject;
                    String[] subargs = Arrays.copyOfRange(args, 1, args.length);
                    ExtendedOptions getopt = new ExtendedOptions(subargs);
                    Configuration options = getopt.getOptions();
                    subargs = getopt.getArguments();
                    command.setOptions(options);
                    log.debug("Number of args: {}", args.length);
                    for (String arg : args) {
                        log.debug("Arg: {}", arg);
                    }
                    command.execute(subargs);
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Unrecognized command");
                System.exit(2);
            } catch (java.lang.SecurityException e) {
                System.err.println("security exception: " + e.getMessage());
                System.exit(3);
            } catch (SetupException e) {
                e.printStackTrace(System.err);
                System.exit(4);
            } catch (IOException e) {
                System.err.println("No console.");
                e.printStackTrace();
                System.exit(5);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.exit(127);
            }

        } catch (SecurityException se) {
            System.err.println(String.format("Security exception while running command %s", (args.length > 0 ? args[0] : "")));
        }
    }
}
