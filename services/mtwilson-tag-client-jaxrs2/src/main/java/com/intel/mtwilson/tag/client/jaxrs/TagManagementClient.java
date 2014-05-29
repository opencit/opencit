/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.tag.model.*;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author ssbangal
 */
public class TagManagementClient extends MtWilsonClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagManagementClient.class);
    
    public TagManagementClient(Properties properties) throws Exception {
        super(properties);
    }
    public TagManagementClient(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }
    
    public Certificate createOneXml(UUID hostHardwareUuid, String selectionXml) {
        log.debug("target: {}", getTarget().getUri().toString());
        return createOneXml(hostHardwareUuid.toString(), selectionXml);
    }

    public Certificate createOneXml(String hostUuidOrIp, String selectionXml) {
        log.debug("target: {}", getTarget().getUri().toString());
        CertificateRequestLocator locator = new CertificateRequestLocator();
        locator.subject = hostUuidOrIp;
        Certificate certificate = 
                getTargetPathWithQueryParams("/tag-certificate-requests-rpc/provision", locator)
                .request()
                .accept(MediaType.APPLICATION_XML)
                .post(Entity.entity(selectionXml, MediaType.APPLICATION_XML), Certificate.class);
        return certificate;
    }


}
