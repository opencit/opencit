/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import java.net.URL;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

/**
 *
 * @author ssbangal
 */
public class DeployTagCertificate extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Selections.class);

    public DeployTagCertificate(URL url) throws Exception{
        super(url);
    }

    public DeployTagCertificate(Properties properties) throws Exception {
        super(properties);
    }    
        
    /**
     * This function verifies whether the certificate was created for the specified host and deploys the
     * certificate on the host if there is a match. 
     * @param certificateId UUID of the certificate that needs to be deployed on to the host.
     * @param host IP address or the FQDN name of the host for which the certificate has to be deployed.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_certificates:deploy,hosts:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/rpc/deploy-tag-certificate
     * Input: {"certificate_id":"a6544ff4-6dc7-4c74-82be-578592e7e3ba","host":"18=92.168.0.1"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  DeployTagCertificate client = new DeployTagCertificate(My.configuration().getClientProperties());
     *  client.deployTagCertificate(UUID.valueOf("a6544ff4-6dc7-4c74-82be-578592e7e3ba"));
     * </pre>
     */
    public void deployTagCertificate(UUID certificateId, String host) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTarget().path("rpc/deploy-tag-certificate").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(certificateId));
        if( !obj.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
            throw new WebApplicationException("Deploy tag certificate failed");
        }
    }
        
}
