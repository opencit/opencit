/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import java.util.Arrays;

/**
 *
 * @author jbuhacof
 */
public class TextConsole {
    public static void main(String[] args) {
        try {
            if( args.length > 0 ) {
                String commandName = args[0];
                Class commandClass = Class.forName("com.intel.mtwilson.setup.cmd."+commandName);
                Object commandObject = commandClass.newInstance();
                Command command = (Command)commandObject;
                String[] subargs = Arrays.copyOfRange(args, 1, args.length);
                command.execute(subargs);
            }
        }
        catch(ClassNotFoundException e) {
            System.err.println("Unrecognized command");
        }
        catch(SetupException e) {
            e.printStackTrace(System.err);
        }
        catch(Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
}
