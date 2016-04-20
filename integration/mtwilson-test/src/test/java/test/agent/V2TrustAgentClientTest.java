/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.agent;

import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import java.io.IOException;
import java.net.URL;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.performance.report.PerformanceInfo;
import com.intel.dcsg.cpg.performance.report.PerformanceUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import com.intel.mtwilson.trustagent.client.jaxrs.TrustAgentClient;
import com.intel.mtwilson.trustagent.model.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.BeforeClass;

/**
 *
 * @author jbuhacoff
 */
public class V2TrustAgentClientTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    private static TrustAgentClient client;
    
    @BeforeClass
    public static void createClient() throws Exception {
        TlsConnection tlsConnection = new TlsConnection(new URL("https://10.1.71.171:1443/v2"), new InsecureTlsPolicy());
        Properties properties = new Properties();
        properties.setProperty("mtwilson.api.username", "mtwilson");
        properties.setProperty("mtwilson.api.password", "");
        properties.setProperty("org.glassfish.jersey.filter.LoggingFilter.printEntity", "true");
        client = new TrustAgentClient(properties, tlsConnection);
    }
    
    @Test
    public void testHostInfoCommand() throws Exception {
        HostInfo hostInfo = client.getHostInfo();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        log.debug(mapper.writeValueAsString(hostInfo));
    }
    
    @Test
    public void testSetAssetTagCommand() throws IOException, DecoderException {
        String hash = "8f110749fd76cc35526c2ed30c95ed113fd0220a";
        String uuid = "f4b17194-cae7-11df-b40b-001517fa9844";
        client.writeTag(Hex.decodeHex(hash.toCharArray()), UUID.valueOf(uuid));
    }
    
    @Test
    public void testTpmQuote() throws Exception {
        TpmQuoteRequest tpmQuoteRequest = new TpmQuoteRequest();
        tpmQuoteRequest.setNonce(RandomUtil.randomByteArray(20));
        tpmQuoteRequest.setPcrs(new int[] { 0, 17, 18, 19 });
        TpmQuoteResponse tpmQuoteResponse = client.getTpmQuote(tpmQuoteRequest.getNonce(), tpmQuoteRequest.getPcrs());
        ObjectMapper mapper = new ObjectMapper();
//        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        log.debug(mapper.writeValueAsString(tpmQuoteResponse));
    }
    
    public static class QuoteTask implements Runnable {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QuoteTask.class);
        
        @Override
        public void run() {
            try {
                TpmQuoteRequest tpmQuoteRequest = new TpmQuoteRequest();
                tpmQuoteRequest.setNonce(RandomUtil.randomByteArray(20));
                tpmQuoteRequest.setPcrs(new int[] { 0, 17, 18, 19 });
                TpmQuoteResponse tpmQuoteResponse = client.getTpmQuote(tpmQuoteRequest.getNonce(), tpmQuoteRequest.getPcrs());
                ObjectMapper mapper = new ObjectMapper();
                log.debug("quote response for nonce {} is {}", Base64.encodeBase64String(tpmQuoteRequest.getNonce()), mapper.writeValueAsString(tpmQuoteResponse));
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
        
    }
    
    @Test
    public void testConcurrentTpmQuotes() throws Exception {
        QuoteTask task1 = new QuoteTask();
        QuoteTask task2 = new QuoteTask();
        PerformanceInfo performanceInfo = PerformanceUtil.measureMultipleConcurrentTasks(2000, task1, task2);
        ObjectMapper mapper = new ObjectMapper();
        log.debug("performance: {}", mapper.writeValueAsString(performanceInfo));
    }
}
