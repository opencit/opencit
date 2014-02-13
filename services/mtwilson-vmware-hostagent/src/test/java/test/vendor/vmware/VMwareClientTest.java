package test.vendor.vmware;

import org.junit.Test;
import com.intel.mtwilson.agent.vmware.*;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
//import com.vmware.vim25.InvalidLocaleFaultMsg;
//import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
//import com.vmware.vim25.RuntimeFaultFaultMsg;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import sun.misc.BASE64Encoder;
//import sun.misc.BASE64Decoder;
/**
 * These tests verify that the data model serializes and de-serializes properly.
 * 
 * Commented out the Base64 tests because:
 * 1) they passed, commons-encoder output is identical to the Sun encoder
 * 2) the Sun encoder is a private API, and on some JVM's it's not available to programs
 * 
 * When you use the commons encoder, be aware that:
 * Base64.encodeBase64String returns output with newlines to fit into 76 character wide space
 * So to get a "pure" string you need to do new String(Base64.encodeBase64(some bytes));
 *
 * @author jbuhacoff
 */
public class VMwareClientTest {
    private Logger log = LoggerFactory.getLogger(getClass());
	/*
    @Test
    public void commonsEncoderCompatibleWithSun() {
        String sunResult = new BASE64Encoder().encode("hello world!".getBytes());
        String commonsResult = Base64.encodeBase64String("hello world!".getBytes());
        System.out.println("hello world! encoded: "+sunResult);
        assertEquals(sunResult, commonsResult);
    }

    @Test
    public void commonsDecoderCompatibleWithSun() throws IOException {
        String sunResult = new String(new BASE64Decoder().decodeBuffer("aGVsbG8gd29ybGQh"));
        String commonsResult = new String(Base64.decodeBase64("aGVsbG8gd29ybGQh"));
        System.out.println("aGVsbG8gd29ybGQh decoded: "+sunResult);
        assertEquals(sunResult, commonsResult);
    }
    */
    /*
    @Test
    public void testHelper() {
        try {
            System.out.println(System.getenv());

            //HashMap result = VMwareHelper.getQuoteInformationForHost("10.1.71.108", "0,20", "https://10.1.71.115:444/sdk;RAUser;Intel123!");
//            System.out.println("Result - " + result.toString());
//            HashMap result = new VMwareClient().getQuoteInformationForHost("10.1.71.141", "0,20", "https://10.1.71.142:444/sdk;Administrator;P@ssw0rd");
            
            
            //HashMap result = VMwareHelper.getQuoteInformationForHost("10.1.71.154", "0,20", "https://10.1.71.142:444/sdk;Administrator;P@ssw0rd");	
//            System.out.println("Result - " + result.toString());
              
//            ManagedObjectReference result = VMwareHelper.findChild("10.1.71.108", "0,20", "https://10.1.71.115:444/sdk;RAUser;Intel123!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    */
    
    @Test
    public void testConnectAndFindHost() throws Exception {
        // these settings force the jvm to use the local fiddler proxy so we can see the traffic...
  System.setProperty("http.proxyHost", "127.0.0.1");
    System.setProperty("https.proxyHost", "127.0.0.1");
    System.setProperty("http.proxyPort", "8888");
    System.setProperty("https.proxyPort", "8888");
    System.setProperty("com.sun.management.jmxremote","true"); // to inform jconsole we want to be monitored
        /*
        VmwareHostAgentFactory factory = new VmwareHostAgentFactory();
        VmwareHostAgent agent = factory.getHostAgent("https://10.1.71.162/sdk;Administrator;intel123!;10.1.71.173", new InsecureTlsPolicy());
        log.debug("is tpm present? {}", agent.isTpmPresent());
        ManagedObjectReference[] array = agent.getClient().getHostReference("10.1.71.173");
        if( array == null ) {
            log.debug("did not find any items");
            return;
        }
        log.debug("found {} items", array.length);
        for(int i=0; i<array.length; i++) {
            log.debug("item: ", array[i].getType());
        }*/
        VMwareClient client = new VMwareClient();
        client.setTlsPolicy(new InsecureTlsPolicy());
        client.connect("https://10.1.71.162/sdk", "Administrator", "intel123!");
        ManagedObjectReference ref = client.getHostReference("10.1.71.173");
        if( ref == null ) {
            log.info("failed to get reference to host");
        }
        else {
            log.info("got reference!");
            client.getHostAttestationReport(ref, "10.1.71.173", "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24");
        }
        client.disconnect();
    }
    
    @Test
    public void clusterTest() throws Exception {
        
//        VMwareClient client = new VMwareClient();
//        client.setTlsPolicy(new InsecureTlsPolicy());
//        client.connect("https://10.1.71.162/sdk", "Administrator", "intel123!");
//        //List<String> ref = client.getClusterNames("https://10.1.71.162:443/sdk;Administrator;intel123!", "Folsom");
//        //List<String> ref = client.getClusterNames("https://10.1.71.162:443/sdk;Administrator;intel123!", "IBM DC");
//        List<String> ref = client.getDatacenterNames("https://10.1.71.162:443/sdk;Administrator;intel123!");
//        
//        
//        if( ref != null ) {
//            for (String str:ref){                
//                System.out.println(str);
//                System.out.println("\n\n***************************************************************");
//            }
//        }
//        else {
//            System.err.println("fail");
//        }
//        client.disconnect();
    }
}
