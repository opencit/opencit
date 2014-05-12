/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.WhiteList;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.CreateWhiteListRpcInput;
import com.intel.mtwilson.as.rest.v2.model.CreateWhiteListWithOptionsRpcInput;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import java.util.LinkedHashMap;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class WhiteListTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileTest.class);

    private static WhiteList client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new WhiteList(My.configuration().getClientProperties());
    }
    
    @Test
    public void testCreateWhitelist() throws Exception {
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.155";
        gkvHost.AddOn_Connection_String = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        CreateWhiteListRpcInput rpcInput = new CreateWhiteListRpcInput();
        rpcInput.setHost(gkvHost);        
        LinkedHashMap rpcOutput = client.createWhitelist(rpcInput);
        log.debug(rpcOutput.toString());
    }

    @Test
    public void testCreateWhitelistWithOptions() throws Exception {
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.155";
        gkvHost.AddOn_Connection_String = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        HostConfigData config = new HostConfigData();
        config.setRegisterHost(true);
        config.setTxtHostRecord(gkvHost);
        CreateWhiteListWithOptionsRpcInput rpcInput = new CreateWhiteListWithOptionsRpcInput();
        rpcInput.setConfig(config);        
        LinkedHashMap rpcOutput = client.createWhitelistWithOptions(rpcInput);
        log.debug(rpcOutput.toString());
    }

}
