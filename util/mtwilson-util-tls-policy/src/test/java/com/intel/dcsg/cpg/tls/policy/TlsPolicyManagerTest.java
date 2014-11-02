/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.tls.policy.impl.CertificateTlsPolicy;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.net.MalformedURLException;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
//import com.intel.dcsg.cpg.tls.policy.impl.TrustKnownCertificateTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.ArrayCertificateRepository;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyManagerTest {
    private static int timeout = 30; // seconds
    private static Logger log = LoggerFactory.getLogger(TlsPolicyManagerTest.class);
    private static boolean useTlsPolicyManager = false;
    
    private ArrayList<String> getTargets() {
        // vmware: 10.1.71.175, 10.1.71.173, 10.1.71.176, 10.1.71.174
        // citrix: 10.1.71.201, 10.1.71.126
        // intel:  10.1.71.167, 10.1.71.170
        ArrayList<String> targets = new ArrayList<String>();
        for(int i=0; i<5; i++) {
        targets.add("https://10.1.71.162/sdk"); //;Administrator;intel123!");
        targets.add("https://10.1.71.163/sdk"); //;Administrator;intel123!");
        targets.add("https://10.1.71.201/"); // root;P@ssw0rd
        }
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
    
    private static void prepare(URL url, TlsPolicy tlsPolicy) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        log.debug("Preparing policy {} for {}",tlsPolicy.getClass().getName(), url.toString());
//        printCertificates(tlsPolicy.getCertificateRepository().getCertificates());
    	SSLContext ctx = SSLContext.getInstance("SSL"); // it's a factory, returns a new instance
        ctx.init(null, new javax.net.ssl.TrustManager[]{ tlsPolicy.getTrustManager() }, null); 
        HttpsURLConnection.setDefaultHostnameVerifier(tlsPolicy.getHostnameVerifier()); // without this, even InsecureTlsPolicy will not prevent java.security.cert.CertificateException: No subject alternative names matching IP address 10.1.71.162 found
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());   // without this, even with InsecureTlsPolicy we get ERROR: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certificateion path to requested target
    }

    private static void prepareWithTlsPolicyManager(URL url, TlsPolicy tlsPolicy) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        log.debug("Preparing policy {} for {} using TLS Policy Manager",tlsPolicy.getClass().getName(), url.toString());
