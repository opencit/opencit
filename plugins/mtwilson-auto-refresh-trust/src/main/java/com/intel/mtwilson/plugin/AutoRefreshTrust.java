/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.plugin;

import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.as.business.trust.BulkHostTrustBO;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.controller.TblSamlAssertionJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.plugin.api.Plugin;
import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
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
    private long maxCacheDuration = 1; // hour
    private TimeUnit maxCacheDurationUnits = TimeUnit.HOURS;
    private long timeout = 60; // seconds
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
    
    @Override
    public void run() {
        // make a list of hosts whose last trust status check is more than max cache duration ago
        List<String> hostsToRefresh = findHostnamesWithExpiredCache();
        log.debug("AutoRefreshTrust got {} hosts to refresh", hostsToRefresh.size());
        HashSet<String> hosts = new HashSet<String>(hostsToRefresh);
        String saml = bulkHostTrustBO.getBulkTrustSaml(hosts, true);
        log.trace("Auto bulk refresh SAML: {}", saml);
    }
    
    public List<String> findHostnamesWithExpiredCache() {
        log.info("findHostnamesWithExpiredCache");
        Query query = samlJpa.getEntityManager().createNativeQuery("SELECT h.Name FROM mw_hosts as h WHERE NOT EXISTS ( SELECT ID FROM mw_saml_assertion as t WHERE h.ID = t.host_id AND t.created_ts > ? )");
        Calendar maxCache = Calendar.getInstance();        
        maxCache.add(Calendar.SECOND, -(int)TimeUnit.SECONDS.convert(maxCacheDuration, maxCacheDurationUnits));
        query.setParameter(1, maxCache);
        List<String> results = query.getResultList();
        return results;
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
