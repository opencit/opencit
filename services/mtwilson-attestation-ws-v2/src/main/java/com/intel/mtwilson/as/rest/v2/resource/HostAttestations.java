/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblSamlAssertion;
import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationFilterCriteria;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationLocator;
import com.intel.mtwilson.as.rest.v2.repository.HostAttestationRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/host-attestations")
public class HostAttestations extends AbstractJsonapiResource<HostAttestation, HostAttestationCollection, HostAttestationFilterCriteria, NoLinks<HostAttestation>, HostAttestationLocator> {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostAttestations.class);
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
                    log.error("Host specified with id {} is not valid.", criteria.hostUuid.toString());
                    throw new RepositoryInvalidInputException();
                }
            } else if (criteria.aikSha1 != null && !criteria.aikSha1.isEmpty()) {
                obj = jpaController.findByAikSha1(criteria.aikSha1);
                if (obj == null) {
                    log.error("Host specified with aik sha1 {} is not valid.", criteria.aikSha1);
                    throw new RepositoryInvalidInputException();
                }
            } else if (criteria.aikPublicKeySha1 != null && !criteria.aikPublicKeySha1.isEmpty()) {
                obj = jpaController.findByAikPublicKeySha1(criteria.aikPublicKeySha1);
                if (obj == null) {
                    log.error("Host specified with aik pub key sha1 {} is not valid.", criteria.aikSha1);
                    throw new RepositoryInvalidInputException();
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                obj = jpaController.findByName(criteria.nameEqualTo);
                if (obj == null) {
                    log.error("Host specified with name {} is not valid.", criteria.nameEqualTo);
                    throw new RepositoryInvalidInputException();
                }
            } else return null;
            
            // since we have found the host with the specified criteria lets check if there is a valid cached saml assertion
            TblSamlAssertion tblSamlAssertion = My.jpa().mwSamlAssertion().findByHostAndExpiry(obj.getName()); //.getId().toString());
            if(tblSamlAssertion != null){
                if(tblSamlAssertion.getErrorMessage() == null|| tblSamlAssertion.getErrorMessage().isEmpty()) {
                    log.debug("Found assertion in cache. Expiry time : " + tblSamlAssertion.getExpiryTs());
                    return tblSamlAssertion.getSaml();
                }else{
                    log.debug("Found assertion in cache with error set.");
                   throw new RepositoryRetrieveException(new Exception("("+ tblSamlAssertion.getErrorCode() + ") " + tblSamlAssertion.getErrorMessage() + " (cached on " + tblSamlAssertion.getCreatedTs().toString()  +")"));
                }
            } else {
                return null;
            }
            
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("Error during retrieval of host attestation status from cache.", ex);
            throw new RepositorySearchException(ex, criteria);
        }        
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces(CryptoMediaType.APPLICATION_SAML)    
    @SuppressWarnings("empty-statement")
    public String createSamlAssertion(HostAttestation item) throws JsonProcessingException {
        log.debug("Creating new SAML assertion for host {}.", item.getHostUuid());
        HostAttestationLocator locator = new HostAttestationLocator();
        locator.id = item.getId();
        
        try { log.debug("createSamlAssertion: {}", mapper.writeValueAsString(item)); } catch(JsonProcessingException e) { log.debug("createSamlAssertion: cannot serialize selector: {}", e.getMessage()); }
        ValidationUtil.validate(item); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        
        repository.create(item);
        
        log.debug("createSamlAssertion: repository create record completed. SAML: {}", item.getSaml());
        return item.getSaml();
    }
    
}
