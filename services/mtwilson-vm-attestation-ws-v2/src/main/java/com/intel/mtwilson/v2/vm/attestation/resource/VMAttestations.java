/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.v2.vm.attestation.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;

import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.vmquote.xml.*;

import com.intel.mtwilson.as.business.trust.HostTrustBO;

import com.intel.mtwilson.v2.vm.attestation.model.VMAttestation;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationCollection;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationFilterCriteria;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationLocator;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.v2.vm.attestation.repository.VMAttestationRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.trustagent.model.VMAttestationRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

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

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces(CryptoMediaType.APPLICATION_SAML)    
    @SuppressWarnings("empty-statement")
    public String createSamlAssertion(VMAttestation item) {
        if (item.getId() == null) {
            item.setId(new UUID());
        }
        
        JAXB jaxb = new JAXB();
        String nonce;
        log.debug("Creating new SAML assertion for host {}.", item.getHostName());
        
        ValidationUtil.validate(item); 
        String samlAssertion;
        
        try {
            if (item.getHostName() != null && !item.getHostName().isEmpty() && 
                    item.getVmInstanceId() != null && !item.getVmInstanceId().isEmpty()) {

                TblHostsJpaController jpaController = My.jpa().mwHosts();
                TblHosts obj = jpaController.findByName(item.getHostName());
                if (obj != null) {
                    HostAgentFactory factory = new HostAgentFactory();
                    HostAgent agent = factory.getHostAgent(obj);
                    nonce = new UUID().toString(); // Generate a new Nonce
                    
                    VMAttestationRequest requestObj = new VMAttestationRequest();
                    requestObj.setVmInstanceId(item.getVmInstanceId());
                    requestObj.setNonce(nonce);
                    
                    try { 
                        log.debug("Requesting VM Quote response for: {}", mapper.writeValueAsString(requestObj)); 
                    } catch(JsonProcessingException e) { 
                        log.debug("Cannot serialize VM Quote request: {}", e.getMessage()); 
                    }

                    String vmAttestationReport = agent.getVMAttestationReport(requestObj);
                    log.debug("GETTING back the response in MTW. {}", vmAttestationReport);
                    try {
                        // Create the VMQuoteResponse object using JAXB and extract the contents of the XML for further processing.
                        VMQuoteResponse vmQuoteResponse = jaxb.read(vmAttestationReport, VMQuoteResponse.class);
                        
                        log.debug("VMQuoteResponse cumulative hash is {} for nonce {}", vmQuoteResponse.getVMQuote().getCumulativeHash(),
                                vmQuoteResponse.getVMQuote().getNonce());
                        
                        Map<String, String> vmAttributes = new HashMap<>();
                        vmAttributes.put("VM_Trust_Stats", "true");
                        vmAttributes.put("VM_Instance_Id", vmQuoteResponse.getVMQuote().getVmInstanceId());
                        vmAttributes.put("VM_Trust_Policy", vmQuoteResponse.getTrustPolicy().getLaunchControlPolicy());
                        String samlForHostWithVMData = new HostTrustBO().getSamlForHostWithVMData(obj, item.getId().toString(), vmAttributes);
                        return samlForHostWithVMData;
                    } catch (IOException | JAXBException | XMLStreamException ex) {
                        log.error("Error during deserializing the VM Quote response using JAXB. {}", ex.getMessage());
                    }
                }
                
            }
        } catch (IOException | CryptographyException ex) {
            log.error("Error during generation of host saml assertion.", ex);
            throw new RepositoryCreateException(ex);
        }        
        
        return null;
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
    }*/
    
}