//        printCertificates(tlsPolicy.getCertificateRepository().getCertificates());
        TlsPolicyManager.getInstance().setTlsPolicy(url.getHost(), tlsPolicy);
    	SSLContext ctx = SSLContext.getInstance("SSL"); // it's a factory, returns a new instance
        ctx.init(null, new javax.net.ssl.TrustManager[]{ TlsPolicyManager.getInstance().getTrustManager() }, null); 
        HttpsURLConnection.setDefaultHostnameVerifier(TlsPolicyManager.getInstance()); // without this, even InsecureTlsPolicy will not prevent java.security.cert.CertificateException: No subject alternative names matching IP address 10.1.71.162 found
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());   // without this, even with InsecureTlsPolicy we get ERROR: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certificateion path to requested target
    }
    
    private static void connect(URL url, TlsPolicy tlsPolicy) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        if( useTlsPolicyManager ) {
            prepareWithTlsPolicyManager(url,tlsPolicy);
        }
        else {
            prepare(url,tlsPolicy);
        }
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
//        SSLSocketFactory sslsocketfactory = ctx.getSocketFactory();
//        SSLSocket sock = (SSLSocket) sslsocketfactory.createSocket();
        log.debug("Connecting to {} with policy: {}", url.toString(), tlsPolicy.getClass().getName());
        InputStream in = url.openStream();
        String content = IOUtils.toString(in);
        System.out.println("---\n"+url.toString()+"\n---\n"+content+"\n---\n\n");
        
    }
    
    @Test
    public void testSSLContextGetInstanceParameters() throws NoSuchAlgorithmException {
    	SSLContext ctx;
        ctx = SSLContext.getInstance("SSL");
//    	ctx = SSLContext.getInstance("SSLv2"); // throws NoSuchAlgorithmException
    	ctx = SSLContext.getInstance("SSLv3");
    	ctx = SSLContext.getInstance("TLS");
    	ctx = SSLContext.getInstance("TLSv1.1");
    	ctx = SSLContext.getInstance("TLSv1.2");
//    	ctx = SSLContext.getInstance("TLS1.1");// throws NoSuchAlgorithmException
//    	ctx = SSLContext.getInstance("TLS1.2");// throws NoSuchAlgorithmException
        
    }
    
    @Test
    public void testInsecurePolicySingleThreadedWithTlsConnection() throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
        TlsPolicy tlsPolicy = new InsecureTlsPolicy();
        ArrayList<String> targets = getTargets();
        for(String target : targets) {
            URL url = new URL(target); // throws MalformedURLException
            TlsConnection tlsConnection = new TlsConnection(url, tlsPolicy);
            HttpsURLConnection urlConnection = tlsConnection.openConnection();
            urlConnection.connect();
            log.debug("connected to {}", url);
            urlConnection.disconnect();
        }
    }
    
    
    @Test
    public void testInsecurePolicySingleThreaded() throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
        TlsPolicy tlsPolicy = new InsecureTlsPolicy();
        ArrayList<String> targets = getTargets();
        for(String target : targets) {
            URL url = new URL(target); // throws MalformedURLException
            try {
                connect(url,tlsPolicy); // throws NoSuchAlgorithmException, KeyManagementException, IOException
            }
            catch(FileNotFoundException e) {
                log.debug("File not found (but ssl connection worked): {}", url.toString());
            }
        }
    }
    
    private static void printCertificates(X509Certificate[] list) {
        for(X509Certificate cert : list) {
            log.debug("Certificate: {}", cert.getSubjectX500Principal().getName());
        }
    }
    private static void printCertificates(List<X509Certificate> list) {
        for(X509Certificate cert : list) {
            log.debug("Certificate: {}", cert.getSubjectX500Principal().getName());
        }
    }
    
    @Test
    public void testKnownCertificatePolicySingleThreaded() throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
        ArrayList<String> targets = getTargets();
        // first, obtain the certificate for each server and save it as the trusted certificate
        HashMap<String,ArrayCertificateRepository> trustStore = getCurrentCertificates(targets);
        // second, create a known certificate policy for each server using the saved certificate
        for(String target : targets) {
            TlsPolicy tlsPolicy = new CertificateTlsPolicy(trustStore.get(target));
            URL url = new URL(target); // throws MalformedURLException
            try {
                connect(url,tlsPolicy); // throws NoSuchAlgorithmException, KeyManagementException, IOException
            }
            catch(FileNotFoundException e) {
                log.debug("File not found (but ssl connection worked): {}", url.toString());
            }
        }
    }
        
    @Test
    public void testKnownCertificatePolicyMultiThreaded() throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
        ArrayList<String> targets = getTargets();
        List<ServerTask> tasks = prepareServerTasks(targets);
        scheduleConcurrentServerTasks(tasks);
    }

    @Test
    public void testKnownCertificatePolicyMultiThreadedWithManager() throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
        ArrayList<String> targets = getTargets();
        List<ServerTask> tasks = prepareServerTasks(targets);
        useTlsPolicyManager = true;
        scheduleConcurrentServerTasks(tasks);
    }
    
    
    private static HashMap<String,ArrayCertificateRepository> getCurrentCertificates(List<String> targets) throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
        HashMap<String,ArrayCertificateRepository> trustStore = new HashMap<String,ArrayCertificateRepository>();
        // prepare a task for each target to download the certs
        ArrayList<CertificateDownloadTask> tasks = new ArrayList<CertificateDownloadTask>();
        for(String target : targets) {
            CertificateDownloadTask downloadTask = new CertificateDownloadTask(new URL(target));
            tasks.add(downloadTask);
        }
        // run them concurrently
        scheduleConcurrentServerTasks(tasks);
        // now get the results
        for(CertificateDownloadTask task : tasks) {
            if( task.isCompleted() ) {
                trustStore.put(task.getURL().toExternalForm(),new ArrayCertificateRepository(task.getCertificates()));
            }
        }
        return trustStore;
    }
    
    private static List<ServerTask> prepareServerTasks(List<String> targets) throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
        // first, obtain the certificate for each server and save it as the trusted certificate
        HashMap<String,ArrayCertificateRepository> trustStore = getCurrentCertificates(targets);

        // second, create a known certificate policy for each server using the saved certificate and schedule the task
        ArrayList<ServerTask> tasks = new ArrayList<ServerTask>();
        for(String target : targets) {
            URL url = new URL(target); // throws MalformedURLException
            TlsPolicy tlsPolicy = new CertificateTlsPolicy(trustStore.get(target));
            ServerTask task = new ServerTask(url,tlsPolicy);
            tasks.add(task);
        }
        return tasks;
    }
    
    private static void scheduleConcurrentServerTasks(List<? extends Task> tasks) {
        ExecutorService scheduler = Executors.newFixedThreadPool(tasks.size()); 
        ArrayList<Future<?>> taskStatus = new ArrayList<Future<?>>();
        for(Task task : tasks) {
            Future<?> status = scheduler.submit(task);
            taskStatus.add(status);
        }
        // wait for all tasks to complete
        for (Future<?> status : taskStatus) {
            try {
                status.get(timeout, TimeUnit.SECONDS); // return value will always be null because we submitted "Runnable" tasks
            } catch (Exception ex) {
                // we will log the exception and ignore the error.
                log.error("Exception while retrieving the status of the tasks. {}", ex.getMessage());
            }
        }
        // print the status of each task
        for(Task task : tasks) {
            printTaskStatus(task);
        }
    }
    
    private static void printTaskStatus(Task task) {
        log.debug("Task results: {}", task.getId());
        if( task.isCompleted() ) {
            log.debug("+ completed");
        }
        else if( task.isError() ) {
            log.debug("+ error: {}", task.getCause().toString());
        }
        else {
            log.debug("+ timeout");
        }
    }
            
    
    private static class ServerTask extends Task {
        private final URL url;
        private final TlsPolicy tlsPolicy;
        public ServerTask(URL url, TlsPolicy tlsPolicy) {
            super(url.toString());
            this.url = url;
            this.tlsPolicy = tlsPolicy;
        }
        @Override
        public void execute() throws Exception {
            connect(url,tlsPolicy);
        }
        public URL getURL() { return url; }
    }
    
    private static class CertificateDownloadTask extends Task {
        private final URL url;
        private X509Certificate[] certificates = null;
        public CertificateDownloadTask(URL url) {
            super(url.toString());
            this.url = url;
        }
        @Override
        public void execute() throws Exception {
            log.debug("Saving server certificates for {}", url.toString());
            certificates = TlsUtil.getServerCertificates(url);
            printCertificates(certificates);
        }
        public URL getURL() { return url; }
        public X509Certificate[] getCertificates() { return certificates; }
    }
    
    /**
     * XXX TODO move this class to possibly a new module like cpg-concurrent or cpg-concurrency
     */
    private static class Task implements Runnable {
        private final String id;
        private Throwable error = null;
        private boolean completed = false;
        public Task(String id) {
            this.id = id;
        }
        @Override
        public void run() {
            try {
                execute();
                completed = true;
            }
            catch(Throwable e) {
                error = e;
            }
        }
        public void execute() throws Exception {
        }
        public String getId() { return id; }
        public boolean isError() { return error != null; }
        public Throwable getCause() { return error; }
        public boolean isCompleted() { return completed; }
    }
    
}
