/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.plugin;

import com.intel.mtwilson.as.business.trust.BulkHostTrustBO;
import com.intel.mtwilson.as.controller.TblSamlAssertionJpaController;
import com.intel.mtwilson.plugin.api.Plugin;
import com.intel.mtwilson.My;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
public class AutoRefreshTrust implements Runnable, Plugin {
    private Logger log = LoggerFactory.getLogger(getClass());
    private boolean enabled = true;
    private long maxCacheDuration = 5; // hour
    private TimeUnit maxCacheDurationUnits = TimeUnit.MINUTES;
    private int refreshTimeBeforeSamlExpiry = 300; // seconds
    private long interval = 120; // seconds
    private TimeUnit intervalUnits = TimeUnit.SECONDS;
    private BulkHostTrustBO bulkHostTrustBO = null;
    private TblSamlAssertionJpaController samlJpa = null;
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setMaxCacheDuration(long maxCacheDuration) { this.maxCacheDuration = maxCacheDuration; }
    public void setMaxCacheDurationUnits(TimeUnit maxCacheDurationUnits) { this.maxCacheDurationUnits = maxCacheDurationUnits; }
    public void setInterval(long interval) { this.interval = interval; }
    public void setIntervalUnits(TimeUnit intervalUnits) { this.intervalUnits = intervalUnits; }
    public void setBulkHostTrustBO(BulkHostTrustBO bulkHostTrustBO) { this.bulkHostTrustBO = bulkHostTrustBO; }
    public void setTblSamlAssertionJpaController(TblSamlAssertionJpaController samlJpa) { this.samlJpa = samlJpa; }
    private volatile boolean running;

    public AutoRefreshTrust(long interval, TimeUnit intervalUnits) {
        this.interval = interval;
        this.intervalUnits = intervalUnits;
    }
    
    
    public void cancel() {
        running = false;
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
                long bulkBOTimeout = My.configuration().getConfiguration().getLong("mtwilson.ms.registration.hostTimeout", 60); // Default is 60 seconds
                bulkHostTrustBO = new BulkHostTrustBO((int)bulkBOTimeout);
                String saml = bulkHostTrustBO.getBulkTrustSaml(hosts, true);
                log.info("Auto bulk refresh SAML: {}", saml);
            } else {
                log.info("AutoRefreshTrust: No hosts for bulk refresh");
            }
            if( !running ) { break; }
            try {
                log.info("AutoRefreshTrust: Auto refresh thread would sleep for {} seconds.", TimeUnit.SECONDS.convert(interval, intervalUnits));
                Thread.sleep(TimeUnit.MILLISECONDS.convert(interval, intervalUnits));
            } catch (InterruptedException ex) {
                log.info("AutoRefreshTrust: Error during waiting for the next process: {}", ex.getMessage());
            }
        }
    }
    
    public List<String> findHostnamesWithExpiredCache() {
        try {
            return My.jpa().mwSamlAssertion().findHostnamesWithExpiredCache(My.configuration().getSamlValidityTimeInSeconds() - refreshTimeBeforeSamlExpiry);
        } catch (Exception ex) {
            log.error("AutoRefreshTrust:findHostnamesWithExpiredCache - Error during retrieval of hosts with expired cache.", ex);
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

//    public List<ExpiredHostStatus> findHostnamesAndLastEntryWithExpiredCache() {
//        log.info("findHostnamesAndLastEntryWithExpiredCache");
//        Query query = samlJpa.getEntityManager().createNativeQuery("SELECT h.Name as hostname,max(s.created_ts) as lastcheck FROM mw_hosts as h JOIN mw_saml_assertion as s ON h.id=s.host_id WHERE NOT EXISTS ( SELECT ID FROM mw_saml_assertion as t WHERE h.ID = t.host_id AND t.created_ts > ? ) GROUP BY h.ID");
//        Calendar maxCache = Calendar.getInstance();        
//        maxCache.add(Calendar.SECOND, -(int)TimeUnit.SECONDS.convert(maxCacheDuration, maxCacheDurationUnits));
//        log.debug("Searching for hosts with last trust status before {}", new Rfc822Date(maxCache.getTime()).toString());
//        query.setParameter(1, maxCache);
//        List<Object[]> results = query.getResultList();
//        log.debug("Got {} results", results.size());
//        ArrayList<ExpiredHostStatus> list = new ArrayList<ExpiredHostStatus>();
//        for(Object[] result : results) {
//            ExpiredHostStatus entry = new ExpiredHostStatus();
//            log.debug("Hostname: {}  last checked: {}", result[0], result[1]);
//            entry.hostname = (String)result[0];
//            entry.lastChecked = (Date)result[1];
//            list.add(entry);
//        }
//        return list;
//    }
    
    
    
}
