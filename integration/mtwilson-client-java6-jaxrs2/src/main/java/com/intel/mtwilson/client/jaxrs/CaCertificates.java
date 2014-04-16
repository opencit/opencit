/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateFilterCriteria;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class CaCertificates extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public CaCertificates(URL url) throws Exception{
        super(url);
    }

    public CaCertificates(Properties properties) throws Exception {
        super(properties);
    }
    public CaCertificates(Configuration configuration) throws Exception {
        super(configuration);
    }
    public CaCertificates(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }
    
    public X509Certificate[] searchCaCertificates(CaCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        X509Certificate[] certificates = getTargetPathWithQueryParams("ca-certificates", criteria).request(OtherMediaType.APPLICATION_X_PEM_FILE).get(X509Certificate[].class);
        return certificates;
    }
    
    public X509Certificate retrieveCaCertificate(String type) {
        //  {id} can be:  "root", "saml", "tls", "privacy"
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", type);
        X509Certificate certificate = getTargetPath("ca-certificates/{id}").resolveTemplates(map).request(OtherMediaType.APPLICATION_PKIX_CERT).get(X509Certificate.class);
        return certificate;
    }
       
}
