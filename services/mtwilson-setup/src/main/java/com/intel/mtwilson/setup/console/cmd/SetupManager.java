/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.console.cmd;

import com.intel.dcsg.cpg.configuration.MutableConfiguration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.console.input.Input;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.dcsg.cpg.util.PascalCaseNamingStrategy;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.launcher.ExtensionCacheLauncher;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.setup.SetupTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;

/**
 * This setup command is a bridge between mtwilson-console and the new
 * mtwilson-setup tasks
 *
 * @author jbuhacoff
 */
public class SetupManager implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetupManager.class);
    private Configuration options = null;

    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }
    
    protected File getConfigurationFile() {
        File file = new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "mtwilson.properties");
        return file;
    }
    
    protected void registerExtensions() {
        // first find all setup tasks
        ExtensionCacheLauncher launcher = new ExtensionCacheLauncher();
        launcher.setRegistrars(new Registrar[]{new ImplementationRegistrar(SetupTask.class)});
        launcher.run(); // loads application jars, scans extension jars for the plugins as specified by getRegistrars()
    }

    @Override
    public void execute(String[] args) throws Exception {
        String hyphenatedTaskName = args.length > 0 ? args[0] : null;

        registerExtensions();
        
        
        // now find the one that the user has asked for
        if( hyphenatedTaskName == null ) {
            execute(getAllSetupTasks());
        }
        else {
            SetupTask task = findSetupTaskByName(hyphenatedTaskName);
            if( task == null ) {
                log.error("Setup task not found: {}", hyphenatedTaskName);
            }
            else {
                execute(task);
            }
        }
        
    }
    
    protected List<SetupTask> getAllSetupTasks() throws IOException {
        // TODO: this way is not good because the tasks would be executed in random order
        // and that is not likely to work.  our options are to either do nothing here
        // and let a subclass define the tasks (in order) to run, or to define a 
        // dependency mechanism so we can take the set of all tasks and create a
        // directed dependency graph, and start executing at the leaves (level 0)
        // and move up executing one level at a time until all tasks have been
        // executed.
        /*
        List<SetupTask> tasks = Extensions.findAll(SetupTask.class);
        for (SetupTask task : tasks) {
            execute(task);
        } 
        */
        return Collections.EMPTY_LIST;
    }
    
    protected SetupTask findSetupTaskByName(String hyphenatedTaskName) throws IOException {
        PascalCaseNamingStrategy namingStrategy = new PascalCaseNamingStrategy();
        String className = namingStrategy.toPascalCase(hyphenatedTaskName);
        List<SetupTask> tasks = Extensions.findAll(SetupTask.class);
        for (SetupTask task : tasks) {
            if (task.getClass().getSimpleName().equals(className)) {
                // found it!
                return task;
            }
        }
        return null;
    }

    protected void execute(SetupTask... tasks) throws IOException {
        execute(Arrays.asList(tasks));
    }
    
    protected void execute(List<SetupTask> tasks) throws IOException {
        PropertiesConfiguration configuration = loadConfiguration();
        for (SetupTask setupTask : tasks) {
            String taskName = setupTask.getClass().getSimpleName();
            setupTask.setConfiguration(configuration);
            if (setupTask.isConfigured()) {
                setupTask.run();
                if (setupTask.isValidated()) {
                    System.out.println("Completed " + taskName);
                } else {
                    System.err.println("Validation error for " + taskName);
                    List<Fault> validationFaults = setupTask.getValidationFaults();
                    for (Fault fault : validationFaults) {
                        System.err.println(fault.toString());
                    }
                }
            } else {
                System.err.println("Configuration error for " + taskName);
                List<Fault> configurationFaults = setupTask.getConfigurationFaults();
                for (Fault fault : configurationFaults) {
                    System.err.println(fault.toString());
                }
            }
        }
        storeConfiguration(configuration);
    }
    
    protected PropertiesConfiguration loadConfiguration() throws IOException {
        // TODO:  need to handle the encrypted configuration file too like My.configuration() does, but using the filesystem location instaed of the looking-everywhere My.configuration() method because we need to have the mtwilson.properties file to write back to from the setup tasks
        File file = getConfigurationFile();
        try(FileInputStream in = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(in);
            PropertiesConfiguration configuration = new PropertiesConfiguration(properties);
            return configuration;
        }
    }
    
    protected void storeConfiguration(PropertiesConfiguration configuration) throws IOException {
        // write the configuration back to disk
        File file = getConfigurationFile();
        try(FileOutputStream out = new FileOutputStream(file)) {
            configuration.getProperties().store(out, "saved by mtwilson setup");
        }
    }
}
