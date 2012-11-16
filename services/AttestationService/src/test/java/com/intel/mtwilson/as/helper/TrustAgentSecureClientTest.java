package com.intel.mtwilson.as.helper;


import com.intel.mountwilson.as.helper.TrustAgentSecureClient;
import org.junit.Test;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.JAXBException;

/**
 *
* 
 * @author jbuhacoff
 */
public class TrustAgentSecureClientTest {
    
    public TrustAgentSecureClientTest() {
        
    }

    private void sendIdentityRequest(String hostname, int port) throws UnknownHostException, IOException, JAXBException, KeyManagementException, NoSuchAlgorithmException {
        System.out.println("Sending Generate Identity");
        byte[] data = "<identity_request></identity_request>".getBytes();
        TrustAgentSecureClient client = new TrustAgentSecureClient(hostname, port, data);
        client.sendQuoteRequest();
    }
    
    private void sendQuoteRequest(String hostname, int port) throws UnknownHostException, IOException, JAXBException, KeyManagementException, NoSuchAlgorithmException {
        System.out.println("Sending Generate Quote");
        byte[] data = "<quote_request><nonce>Iamnonce</nonce><pcr_list>3,19</pcr_list></quote_request>".getBytes();
        //             data = "<quote_request><nonce>+nao5lHKxcMoqIGY3LuAYQ==</nonce><pcr_list>3-5,4-8</pcr_list></quote_request>".getBytes();
        TrustAgentSecureClient client = new TrustAgentSecureClient(hostname, port, data);
        client.sendQuoteRequest();       
    }
    
    @Test
    public void testIdentityRequest() {
        try {
            sendIdentityRequest("10.1.71.145", 9999);
        }
        catch(Exception e) {
            System.err.println("Exception during testIdentityRequest: "+e.toString());
        }
    }

    @Test
    public void testQuoteRequest() {
        try {
            sendQuoteRequest("10.1.71.145", 9999);
        }
        catch(Exception e) {
            System.err.println("Exception during testIdentityRequest: "+e.toString());
        }
    }
    
    @Test
    public void testTASecureClient() {

//        String hostname = "10.1.71.96"; // ubuntu 
//        String hostname = "10.1.130.152"; // trust agent, seems to work fine
        String hostname = "10.1.71.145";
        int port = 9999;
        try {

//            System.out.println("Sending BAD request");
//            byte[] data = "<client_request></client_request>".getBytes();
//            TrustAgentSecureClient client = new TrustAgentSecureClient(hostname, port, data);
//            client.sendRequest();
//
//            System.out.println("Sending Generate Identity");
//            data = "<identity_request></identity_request>".getBytes();
//            client = new TrustAgentSecureClient(hostname, port, data);
//            client.sendRequest();
//
            System.out.println("Sending Generate Quote");
            byte[] data;
//             data = "<quote_request><nonce>+nao5lHKxcMoqIGY3LuAYQ==</nonce><pcr_list>3-5,4-8</pcr_list></quote_request>".getBytes();
             data = "<quote_request><nonce>Iamnonce</nonce><pcr_list>3,19</pcr_list></quote_request>".getBytes();
            TrustAgentSecureClient client = new TrustAgentSecureClient(hostname, port, data);
            client.sendQuoteRequest();

//            System.out.println("Result " + new TrustAgentSecureClient(hostname, port, null).getAIKCertificate());

        } catch (Throwable e) {
//            log.info("Error while contacting Trust Agent " + e.getMessage());
            e.printStackTrace();
        }
    }
}
