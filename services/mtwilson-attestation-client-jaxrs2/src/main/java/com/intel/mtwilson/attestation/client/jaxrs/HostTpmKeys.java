/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.as.rest.v2.model.BindingKeyEndorsementRequest;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Hosts</code> is the class used for creation, updation and deletion of Hosts in the Mt.Wilson system.
 * @author ssbangal
 */
public class HostTpmKeys extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostTpmKeys(URL url) throws Exception{
        super(url);
    }

    public HostTpmKeys(Properties properties) throws Exception {
        super(properties);
    }

    public HostTpmKeys(Configuration configuration) throws Exception {
        super(configuration);
    }
    
    public HostTpmKeys(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }
    
    public String createBindingKeyCertificate(BindingKeyEndorsementRequest obj) throws JsonProcessingException {
        log.debug("target: {}", getTarget().getUri().toString());
        ObjectMapper mapper = new ObjectMapper();
        log.debug("pojo: {}", mapper.writeValueAsString(obj));
        String pemCertificate = "";
        Object result = getTarget().path("rpc/certify-host-binding-key").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
        log.debug("Type of result is {}", result.getClass().getCanonicalName());
        if (result.getClass().equals(LinkedHashMap.class)) {
            LinkedHashMap resultMap = (LinkedHashMap)result;
            log.debug(resultMap.toString());
            if (resultMap.containsKey("binding_key_pem_certificate")) {
                pemCertificate = resultMap.get("binding_key_pem_certificate").toString().trim();
                log.debug("Result of certifying host binding key is {}.", pemCertificate);
            }
        }
        return pemCertificate;
    }
    
}
