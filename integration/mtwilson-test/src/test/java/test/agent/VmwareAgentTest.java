/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.agent;

import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.io.IOException;
import java.security.KeyManagementException;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class VmwareAgentTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testNoHostnameInURL() throws KeyManagementException, IOException {
        String host1 = "vmware:https://10.1.71.162:443/sdk;Administrator;intel123!";
        
        HostAgentFactory hostAgentFactory = new HostAgentFactory();
        HostAgent agent1 = hostAgentFactory.getHostAgent(new ConnectionString(host1), new InsecureTlsPolicy());
        PcrManifest manifest1 = agent1.getPcrManifest();
        Pcr pcr1 = manifest1.getPcr(19);
        PcrEventLog eventLog1 = manifest1.getPcrEventLog(19);
        List<Measurement> list1 = eventLog1.getEventLog();
        log.debug("pcr 19 = {}", pcr1.toString());
    }    

    @Test
    public void testWithHostnameInURL() throws KeyManagementException, IOException {
        String host1 = "vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;h=10.1.71.173";
        
        HostAgentFactory hostAgentFactory = new HostAgentFactory();
        HostAgent agent1 = hostAgentFactory.getHostAgent(new ConnectionString(host1), new InsecureTlsPolicy());
        PcrManifest manifest1 = agent1.getPcrManifest();
        Pcr pcr1 = manifest1.getPcr(19);
        PcrEventLog eventLog1 = manifest1.getPcrEventLog(19);
        List<Measurement> list1 = eventLog1.getEventLog();
        log.debug("pcr 19 = {}", pcr1.toString());
    }    

}
