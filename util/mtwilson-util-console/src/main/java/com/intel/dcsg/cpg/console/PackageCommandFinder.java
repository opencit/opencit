/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a package to search (packageName) and a command name (commandName), this class will attempt
 * to load packageName.commandName and cast it to a Command interface.
 * 
 * For example:
 * <code>
 * PackageCommandFinder finder = new PackageCommandFinder("test.cmd");
 * Command toRun = finder.forName("HelloWorld"); // will search for test.cmd.HelloWorld
 * </code>
 * 
 * @author jbuhacoff
 * @deprecated use PluginRegistry and PluginRegistryFactory
 */
public class PackageCommandFinder implements CommandFinder {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String packageName;
    public PackageCommandFinder(String packageName) {
        this.packageName = packageName;
    }
    
    @Override
    public Command forName(String commandName) {
        try {
            log.debug("Command package: {}", packageName);
            Class commandClass = Class.forName(packageName+"."+commandName); // ClassNotFoundException
            Object commandObject = commandClass.newInstance(); // InstantiationException, IllegalAccessException
            Command command = (Command)commandObject;
            return command;
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.debug("Cannot load command "+commandName+" in package "+packageName+": "+e.toString());
            return null;
        }
    }
    
}
