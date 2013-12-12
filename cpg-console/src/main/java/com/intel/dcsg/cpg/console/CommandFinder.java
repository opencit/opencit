/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

/**
 * How to use this interface:  
 * 
 * 1. Create a class that implements CommandFinder in your project. Something like this:
 * 
public class MyCommandFinder implements CommandFinder {
    private PackageCommandFinder finder = new PackageCommandFinder("com.intel.my.app.cmd");
    
    @Override
    public Command forName(String commandName) {
        return finder.forName(commandName);
    }
}
 * 
 * 2. Create the file META-INF/services/com.intel.dcg.console.CommandFinder and set its contents like this:
# My application console commands
com.intel.my.app.MyCommandFinder
 * 
 * 3. Set your Main class to com.intel.dcg.console.Main or create your own Main class as an empty subclass of that
 * It will automatically scan the classpath and find your service provider file (META-INF/services/com.intel.dcg.console.CommandFinder) and
 * then call your implementation of CommandFinder to find every command. If your finder returns null it will continue
 * looking with other finders if there are any.  If your finder returns a command then that command will be used.
 * 
 * The PackageCommandFinder implementation is a convenience class you can use when you have placed all your commands
 * in a single package, so you can just provide the package name and it will do the rest - as you can see in the
 * sample code above the forName(String) method simply delegates to the PackageCommandFinder that has been initialized
 * with the package name in the example application that contains the commands.
 * 
 * @author jbuhacoff
 */
public interface CommandFinder {
    Command forName(String commandName);
}
