/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.as.rest;

import com.intel.mountwilson.as.business.HostBO;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.as.data.TblHosts;
import com.intel.mountwilson.as.helper.ASComponentFactory;
import com.intel.mountwilson.util.vmware.VMwareClient;
import com.intel.mtwilson.datatypes.Hostname;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import com.intel.mtwilson.util.crypto.CryptographyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.ejb.Stateless;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import com.intel.mountwilson.util.vmware.VMwareConnectionPool;
import com.intel.mountwilson.util.vmware.VMwareConnectionException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
@Stateless
@Path("/test")
public class Test {
    private Logger log = LoggerFactory.getLogger(getClass());
    private HostBO hostBO = new ASComponentFactory().getHostBO(); 
    private static VMwareConnectionPool vcenterPool = new VMwareConnectionPool();
    
    /**
     *
     * @param hosts
     * @param forceVerify
     * @param threads number of threads to use
     * @param timeout in seconds for all threads to complete
     * @return
     */
    @RolesAllowed({"Attestation", "Report"})
    @Path("/vcenter/async")
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    public String vmware( 
            @QueryParam("hosts") String hosts,
            @QueryParam("force_verify") @DefaultValue("true") Boolean forceVerify,
            @QueryParam("threads") @DefaultValue("5") Integer threads,
            @QueryParam("timeout") @DefaultValue("600") Integer timeout,
            @QueryParam("pool") @DefaultValue("true") Boolean usePool,
            @QueryParam("duplicates") @DefaultValue("false") Boolean allowDuplicates
    ) {
        try {
            Collection<String> hostCollection;
            if( allowDuplicates ) {
                hostCollection = new ArrayList<String>();
            }
            else {
                hostCollection = new HashSet<String>();
            }
            hostCollection.addAll(Arrays.asList(hosts.split(",")));
            
            Set<VCenterHostQuote> tasks = new HashSet<VCenterHostQuote>();
            ExecutorService scheduler = Executors.newFixedThreadPool(threads);
            
            List<String> results = new ArrayList<String>();
            
            for(String host : hostCollection) {
                VCenterHostQuote task = new VCenterHostQuote( usePool ? vcenterPool : null, hostBO, host);
                tasks.add(task);
                scheduler.submit(task);
            }
            
            scheduler.shutdown();
            
            if( scheduler.awaitTermination(timeout, TimeUnit.SECONDS) ) {
                // all tasks completed
                results.add("All tasks completed");
            }
            else {
                // timeout happened, but we may have some results
                results.add("Timeout reached");
            }
            
            for(VCenterHostQuote task : tasks) {
                if( task.isError() ) {
                    results.add(task.getHostname()+" error: "+task.getError());
                }
                else {
                    results.add(task.getHostname()+" OK: "+task.getResult());
                }
            }
            
            String report = StringUtils.join(results, "\n");

            return report;
        } catch (Exception ex) {
            throw new ASException(ex);
        }
        
    }
    
    private class VCenterHostQuote implements Runnable {
        private VMwareConnectionPool pool = null;
        private HostBO dao;
        private String result;
        private String connectionString = null; // example: "https://10.1.71.162:443/sdk;administrator;intel123!";
        private String hostname = null; // example: "10.1.71.174"
        private String error = null;
        
        public VCenterHostQuote(VMwareConnectionPool pool, HostBO dao, String hostname) {
            this.pool = pool;
            this.dao = dao;
            this.hostname = hostname;
        }

        public VCenterHostQuote(String connectionString, String hostname) {
            this.connectionString = connectionString;
            this.hostname = hostname;
        }
        
        public VCenterHostQuote(HostBO dao, String hostname) {
            this.dao = dao;
            this.hostname = hostname;
        }
        
        public void loadConnectionString() throws CryptographyException {
            TblHosts host = dao.getHostByName(new Hostname(hostname));
            this.connectionString = host.getAddOnConnectionInfo();
        }
        
        @Override
        public void run() {
            if( isError() ) { return; } // avoid clobbering previous error
            try {
                if( connectionString == null ) { loadConnectionString(); }
                TxtHostRecord host = new TxtHostRecord();
                host.HostName = hostname;
                host.AddOn_Connection_String = connectionString;
                VMwareClient client = getClient(host);
                if( client == null ) {
                	log.error("Cannot create client");
                }
                else {
	                result = client.getHostAttestationReport(host, "0,17,18,20");
	                log.info("Got response for "+hostname);
                }
            } catch (Exception ex) {
                error = ex.toString();
            }
        }
        
        public boolean isError() { return error != null; }
        public String getResult() { return result; }
        public String getError() { return error; }
        public String getHostname() { return hostname; }

        // this code has to move to vmware client in trust utils library...
        private VMwareClient getClient(TxtHostRecord host) {
            if( pool == null ) {
                try {
                    VMwareClient client = new VMwareClient();
                    client.connect(host.AddOn_Connection_String);
                    return client;
                }
                catch(Exception e) {
                    error = e.toString();
                    return null;
                }
            }
            else {
                try {
                    return pool.getClientForConnection(host.AddOn_Connection_String);
                }
                catch(VMwareConnectionException e) {
                    error = e.toString();
                    return null;
                }
            }
        }
        
    }
    
}
