/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client;

import java.util.Arrays;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacof
 */
public class TextConsole {
//    private static Logger log = LoggerFactory.getLogger(TextConsole.class);
    
    public static void main(String[] args) {
        try {
            if( args.length > 0 ) {
                String commandName = args[0];
                Class commandClass = Class.forName("com.intel.mtwilson.client.cmd."+commandName);
                Object commandObject = commandClass.newInstance();
                Command command = (Command)commandObject;
                String[] subargs = Arrays.copyOfRange(args, 1, args.length);
                command.execute(subargs);
            }
        }
        catch(ClassNotFoundException e) {
            System.err.println("Unrecognized command: "+args[0]+": "+e.getLocalizedMessage()); // ClassNotFoundException can only happen if args.length > 0
        }
        catch(Exception e) {
            e.printStackTrace(System.err);
        }
    }

}
