/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.attestation;

import com.intel.mtwilson.agent.HostAgent;
import org.junit.Test;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.io.IOException;
import java.security.KeyManagementException;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given two vmware hosts that should be identical... find out why we are seeing one is trusted and one is untrusted
 * @author jbuhacoff
 */
public class VmwareWhitelistHostTest679 {
    private Logger log = LoggerFactory.getLogger(getClass());
    /**
     * TODO:  use "My" to load the two hosts... 
     * 
     * Sample output:
     * 
2013-05-08 22:17:01,469 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:51] Host1: vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;10.1.71.173
2013-05-08 22:17:01,469 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:52] Host2: vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;10.1.71.175
2013-05-08 22:17:01,470 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:55] PCR 19
2013-05-08 22:17:01,470 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:56] Host1: 8593947A5C95DB8343AEE3CD2141064BF8BE423D
2013-05-08 22:17:01,470 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:57] Host2: CE263E17584186142B63A58971D48822C9CE5F02
2013-05-08 22:17:01,470 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:60] MEASUREMENTS
2013-05-08 22:17:01,475 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:66] Host1 has 2217B0FBD436EF1A806756A98751B3393C716FA8 state.tgz
2013-05-08 22:17:01,475 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:66] Host1 has 94224C5D622D71EA040BAFFB4CA7A3D20A3B86D8 imgdb.tgz
2013-05-08 22:17:01,475 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:66] Host1 has 735B7A5314A5DEA3649F6340F2755A946E2EEA05 /b.b00 vmbTrustedBoot=true tboot=0x0x101a000 no-auto-partition bootUUID=7283225de1512d0ae1f696eb510fe1c5
2013-05-08 22:17:01,476 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:69] Host2 has 7CE1EDB3A2114085ABA17A64BD1BD3BA97278D92 state.tgz
2013-05-08 22:17:01,476 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:69] Host2 has DD29FFACF52CF3FAFF2F1DB0720C313AC4A4266E imgdb.tgz
2013-05-08 22:17:01,476 DEBUG [main] t.a.VmwareWhitelistHostTest679 [VmwareWhitelistHostTest679.java:69] Host2 has 59CFF58822991C17D929D210CDAFA5CF6A3B54A8 /b.b00 vmbTrustedBoot=true tboot=0x0x101a000 no-auto-partition bootUUID=779460a2ce3d85825649f743a3c1362c
     * 
     * In this example you can see the hosts have the same whitelisted modules (since none come up as differences).
     * The state.tgz and imgdb.tgz modules are "dynamic" -- they have a different value after every reboot on each host
     * and there's no point in comparing them to anything - -but they are included in the PCR integrity calculation.
     * The boot options are a host-specific event which is stored in the database and is consistent across reboots.
     * 
     */
    @Test
    public void testHostReports() throws KeyManagementException, IOException {
        // TODO need to get these host ip's dynamically from the environment file... but
        // we need a way to specify "i need two different vmware hosts that are expected to
        // have the same mle"  so we can do meaningful tests w/o knowing in advance WHICH two
        // vmware hosts they are.   possibilities are for someone to maintain a tagged list
        // of hosts,  or maybe to create an automated tool into which we put all the connection
        // strings and it figures out which are the same, which are different, etc. and 
        // makes the data available for tests. 
        String host1 = "vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;10.1.71.173";
        String host2 = "vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;10.1.71.175";
        
        HostAgentFactory hostAgentFactory = new HostAgentFactory();
        HostAgent agent1 = hostAgentFactory.getHostAgent(new ConnectionString(host1), new InsecureTlsPolicy());
        PcrManifest manifest1 = agent1.getPcrManifest();
        Pcr pcr1 = manifest1.getPcr(19);
        PcrEventLog eventLog1 = manifest1.getPcrEventLog(19);
        List<Measurement> list1 = eventLog1.getEventLog();
        HostAgent agent2 = hostAgentFactory.getHostAgent(new ConnectionString(host2), new InsecureTlsPolicy());
        PcrManifest manifest2 = agent2.getPcrManifest();
        Pcr pcr2 = manifest2.getPcr(19);        
        PcrEventLog eventLog2 = manifest2.getPcrEventLog(19);
        List<Measurement> list2 = eventLog2.getEventLog();
        
        log.debug("Host1: {}", host1);
        log.debug("Host2: {}", host2);
        
        // compare pcr 19 value
        log.info("PCR 19");
        log.debug("Host1: {}", pcr1.getValue().toString());
        log.debug("Host2: {}", pcr2.getValue().toString());

        // compare pcr 19 event log:
        log.info("MEASUREMENTS");
        HashSet<Measurement> extraHost1 = new HashSet<Measurement>(list1);
        extraHost1.removeAll(list2);
        HashSet<Measurement> extraHost2 = new HashSet<Measurement>(list2);
        extraHost2.removeAll(list1);
        for(Measurement m : extraHost1) {
            log.debug("Host1 has {} {}", m.getValue().toString(), m.getLabel());
        }
        for(Measurement m : extraHost2) {
            log.debug("Host2 has {} {}", m.getValue().toString(), m.getLabel());
        }
    }
    
    @Test
    public void testHostAttestations() throws KeyManagementException, IOException {
        String host1 = "vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;10.1.71.173";
        String host2 = "vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;10.1.71.175";
        
    }
    
}
