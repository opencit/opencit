/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.attestation.client.jaxrs.WhiteList;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.CreateWhiteListRpcInput;
import com.intel.mtwilson.as.rest.v2.model.CreateWhiteListWithOptionsRpcInput;
import com.intel.mtwilson.as.rest.v2.model.WhitelistConfigurationData;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class WhiteListTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhiteListTest.class);

    private static WhiteList client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator.class);
        
        client = new WhiteList(My.configuration().getClientProperties());
    }
    
    /**
     * Note the path you need at the end of mtwilson.api.url is /mtwilson/v2 to
     * run this test:
     * <pre>
mtwilson.api.url=https\://10.1.71.56\:8443/mtwilson/v2
     * </pre>
     * 
     * @throws Exception 
     */
    @Test
    public void testCreateWhitelist() throws Exception {
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.155";
        gkvHost.AddOn_Connection_String = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        CreateWhiteListRpcInput rpcInput = new CreateWhiteListRpcInput();
        rpcInput.setHost(gkvHost);        
        boolean rpcOutput = client.createWhitelist(rpcInput);
    }

    @Test
    public void testCreateWhitelistWithOptions() throws Exception {
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.155";
        gkvHost.AddOn_Connection_String = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        WhitelistConfigurationData config = new WhitelistConfigurationData();
        config.setBiosWhiteList(true);
        config.setVmmWhiteList(true);
        config.setBiosPCRs("0,17");
        config.setVmmPCRs("18,19,20");
        config.setOverWriteWhiteList(false);
        config.setRegisterHost(false);
        config.setBiosMleName("Custom_BIOS_Name");
        config.setVmmMleName("Custom_VMM_Name");
        config.setTxtHostRecord(gkvHost);
        CreateWhiteListWithOptionsRpcInput rpcInput = new CreateWhiteListWithOptionsRpcInput();
        rpcInput.setWlConfig(config);        
        boolean rpcOutput = client.createWhitelistWithOptions(rpcInput);
    }

}
