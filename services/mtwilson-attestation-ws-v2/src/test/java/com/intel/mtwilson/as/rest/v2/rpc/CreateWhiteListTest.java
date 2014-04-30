/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostWhiteListTarget;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class CreateWhiteListTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateWhiteListTest.class);
    
    @Test
    public void testCreateWhiteList() throws Exception {
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.155";
        gkvHost.AddOn_Connection_String = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        
        CreateWhiteListRunnable whiteListRunnable = new CreateWhiteListRunnable();
        whiteListRunnable.setHost(gkvHost);
        whiteListRunnable.run();
        
        log.debug("Create white list status is {}", whiteListRunnable.getResult());
    }

    @Test
    public void testCreateWhiteListWithOptions() throws Exception {
        HostConfigData wlObj = new HostConfigData();        
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.155";
        gkvHost.AddOn_Connection_String = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        wlObj.setTxtHostRecord(gkvHost);
        wlObj.setBiosPCRs("0,17");
        wlObj.setVmmPCRs("18,19,20");
        wlObj.setBiosWhiteList(true);
        wlObj.setVmmWhiteList(true);
        wlObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
        wlObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
        wlObj.setRegisterHost(false);
        
        CreateWhiteListWithOptionsRunnable whiteListRunnable = new CreateWhiteListWithOptionsRunnable();
        whiteListRunnable.setHost(wlObj);
        whiteListRunnable.run();
        
        log.debug("Create white list status is {}", whiteListRunnable.getResult());
    }
    
}
