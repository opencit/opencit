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
import com.intel.mtwilson.model.Sha1Digest;
import com.intel.mtwilson.policy.impl.HostTrustPolicyManager;
import com.intel.mtwilson.policy.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.util.MyJpaDatastore;
import java.util.Set;

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
    
    /**
     * Extend ZERO with ZERO (20 byte):
2013-04-11 23:26:58,846 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:59] PCR 20 = 0000000000000000000000000000000000000000
2013-04-11 23:26:58,855 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:59] PCR 20 = b80de5d138758541c5f05265ad144ab9fa86d1db
2013-04-11 23:26:58,856 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:59] PCR 20 = 850659b18eb6fb4ccdcb113ca4266eb945449466
2013-04-11 23:26:58,856 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:59] PCR 20 = 040acda4a47c13e2959cae1a56b76401b713524e
2013-04-11 23:26:58,856 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:62] PCR 20 = ad919ad2b122e5741612e16329aa7626b63ada73
     * 
     * Extend ZERO with SHA1(20 bytes of zero):
2013-04-11 23:31:05,432 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:84] PCR 20 = 0000000000000000000000000000000000000000
2013-04-11 23:31:05,435 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:84] PCR 20 = 2c754ca949cfa83b323df8ed0057333551d15dc2
2013-04-11 23:31:05,436 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:84] PCR 20 = 596788206ef0f0cf6a771fd99bde6d0fcf4eed69
2013-04-11 23:31:05,436 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:84] PCR 20 = 52dca4d81928d58b24da08371b5fbc591149d88d
2013-04-11 23:31:05,436 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:87] PCR 20 = 6fedb305092c25a0c248cc8d1432a56828537928
     * 
     * 
     * Extend ZERO with ZERO (40 byte):
2013-04-11 23:28:36,412 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:68] PCR 20 = 0000000000000000000000000000000000000000
2013-04-11 23:28:36,421 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:68] PCR 20 = fb3d8fb74570a077e332993f7d3d27603501b987
2013-04-11 23:28:36,421 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:68] PCR 20 = a5a3f1d7e009bc2d2b9ef4cead302b4f859701f3
2013-04-11 23:28:36,421 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:68] PCR 20 = fb7713dcf801b33a4e818cc5817fb2975964330f
2013-04-11 23:28:36,422 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:71] PCR 20 = 41d0aac78121b5715fccc2fb9b2b22ae51590d88
* 
* Extend ZERO with SHA1(40 bytes of zero):
2013-04-11 23:30:03,552 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:78] PCR 20 = 0000000000000000000000000000000000000000
2013-04-11 23:30:03,555 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:78] PCR 20 = 37c42bd6cc033b33086291c007c3aa12707a8bb4
2013-04-11 23:30:03,555 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:78] PCR 20 = 356b9870872a266234d38c4ccd79d739d062eb5d
2013-04-11 23:30:03,555 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:78] PCR 20 = c8f7670a0dc2eaf15c90ae7d9697bfd46b4111e3
2013-04-11 23:30:03,556 DEBUG [main] t.p.TestVmwareEsxi51 [TestVmwareEsxi51.java:81] PCR 20 = b3ee188648dd01ebf857e9a08f28f482d4afae52
* 
     * @throws Exception 
     */
    @Test
    public void testPcr20WithJustZeros() throws Exception {
        Sha1Digest pcr20 = Sha1Digest.ZERO;
        Sha1Digest sha1forty = Sha1Digest.valueOf(new byte[] { 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0});
        for(int i=0; i<4; i++) {
            log.debug("PCR 20 = {}", pcr20.toString());
            pcr20 = pcr20.extend(sha1forty); // sha1(40 bytes of zero)
        }
        log.debug("PCR 20 = {}", pcr20.toString());
    }
    
    
    /*
    @Test
    public void testGeneratePolicyForHost() throws Exception {
        TblHosts host = initNewHost();
        HostAgentFactory agentFactory = new HostAgentFactory();
        HostAgent agent = agentFactory.getHostAgent(host);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = agent.getPcrManifest();
        hostReport.variables = agent.getHostAttributes();
        HostTrustPolicyManager policyFactory = new HostTrustPolicyManager(pm.getEntityManagerFactory("ASDataPU"));
//        Set<Rule> rules = policyFactory.generateTrustRulesForHost(host, hostReport);
//        Policy policy = new Policy("Automatically generated policy for "+host.getName(), rules);
        Policy policy = policyFactory.createWhitelistFromHost(host);
        System.out.println(json.writeValueAsString(policy));
    }
    */
}
