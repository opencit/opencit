/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.setup.ConfigurationException;
import com.intel.mtwilson.setup.SetupTask;
import com.intel.mtwilson.setup.ValidationException;
import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class SetupTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetupTest.class);

    private List<SetupTask> getSetupTasks() {
        ArrayList<SetupTask> tasks = new ArrayList<>();
        tasks.add(new ConfigureFilesystem());
        tasks.add(new CreateMtWilsonPropertiesFile());
        tasks.add(new CreateCertificateAuthorityKey());
        CreateTlsCertificate createTlsCertificate = new CreateTlsCertificate();
        createTlsCertificate.setDnsAlternativeName("localhost");
        createTlsCertificate.setIpAlternativeName("127.0.0.1");
//        createTlsCertificate.setTlsKeystorePassword("password"); // or use My.configuration()...
        tasks.add(createTlsCertificate);
        CreateSamlCertificate createSamlCertificate = new CreateSamlCertificate();
//        createSamlCertificate.setSamlKeystorePassword("password");// or use My.configuration()...
        tasks.add(createSamlCertificate);
        tasks.add(new ConfigureDatabase());
        tasks.add(new InitDatabase());
        
        // see also CreateAdminUser in mtwilson-shiro-setup
        
//        tasks.add(new MigrateUsers());
        
        return tasks;
    }
    
    @Test
    public void testLastSetupTask() {
        List<SetupTask> tasks = getSetupTasks();
        ArrayList<SetupTask> last = new ArrayList<SetupTask>();
        last.add(tasks.get(tasks.size()-1));
        runSetupTasks(last);
    }
    
    @Test
    public void testAllSetupTasks() {
        List<SetupTask> tasks = getSetupTasks();
        runSetupTasks(tasks);
    }
    
    @Test
    public void testPreviewAllSetupTasks() throws Exception {
        List<SetupTask> tasks = getSetupTasks();
        previewSetupTasks(tasks);
    }
        
    public void runSetupTasks(List<SetupTask> tasks) {
        XStream xs = new XStream();
        for(SetupTask task : tasks) {
            try {
            if( task.isConfigured() && task.isValidated() ) {
                log.debug("nothing to do for {}", task.getClass().getName());
            }
            else if( !task.isConfigured() ) {
                for(Fault fault : task.getConfigurationFaults()) {
                    log.debug("configuration: {}", fault.toString());
                }
            }
            else if( task.isConfigured() && !task.isValidated() ) {
                task.run();
                log.debug("after run: setup task {} configured? {} validated? {}", task.getClass().getName(), task.isConfigured(), task.isValidated());
                for(Fault fault : task.getConfigurationFaults()) {
                    log.debug("configuration: {}", fault.toString());
                }
                for(Fault fault : task.getValidationFaults()) {
                    log.debug("validation: {}", fault.toString());
                }
                // display the task configuration -- this is important for tasks that may generate some default configuration or generate random passwords etc.   a GUI might look for specific tasks that are known to generate passwords ( TODO - should we have a marker interface for this? ) and show the generated passwords to the admin
                String xml = xs.toXML(task);
                log.info("{}", xml);
                
            }
            else {
                log.error("unexpected condition setup task {} configured? {} validated? {}", task.getClass().getName(), task.isConfigured(), task.isValidated());
            }
            }
            catch(ConfigurationException e) {
                log.debug("configuration error {}", e.getMessage());
                for(Fault fault : task.getConfigurationFaults()) {
                    log.debug("configuration: {}", fault.toString());
                }
            }
            catch(ValidationException e) {
                log.debug("validation error {}", e.getMessage());
                for(Fault fault : task.getValidationFaults()) {
                    log.debug("validation: {}", fault.toString());
                }
            }
            catch(Exception e) {
                log.debug("runtime error", e);
            }
        }
    }

    public void previewSetupTasks(List<SetupTask> tasks) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        for(SetupTask task : tasks) {
            if( task.isConfigured() && task.isValidated() ) {
                log.debug("nothing to do for {}", task.getClass().getName());
                log.debug("configured {}: {}", task.getClass().getName(), mapper.writeValueAsString(task));
            }
            else if( !task.isConfigured() ) {
                for(Fault fault : task.getConfigurationFaults()) {
                    log.debug("configuration: {}", fault.toString());
                }
            }
            else if( task.isConfigured() && !task.isValidated() ) {
                log.debug("Task to run: {}", task.getClass().getName());
            }
            else {
                log.error("unexpected condition setup task {} configured? {} validated? {}", task.getClass().getName(), task.isConfigured(), task.isValidated());
            }
        }
    }

}
