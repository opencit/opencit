/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.console.cmd;

import com.intel.dcsg.cpg.configuration.EnvironmentConfiguration;
import com.intel.dcsg.cpg.configuration.KeyTransformerConfiguration;
import com.intel.dcsg.cpg.configuration.MutableCompositeConfiguration;
import com.intel.dcsg.cpg.configuration.MutableConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.console.input.Input;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.dcsg.cpg.util.PascalCaseNamingStrategy;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.launcher.ExtensionCacheLauncher;
import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.util.AllCapsNamingStrategy;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.setup.ConfigurationException;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.SetupTask;
import com.intel.mtwilson.setup.ValidationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

/**
 * This setup command is a bridge between mtwilson-console and the new
 * mtwilson-setup tasks
 *
 * @author jbuhacoff
 */
public class SetupManager implements Command {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetupManager.class);
    private org.apache.commons.configuration.Configuration options = null;

    @Override
    public void setOptions(org.apache.commons.configuration.Configuration options) {
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

        registerExtensions();

        // now find the setup tasks that the user has asked for or use a default set
        if (args.length == 0) {
            execute(getAllSetupTasks());
        } else {
            execute(getSetupTasksByName(args));
        }

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

    protected Map<String, String> getConversionMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("mtwilson", "MtWilson"); // so a name like download-mtwilson-tls-certificate will be translated to DownloadMtWilsonTlsCertificate instead of DownloadMtwilsonTlsCertificate (notice the 'w' in MtWilson)
        map.put("privacyca", "PrivacyCA");
        return map;
    }

    protected SetupTask findSetupTaskByName(String hyphenatedTaskName) throws IOException {
        PascalCaseNamingStrategy namingStrategy = new PascalCaseNamingStrategy(getConversionMap());
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
        PropertiesConfiguration properties = loadConfiguration();
        Configuration env = new KeyTransformerConfiguration(new AllCapsNamingStrategy(), new EnvironmentConfiguration()); // transforms mtwilson.ssl.cert.sha1 to MTWILSON_SSL_CERT_SHA1 
        MutableCompositeConfiguration configuration = new MutableCompositeConfiguration(properties, env);
        try {
            for (SetupTask setupTask : tasks) {
                String taskName = setupTask.getClass().getSimpleName();
                setupTask.setConfiguration(configuration);
                try {
                    setupTask.run(); // calls isConfigured and isValidated automatically and throws ConfigurationException, SetupException, or ValidationException on error
                } catch (ConfigurationException e) {
                    log.error("Configuration error for {}: {}", taskName, e.getMessage());
                    System.err.println("Configuration error for " + taskName);
                    List<Fault> configurationFaults = setupTask.getConfigurationFaults();
                    for (Fault fault : configurationFaults) {
                        System.err.println(fault.toString());
                    }
                } catch (ValidationException e) {
                    log.error("Validation error for {}: {}", taskName, e.getMessage());
                    System.err.println("Validation error for " + taskName);
                    List<Fault> validationFaults = setupTask.getValidationFaults();
                    for (Fault fault : validationFaults) {
                        System.err.println(fault.toString());
                    }
                } catch (SetupException e) {
                    log.error("Runtime error for {}: {}", taskName, e.getMessage());
                    log.debug("Runtime error for {}", taskName, e); // debug stack trace
                    Throwable cause = e.getCause();
                    System.err.println("Runtime error for " + taskName + ": " + (cause == null ? e.getMessage() : cause.getMessage()));
                }
            }
        } catch (Exception e) {
            log.debug("Setup error", e);
            System.err.println("Setup error: " + e.getMessage());
        }
        storeConfiguration(properties);
    }

    protected PropertiesConfiguration loadConfiguration() throws IOException {
        // TODO:  need to handle the encrypted configuration file too like My.configuration() does, but using the filesystem location instaed of the looking-everywhere My.configuration() method because we need to have the mtwilson.properties file to write back to from the setup tasks
        File file = getConfigurationFile();
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(in);
                PropertiesConfiguration configuration = new PropertiesConfiguration(properties);
                return configuration;
            }
        }
        return new PropertiesConfiguration();
    }

    protected void storeConfiguration(PropertiesConfiguration configuration) throws IOException {
        // write the configuration back to disk
        File file = getConfigurationFile();
        try (FileOutputStream out = new FileOutputStream(file)) {
            configuration.getProperties().store(out, "saved by mtwilson setup");
        }
    }
}
