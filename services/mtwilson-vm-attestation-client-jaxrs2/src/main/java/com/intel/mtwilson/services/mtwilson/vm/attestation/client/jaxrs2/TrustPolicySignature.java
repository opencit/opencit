/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.services.mtwilson.vm.attestation.client.jaxrs2;

import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author boskisha
 */
public class TrustPolicySignature extends MtWilsonClient {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustPolicySignature.class);

    public TrustPolicySignature(Properties properties) throws Exception {
        super(properties);
    }
    public TrustPolicySignature(URL url) throws Exception {
        super(url);
    }
    
//    public TrustPolicy signTrustPolicy(TrustPolicy trustPolicy) throws IOException, JAXBException, XMLStreamException {
//        log.debug("target: {}", getTarget().getUri().toString());
//        JAXB jaxb = new JAXB();
//        String signedPolicy = getTarget().path("trustpolicy-signature").request().accept(MediaType.APPLICATION_XML).post(Entity.xml(jaxb.write(trustPolicy)), String.class);
//        return jaxb.read(signedPolicy, TrustPolicy.class);        
//    }
    
    public String signTrustPolicy(String trustPolicy) throws IOException, JAXBException, XMLStreamException {
        log.debug("target: {}", getTarget().getUri().toString());
        String signedPolicy = getTarget().path("trustpolicy-signature").request().accept(MediaType.APPLICATION_XML).post(Entity.xml(trustPolicy), String.class);
        return signedPolicy;        
    }
}
