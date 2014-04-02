/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.tag.model.Certificate;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
//import org.restlet.data.Status;
//import org.restlet.resource.ResourceException;
//import org.restlet.resource.ServerResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The "deploy" link next to each certificate in the UI calls this RPC
 * 
 * @author ssbangal
 */
@RPC("deploy-tag-certificate")
@JacksonXmlRootElement(localName="deploy_tag_certificate")
public class DeployTagCertificate implements Runnable{
    
    private Logger log = LoggerFactory.getLogger(getClass().getName());
       
    private UUID certificateId;
    private InternetAddress host;
    
    public UUID getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(UUID certificateId) {
        this.certificateId = certificateId;
    }

    public InternetAddress getHost() {
        return host;
    }

    public void setHost(InternetAddress host) {
        this.host = host;
    }
    
    
    @Override
    public void run() {
        log.debug("Got request to deploy certificate with ID {}.", certificateId);        
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
        
            Certificate obj = dao.findById(certificateId);
            if (obj != null) 
            {
                // Before deploying, we need to verify if the host is same as the one for which the certificate was created.
                List<TxtHostRecord> hostList = Global.mtwilson().queryForHosts(host.toString(), true);
                if(hostList == null || hostList.isEmpty() ) {
                    log.error("No hosts were returned back matching name " + host.toString());
                    Response.status(Response.Status.NOT_FOUND);
                    throw new WebApplicationException("No hosts were found matching the specified criteria.", Response.Status.NOT_FOUND);
                }
                TxtHostRecord hostRecord = hostList.get(0);
                
                if (!hostRecord.Hardware_Uuid.equals(obj.getSubject())) {
                    log.error("The certificate provided [{}] does not map to the host specified [{}]. Certificate will not be deployed on the host.", obj.getSubject(), hostRecord.Hardware_Uuid);
                    throw new WebApplicationException("The certificate provided does not map to the host specified. Certificate will not be deployed on the host.", Response.Status.CONFLICT);
                }
                
                deployAssetTagToHost(obj.getSha1(), hostRecord);
            }

        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate deployment.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        } 
        
    }

    private void deployAssetTagToHost(Sha1Digest tag, TxtHostRecord hostRecord) throws IOException {
        HostAgentFactory hostAgentFactory = new HostAgentFactory();
        ByteArrayResource tlsKeystore = new ByteArrayResource();
        ConnectionString connectionString = ConnectionString.from(hostRecord);
        // XXX TODO use the tls policy factory with the keystore for this host ... from the host tls keystore table
        HostAgent hostAgent = hostAgentFactory.getHostAgent(connectionString, new InsecureTlsPolicy());
        hostAgent.setAssetTag(tag);
    }
    
}
