/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.v2.vm.attestation.resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.intel.mtwilson.v2.vm.attestation.model.VMAttestation;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationCollection;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationFilterCriteria;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationLocator;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.v2.vm.attestation.repository.VMAttestationRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/vm-attestations")
public class VMAttestations extends AbstractJsonapiResource<VMAttestation, VMAttestationCollection, VMAttestationFilterCriteria, NoLinks<VMAttestation>, VMAttestationLocator> {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VMAttestations.class);
    private ObjectMapper mapper = new ObjectMapper(); // for debugging only

    private VMAttestationRepository repository;

    public VMAttestations() {
        repository = new VMAttestationRepository();
    }    

    @Override
    protected VMAttestationCollection createEmptyCollection() {
        return new VMAttestationCollection();
    }

    @Override
    protected VMAttestationRepository getRepository() {
        return repository;
    }
    
/*    @GET
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
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                obj = jpaController.findByName(criteria.nameEqualTo);
                if (obj == null) {
                    log.error("Host specified with name {} is not valid.", criteria.nameEqualTo);
                    throw new RepositoryInvalidInputException();
                }
            } else return null;
            
            // since we have found the host with the specified criteria lets check if there is a valid cached saml assertion
            TblSamlAssertion tblSamlAssertion = My.jpa().mwSamlAssertion().findByHostAndExpiry(obj.getName());
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
    public String createSamlAssertion(HostAttestation item) {
        log.debug("Creating new SAML assertion for host {}.", item.getHostUuid());
        HostAttestationLocator locator = new HostAttestationLocator();
        locator.id = item.getId();
        
        try { log.debug("createSamlAssertion: {}", mapper.writeValueAsString(item)); } catch(JsonProcessingException e) { log.debug("createSamlAssertion: cannot serialize selector: {}", e.getMessage()); }
        ValidationUtil.validate(item); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        String samlAssertion;
        
        try {
            TblHosts obj = My.jpa().mwHosts().findHostByUuid(item.getHostUuid());
            if (obj == null) {
                log.error("Host specified with id {} is not valid.", item.getHostUuid());
                throw new RepositoryInvalidInputException();
            }
            
            samlAssertion = new HostTrustBO().getTrustWithSaml(obj, obj.getName(), true);
            
        } catch (Exception ex) {
            log.error("Error during generation of host saml assertion.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
        
        return samlAssertion;
    }*/
    
}
