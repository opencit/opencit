/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.intel.mtwilson.atag.model.Certificate;
import com.intel.mtwilson.atag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.atag.Derby;
import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.validation.ObjectModel;
import java.io.IOException;
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
    
    @Get("json|xml")
    public Certificate existingCertificate() {
        String uuid = getAttribute("id");
        Certificate certificate = dao.findByUuid(UUID.valueOf(uuid));
        if( certificate == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        return certificate;
    }
    
    @Get("bin")
    public byte[] existingCertificateContent() {
        String uuid = getAttribute("id");
        Certificate certificate = dao.findByUuid(UUID.valueOf(uuid)); 
        if( certificate == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        return certificate.getCertificate();
    }
    
    @Get("txt")
    public String existingCertificateContentPem() throws IOException {
        String uuid = getAttribute("id");
        Certificate certificate = dao.findByUuid(UUID.valueOf(uuid)); 
        if( certificate == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        log.debug("resource url: {}", getRequest().getResourceRef().getIdentifier());
        Pem pem = new Pem("X509 ATTRIBUTE CERTIFICATE", certificate.getCertificate());
        pem.getHeaders().put("URL", My.configuration().getAssetTagServerURL()+"/certificates/"+uuid);
        return pem.toString();
    }

    @Delete
    public void deleteCertificate() {
        String uuid = getAttribute("id");
        Certificate certificate = dao.findByUuid(UUID.valueOf(uuid));
        if( certificate == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
        dao.delete(certificate.getId());
        setStatus(Status.SUCCESS_NO_CONTENT);
    }
    
    public static enum CertificateActionName {
        REVOKE, PROVISION;
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
     * Use a jackson mix-in to add a @JsonProperty(name="accepted") to the final isValid() method in ObjectModel.
     */
    public static abstract class CertificateAction extends ObjectModel {
        private CertificateActionName name;
        private UUID uuid;
        public CertificateAction(CertificateActionName name) {
            this.name = name;
        }
        public CertificateActionName getAction() { return name; }
        public UUID getUuid() { return uuid; }
        public void setUuid(UUID certificateUuid) {
            this.uuid = certificateUuid;
        }
        
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

        @Override
        protected void validate() {
            // we use today's date as the effective date if it is not provided
        }

    }
    public static class CertificateProvisionAction extends CertificateAction {
        public InternetAddress host;
        public CertificateProvisionAction() {
            super(CertificateActionName.PROVISION);
        }

        public void setHost(InternetAddress host) {
            this.host = host;
        }

        public InternetAddress getHost() {
            return host;
        }
        
        @Override
        protected void validate() {
            if( host == null ) {
                fault("Host address is required");
            }
        }
    }
    
    @JsonInclude(Include.NON_NULL)
    public static class CertificateActionChoice {
        public CertificateActionName action; // "revoke", "provision"
        public CertificateRevokeAction revoke;
        public CertificateProvisionAction provision;
    }
    
    @Post("json:json")
    public CertificateActionChoice[] actionCertificate(CertificateActionChoice[] actions) throws IOException {
        UUID uuid = UUID.valueOf(getAttribute("id"));
        Certificate certificate = dao.findByUuid(uuid);
        if( certificate == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        
        for( CertificateActionChoice action : actions ) {
            switch(action.action) {
                case PROVISION:
                    if( action.provision == null ) {
                        action.provision = new CertificateProvisionAction(); // but won't validate because hostname was not provided
                    }
                    action.provision.setUuid(uuid);
                    if( action.provision.isValid() ) {
                        // XXX TODO send it to the host...
                    }
                    break;
                case REVOKE:
                    action.revoke.setUuid(uuid);
                    if( action.revoke.isValid() ) {
                        // update the database...
                        dao.updateRevoked(certificate.getId(), true);
                    }
                    break;
                default:
                    log.error("Unsupported action: {}", action.action.name());
            }
        }
        return actions;
    }

}
