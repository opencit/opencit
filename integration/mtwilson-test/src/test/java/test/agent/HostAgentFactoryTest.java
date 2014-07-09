/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.agent;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.agent.vmware.VMwareClient;
import com.intel.mtwilson.agent.vmware.VmwareHostAgent;
import com.intel.mtwilson.agent.vmware.VmwareHostAgentFactory;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactory;
import com.intel.mtwilson.tls.policy.factory.impl.TblHostsTlsPolicyFactory;
import com.intel.mtwilson.tls.policy.factory.impl.TxtHostRecordTlsPolicyFactory;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class HostAgentFactoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostAgentFactoryTest.class);

    @BeforeClass
    public static void registerExtensions() {
        Extensions.register(VendorHostAgentFactory.class, VmwareHostAgentFactory.class);
        Extensions.register(TlsPolicyFactory.class, TblHostsTlsPolicyFactory.class);
        Extensions.register(TlsPolicyFactory.class, TxtHostRecordTlsPolicyFactory.class);
        Extensions.register(TlsPolicyCreator.class, InsecureTlsPolicyCreator.class);        
    }
    
    @Test
    public void testCreateHostAgentFromTxtHostRecord() {
        TxtHostRecord host = new TxtHostRecord();
        host.HostName = "10.1.71.173";
        host.AddOn_Connection_String = "vmware:https://10.1.71.162:443/sdk;Administrator;intel123!";
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent agent = factory.getHostAgent(host);
        log.debug("agent class {}", agent.getClass().getName());
        if( agent instanceof VmwareHostAgent ) {
            VMwareClient client = ((VmwareHostAgent)agent).getClient();
            log.debug("vcenter version: {}", client.getVCenterVersion());
        }
    }
}
