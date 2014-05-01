/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.adapter;

import com.intel.mtwilson.as.rest.v2.model.CreateWhiteListRpcInput;
import com.intel.mtwilson.as.rest.v2.rpc.CreateWhiteListRunnable;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.rpc.v2.resource.RunnableRpcAdapter;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class RunnableAdapterTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RunnableAdapterTest.class);

    public static class IntegerAdder implements Runnable {
        Integer x, y, result;
        @Override
        public void run() {
            result = x + y;
        }
    }
    
    @Test
    public void testRunnable() {
        IntegerAdder rpc = new IntegerAdder();
        rpc.x = 1;
        rpc.y = 2;
        RunnableRpcAdapter adapter = new RunnableRpcAdapter(rpc);
        adapter.invoke();
        log.debug("result = {}", rpc.result);
    }
    
    @Test
    public void testInputMapping() {
        String testInputJson = "{\"rpc_input\":{\"host\":{\"host_name\":\"10.1.71.155\",\"ipaddress\":null,\"port\":null,\"bios_name\":null,\"bios_version\":null,\"bios_oem\":null,\"vmm_name\":null,\"vmm_version\":null,\"vmm_osname\":null,\"vmm_osversion\":null,\"add_on_connection_string\":\"vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd\",\"description\":null,\"email\":null,\"location\":null,\"aik_certificate\":null,\"aik_public_key\":null,\"aik_sha1\":null,\"processor_info\":null},\"result\":false}}]]";
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.155";
        gkvHost.AddOn_Connection_String = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        CreateWhiteListRpcInput rpcInput = new CreateWhiteListRpcInput();
        rpcInput.setHost(gkvHost);
        
        CreateWhiteListRunnable whiteListRunnable = new CreateWhiteListRunnable();
        whiteListRunnable.rpcInput = rpcInput;
        
       // String testInputJson = "{\"rpc_input\":{\"host\":{\"host_name\":\"10.1.71.155\",\"ipaddress\":null,\"port\":null,\"bios_name\":null,\"bios_version\":null,\"bios_oem\":null,\"vmm_name\":null,\"vmm_version\":null,\"vmm_osname\":null,\"vmm_osversion\":null,\"add_on_connection_string\":\"vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd\",\"description\":null,\"email\":null,\"location\":null,\"aik_certificate\":null,\"aik_public_key\":null,\"aik_sha1\":null,\"processor_info\":null},\"result\":false}}";
        String testInputJson2 = "{\"host\":{\"host_name\":\"10.1.71.155\",\"ipaddress\":null,\"port\":null,\"bios_name\":null,\"bios_version\":null,\"bios_oem\":null,\"vmm_name\":null,\"vmm_version\":null,\"vmm_osname\":null,\"vmm_osversion\":null,\"add_on_connection_string\":\"vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd\",\"description\":null,\"email\":null,\"location\":null,\"aik_certificate\":null,\"aik_public_key\":null,\"aik_sha1\":null,\"processor_info\":null},\"result\":false}}";
        RunnableRpcAdapter adapter = new RunnableRpcAdapter(whiteListRunnable);
        //adapter.invoke();
        adapter.setInput(testInputJson);
        
    }
}
