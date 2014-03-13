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
import com.intel.mtwilson.tag.rest.v2.model.Certificate;
import java.io.IOException;
import java.util.List;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@RPC("certificate_provision")
@JacksonXmlRootElement(localName="certificate_provision")
public class CertificateProvisionRunnable extends ServerResource implements Runnable{
    
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
                    List<TxtHostRecord> hostList = Global.mtwilson().queryForHosts(host.toString());
                    if(hostList == null || hostList.size() == 0) {
                        log.error("No hosts were returned back matching name " + host.toString());
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No hosts were found matching the specified criteria.");
                    }
                    TxtHostRecord hostRecord = hostList.get(0);
                    deployAssetTagToHost(obj.getSha1(), hostRecord);
            }

        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate deployment.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
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
