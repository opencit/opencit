/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.RegisterHosts;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.RegisterHostsRpcInput;
import com.intel.mtwilson.as.rest.v2.model.RegisterHostsWithOptionsRpcInput;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostConfigDataList;
import com.intel.mtwilson.datatypes.HostWhiteListTarget;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import java.util.LinkedHashMap;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class RegisterHostTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileTest.class);

    private static RegisterHosts client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new RegisterHosts(My.configuration().getClientProperties());
    }
    
    @Test
    public void testRegisterHost() throws Exception {
        TxtHostRecord host = new TxtHostRecord();
        host.HostName = "10.1.71.155";
        host.AddOn_Connection_String = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        TxtHostRecordList hostList = new TxtHostRecordList();
        hostList.getHostRecords().add(host);
        
        RegisterHostsRpcInput rpcInput = new RegisterHostsRpcInput();
        rpcInput.setHosts(hostList);
        LinkedHashMap rpcOutput = client.registerHosts(rpcInput);
        log.debug(rpcOutput.toString());
    }

    @Test
    public void testRegisterHostWithOptions() throws Exception {
        TxtHostRecord host = new TxtHostRecord();
        host.HostName = "10.1.71.155";
        host.AddOn_Connection_String = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        HostConfigData config = new HostConfigData();
        config.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
        config.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
        config.setTxtHostRecord(host);
        
        HostConfigDataList hostConfigDataList = new HostConfigDataList();
        hostConfigDataList.getHostRecords().add(config);
        
        RegisterHostsWithOptionsRpcInput rpcInput = new RegisterHostsWithOptionsRpcInput();
        rpcInput.setHosts(hostConfigDataList);
        LinkedHashMap rpcOutput = client.registerHostsWithOptions(rpcInput);
        log.debug(rpcOutput.toString());
    }

}
