/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

//import com.intel.dcsg.cpg.validation.ObjectModel;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subclasses must implement execute() from Command.
 * Any errors encountered inside execute() should be thrown as exceptions.
 * @author jbuhacoff
 */
public abstract class AbstractCommand implements Command {
    protected Logger log = LoggerFactory.getLogger(getClass());
    protected Configuration options = null;
    
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }
    
    public Configuration getOptions() {
        return options;
    }
    
    /**
     * 
     * @param args list of option names that are required
     */
    /*
    protected void requireOptions(String... args) {
        for(String arg : args) {
            String value = options.getString(arg);
            if( value == null ) {
                fault("required option %s is missing", arg);
                continue;
            }
            if( value.isEmpty() ) {
                fault("requierd option %s is empty", arg);
                continue;
            }
        }
    }
    */
}
