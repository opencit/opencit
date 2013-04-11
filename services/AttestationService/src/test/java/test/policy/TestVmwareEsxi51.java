/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.policy;

import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrManifest;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.util.MyJpaDatastore;

/**
 *
 * @author jbuhacoff
 */
public class TestVmwareEsxi51 {
    private static MyJpaDatastore pm = new MyJpaDatastore();
    private transient Logger log = LoggerFactory.getLogger(getClass());
    private transient static ObjectWriter json = new ObjectMapper().writerWithDefaultPrettyPrinter();

    private transient String hostname = "10.1.71.155";
    private transient String connection = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
    
    private TblHosts initNewHost() {
        TblHosts host = new TblHosts();
        host.setName(hostname);
        host.setTlsPolicyName("TRUST_FIRST_CERTIFICATE");
        host.setTlsKeystore(null);
        host.setAddOnConnectionInfo(connection); // XXX notice we do not set the IPAddress or Port in the database... don't need to because now we are setting ConnectionString which includes them both and is passed to the host agent and the host agent extracts ip address and port from this string.
        return host;
    }

    @Test
    public void testHostAgentPcrManifest() throws Exception {
        TblHosts host = initNewHost();
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent agent = factory.getHostAgent(host);
        PcrManifest pcrManifest = agent.getPcrManifest();
        assertNotNull(pcrManifest);
        for(int i=0; i<24; i++) {
            Pcr pcr = pcrManifest.getPcr(i);
            log.debug("Pcr {} = {}", i, pcr.getValue().toString());
        }
    }
    
    
}
