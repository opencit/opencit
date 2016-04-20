/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateLocator;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 * The "deploy" link next to each certificate in the UI calls this RPC
 * 
 * @author ssbangal
 */
@RPC("deploy-tag-certificate")
@JacksonXmlRootElement(localName="deploy_tag_certificate")
public class DeployTagCertificate implements Runnable{
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeployTagCertificate.class);

       
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
    @RequiresPermissions({"tag_certificates:deploy","hosts:search"})         
    public void run() {
        log.debug("RPC: DeployTagCertificate - Got request to deploy certificate with ID {}.", certificateId);        
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
            CertificateLocator locator = new CertificateLocator();
            locator.id = certificateId;
            
            Certificate obj = dao.findById(certificateId);
            if (obj != null) 
            {
                // verify the certificate validity first
                Date today = new Date();
                log.debug("RPC: DeployTagCertificate - Certificate not before {}", obj.getNotBefore());
                log.debug("RPC: DeployTagCertificate - Certificate not after {}", obj.getNotAfter());
                log.debug("RPC: DeployTagCertificate - Current date {}", today);
                if (today.before(obj.getNotBefore()) || today.after(obj.getNotAfter())) {
                    log.error("RPC: DeployTagCertificate - Certificate with subject {} is expired/invalid. Will not be deployed.", obj.getSubject());
                    throw new RepositoryInvalidInputException(locator);
                }
                
                // Before deploying, we need to verify if the host is same as the one for which the certificate was created.
                List<TxtHostRecord> hostList = Global.mtwilson().queryForHosts(host.toString(), true);
                if(hostList == null || hostList.isEmpty() ) {
                    log.error("RPC: DeployTagCertificate - No hosts were returned back matching name " + host.toString());
                    throw new RepositoryInvalidInputException(locator);
                }
                TxtHostRecord hostRecord = hostList.get(0);
                
                if (!hostRecord.Hardware_Uuid.equals(obj.getSubject())) {
                    log.error("RPC: DeployTagCertificate - The certificate provided [{}] does not map to the host specified [{}]. Certificate will not be deployed on the host.", obj.getSubject(), hostRecord.Hardware_Uuid);
                    throw new RepositoryInvalidInputException(locator);
                }
                
                deployAssetTagToHost(obj.getSha1(), hostRecord);
            } else {
                log.error("RPC: DeployTagCertificate - Failed to retreive certificate while trying to discover host by certificate ID.");
                throw new RepositoryInvalidInputException(locator);
            }

        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("RPC: DeployTagCertificate - Error during certificate deployment.", ex);
            throw new RepositoryException(ex);
        } 
        
    }

    private void deployAssetTagToHost(Sha1Digest tag, TxtHostRecord hostRecord) throws IOException {
        HostAgentFactory hostAgentFactory = new HostAgentFactory();
        //ByteArrayResource tlsKeystore = new ByteArrayResource();
//        ConnectionString connectionString = ConnectionString.from(hostRecord);
//        HostAgent hostAgent = hostAgentFactory.getHostAgent(connectionString, new InsecureTlsPolicy());
        HostAgent hostAgent = hostAgentFactory.getHostAgent(hostRecord);
        hostAgent.setAssetTag(tag);
    }
    
}
