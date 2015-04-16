/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Model;
import com.intel.mtwilson.setup.faults.ConfigurationFault;
import com.intel.mtwilson.setup.faults.ValidationFault;
import com.intel.mtwilson.util.validation.faults.Thrown;
import java.util.ArrayList;
import java.util.List;

/**
 * Subclasses should use getConfiguration() to obtain existing configuration
 * information. Avoid using the "My" class because the application may be trying
 * to run a setup task with an alternative provided configuration and it would
 * be counter-intuitive for configuration to come from somewhere else.
 * Subclasses can write generated configuration properties back to the
 * Configuration object obtained from getConfiguration() and the
 * application is responsible for saving that into mtwilson.properties after
 * running the setup tasks.
 * It is expected that an application running multiple setup tasks in a 
 * sequence will initialize them all with the same Configuration 
 * instance so each step can build on previous steps.
 * 
 * Generated properties that should not be saved into configuration but instead
 * should be only displayed to the user (like generated administrator password)
 * should be stored in private variables accessible with bean setters/getters.
 * The application is responsible for prompting the user for these if necessary
 * and for
 * displaying them if they were generated. 
 * 
 * An application could define an alternate configuration file or format with
 * such display-only parameters to make an install repeatable while making
 * a clear distinction between what should be saved permanently in configuration
 * files and what is simply saved to a file for automation purposes and needs
 * to be removed after setup. Such files should only be guaranteed to be reusable
 * across instances of the same version of mtwilson. 
 * 
 * 
 * @author jbuhacoff
 */
public abstract class AbstractSetupTask implements SetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractSetupTask.class);

//    private transient Integer lastHashCode = null;
    private transient Configuration configuration = new PropertiesConfiguration();
    private transient ArrayList<Fault> configurationFaults = new ArrayList<>();
    private transient ArrayList<Fault> validationFaults = new ArrayList<>();

    /**
     * The application should call isConfigured() before calling isValidated()
     * or run() on the setup task so the configure() method will always be 
     * called before validate() and 
     * before execute() and both validate() and execute() will only be called
     * if there are no configuration errors.
     * 
     * The configure() method should check that all necessary configuration
     * is available (or generate it if possible) and log configuration
     * errors for situations that cannot be fixed automatically or require
     * user input. 
     * 
     * Typically the configure() method will store the configuration in either
     * private member variables for transient settings or into the provided Configuration
     * object itself for persistent settings. Those member variables and
     * saved configuration can then be accessed from validate() and execute().
     * 
     * The job of configure() is to ensure that all the settings needed 
     * in order to complete execute() successfully are present and inform
     * the calling code if they are not present (by logging configuration faults)
     * 
     * @throws Exception 
     */
    abstract protected void configure() throws Exception;

    /**
     * The validate() method relies on configuration prepared by configure()
     * and checks that whatever was supposed to happen in execute() either
     * happened successfully or for any other reason it's not required to
     * call execute(). It logs validation errors if there is any issue which
     * requires further configuration or calling execute().
     * 
     * @throws Exception 
     */
    abstract protected void validate() throws Exception;
    
    /**
     * The execute() method relies on configuration prepared by configure()
     * and is responsible for successful completion of the setup task. If
     * that cannot be accomplished it must throw an exception.
     * @throws Exception 
     */
    abstract protected void execute() throws Exception;

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
    
    
    
    @Override
    public List<Fault> getConfigurationFaults() {
        return configurationFaults;
    }

    @Override
    public List<Fault> getValidationFaults() {
        return validationFaults;
    }
    
    @Override
    public boolean isValidated() {
        try {
            validationFaults.clear();
            validate();
            return validationFaults.isEmpty();
        }
        catch(Exception e) {
            throw new ValidationException(e);
        }
    }

    @Override
    public boolean isConfigured() {
        try {
            configurationFaults.clear();
            configure();
            return configurationFaults.isEmpty();
        }
        catch(Exception e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public void run() {
        if( !isConfigured() ) {
            throw new IllegalStateException("Configuration required");
        }
        try {
            execute();
        }
        catch(Exception e) {
            log.error("Setup task error: {}", e.getMessage());
            log.debug("Setup task error", e);
            throw new SetupException("Setup error", e);
        }
        if( !isValidated() ) {
            throw new IllegalStateException("Validation failed");
        }
    }
    
    // following section is similar to  cpg-validation ObjectModel  ; the only reason we're not extending ObjectModel is that isValid() doesn't make sense for us - we have isConfigured() and isRequired() instead
    
    protected final void configuration(Fault fault) {
        configurationFaults.add(fault);
    }

    protected final void configuration(String description) {
        configurationFaults.add(new ConfigurationFault(description));
    }
    
    protected final void configuration(String format, Object... args) {
        configurationFaults.add(new ConfigurationFault(format, args));
    }
    
    protected final void configuration(Throwable e, String description) {
        configurationFaults.add(new Thrown(e, description));
    }

    protected final void configuration(Throwable e, String format, Object... args) {
        configurationFaults.add(new Thrown(e, format, args));
    }
   
    
    protected final void validation(Fault fault) {
        validationFaults.add(fault);
    }

    protected final void validation(String description) {
        validationFaults.add(new ValidationFault(description));
    }
    
    protected final void validation(String format, Object... args) {
        validationFaults.add(new ValidationFault(format, args));
    }
    
    protected final void validation(Throwable e, String description) {
        validationFaults.add(new Thrown(e, description));
    }
    
    protected final void validation(Throwable e, String format, Object... args) {
        validationFaults.add(new Thrown(e, format, args));
    }
    
    // convenience methods
    /*
    protected void requireNonEmptyString(String key) {
        String value = configuration.getString(key);
        if( value == null || value.isEmpty() ) {
            configuration("Missing required setting: %s", key);
        }
    }
    */
}
