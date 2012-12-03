/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.business.trust;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.helper.ASComponentFactory;
import com.intel.mountwilson.util.vmware.VMwareClient;
import com.intel.mountwilson.util.vmware.VMwareConnectionException;
import com.intel.mountwilson.util.vmware.VMwareConnectionPool;
import com.intel.mtwilson.as.helper.ASComponentFactory;
import com.intel.mtwilson.datatypes.BulkHostTrustResponse;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.HostTrust;
import com.intel.mtwilson.datatypes.Hostname;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.crypto.CryptographyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class BulkHostTrustBO {
    private Logger log = LoggerFactory.getLogger(getClass());
    private HostTrustBO hostTrustBO = new ASComponentFactory().getHostTrustBO(); 
    private static VMwareConnectionPool vcenterPool = new VMwareConnectionPool();
    private int maxThreads;
    private int timeout;
    
    public BulkHostTrustBO(int maxThreads, int timeout) {
        this.maxThreads = maxThreads;
        this.timeout = timeout;
    }
    
    public String getBulkTrustSaml(Set<String> hosts, boolean forceVerify) {
        try {
            Set<HostQuoteSaml> tasks = new HashSet<HostQuoteSaml>();
            ExecutorService scheduler = Executors.newFixedThreadPool(maxThreads);
            
            List<String> results = new ArrayList<String>();
            
            for(String host : hosts) {
                HostQuoteSaml task = new HostQuoteSaml(hostTrustBO, host, forceVerify);
                tasks.add(task);
                scheduler.submit(task);
            }
            
            scheduler.shutdown();
            
            if( scheduler.awaitTermination(timeout, TimeUnit.SECONDS) ) {
                log.info("All tasks completed on time");
            }
            else {
                log.info("Timeout reached before all tasks completed"); // should set the error code ErrorCode.AS_ASYNC_TIMEOUT on the ones that timed out (no result available)
            }
            
            for(HostQuoteSaml task : tasks) {
                if( task.getResult() == null ) {
                    results.add(task.getTimeoutResult());
                }
                else if( task.isError() ) {
                    results.add(task.getResult()); // already an error response
                }
                else {
                    results.add(task.getResult());
                }
            }
            
            String report = String.format("<Hosts>%s</Hosts>", StringUtils.join(results, ""));

            return report;
        } catch (Exception ex) {
            throw new ASException(ex);
        }
    }
    
    public BulkHostTrustResponse getBulkTrustJson(Set<String> hosts, boolean forceVerify) {
        try {
                        
            Set<HostQuoteJson> tasks = new HashSet<HostQuoteJson>();
            ExecutorService scheduler = Executors.newFixedThreadPool(maxThreads);
            
            List<HostTrust> results = new ArrayList<HostTrust>();
            
            for(String host : hosts) {
                HostQuoteJson task = new HostQuoteJson(hostTrustBO, host, forceVerify);
                tasks.add(task);
                scheduler.submit(task);
            }
            
            scheduler.shutdown();
            
            if( scheduler.awaitTermination(timeout, TimeUnit.SECONDS) ) {
                log.info("All tasks completed on time");
            }
            else {
                log.info("Timeout reached before all tasks completed"); // should set the error code ErrorCode.AS_ASYNC_TIMEOUT on the ones that timed out (no result available)
            }
            
            
            for(HostQuoteJson task : tasks) {
                if( task.getResult() == null ) {
                    results.add(task.getTimeoutResult());
                }
                else if( task.isError() ) {
                    results.add(task.getResult()); // already in error format
                }
                else {
                    results.add(task.getResult());
                }
            }
            
            BulkHostTrustResponse report = new BulkHostTrustResponse();
            for(HostTrust result : results) {
                report.getHosts().add(result);
            }

            return report;
        } catch (Exception ex) {
            throw new ASException(ex);
        }
    }
    
    private class HostQuoteSaml implements Runnable {
        private HostTrustBO dao;
        private String hostname = null; // example: "10.1.71.174"
        private boolean forceVerify;
        private String result = null;
        private boolean isError = false;
        
        public HostQuoteSaml(HostTrustBO dao, String hostname, boolean forceVerify) {
            this.dao = dao;
            this.hostname = hostname;
            this.forceVerify = forceVerify;
        }
        
        @Override
        public void run() {
            if( isError() ) { return; } // avoid clobbering previous error
            try {
                String saml = dao.getTrustWithSaml(hostname, forceVerify);
                result = String.format("<Host><Name>%s</Name><ErrorCode>%s</ErrorCode><Assertion><![CDATA[%s]]></Assertion></Host>", hostname, ErrorCode.OK.toString(), saml);
            } 
            catch(ASException e) {
                isError = true;
                result = String.format("<Host><Name>%s</Name><ErrorCode>%s</ErrorCode><ErrorMessage>%s</ErrorMessage></Host>", hostname, e.getErrorCode().toString(), e.getErrorMessage());
            }
            catch(Exception e) {
                isError = true;
                result = String.format("<Host><Name>%s</Name><ErrorCode>%s</ErrorCode><ErrorMessage>%s</ErrorMessage></Host>", hostname, ErrorCode.UNKNOWN_ERROR.toString(), e.getLocalizedMessage());
            }
        }
        
        public boolean isError() { return isError; }
        public String getResult() { return result; }
        public String getHostname() { return hostname; }
        public String getTimeoutResult() { return String.format("<Host><Name>%s</Name><ErrorCode>%s</ErrorCode><ErrorMessage>%s</ErrorMessage></Host>", hostname, ErrorCode.AS_ASYNC_TIMEOUT.toString(), "Exceeded timeout of "+timeout+" seconds"); }
        
    }
    
    private class HostQuoteJson implements Runnable {
        private HostTrustBO dao;
        private String hostname = null; // example: "10.1.71.174"
        private boolean forceVerify;
        private HostTrust result = null;
        private boolean isError = false;
        
        public HostQuoteJson(HostTrustBO dao, String hostname, boolean forceVerify) {
            this.dao = dao;
            this.hostname = hostname;
            this.forceVerify = forceVerify;
        }
        
        @Override
        public void run() {
            if( isError() ) { return; } // avoid clobbering previous error
            try {
                result = dao.getTrustWithCache(hostname, forceVerify);
            }
            catch(ASException e) {
                isError = true;
                result = new HostTrust(e.getErrorCode(),e.getMessage(),hostname, null, null); 
            }
            catch(Exception e) {
                isError = true;
                result = new HostTrust(ErrorCode.UNKNOWN_ERROR,e.getLocalizedMessage(),hostname, null, null); 
            }
        }
        
        public boolean isError() { return isError; }
        public HostTrust getResult() { return result; }
        public String getHostname() { return hostname; }
        public HostTrust getTimeoutResult() { return new HostTrust(ErrorCode.AS_ASYNC_TIMEOUT,"Exceeded timeout of "+timeout+" seconds",hostname, null, null); }

        
    }
}
