/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.threads;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.saml.IssuerConfiguration;
import com.intel.mtwilson.saml.SamlConfiguration;
import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.commons.configuration.Configuration;

/**
 * Initializes the data encryption key and the SAML issuer configuration
 * once when the attestation starts.
 *
 * @author jbuhacoff
 */
@WebListener
public class Attestation implements ServletContextListener {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Attestation.class);
    private static ExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
        log.debug("Initializing ASDataCipher...");
        My.initDataEncryptionKey();
        int maxThreads = ASConfig.getConfiguration().getInt("mtwilson.bulktrust.threads.max", 16);
        log.debug("Creating fixed thread pool with n={}", maxThreads);
        executor = Executors.newFixedThreadPool(maxThreads, new AttestationThreadFactory());
        
        if( IssuerConfigurationHolder.samlIssuerConfiguration == null ) {
            log.error("Failed to initialize SAML issuer");
        }
        log.debug("Creating SamlGenerator with issuer configuration: {}", IssuerConfigurationHolder.samlIssuerConfiguration); // 
        if( IssuerConfigurationHolder.samlIssuerConfiguration != null ) {
            log.debug("Creating SamlGenerator with issuer: {} and validity seconds: {}", IssuerConfigurationHolder.samlIssuerConfiguration.getIssuerName(), IssuerConfigurationHolder.samlIssuerConfiguration.getValiditySeconds());
        }        
        }
        catch(Throwable e) {
            log.error("Failed to initialize attestation service", e);
        }
    }

    /**
     * Uses example code from Java documentation to shut down the executor:
     * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
     *
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.debug("Shutdown thread pool");
        if (executor != null) {
            executor.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        System.err.println("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                executor.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }

    public static ExecutorService getExecutor() {
        return executor;
    }
    
    public static IssuerConfiguration getIssuerConfiguration() {
        return IssuerConfigurationHolder.samlIssuerConfiguration;
    }

    public static class AttestationThreadFactory implements ThreadFactory {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttestationThreadFactory.class);
        private static final AtomicLong sequence = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            log.debug("Creating thread for runnable: {}", r.getClass().getName());
            Thread newThread = new Thread(r, "Attestation-" + sequence.incrementAndGet());
            return newThread;
        }
    }

    private static class IssuerConfigurationHolder {
        private static final IssuerConfiguration samlIssuerConfiguration = loadIssuerConfiguration();
        
        private static IssuerConfiguration loadIssuerConfiguration() {
            try {
                log.debug("loadIssuerConfiguration");
                Configuration configuration = My.configuration().getConfiguration();
                String issuerName = configuration.getString(SamlConfiguration.SAML_ISSUER);
                if( issuerName == null ) {
                    issuerName = configuration.getString("mtwilson.api.url");
                    if( issuerName == null ) {
                        InetAddress localhost = InetAddress.getLocalHost();
                        issuerName = "https://" + localhost.getHostAddress() + ":8443/mtwilson";   // was; 8181/AttestationService       
                        configuration.setProperty(SamlConfiguration.SAML_ISSUER, issuerName);
                    }
                    else {
                        configuration.setProperty(SamlConfiguration.SAML_ISSUER, issuerName);
                    }
                }
                log.debug("loadIssuerConfiguration creating IssuerConfiguration");
                return new IssuerConfiguration(new CommonsConfiguration(configuration));
            }
            catch(IOException | GeneralSecurityException e) {
                throw new IllegalStateException("Cannot load SAML issuer configuration", e);
            }
        }
    }
    
}
