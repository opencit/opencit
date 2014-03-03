/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.dcsg.cpg.configuration.MutableConfiguration;
import com.intel.dcsg.cpg.validation.Fault;
import java.util.List;

/**
 * XXX TODO  maybe instead of extending Runnable we need to extend Task from
 * cpg-performance  and get progress info etc. 
 * 
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
 * TODO:  should there be a migration interface, like migrate(Configuration previous, Configuration current) to automatically import
 * data from one to the other?  could be used to migrate between versions or even on the same instance if the administrator changes
 * the configuration then if there is anything that can be migrated it can happen, while other files that are not needed may be 
 * discarded or archived. For example if saml certificate distinguished name is changed, user can choose to keep the old cert as 
 * a revoked cert for verifying assertions made while it was valid, or deleting it. Without the migration interface, the administrator
 * would have to manually revoke and import the old cert or delete it.
 * 
 * TODO:  the conversion between bean attributes and configuration properties that is done by the setup task implementations
 * may be something that is generally useful as an improvement for My.configuration()  to allow the application to get 
 * the necessary configuration directly like X509Certificate getSamlCertificate() instead of getting 4 properties for the 
 * keystore location and password and certificate alias and password within the keystore and then repeating the code for
 * loading it.  The limit on how far the abstraction goes may be defined in different ways:
 * 1. it can be the business layer itself - the configuration interface is
 * only responsible for loading concrete pieces that can be used by the application in any way. So the configuration interface
 * would not be used to load the saml business object itself which signs documents,  but it would be used to load objects
 * that may be set on the saml business object using its setters in order to configure it. 
 * 2. it can be anything in java.lang.  - that is, any object that is part of the JRE can be referenced in the configuration
 * but anything in another package is outside the scope. that makes it easy to draw the line and prevent scope creep into
 * the business layer.
 * 3. configuration can be anything including complete business objects; this is something that has already been solved
 * by the Spring project so it might be good to integrate spring configuration and a generic viewer that can look up and down
 * the object hierarchy to expose configuration at any depth 
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
