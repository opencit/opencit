/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.client.jaxrs;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.trustagent.model.*;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author ssbangal
 */
public class TrustAgentClient extends MtWilsonClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustAgentClient.class);
    
    public TrustAgentClient(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }
    
    public X509Certificate getAik() {
        log.debug("target: {}", getTarget().getUri().toString());
        X509Certificate aik = getTarget()
                .path("/aik")
                .request()
                .accept(CryptoMediaType.APPLICATION_PKIX_CERT)
                .get(X509Certificate.class);
        return aik;
    }

    public X509Certificate getAikCa() {
        log.debug("target: {}", getTarget().getUri().toString());
        X509Certificate aik = getTarget()
                .path("/aik/ca")
                .request()
                .accept(CryptoMediaType.APPLICATION_PKIX_CERT)
                .get(X509Certificate.class);
        return aik;
    }
    
    public HostInfo getHostInfo() {
        log.debug("target: {}", getTarget().getUri().toString());
        HostInfo hostInfo = getTarget()
                .path("/host")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(HostInfo.class);
        return hostInfo;
    }
    
    public void writeTag(byte[] tag, UUID hardwareUuid) {
        TagWriteRequest tagWriteRequest = new TagWriteRequest();
        tagWriteRequest.setTag(tag);
        tagWriteRequest.setHardwareUuid(hardwareUuid);
        getTarget()
                .path("/tag")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(tagWriteRequest));
    }
    
    public TpmQuoteResponse getTpmQuote(byte[] nonce, int[] pcrs) {
        TpmQuoteRequest tpmQuoteRequest = new TpmQuoteRequest();
        tpmQuoteRequest.setNonce(nonce);
        tpmQuoteRequest.setPcrs(pcrs);
        log.debug("target: {}", getTarget().getUri().toString());
        TpmQuoteResponse tpmQuoteResponse = getTarget()
                .path("/tpm/quote")
                .request()
                .accept(MediaType.APPLICATION_XML)
                .post(Entity.json(tpmQuoteRequest), TpmQuoteResponse.class);
        return tpmQuoteResponse;
    }

}
