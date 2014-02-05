/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.ssl;

import com.intel.dcsg.cpg.tls.policy.impl.*;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileNotFoundException;
import static org.junit.Assert.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.IOUtils;
//import org.apache.http.conn.scheme.Scheme;
//import org.apache.http.conn.ssl.SSLSocketFactory;

/**
 * This class tests a model where each destination address can have just
 * one TLS Policy associated with it.  Selecting this model means we would
 * have to edit the database schema to add a new mw_tls_policy table with
 * fields like (ID, InternetAddress, TlsPolicyName, TlsKeystore). 
 * So for example, there would only be one policy associated with a given
 * vcenter and it can be managed independently of any related host records.
 * 
 * The challenge is still that the clients just accept a URL and they open
 * their own SSL connection - so we can't even provide the right policy when
 * it's one policy per address... there is still a race condition for 
 * concurrent connections.
 *
 * @author jbuhacoff
 */
public class SinglePolicyPerAddressTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private ArrayList<String> getTargets() {
        // vmware: 10.1.71.175, 10.1.71.173, 10.1.71.176, 10.1.71.174
        // citrix: 10.1.71.201, 10.1.71.126
        // intel:  10.1.71.167, 10.1.71.170
        ArrayList<String> targets = new ArrayList<String>();
        targets.add("https://10.1.71.162/sdk"); //;Administrator;intel123!");
        targets.add("https://10.1.71.163/sdk"); //;Administrator;intel123!");
        targets.add("https://10.1.71.201/"); // root;P@ssw0rd
//        targets.add("https://10.1.71.126/;Administrator;intel123!");
//        targets.add("https://10.1.71.167:9999");
//        targets.add("https://10.1.71.170:9999");
        return targets;
    }
    
    @Test
    public void testTwoSslContextInstancesAreDifferent() throws NoSuchAlgorithmException {
    	SSLContext ctx1 = SSLContext.getInstance("SSL");
    	SSLContext ctx2 = SSLContext.getInstance("SSL");
        assertNotEquals(ctx1,ctx2);
        log.debug("context 1 hashcode {}", ctx1.hashCode());
        log.debug("context 2 hashcode {}", ctx2.hashCode());
    }
    
    private void connect(URL url, TlsPolicy tlsPolicy) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        /*
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new X509TrustManager[] { tlsPolicy.getTrustManager() }, null); // key manager, trust manager, securerandom
            SSLSocketFactory sf = new SSLSocketFactory(
                sslcontext,
                tlsPolicy.getHostnameVerifier()
                );
            Scheme https = new Scheme("https", port, sf); // URl defaults to 443 for https but if user specified a different port we use that instead
            sr.register(https);            
*/
    	SSLContext ctx = SSLContext.getInstance("SSL"); // it's a factory, returns a new instance
        ctx.init(null, new javax.net.ssl.TrustManager[]{ tlsPolicy.getTrustManager() }, null); 
        HttpsURLConnection.setDefaultHostnameVerifier(tlsPolicy.getHostnameVerifier()); // without this, even InsecureTlsPolicy will not prevent java.security.cert.CertificateException: No subject alternative names matching IP address 10.1.71.162 found
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());   // without this, even with InsecureTlsPolicy we get ERROR: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certificateion path to requested target
//        SSLSocketFactory sslsocketfactory = ctx.getSocketFactory();
//        SSLSocket sock = (SSLSocket) sslsocketfactory.createSocket();
        InputStream in = url.openStream();
        String content = IOUtils.toString(in);
        System.out.println("---\n"+url.toString()+"\n---\n"+content+"\n---\n\n");
        
    }
    
    @Test
    public void testInsecurePolicy() throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
        TlsPolicy tlsPolicy = new InsecureTlsPolicy();
        ArrayList<String> targets = getTargets();
        for(String target : targets) {
            URL url = new URL(target); // throws MalformedURLException
            try {
                connect(url,tlsPolicy); // throws NoSuchAlgorithmException, KeyManagementException, IOException
            }
            catch(FileNotFoundException e) {
                log.error("File not found (but ssl connection worked): {}", url.toString());
            }
        }
    }
    
    /**
     * This one should work because we can create a different instance for each connection;
     * However it's not an option for production since we don't control the client code...
     */
    @Test
    public void testApacheHttpConnectionsWithTrustFirstPolicy() {
        // set up the workload: 
        ArrayList<String> targets = getTargets();
//        HashMap<String,TrustFirstCertificateTlsPolicy> map = new HashMap<String,TrustFirstCertificateTlsPolicy>();
        for(String url : targets) {/*
            ByteArrayResource resource = new ByteArrayResource();
            
            TrustFirstCertificateTlsPolicy tlsPolicy = new TrustFirstCertificateTlsPolicy();
            map.put(tlsPolicy);*/
        }
        // connect one at a time with a trust first policy to collect the current certificates
        // now we have a "trust first certicate" policy and keystore for each one.
        // spawn a thread for each connection and connect again, look for errors; expect some random errors
    }

    /**
     * This one should result in errors because the ssl context is shared
     */
    @Test
    public void testJavaSslContextConnectionsWithTrustFirstPolicy() {
        // set up the workload: 
        // vmware: 10.1.71.175, 10.1.71.173, 10.1.71.176, 10.1.71.174
        // citrix: 10.1.71.201, 10.1.71.126
        // intel:  10.1.71.167, 10.1.71.170
        ArrayList<String> targets = new ArrayList<String>();
        targets.add("https://10.1.71.162/sdk;");
        // connect one at a time with a trust first policy to collect the current certificates
        // now we have a "trust first certicate" policy and keystore for each one.
        // spawn a thread for each connection and connect again, look for errors; expect some random errors
    }
    
    @Test
    public void testConnectionsWithInsecurePolicy() {
        // set up the workload: 
        // vmware: 10.1.71.175, 10.1.71.173, 10.1.71.176, 10.1.71.174
        // citrix: 10.1.71.201, 10.1.71.126
        // intel:  10.1.71.167, 10.1.71.170
        // now we have an "insecure" policy 
        // spawn a thread for each connection and connect again, look for errors; expect none
    }
    
}

