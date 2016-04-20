/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.ASComponentFactory;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.launcher.ws.ext.V1;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.security.annotations.PermitAll;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import com.intel.mtwilson.util.MWException;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Consumes;
//import javax.ejb.Stateless;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
@V1
//@Stateless
@Path("/AttestationService/resources/test")
public class Test {
    private Logger log = LoggerFactory.getLogger(getClass());
    private HostBO hostBO = ASComponentFactory.getHostBO(); 
    private static HostAgentFactory hostAgentFactory = new HostAgentFactory(); 
//    private static VMwareConnectionPool vcenterPool = new VMwareConnectionPool(); // BUG #497 replacing this with the HostAgentFactory - the underlying implementation uses a pool and respects tls policy for each host
    
    
    @PermitAll
    @GET
    @Path("/error/400")
    @Produces(MediaType.TEXT_PLAIN)
    public String error400TextPlain() {
        throw new MWException(ErrorCode.UNKNOWN_ERROR);
    }
    @PermitAll
    @GET
    @Path("/error/400")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String error400ApplicatinoJson() {
        throw new MWException(ErrorCode.UNKNOWN_ERROR);
    }
    
    @PermitAll
    @POST
    @Path("/testMediaType")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String testMediaTypePlainText(ApiClientCreateRequest apiClientRequest) {
        log.error("API client registration: {}", Base64.encodeBase64String(apiClientRequest.getCertificate()));
        //new ApiClientBO().create(apiClientRequest, null);
        return "OK";
    }
    
    @PermitAll
    @POST
    @Path("/testMediaType")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String testMediaTypeJSON(ApiClientCreateRequest apiClientRequest) {
        log.error("API client registration: {}", Base64.encodeBase64String(apiClientRequest.getCertificate()));
        //new ApiClientBO().create(apiClientRequest, null);
        return "OK";
    }
    
    @PermitAll
    @POST
    @Path("/testMediaType")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public String testMediaTypeXML(ApiClientCreateRequest apiClientRequest) {
        log.error("API client registration: {}", Base64.encodeBase64String(apiClientRequest.getCertificate()));
        //new ApiClientBO().create(apiClientRequest, null);
        return "OK";
    }
    
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
                VCenterHostQuote task = new VCenterHostQuote( /* BUG #497 usePool ? vcenterPool : null,*/ hostBO, host);
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
            // throw new ASException(ex);
            log.error("Error during testing.", ex);
            throw new ASException(ErrorCode.SYSTEM_ERROR, ex.getClass().getSimpleName());
        }
        
    }
    
    private class VCenterHostQuote implements Runnable {
//        private VMwareConnectionPool pool = null;
        private HostBO dao;
        private String result;
//        private String connectionString = null; // example: "https://10.1.71.162:443/sdk;administrator;intel123!";
        private String hostname = null; // example: "10.1.71.174"
        private String error = null;
        private TblHosts hostRecord = null;
        private HostAgent hostAgent = null;
        
        /*
        public VCenterHostQuote(VMwareConnectionPool pool, HostBO dao, String hostname) {
            this.pool = pool;
            this.dao = dao;
            this.hostname = hostname;
        }*/

        /*
        public VCenterHostQuote(String connectionString, String hostname) {
            this.connectionString = connectionString;
            this.hostname = hostname;
        }*/
        
        public VCenterHostQuote(HostBO dao, String hostname) {
            this.dao = dao;
            this.hostname = hostname;
        }
        
        public void loadConnectionString() throws IOException, CryptographyException {
            hostRecord = dao.getHostByName(new Hostname(hostname));
//            this.connectionString = host.getAddOnConnectionInfo();
        }
        
        @Override
        public void run() {
            if( isError() ) { return; } // avoid clobbering previous error
            try {
                if( hostRecord == null ) { loadConnectionString(); }
//                TxtHostRecord host = new TxtHostRecord();
//                host.HostName = hostname;
//                host.AddOn_Connection_String = connectionString;
                hostAgent = hostAgentFactory.getHostAgent(hostRecord);
//                VMwareClient client = hostAgent.getgetClient(host);
	                result = hostAgent.getVendorHostReport(); //getHostAttestationReport(host, "0,17,18,20");
	                log.debug("Got response for "+hostname);
            } catch (Exception ex) {
                // error = ex.toString();
                log.error("Error during testing", ex);
                error = ex.getClass().getSimpleName();
            }
        }
        
        public boolean isError() { return error != null; }
        public String getResult() { return result; }
        public String getError() { return error; }
        public String getHostname() { return hostname; }

        
    }
    
}
