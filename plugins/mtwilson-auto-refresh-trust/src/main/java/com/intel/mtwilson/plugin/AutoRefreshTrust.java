/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.plugin;

import com.intel.mtwilson.as.business.trust.BulkHostTrustBO;
import com.intel.mtwilson.as.controller.TblSamlAssertionJpaController;
import com.intel.mtwilson.plugin.api.Plugin;
import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import com.intel.mtwilson.My;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.persistence.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Required configuration:
 * 1. enabled - true if the trust status of all hosts should be refreshed automatically
 * 2. max time in cache (seconds) - if a host trust record is older than this number it will be automatically refreshed
 * 
 * This bean should be executed periodically (once every minute, or every 5 minutes) in order to automatically
 * refresh the trust status
 * 
 * @author jbuhacoff
 */
@WebListener
public class AutoRefreshTrust implements ServletContextListener, Runnable, Plugin {
    private Logger log = LoggerFactory.getLogger(getClass());
    private boolean enabled = true;
    private long maxCacheDuration = 5; // hour
    private TimeUnit maxCacheDurationUnits = TimeUnit.MINUTES;
    private int refreshTimeBeforeSamlExpiry = 300; // seconds
    private long timeout = 120; // seconds
    private TimeUnit timeoutUnits = TimeUnit.SECONDS;
    private BulkHostTrustBO bulkHostTrustBO = null;
    private TblSamlAssertionJpaController samlJpa = null;
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setMaxCacheDuration(long maxCacheDuration) { this.maxCacheDuration = maxCacheDuration; }
    public void setMaxCacheDurationUnits(TimeUnit maxCacheDurationUnits) { this.maxCacheDurationUnits = maxCacheDurationUnits; }
    public void setTimeout(long timeout) { this.timeout = timeout; }
    public void setTimeoutUnits(TimeUnit timeoutUnits) { this.timeoutUnits = timeoutUnits; }
    public void setBulkHostTrustBO(BulkHostTrustBO bulkHostTrustBO) { this.bulkHostTrustBO = bulkHostTrustBO; }
    public void setTblSamlAssertionJpaController(TblSamlAssertionJpaController samlJpa) { this.samlJpa = samlJpa; }
    Thread mainThread;
    private volatile boolean running;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("AutoRefreshTrust: About to start the thread");
        mainThread = new Thread(this);
        mainThread.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("AutoRefreshTrust: About to end the thread");
        running = false;
        mainThread.interrupt();        
    }
    
    @Override
    public void run() {
        running = true;
        while (running) {
            // make a list of hosts whose last trust status check is more than max cache duration ago
            List<String> hostsToRefresh = findHostnamesWithExpiredCache();
            if (hostsToRefresh != null && hostsToRefresh.size() > 0) {
                log.info("AutoRefreshTrust got {} hosts to refresh", hostsToRefresh.size());
                HashSet<String> hosts = new HashSet<>(hostsToRefresh);
                long bulkBOTimeout = My.configuration().getConfiguration().getLong("mtwilson.ms.registration.hostTimeout", timeout); // Default is 60 seconds
                bulkHostTrustBO = new BulkHostTrustBO((int)bulkBOTimeout);
                String saml = bulkHostTrustBO.getBulkTrustSaml(hosts, true);
                log.info("Auto bulk refresh SAML: {}", saml);
            } else {
                log.info("AutoRefreshTrust: No hosts for bulk refresh");
            }
            try {
                long sleepInterval = My.configuration().getConfiguration().getLong("mtwilson.auto.refresh.trust.interval.seconds", timeout);
                if (sleepInterval == 0) {
                    // If the user sets the auto refresh interval to 0, then stop the thread
                    running = false;
                    log.info("AutoRefreshTrust: User has set the refresh interval to {} seconds. So, stopping the auto refresh thread.", sleepInterval);
                } else {
                    log.info("AutoRefreshTrust: Auto refresh thread would sleep for {} seconds.", sleepInterval);
                    Thread.sleep(sleepInterval*1000);
                }
            } catch (InterruptedException ex) {
                log.info("AutoRefreshTrust: Error during waiting for the next process");
            }
        }
    }
    
    public List<String> findHostnamesWithExpiredCache() {
        try {
            log.info("AutoRefreshTrust: findHostnamesWithExpiredCache");
            samlJpa = My.jpa().mwSamlAssertion();
            // To find the list of hosts which would have their SAML getting expired, we calculate what is the earliest create date for which the SAML would expire
            // and also add a buffer time of about 5 min so that we might get to processing the host before it actually expires.
            Query query = samlJpa.getEntityManager().createNativeQuery("SELECT h.Name FROM mw_hosts as h WHERE NOT EXISTS ( SELECT ID FROM mw_saml_assertion as t WHERE h.ID = t.host_id AND t.created_ts > ? )");
            Calendar maxCache = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
            String currentTime = sdf.format(maxCache.getTime());
            Integer expiryInSeconds = My.configuration().getSamlValidityTimeInSeconds() - refreshTimeBeforeSamlExpiry;
            maxCache.add(Calendar.SECOND, -expiryInSeconds);
            log.info("AutoRefreshTrust: Query hosts whose SAML was created after {}. Current time is {}.", sdf.format(maxCache.getTime()), currentTime);
            query.setParameter(1, maxCache);
            List<String> results = query.getResultList();
            return results;
        } catch (Exception ex) {
            log.error("AutoRefreshTrust: Error during query of host names.", ex);
            return null;
        }
    }

    public static class ExpiredHostStatus {
        String hostname;
        Date lastChecked; // alwasys more than maxCacheDuration ago...
    }
    
    /*
    public static class NextHostStatus {
        String hostname;
        long delay;
        TimeUnit delayUnit = TimeUnit.SECONDS;
    }*/

    public List<ExpiredHostStatus> findHostnamesAndLastEntryWithExpiredCache() {
        log.info("findHostnamesAndLastEntryWithExpiredCache");
        Query query = samlJpa.getEntityManager().createNativeQuery("SELECT h.Name as hostname,max(s.created_ts) as lastcheck FROM mw_hosts as h JOIN mw_saml_assertion as s ON h.id=s.host_id WHERE NOT EXISTS ( SELECT ID FROM mw_saml_assertion as t WHERE h.ID = t.host_id AND t.created_ts > ? ) GROUP BY h.ID");
        Calendar maxCache = Calendar.getInstance();        
        maxCache.add(Calendar.SECOND, -(int)TimeUnit.SECONDS.convert(maxCacheDuration, maxCacheDurationUnits));
        log.debug("Searching for hosts with last trust status before {}", new Rfc822Date(maxCache.getTime()).toString());
        query.setParameter(1, maxCache);
        List<Object[]> results = query.getResultList();
        log.debug("Got {} results", results.size());
        ArrayList<ExpiredHostStatus> list = new ArrayList<ExpiredHostStatus>();
        for(Object[] result : results) {
            ExpiredHostStatus entry = new ExpiredHostStatus();
            log.debug("Hostname: {}  last checked: {}", result[0], result[1]);
            entry.hostname = (String)result[0];
            entry.lastChecked = (Date)result[1];
            list.add(entry);
        }
        return list;
    }
    
    
    
}
