/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.performance;

import com.intel.mtwilson.My;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.model.*;
import java.io.IOException;
import com.intel.mtwilson.agent.vmware.*;
import com.intel.mtwilson.agent.citrix.*;
import com.intel.mtwilson.agent.intel.*;
import com.intel.dcsg.cpg.performance.*;
import com.intel.dcsg.cpg.performance.report.*;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.net.MalformedURLException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO:
 * 1. single-threaded, single-host performance test
 * 1. single-threaded, multiple-host performance test
 * 1. multi-threaded, single-host performance test
 * 1. multi-threaded, multiple-host performance test
 * X. utility class for loading environment  ---  already in My.environment() ?  
 * X. utility functions for picking out hosts in the environment according to characteristics... and possibly
 *    a data file to support that.  for example "i need two vmware hosts with the same MLE"  or maybe some 
 *    functions to detect these required combinations within a defined environment and then make available what
 *    we find...
 * @author jbuhacoff
 */
public class HostTrustStatusPerformanceTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    int howManyTimes =10; // when testing a single task multiple times, do it this number of times (sample size)
    
    private Map<Vendor,List<ConnectionString>> getHostsByVendor() throws IOException {
        EnumMap<Vendor,List<ConnectionString>> vendorMap = new EnumMap<Vendor,List<ConnectionString>>(Vendor.class);
        List<ConnectionString> hostlist = My.env().getHostConnectionList();
        for(ConnectionString hosturl : hostlist) { 
            Vendor vendor = hosturl.getVendor();
            if( vendor == null ) {
                log.warn("Cannot identify vendor for host url: {}", hosturl.getAddOnConnectionString());
                continue;
            }
            if( !vendorMap.containsKey(vendor) ) {
                vendorMap.put(vendor, new ArrayList<ConnectionString>());
            }
            vendorMap.get(vendor).add(hosturl);
//            log.debug("Connection: {}", vendor);
        }
        return vendorMap;
    }
    
    private List<ConnectionString> getHostsByVendor(Vendor vendor) throws IOException {
        Map<Vendor,List<ConnectionString>> vendorMap = getHostsByVendor();
        if( vendorMap.containsKey(vendor) ) {
            return vendorMap.get(vendor);
        }
        return new ArrayList<ConnectionString>();
    }
    
    public static class HostTrustTask extends Task {
        public HostTrustTask(String hostname) {
            super(hostname);
        }
        @Override
        public void execute() throws Exception {
            My.client().getHostTrust(new Hostname(getId())); // id is passed to super constructor, which is our hostname
        }
    }

    public static class GetTpmStatusTask extends Task {
        private HostAgent agent;
        public GetTpmStatusTask(String hostname, HostAgent agent) {
            super(hostname);
            this.agent = agent;
        }
        @Override
        public void execute() throws Exception {
            agent.isTpmPresent();
        }
    }

    public static class GetPcrManifestTask extends Task {
        private HostAgent agent;
        public GetPcrManifestTask(String hostname, HostAgent agent) {
            super(hostname);
            this.agent = agent;
        }
        @Override
        public void execute() throws Exception {
            agent.getPcrManifest();
        }
    }

    public static class GetIntelPcrManifestWithoutCacheTask extends Task {
        private IntelHostAgentFactory factory = new IntelHostAgentFactory();
        public GetIntelPcrManifestWithoutCacheTask(String hostname) throws IOException {
            super(hostname);
        }
        @Override
        public void execute() throws Exception {
            HostAgent agent = factory.getHostAgent("https://10.1.71.167:9999", new InsecureTlsPolicy());
            agent.getPcrManifest();
        }
    }
    
    private ObjectMapper mapper = new ObjectMapper();
    
    @Test
    public void testSingleThreadSingleHostPerformance() throws IOException, MalformedURLException, ApiException, SignatureException, Exception {
        List<ConnectionString> hostlist = getHostsByVendor(Vendor.VMWARE); //My.env().getHostConnectionList();
        ConnectionString host = hostlist.get(0); // for this test we are just going to use a single vmware host
        log.debug("host: {}", host.getHostname().toString());
        /*
        // XXX TODO we need a simple api to add a host with just vendor type & hostname/ipaddress,  or with complete connection string, or with just hostname/ipaddress (and if there's username/password required it can be provided later)
        TxtHostRecord txtHostRecord = new TxtHostRecord();
        txtHostRecord.AddOn_Connection_String = host.getConnectionStringWithPrefix();
        txtHostRecord.HostName = host.getHostname().toString();
        txtHostRecord.IPAddress = host.getHostname().toString();
        My.client().configureWhiteList(txtHostRecord);
        My.client().addHost(null)
        */
//        My.client().getHostTrust(host.getHostname());
        PerformanceInfo info = PerformanceUtil.measureSingleTask(new HostTrustTask(host.getHostname().toString()), howManyTimes);
        long[] data = info.getData();
        log.debug("samples: {}", data.length);
        log.debug("min: {}", info.getMin());
        log.debug("max: {}", info.getMax());
        log.debug("avg: {}", info.getAverage());
        log.debug("performance info: {}", mapper.writeValueAsString(info));
    }
    
    
    @Test
    public void testVmwareAgentGetTpmStatusPerformance() throws IOException {
        VmwareHostAgentFactory factory = new VmwareHostAgentFactory();
        HostAgent agent = factory.getHostAgent("https://10.1.71.162/sdk;Administrator;intel123!;10.1.71.173", new InsecureTlsPolicy());
        GetTpmStatusTask task = new GetTpmStatusTask("isTpmPresent-10.1.71.173", agent);
        PerformanceInfo info = PerformanceUtil.measureSingleTask(task, howManyTimes);
        long[] data = info.getData();
        log.debug("samples: {}", data.length);
        log.debug("min: {}", info.getMin());
        log.debug("max: {}", info.getMax());
        log.debug("avg: {}", info.getAverage());
        log.debug("performance info: {}", mapper.writeValueAsString(info));
    }

    @Test
    public void testVmwareAgentGetPcrManifestsPerformance() throws IOException {
        VmwareHostAgentFactory factory = new VmwareHostAgentFactory();
        HostAgent agent = factory.getHostAgent("https://10.1.71.93/sdk;Administrator;P@ssw0rd;10.1.71.144", new InsecureTlsPolicy());
        GetPcrManifestTask task = new GetPcrManifestTask("getPcrManifest-10.1.71.144", agent);
        PerformanceInfo info = PerformanceUtil.measureSingleTask(task, howManyTimes);
        long[] data = info.getData();
        log.debug("samples: {}", data.length);
        log.debug("min: {}", info.getMin());
        log.debug("max: {}", info.getMax());
        log.debug("avg: {}", info.getAverage());
        log.debug("performance info: {}", mapper.writeValueAsString(info));
    }
    

    @Test
    public void testCitrixAgentGetPcrManifestsPerformance() throws IOException {
        CitrixHostAgentFactory factory = new CitrixHostAgentFactory();
        HostAgent agent = factory.getHostAgent("https://10.1.71.201/;root;P@ssw0rd", new InsecureTlsPolicy());
        GetPcrManifestTask task = new GetPcrManifestTask("getPcrManifest-10.1.71.201", agent);
        PerformanceInfo info = PerformanceUtil.measureSingleTask(task, howManyTimes);
        long[] data = info.getData();
        log.debug("samples: {}", data.length);
        log.debug("min: {}", info.getMin());
        log.debug("max: {}", info.getMax());
        log.debug("avg: {}", info.getAverage());
        log.debug("performance info: {}", mapper.writeValueAsString(info));
    }
    
    
    /**
     * The intel agent caches the results so only the first request will take time, and all the others
     * will immediately return
     */
    @Test
    public void testIntelAgentGetPcrManifestsPerformanceWithCaching() throws IOException {
        IntelHostAgentFactory factory = new IntelHostAgentFactory();
        HostAgent agent = factory.getHostAgent("https://10.1.71.167:9999", new InsecureTlsPolicy());
        GetPcrManifestTask task = new GetPcrManifestTask("getPcrManifest-10.1.71.167", agent);
        PerformanceInfo info = PerformanceUtil.measureSingleTask(task, howManyTimes);
        long[] data = info.getData();
        log.debug("samples: {}", data.length);
        log.debug("min: {}", info.getMin());
        log.debug("max: {}", info.getMax());
        log.debug("avg: {}", info.getAverage());
        log.debug("performance info: {}", mapper.writeValueAsString(info));
    }
    
    
    
    /**
     * TO prevent caching you need to either instantiate a new host agent or reset() it (not yet implemented)
     * @throws IOException 
     */
    @Test
    public void testIntelAgentGetPcrManifestsPerformanceWithoutCaching() throws IOException {
        GetIntelPcrManifestWithoutCacheTask task = new GetIntelPcrManifestWithoutCacheTask("getPcrManifest-10.1.71.167");
        PerformanceInfo info = PerformanceUtil.measureSingleTask(task, howManyTimes);
        long[] data = info.getData();
        log.debug("samples: {}", data.length);
        log.debug("min: {}", info.getMin());
        log.debug("max: {}", info.getMax());
        log.debug("avg: {}", info.getAverage());
        log.debug("performance info: {}", mapper.writeValueAsString(info));
    }

}
