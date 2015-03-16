/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.console.cmd;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.text.transform.PascalCaseNamingStrategy;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.My;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.setup.SetupConfigurationProvider;
import com.intel.mtwilson.setup.ConfigurationException;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.SetupTask;
import com.intel.mtwilson.setup.ValidationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * This setup command is a bridge between mtwilson-console and the new
 * mtwilson-setup tasks
 *
 * Usage:  setup [--force] [task1 task2 ... taskN]
 * 
 * Specifying task names is optional; you can specify one or more tasks 
 * as individual arguments to be run. If you don't specify tasks then all
 * tasks will be run.
 * 
 * If a task is already configured and validated it will be skipped, 
 * unless you provide the --force option which will run tasks even if
 * they are already validated.
 * 
 * If you want only to validate the configuration but not execute any of 
 * the tasks,  use the --noexec option.
 * @since 3.0
 * @author jbuhacoff
 */
public class Setup implements Command {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Setup.class);
    private org.apache.commons.configuration.Configuration options = null;

    @Override
    public void setOptions(org.apache.commons.configuration.Configuration options) {
        this.options = options;
    }
    
    /**
     * This method returns false by default with the assumption that
     * if a setup task has already been completed the administrator
     * wants to skip it.
     * 
     * @return default false
     */
    protected boolean isForceEnabled() {
        return options.getBoolean("force", false);
    }
    
    /**
     * This method returns true by default with the assumption that
     * the administrator WANTS to run the setup tasks.  To do a 
     * dry run (configure and validate but no execution) use the
     * @{code --no-exec} option to disable execution.
     * @return default true
     */
    protected boolean isExecEnabled() {
        return options.getBoolean("exec", true);
    }
    /**
     * This method returns false by default 
     * with the assumption that setup tasks
     * are ordered and that later tasks depend on the results of earlier tasks;
     * therefore if an earlier task fails the entire set should stop because
     * trying subsequent tasks will likely only generate precondition 
     * errors that are 
     * distracting from the root cause.
     * 
     * @return default false
     */
    protected boolean continueAfterRuntimeException() {
        return options.getBoolean("continue", false);
    }

    protected File getConfigurationFile() {
//        Filesystem fs = new Filesystem();
//        return fs.getConfigurationFile();
        return My.configuration().getConfigurationFile();
    }

    /**
     * Optional arguments are the task names to execute; if not provided then
     * all available tasks will be executed in a pre-defined order (see
     * SetupTaskFactory invoked by subclasses of this command)
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void execute(String[] args) throws Exception {

        // now find the setup tasks that the user has asked for or use a default set
        if (args.length == 0) {
            log.error("One or more tasks must be specified");
//            execute(getAllSetupTasks());
            return;
        }
        
        execute(getSetupTasksByName(args));

    }

    protected List<SetupTask> getSetupTasksByName(String[] names) throws IOException {
        ArrayList<String> tasksNotFound = new ArrayList<>();
        ArrayList<SetupTask> tasks = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            String hyphenatedTaskName = names[i];
            SetupTask task = findSetupTaskByName(hyphenatedTaskName);
            if (task == null) {
                log.error("Setup task not found: {}", hyphenatedTaskName);
                tasksNotFound.add(hyphenatedTaskName);
            } else {
                tasks.add(task);
            }

        }
        if (!tasksNotFound.isEmpty()) {
            throw new IOException("Unknown tasks: " + StringUtils.join(tasksNotFound, ", "));
        }
        return tasks;
    }

    protected List<SetupTask> getAllSetupTasks() throws IOException {
        /*
         List<SetupTask> tasks = Extensions.findAll(SetupTask.class);
         for (SetupTask task : tasks) {
         execute(task);
         } 
         */
        return Collections.EMPTY_LIST;
    }

    protected Map<String, String> getConversionMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("mtwilson", "MtWilson"); // so a name like download-mtwilson-tls-certificate will be translated to DownloadMtWilsonTlsCertificate instead of DownloadMtwilsonTlsCertificate (notice the 'w' in MtWilson)
        map.put("ca", "CA"); // privacy-ca becomes PrivacyCA, endorsement-ca becomes EndorsementCA, etc.
        return map;
    }

    protected SetupTask findSetupTaskByName(String hyphenatedTaskName) throws IOException {
        PascalCaseNamingStrategy namingStrategy = new PascalCaseNamingStrategy(getConversionMap());
        String className = namingStrategy.toPascalCase(hyphenatedTaskName);
        log.debug("Setup findSetupTaskByName: {} -> {}", hyphenatedTaskName, className);
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

    public void execute(List<SetupTask> tasks) throws IOException {
        SetupConfigurationProvider provider = new SetupConfigurationProvider(ConfigurationFactory.getConfigurationProvider());
        Configuration configuration = provider.load();
//        Configuration configurationAdapter =  new CommonsConfiguration(configuration);
//        Configuration env = new KeyTransformerConfiguration(new AllCapsNamingStrategy(), new EnvironmentConfiguration()); // transforms mtwilson.ssl.cert.sha1 to MTWILSON_SSL_CERT_SHA1 
//        MutableCompositeConfiguration configuration = new MutableCompositeConfiguration(properties, env);
        boolean error = false;
        try {
            for (SetupTask setupTask : tasks) {
                String taskName = setupTask.getClass().getSimpleName();
                setupTask.setConfiguration(configuration);
//                log.debug("set tpm owner password {} for task {}", properties.getString("tpm.owner.secret"), taskName);
                try {
                    if( setupTask.isConfigured() && setupTask.isValidated() && !isForceEnabled() ) {
                        log.debug("Skipping {}", taskName);
                    }
                    else if( !isExecEnabled() ) {
                        // only show validation errors, don't run the task
                        List<Fault> configurationFaults = setupTask.getConfigurationFaults();
                        for (Fault fault : configurationFaults) {
                            log.warn("Configuring {}: {}", taskName, fault.toString());
                        }
                        List<Fault> validationFaults = setupTask.getValidationFaults();
                        for (Fault fault : validationFaults) {
                            log.warn("Validating {}: {}", taskName, fault.toString());
                        }                        
                    }
                    else {
                        for (Fault fault : setupTask.getConfigurationFaults()) {
                            log.debug("Configuration check in {}: {}", taskName, fault.toString());
                        }
                        for (Fault fault : setupTask.getValidationFaults()) {
                            log.debug("Validation check in {}: {}", taskName, fault.toString());
                        }
                        
                        log.debug("Running {}", taskName);
                        try {
                            setupTask.run(); // calls isConfigured and isValidated automatically and throws ConfigurationException, SetupException, or ValidationException on error
                        }
                        catch(IllegalStateException e) {
                            error = true;
                            log.error("Error from {}: {}", taskName, e.getMessage());
                            List<Fault> configurationFaults = setupTask.getConfigurationFaults();
                            for (Fault fault : configurationFaults) {
                                log.warn("Configuring {}: {}", taskName, fault.toString());
                            }
                            List<Fault> validationFaults = setupTask.getValidationFaults();
                            for (Fault fault : validationFaults) {
                                log.warn("Validating {}: {}", taskName, fault.toString());
                            }
                            if( !continueAfterRuntimeException() ) {
                                break;
                            }
                        }
                    }
                } catch (ConfigurationException e) {
                    error = true;
                    log.error("Cannot configure {}: {}", taskName, e.getMessage());
                    log.debug("Configuration error", e);
                } catch (ValidationException e) {
                    error = true;
                    log.error("Cannot validate {}: {}", taskName, e.getMessage());
                    log.debug("Validation error", e);
                } catch (SetupException e) {
                    error = true;
                    log.error("Cannot run {}: {}", taskName, e.getMessage());
                    log.debug("Runtime error", e); // debug stack trace
                }
            }
        } catch (Exception e) {
            error = true;
            log.error("Setup error: {}", e.getMessage());
            log.debug("Setup error", e);
        }
        
        provider.save(configuration);
        
        if( error ) {
            // the main application (cpg-console Main) will print this
            // message and return a non-zero exit code
            throw new IllegalStateException("Encountered errors during setup");
        }
    }
    
    
}
