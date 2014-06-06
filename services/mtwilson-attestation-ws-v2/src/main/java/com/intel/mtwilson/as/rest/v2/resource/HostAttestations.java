/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.data.TblSamlAssertion;

import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationFilterCriteria;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationLocator;
import com.intel.mtwilson.as.rest.v2.repository.HostAttestationRepository;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/host-attestations")
public class HostAttestations extends AbstractJsonapiResource<HostAttestation, HostAttestationCollection, HostAttestationFilterCriteria, NoLinks<HostAttestation>, HostAttestationLocator> {
    
    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private ObjectMapper mapper = new ObjectMapper(); // for debugging only

    private HostAttestationRepository repository;

    public HostAttestations() {
        repository = new HostAttestationRepository();
    }    

    @Override
    protected HostAttestationCollection createEmptyCollection() {
        return new HostAttestationCollection();
    }

    @Override
    protected HostAttestationRepository getRepository() {
        return repository;
    }
    
    @GET
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces(CryptoMediaType.APPLICATION_SAML)    
    public String searchCollectionSaml(@BeanParam HostAttestationFilterCriteria criteria) {
        try { log.debug("searchCollection: {}", mapper.writeValueAsString(criteria)); } catch(JsonProcessingException e) { log.debug("searchCollection: cannot serialize selector: {}", e.getMessage()); }
        ValidationUtil.validate(criteria); 
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj;
            if (criteria.hostUuid != null) {
                obj = jpaController.findHostByUuid(criteria.hostUuid.toString());
                if (obj == null) {
                    throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, criteria.hostUuid.toString());
                }
            } else if (criteria.aikSha1 != null && !criteria.aikSha1.isEmpty()) {
                obj = jpaController.findByAikSha1(criteria.aikSha1);
                if (obj == null) {
                    throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, criteria.aikSha1);
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                obj = jpaController.findByName(criteria.nameEqualTo);
                if (obj == null) {
                    throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, criteria.nameEqualTo);
                }
            } else return null;
            
            // since we have found the host with the specified criteria lets check if there is a valid cached saml assertion
            TblSamlAssertion tblSamlAssertion = My.jpa().mwSamlAssertion().findByHostAndExpiry(obj.getName());
            if(tblSamlAssertion != null){
                if(tblSamlAssertion.getErrorMessage() == null|| tblSamlAssertion.getErrorMessage().isEmpty()) {
                    log.debug("Found assertion in cache. Expiry time : " + tblSamlAssertion.getExpiryTs());
                    return tblSamlAssertion.getSaml();
                }else{
                    log.debug("Found assertion in cache with error set, returning that.");
                   throw new ASException(new Exception("("+ tblSamlAssertion.getErrorCode() + ") " + tblSamlAssertion.getErrorMessage() + " (cached on " + tblSamlAssertion.getCreatedTs().toString()  +")"));
                }
            } else {
                return null;
            }
            
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during retrieval of host attestation status from cache.", ex);
            throw new ASException(ErrorCode.AS_HOST_ATTESTATION_REPORT_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces(CryptoMediaType.APPLICATION_SAML)    
    @SuppressWarnings("empty-statement")
    public String createSamlAssertion(HostAttestation item) {
        log.debug("Entering the creation of SAML assertion function.");
        try { log.debug("createSamlAssertion: {}", mapper.writeValueAsString(item)); } catch(JsonProcessingException e) { log.debug("createSamlAssertion: cannot serialize selector: {}", e.getMessage()); }
        ValidationUtil.validate(item); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        String samlAssertion;
        
        try {
            TblHosts obj = My.jpa().mwHosts().findHostByUuid(item.getHostUuid());
            if (obj == null) {
                throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, item.getHostUuid());
            }
            
            samlAssertion = new HostTrustBO().getTrustWithSaml(obj, obj.getName(), true);
            
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during generation of host saml assertion.", ex);
            throw new ASException(ErrorCode.AS_HOST_ATTESTATION_REPORT_ERROR, ex.getClass().getSimpleName());
        }        
        
        return samlAssertion;
    }
    
}
