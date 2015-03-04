/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.as.rest.v2.model.BindingKeyEndorsementRequest;
import com.intel.mtwilson.as.rest.v2.model.SigningKeyEndorsementRequest;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    public X509Certificate createBindingKeyCertificate(BindingKeyEndorsementRequest obj) throws JsonProcessingException, Base64DecodingException, CertificateException {
        log.debug("target: {}", getTarget().getUri().toString());
        ObjectMapper mapper = new ObjectMapper();
        log.debug("pojo: {}", mapper.writeValueAsString(obj));
        X509Certificate bindingKeyDerCertificate = null;
        Object result = getTarget().path("rpc/certify-host-binding-key").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
        log.debug("Type of result is {}", result.getClass().getCanonicalName());
        if (result.getClass().equals(LinkedHashMap.class)) {
            LinkedHashMap resultMap = (LinkedHashMap)result;
            log.debug(resultMap.toString());
            if (resultMap.containsKey("binding_key_der_certificate")) {
                String base64EncodedCert = resultMap.get("binding_key_der_certificate").toString().trim();
                log.debug("Base 64 encoded binding certificate is {}", base64EncodedCert);
                byte[] decode = Base64.decode(base64EncodedCert);
                    bindingKeyDerCertificate = X509Util.decodeDerCertificate(decode);
                    log.debug("Successfully retrieved the certified binding key. {}.", 
                            X509Util.encodePemCertificate(bindingKeyDerCertificate));
            }
        }
        return bindingKeyDerCertificate;
    }

    public X509Certificate createSigningKeyCertificate(SigningKeyEndorsementRequest obj) throws JsonProcessingException, Base64DecodingException, CertificateException {
        log.debug("target: {}", getTarget().getUri().toString());
        ObjectMapper mapper = new ObjectMapper();
        log.debug("pojo: {}", mapper.writeValueAsString(obj));
        X509Certificate signingKeyDerCertificate = null;
        Object result = getTarget().path("rpc/certify-host-signing-key").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
        log.debug("Type of result is {}", result.getClass().getCanonicalName());
        if (result.getClass().equals(LinkedHashMap.class)) {
            LinkedHashMap resultMap = (LinkedHashMap)result;
            log.debug(resultMap.toString());
            if (resultMap.containsKey("signing_key_der_certificate")) {
                String base64EncodedCert = resultMap.get("signing_key_der_certificate").toString().trim();
                log.debug("Base 64 encoded signing certificate is {}", base64EncodedCert);
                byte[] decode = Base64.decode(base64EncodedCert);
                signingKeyDerCertificate = X509Util.decodeDerCertificate(decode);
                log.debug("Successfully retrieved the certified signing key. {}.",
                        X509Util.encodePemCertificate(signingKeyDerCertificate));
            }
        }
        return signingKeyDerCertificate;
    }
    
}
