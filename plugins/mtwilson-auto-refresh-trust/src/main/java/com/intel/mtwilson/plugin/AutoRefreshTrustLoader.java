/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.plugin;

import com.intel.mtwilson.My;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
@WebListener
public class AutoRefreshTrustLoader implements ServletContextListener {

    private transient static Logger log = LoggerFactory.getLogger(AutoRefreshTrustLoader.class);
    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private static final long DEFAULT_AUTO_REFRESH_TRUST_INTERVAL = 120;
    private Thread mainThread;
    private AutoRefreshTrust art = null;
    private ScheduledFuture<?> future = null;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("AutoRefreshTrust: About to start the thread");
        My.initDataEncryptionKey();
        long sleepInterval = My.configuration().getConfiguration().getLong("mtwilson.auto.refresh.trust.interval.seconds", DEFAULT_AUTO_REFRESH_TRUST_INTERVAL);
        if (sleepInterval == 0) {
            log.debug("mtwilson.auto.refresh.trust.interval.seconds=0, skipping AutoRefreshTrust");
        } else {
            art = new AutoRefreshTrust(sleepInterval, TimeUnit.SECONDS);
            future = executor.scheduleWithFixedDelay(art, sleepInterval, sleepInterval, TimeUnit.SECONDS);
//            mainThread = new Thread(art);
//            mainThread.start();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (art != null) {
            log.info("AutoRefreshTrust: About to end the thread");
            art.cancel();
//            mainThread.interrupt();
            
        }
        if( future != null ) {
            future.cancel(true);            
    }
        if( executor != null ) {
            executor.shutdown();
        }
    }
//    @Override
//    public Plugin loadPlugin() {
//        try {
//            AutoRefreshTrust plugin = new AutoRefreshTrust();
//            plugin.setEnabled(true);
//            plugin.setMaxCacheDuration(5);
//            plugin.setMaxCacheDurationUnits(TimeUnit.MINUTES);
//            plugin.setTimeout(60);
//            plugin.setTimeoutUnits(TimeUnit.SECONDS);
//            TblSamlAssertionJpaController samlJpa = My.jpa().mwSamlAssertion();
//            plugin.setTblSamlAssertionJpaController(samlJpa);
//            BulkHostTrustBO bulkHostTrustBO = new BulkHostTrustBO((int)TimeUnit.SECONDS.convert(60, TimeUnit.SECONDS));
//            plugin.setBulkHostTrustBO(bulkHostTrustBO);
//            
//            // before we load the plugin, make sure that we start the background task
//            long interval = ASConfig.getConfiguration().getInt("mtwilson.auto.refresh.trust.interval", (int)TimeUnit.SECONDS.convert(30, TimeUnit.MINUTES));
//            log.debug("Scheduling auto refresh plugin every {} seconds", interval);
//            executor.scheduleAtFixedRate(plugin, interval, interval, TimeUnit.SECONDS);
//            return plugin;
//        } catch (IOException ex) {
//            log.error("Error in auto refresh trust plugin", ex);
//            throw new ASException(ErrorCode.SYSTEM_ERROR, ex.getClass().getSimpleName());
//        }
//    }
}
