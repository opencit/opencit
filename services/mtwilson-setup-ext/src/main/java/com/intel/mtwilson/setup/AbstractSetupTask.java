/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.dcsg.cpg.configuration.MutableConfiguration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Model;
import java.util.ArrayList;
import java.util.List;

/**
 * Subclasses should use getConfiguration() to obtain existing configuration
 * information. Avoid using the "My" class because the application may be trying
 * to run a setup task with an alternative provided configuration and it would
 * be counter-intuitive for configuration to come from somewhere else.
 * Subclasses can write generated configuration properties back to the
 * MutableConfiguration object obtained from getConfiguration() and the
 * application is responsible for saving that into mtwilson.properties after
 * running the setup tasks.
 * It is expected that an application running multiple setup tasks in a 
 * sequence will initialize them all with the same MutableConfiguration 
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
    private transient MutableConfiguration configuration = new PropertiesConfiguration();
    private transient ArrayList<Fault> configurationFaults = new ArrayList<Fault>();
    private transient ArrayList<Fault> validationFaults = new ArrayList<Fault>();

    abstract protected void configure() throws Exception;

    abstract protected void validate() throws Exception;
    
    abstract protected void execute() throws Exception;

    @Override
    public void setConfiguration(MutableConfiguration configuration) {
        this.configuration = configuration;
    }

    public MutableConfiguration getConfiguration() {
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
            throw new ConfigurationException("Configuration required: "+getClass().getName());
        }
        try {
            execute();
        }
        catch(Exception e) {
            log.error("Setup task error: {}", e.getMessage());
            log.debug("Setup task error", e);
            throw new SetupException("Setup error"); // TODO:  add exception as second argument here
        }
    }
    
    // following section is similar to  cpg-validation ObjectModel  ; the only reason we're not extending ObjectModel is that isValid() doesn't make sense for us - we have isConfigured() and isRequired() instead
    
    protected final void configuration(Fault fault) {
        configurationFaults.add(fault);
    }

    protected final void configuration(String description) {
        configurationFaults.add(new Fault(description));
    }
    
    protected final void configuration(String format, Object... args) {
        configurationFaults.add(new Fault(format, args));
    }
    
    protected final void configuration(Throwable e, String description) {
        configurationFaults.add(new Fault(e, description));
    }
    
    protected final void configuration(Throwable e, String format, Object... args) {
        configurationFaults.add(new Fault(e, format, args));
    }
    
    protected final void configuration(Model m, String format, Object... args) {
        configurationFaults.add(new Fault(m, format, args));
    }

    protected final void validation(Fault fault) {
        validationFaults.add(fault);
    }

    protected final void validation(String description) {
        validationFaults.add(new Fault(description));
    }
    
    protected final void validation(String format, Object... args) {
        validationFaults.add(new Fault(format, args));
    }
    
    protected final void validation(Throwable e, String description) {
        validationFaults.add(new Fault(e, description));
    }
    
    protected final void validation(Throwable e, String format, Object... args) {
        validationFaults.add(new Fault(e, format, args));
    }
    
    protected final void validation(Model m, String format, Object... args) {
        validationFaults.add(new Fault(m, format, args));
    }
    
    
}
