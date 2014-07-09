/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.api.v2;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.agent.citrix.CitrixHostAgentFactory;
import com.intel.mtwilson.agent.intel.IntelHostAgentFactory;
import com.intel.mtwilson.agent.vmware.VmwareHostAgentFactory;
import com.intel.mtwilson.as.rest.v2.rpc.CreateWhiteListRunnable;
import com.intel.mtwilson.as.rest.v2.rpc.RegisterHostsRunnable;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class WhiteListTest {
    
    @BeforeClass 
    public static void registerPluginsForTest() {
        Extensions.register(VendorHostAgentFactory.class, VmwareHostAgentFactory.class);
        Extensions.register(VendorHostAgentFactory.class, CitrixHostAgentFactory.class);
        Extensions.register(VendorHostAgentFactory.class, IntelHostAgentFactory.class);
    }
    
    @Test
    public void CreateWhiteListWithDefaultOptions() throws Exception {
        CreateWhiteListRunnable runObj = new CreateWhiteListRunnable();
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.HostName = "10.1.71.175";
        hostObj.AddOn_Connection_String = "https://10.1.71.162:443/sdk;Administrator;intel123!";
//        hostObj.HostName = "10.1.71.91";
//        hostObj.AddOn_Connection_String = "http://10.1.71.91:443/;root;P@ssw0rd";
        
//        hostObj.HostName = "10.1.71.45";
//        hostObj.AddOn_Connection_String = "https://10.1.71.45:9999";
        runObj.setHost(hostObj);
        runObj.run();
    }
    
    @Test
    public void testRegisterHost() throws Exception {
        RegisterHostsRunnable runObj = new RegisterHostsRunnable();
        TxtHostRecord hostObj = new TxtHostRecord();
//        hostObj.HostName = "10.1.71.175";
//        hostObj.AddOn_Connection_String = "https://10.1.71.162:443/sdk;Administrator;intel123!";
        hostObj.HostName = "10.1.71.91";
        hostObj.AddOn_Connection_String = "http://10.1.71.91:443/;root;P@ssw0rd";
        TxtHostRecordList hosts = new TxtHostRecordList();
        hosts.getHostRecords().add(hostObj);
        runObj.setHosts(hosts);
        runObj.run();
        
    }
    
}
