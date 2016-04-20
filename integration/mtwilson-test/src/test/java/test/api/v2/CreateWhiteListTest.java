/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.api.v2;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.agent.citrix.CitrixHostAgentFactory;
import com.intel.mtwilson.agent.intel.IntelHostAgentFactory;
import com.intel.mtwilson.agent.vmware.VmwareHostAgentFactory;
import com.intel.mtwilson.as.rest.v2.model.WhitelistConfigurationData;
import com.intel.mtwilson.as.rest.v2.rpc.CreateWhiteListRunnable;
import com.intel.mtwilson.as.rest.v2.rpc.CreateWhiteListWithOptionsRunnable;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostWhiteListTarget;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class CreateWhiteListTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateWhiteListTest.class);
    
    /**
     * Avoid this exception: 
     * <pre>
     * java.lang.IllegalArgumentException: Cannot create Host Agent for 10.1.71.155: java.lang.UnsupportedOperationException: Unsupported host type: vmware
     * </pre>
     * 
     */
    @BeforeClass
    public static void registerHostAgents() {
        Extensions.register(VendorHostAgentFactory.class, VmwareHostAgentFactory.class);
        Extensions.register(VendorHostAgentFactory.class, CitrixHostAgentFactory.class);
        Extensions.register(VendorHostAgentFactory.class, IntelHostAgentFactory.class);
    }
    
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
        WhitelistConfigurationData wlObj = new WhitelistConfigurationData();        
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
        whiteListRunnable.setWlConfig(wlObj);
        whiteListRunnable.run();
        
        log.debug("Create white list status is {}", whiteListRunnable.getResult());
    }
    
}
