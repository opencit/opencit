/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.agent.intel;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.client.jaxrs.MtWilsonClient;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.trustagent.model.TpmQuoteRequest;
import com.intel.mtwilson.trustagent.model.TpmQuoteResponse;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class TrustAgentJaxrsClient extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public TrustAgentJaxrsClient(URL url) {
        super(url);
    }

    public TrustAgentJaxrsClient(Properties properties) throws Exception {
        super(properties);
    }
    public TrustAgentJaxrsClient(Configuration configuration) throws Exception {
        super(configuration);
    }
    
    public X509Certificate getAikCertificate() {
        log.debug("target: {}", getTarget().getUri().toString());
        X509Certificate aikCertificate = getTarget()
                .path("/aik")
                .request()
                .accept(OtherMediaType.APPLICATION_PKIX_CERT)
                .get(X509Certificate.class);
        return aikCertificate;
    }
    
    public X509Certificate getAikCaCertificate() {
        log.debug("target: {}", getTarget().getUri().toString());
        X509Certificate aikCaCertificate = getTarget()
                .path("/aik/ca")
                .request()
                .accept(OtherMediaType.APPLICATION_PKIX_CERT)
                .get(X509Certificate.class);
        return aikCaCertificate;
    }

    public PcrManifest getPcrManifest() {
        log.debug("target: {}", getTarget().getUri().toString());
        byte[] nonce = RandomUtil.randomByteArray(20); // TODO: INSECURE   hash with host ip address (and do same automatically on host) for MITM protection
        int[] pcrs = new int[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23 };
        
        TpmQuoteRequest tpmQuoteRequest = new TpmQuoteRequest(nonce, pcrs);
        
        TpmQuoteResponse tpmQuoteResponse = getTarget()
                .path("/tpm/quote")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(tpmQuoteRequest, MediaType.APPLICATION_JSON), TpmQuoteResponse.class);
        
        return null; // TODO:  extract the pcr manifest from the quote response and construct a PcrManifest object;  can only be done after eventLog in tpmquoteresponse has been normalized to model object instead of embedded xml string or else we have to parse it here...
    }
    
    

}
