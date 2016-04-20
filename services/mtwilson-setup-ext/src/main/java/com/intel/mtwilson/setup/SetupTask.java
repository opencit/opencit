/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.dcsg.cpg.configuration.MutableConfiguration;
import com.intel.dcsg.cpg.validation.Fault;
import java.util.List;

/**
 * Each implementation of SetupTask is a bean with attributes (setters and getters)
 * that
 * can be used by the application to configure that bean before execution and
 * to learn about its final configuration after execution. 
 * The setters and getters should use specific classes to give a hint to 
 * the application about the type of data the user needs to provide. 
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
 * Update: required was changed to validated so in the above table required = !validated. 
 * 
 * 
 * @author jbuhacoff
 */
public interface SetupTask extends Runnable {
    /**
     * If isValidated is true, the setup task can be skipped unless the user
     * wants to change its configuration and run it again. Note that changing
     * the configuration and running it again will not necessarily undo 
     * side-effects from a previous execution. For example if the task was
     * previously configured to create a file at /path/to/file1 and it is
     * reconfigured with /path/to/file2,  it will create /path/to/file2 but
     * /path/to/file1 will still exist. 
     * 
     * If isValidated is false, the setup task may not have been fully configured
     * or there was a problem during execution (or it hasn't executed yet at all)
     * 
     * When isValidated is false, you can get a list of the issues by calling
     * getValidationFaults().
     * 
     * @return true if the configuration task has been successfully completed
     */
    boolean isValidated(); 
    
    /**
     * If isConfigured is true, the setup task has been fully configured.
     * The user can still change the configuration before running the task.
     * 
     * If isConfigured is false, the setup task requires the user to provide
     * some configuration that cannot be generated automatically. Tasks should
     * provide a default configuration whenever possible.
     * 
     * When isConfigured is false, you can get a list of the configuration 
     * issues by calling getConfigurationFaults().
     * 
     * @return 
     */
    boolean isConfigured(); 
    
    /**
     * 
     * @return a list of configuration faults, or an empty list if there are none; never null
     */
    List<Fault> getConfigurationFaults();

    /**
     * 
     * @return a list of validation faults, or an empty list if there are none; never null
     */
    List<Fault> getValidationFaults();
    
    /**
     * Set the configuration that the setup task should use. The setup task
     * will configure itself using this configuration and will write back
     * any generated configuration. 
     * 
     * @param configuration 
     */
    void setConfiguration(MutableConfiguration configuration);
    
}
