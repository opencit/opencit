/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a registry of available commands which maps command name to implementation
 * class name. When a command is chosen based on its command name, the 
 * RegistryCommandFinder attempts to load the corresponding class and create
 * a new instance. Always creates new command instances - they are not cached.
 * 
 * For example:
 * <code>
 * Map<String,String> availableCommands = loadCommandRegistry(); // not provided
 * RegistryCommandFinder finder = new RegistryCommandFinder(availableCommands);
 * Command toRun = finder.forName("hello-world"); // will search for the registry for an entry with key "hello-world" 
 * </code>
 * 
 * @author jbuhacoff
 * @deprecated use PluginRegistry and PluginRegistryFactory
 */
public class RegistryCommandFinder implements CommandFinder {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Key is command name, like "hello-world"
     * Value is implementation class fully qualified name, like "com.intel.mtwilson.test.HelloWorld"
     */
    private final Map<String,String> registry;
    
    public RegistryCommandFinder(Map<String,String> registry) {
        this.registry = registry;
    }
    
    @Override
    public Command forName(String commandName) {
        if( registry == null ) {
            throw new IllegalStateException("Cannot search for commands until registry is set");
        }
        log.debug("Searching for command name: {}", commandName);
        String fqcn = registry.get(commandName);
        if( fqcn == null || fqcn.isEmpty() ) {
            log.debug("Command not registered: {}", commandName);
            return null;
        }
        try {
            Class commandClass = Class.forName(fqcn); // ClassNotFoundException
            Object commandObject = commandClass.newInstance(); // InstantiationException, IllegalAccessException
            Command command = (Command)commandObject;
            return command;
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.debug("Cannot load command {} from class {}: {}",commandName, fqcn, e.toString());
            return null;
        }
    }
    
}
