/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.plugin;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.as.business.trust.BulkHostTrustBO;
import com.intel.mtwilson.as.controller.TblSamlAssertionJpaController;
import com.intel.mtwilson.as.ASPersistenceManager;
import com.intel.mtwilson.plugin.api.Plugin;
import com.intel.mtwilson.plugin.api.PluginLoader;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class AutoRefreshTrustLoader implements PluginLoader {
    private transient static Logger log = LoggerFactory.getLogger(AutoRefreshTrustLoader.class);
    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    
    @Override
    public Plugin loadPlugin() {
        AutoRefreshTrust plugin = new AutoRefreshTrust();
        plugin.setEnabled(true);
        plugin.setMaxCacheDuration(5);
        plugin.setMaxCacheDurationUnits(TimeUnit.MINUTES);
        plugin.setTimeout(60);
        plugin.setTimeoutUnits(TimeUnit.SECONDS);
        TblSamlAssertionJpaController samlJpa = new TblSamlAssertionJpaController(new ASPersistenceManager().getEntityManagerFactory("ASDataPU"));
        plugin.setTblSamlAssertionJpaController(samlJpa);
        BulkHostTrustBO bulkHostTrustBO = new BulkHostTrustBO((int)TimeUnit.SECONDS.convert(60, TimeUnit.SECONDS));
        plugin.setBulkHostTrustBO(bulkHostTrustBO);
        
        // before we load the plugin, make sure that we start the background task
        long interval = ASConfig.getConfiguration().getInt("mtwilson.auto.refresh.trust.interval", (int)TimeUnit.SECONDS.convert(30, TimeUnit.MINUTES));
        log.debug("Scheduling auto refresh plugin every {} seconds", interval);
        executor.scheduleAtFixedRate(plugin, interval, interval, TimeUnit.SECONDS);
        
        return plugin;
    }
}
