/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.CaCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateFilterCriteria;
import java.net.URL;
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

    public CaCertificates(URL url) {
        super(url);
    }

    public CaCertificates(Properties properties) throws Exception {
        super(properties);
    }
    
    public CaCertificateCollection searchCaCertificates(CaCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        // The possible values of the "id" attribute are
        // - root
        // - saml
        // - tls
        // - privacy
        CaCertificateCollection objCollection = getTargetPathWithQueryParams("mles", criteria).request(MediaType.APPLICATION_JSON).get(CaCertificateCollection.class);
        return objCollection;
    }
       
}
