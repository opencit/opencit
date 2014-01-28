/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.dcsg.cpg.validation.Fault;
import java.io.IOException;
import java.util.List;

/**
 * XXX TODO  maybe instead of extending Runnable we need to extend Task from
 * cpg-performance  and get progress info etc. 
 * 
 * Recommended application actions:
 * 
 * required && configured:   show current configuration, allow user to change if desired, then run
 * 
 * required && !configured:  show current configuration, highlight what must be configured that isn't already, then run (well, re-evaluate and run if it's configured)
 * 
 * !required && configured:  show current configuration, note that it seems to already be set up
 * 
 * !required && !configured: show current configuration, note that it seems to already be set up but may be missing some configuration anyway
 * 
 * @author jbuhacoff
 */
public interface SetupTask extends Runnable {
    // validate current status - is there anything that needs to be done or is this component setup ok?
    boolean isValidated(); // true if task should be run because some piece of setup is missing or broken; false if the task is optional or seems to have already been done before
    // validate input (does user need to provide any input or do we have everything we need?)
    // (this does not include checking if defaults were changed -- the ui can see them and show the user)
    boolean isConfigured(); // true if all required inputs are known; false if we need user input for something
    
//    void configure();
    
    List<Fault> getConfigurationFaults();
    
    List<Fault> getValidationFaults();
    
}
