/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Model;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public abstract class AbstractSetupTask implements SetupTask {
//    private transient Integer lastHashCode = null;
    private transient ArrayList<Fault> configurationFaults = new ArrayList<Fault>();
    private transient ArrayList<Fault> validationFaults = new ArrayList<Fault>();

    abstract protected void configure() throws Exception;

    abstract protected void validate() throws Exception;
    
    abstract protected void execute() throws Exception;
    
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
            throw new SetupException("Setup error", e);
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
