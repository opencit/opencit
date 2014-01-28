/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.atag.model.Certificate;
import com.intel.mtwilson.atag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.validation.ObjectModel;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.api.MtWilson;
import com.intel.mtwilson.atag.Global;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.model.Hostname;
import com.intel.mtwilson.tls.InsecureTlsPolicy;
import java.io.IOException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.Date;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References: http://restlet.org/learn/guide/2.2/core/resource/
 *
 * @author jbuhacoff
 */
public class CertificateResource extends ServerResource {
    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateDAO dao = null;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.certificateDao();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }


    @Override
    protected void doRelease() throws ResourceException {
        if( dao != null ) { dao.close(); }
        super.doRelease();
    }
    
    private Certificate find(String id) {
        if( UUID.isValid(id) ) {
            return dao.findByUuid(UUID.valueOf(id));
        }
        if( Sha256Digest.isValidHex(id)) {
            return dao.findBySha256(id);
        }
        return null;
    }
    
    @Get("json|xml")
    public Certificate existingCertificate() {
        String id = getAttribute("id");
        Certificate certificate = find(id);
        if( certificate == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        return certificate;
    }
    
    @Get("bin")
    public byte[] existingCertificateContent() {
        String id = getAttribute("id");
        Certificate certificate = find(id);
        if( certificate == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        return certificate.getCertificate();
    }
    
    @Get("txt")
    public String existingCertificateContentPem() throws IOException {
        String id = getAttribute("id");
        Certificate certificate = find(id);
        if( certificate == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        log.debug("resource url: {}", getRequest().getResourceRef().getIdentifier());
        Pem pem = new Pem("X509 ATTRIBUTE CERTIFICATE", certificate.getCertificate());
        pem.getHeaders().put("URL", My.configuration().getAssetTagServerURL()+"/certificates/"+id);
        return pem.toString();
    }

    @Delete
    public void deleteCertificate() {
        String id = getAttribute("id");
        Certificate certificate = find(id);
        if( certificate == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
        dao.delete(certificate.getId());
        setStatus(Status.SUCCESS_NO_CONTENT);
    }
    
    public static enum CertificateActionName {
        REVOKE, PROVISION, DEPLOY;
        @Override
        public String toString() {
            return name().toLowerCase();
        }
        @JsonCreator
        public static CertificateActionName valueOfText(String text) {
            for(CertificateActionName action : CertificateActionName.values()) {
                if( action.name().equalsIgnoreCase(text)) { return action; }
            }
            throw new IllegalArgumentException("Unknown action");
        }
    }
    
    /**
     * XXX TODO Use a jackson mix-in to add a @JsonProperty(name="accepted") to the inherited isValid() method from ObjectModel 
     * (but not to the ObjectModel class itself)
     */
    public static abstract class CertificateAction /*extends ObjectModel*/ {
        private CertificateActionName name;
        private UUID uuid;
        public CertificateAction(CertificateActionName name) {
            this.name = name;
        }
//        public CertificateActionName getAction() { return name; }
        public UUID getUuid() { return uuid; }
        public void setUuid(UUID certificateUuid) {
            this.uuid = certificateUuid;
        }
        
    }
  
    
    public static class CertificateDeployAction extends CertificateAction {
        public Date effective;
        public CertificateDeployAction() {
            super(CertificateActionName.DEPLOY);
            effective = new Date();
        }

        public Date getEffective() {
            return effective;
        }

        public void setEffective(Date effective) {
            this.effective = effective;
        }

/*        @Override
        protected void validate() {
            // we use today's date as the effective date if it is not provided
        }*/

    }
    public static class CertificateRevokeAction extends CertificateAction {
        public Date effective;
        public CertificateRevokeAction() {
            super(CertificateActionName.REVOKE);
            effective = new Date();
        }

        public Date getEffective() {
            return effective;
        }

        public void setEffective(Date effective) {
            this.effective = effective;
        }

/*        @Override
        protected void validate() {
            // we use today's date as the effective date if it is not provided
        }*/

    }
    public static class CertificateProvisionAction extends CertificateAction {
        public InternetAddress host;
        public int port;
        // for citrix:
        public String username;
        public String password;
        
        
        public CertificateProvisionAction() {
            super(CertificateActionName.PROVISION);
        }

        public void setHost(InternetAddress host) {
            this.host = host;
        }

        public InternetAddress getHost() {
            return host;
        }

        public void setPort(int port) {
            this.port = port;
        }
        
        public int getPort() {
            return port;
        }
        
        public String getUsername() {
            return username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPassword() {
            return password;
        }
        
        
        /*
        @Override
        protected void validate() {
            if( host == null ) {
                fault("Host address is required");
            }
        }*/

        public void setUsername(String username) {
            this.username = username;
        }
    }
    
    @JsonInclude(Include.NON_NULL)
    public static class CertificateActionChoice {
        public CertificateRevokeAction revoke;
        public CertificateProvisionAction provision;
        public CertificateDeployAction deploy;
        
    }
    
    @Post("json:json")
    public CertificateActionChoice actionCertificate(CertificateActionChoice actionChoice) throws IOException, ApiException, SignatureException {
        
         UUID uuid = UUID.valueOf(getAttribute("id"));
          Certificate certificate = dao.findByUuid(uuid);
          if( certificate == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
          }
          
        // only one of the actions can be processed for any one request
        if( actionChoice.revoke != null ) {
            actionChoice.revoke.setUuid(uuid);
            if( true /*actionChoice.revoke.isValid()*/ ) {
                // update the database...
                dao.updateRevoked(certificate.getId(), true);
                // update mt wilson ...
                AssetTagCertRevokeRequest request = new AssetTagCertRevokeRequest();
                request.setSha256OfAssetCert(certificate.getSha256().toByteArray());
                Global.mtwilson().revokeAssetTagCertificate(request);
//                        My.client().revokeAssetTagCertificate(request);
                // XXX TODO revoke it from host (send zeros);
            }
            CertificateActionChoice result = new CertificateActionChoice();
            result.revoke = actionChoice.revoke;
            return result;
        }
        if( actionChoice.provision != null ) {
            actionChoice.provision.setUuid(uuid);
            if( true /* actionChoice.provision.isValid()*/ ) {
                // first post the certificate to mt wilson
                // This now done when the cert is created.
                /*
                AssetTagCertCreateRequest request = new AssetTagCertCreateRequest();
                request.setCertificate(certificate.getCertificate());
                Global.mtwilson().importAssetTagCertificate(request);
//                        My.client().importAssetTagCertificate(request);
                */
                // XXX TODO send it to the host...
                try {
                    deployAssetTagToHost(certificate.getSha1(), actionChoice.provision.getHost(),actionChoice.provision.port, actionChoice.provision.getUsername(), actionChoice.provision.getPassword());
                }
                catch(IOException e) {
                    // need a way to send the error in the result... i18n Message and error code
                    log.error("Cannot deploy asset tag to host {}", actionChoice.provision.getHost(), e);
                }
            }            
            CertificateActionChoice result = new CertificateActionChoice();
            result.provision = actionChoice.provision;
            return result;
        }
        if( actionChoice.deploy != null ) {
         actionChoice.deploy.setUuid(uuid);
         //log.debug("assetTag deploying cert to MTW");
         // this now happens when the cert is created
         if(true){
             AssetTagCertCreateRequest request = new AssetTagCertCreateRequest();
             request.setCertificate(certificate.getCertificate());
             Global.mtwilson().importAssetTagCertificate(request);
         }
         CertificateActionChoice result = new CertificateActionChoice();
         result.deploy = actionChoice.deploy;
         return result;
        }
      
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
    }
    
    private void deployAssetTagToHost(Sha1Digest tag, InternetAddress host, int port, String username, String password) throws IOException {
        log.debug("deploy Asset Tag port == " + port);
        HostAgentFactory hostAgentFactory = new HostAgentFactory();
        ByteArrayResource tlsKeystore = new ByteArrayResource();
//        TlsPolicy tlsPolicy = hostAgentFactory.getTlsPolicy("TRUST_FIRST_CERTIFICATE", tlsKeystore);
        ConnectionString connectionString = null;
        if(port == 443) {
            log.debug("writing citrix asset tag ["+host.toString()+" , " + port + ", " + username + ", " + password + "]");
            connectionString = ConnectionString.forCitrix(new Hostname(host.toString()), username, password);
        }else {
            log.debug("writing ta asset tag ["+host.toString()+" , " + port + ", " + username + ", " + password + "]");
            connectionString = ConnectionString.forIntel(host.toString(),port);
        }
        HostAgent hostAgent = hostAgentFactory.getHostAgent(connectionString, new InsecureTlsPolicy());
        hostAgent.setAssetTag(tag);
    }

}
