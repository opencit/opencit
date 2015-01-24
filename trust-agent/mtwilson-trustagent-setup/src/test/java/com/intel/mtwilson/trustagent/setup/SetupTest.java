/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.setup.SetupTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import java.util.Properties;

/**
 *
 * @author jbuhacoff
 */
public class SetupTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetupTest.class);

    public static List<SetupTask> getTasks() {
        ArrayList<SetupTask> list = new ArrayList<>();
        list.add(new ConfigureFromEnvironment());
        list.add(new CreateKeystorePassword());
        list.add(new CreateTlsKeypair());
        list.add(new CreateAdminUser());
        list.add(new CreateTpmOwnerSecret());
        list.add(new CreateAikSecret());
        list.add(new TakeOwnership());
        list.add(new DownloadMtWilsonTlsCertificate());
        list.add(new DownloadMtWilsonPrivacyCACertificate());
        list.add(new RequestEndorsementCertificate());
        list.add(new RequestAikCertificate());
        // TODO: register host with Mt Wilson (TBD - requires Mt Wilson to allow registration and setting trust policy as separate steps, which is not yet implemented)
        return list;
    }
    
    // copied from mtwilson-trustagent-console:Setup
    protected File getConfigurationFile() {
        File file = new File(Folders.configuration() + File.separator + "trustagent.properties");
        return file;
    }

    // copied from mtwilson-setup:SetupManager
    protected PropertiesConfiguration loadConfiguration() throws IOException {
        File file = getConfigurationFile();
        try (FileInputStream in = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(in);
            PropertiesConfiguration configuration = new PropertiesConfiguration(properties);
            return configuration;
        }
    }

    // copied from mtwilson-setup:SetupManager
    protected void storeConfiguration(PropertiesConfiguration configuration) throws IOException {
        // write the configuration back to disk
        File file = getConfigurationFile();
        try (FileOutputStream out = new FileOutputStream(file)) {
            configuration.getProperties().store(out, "saved by mtwilson setup");
        }
    }

    @Test
    public void testSetupTasks() throws IOException {
        PropertiesConfiguration configuration = loadConfiguration();
        List<SetupTask> tasks = getTasks();
        for (SetupTask task : tasks) {
            task.setConfiguration(configuration);
            if (task.isConfigured()) {
                log.debug("Running task: {}", task.getClass().getName());
                task.run();
            } else {
                log.error("Configuration errors: {}", task.getConfigurationFaults());

            }
        }
    }

    @Test
    public void testOneSetupTask() throws IOException {
        PropertiesConfiguration configuration = loadConfiguration();
        SetupTask task = new CreateAdminUser();
        task.setConfiguration(configuration);
        if (task.isConfigured()) {
            log.debug("Running task: {}", task.getClass().getName());
            task.run();
        } else {
            log.error("Configuration errors: {}", task.getConfigurationFaults());

        }

    }
}
