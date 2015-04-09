/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.v2.vm.attestation.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.rfc822.SignatureException;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;

import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.vmquote.xml.TrustPolicy;
import com.intel.mtwilson.vmquote.xml.Measurements;

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
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.trustagent.model.VMAttestationRequest;
import com.intel.mtwilson.trustagent.model.VMQuoteResponse;
import com.intel.mtwilson.vmquote.xml.VMQuote;
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
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

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
                    
                    VMQuoteResponse vmQuoteResponse = agent.getVMAttestationReport(requestObj);
                    log.debug("Retrieved VM attestation report in MTW.");
                    
                    if ((vmQuoteResponse != null) && (vmQuoteResponse.getVmMeasurements().length > 0) &&
                            (vmQuoteResponse.getVmQuote().length > 0) && (vmQuoteResponse.getVmTrustPolicy().length > 0)) {
                        try {

                            String vmQuoteXml = IOUtils.toString(vmQuoteResponse.getVmQuote(), "UTF-8");
                            String trustPolicyXml = IOUtils.toString(vmQuoteResponse.getVmTrustPolicy(), "UTF-8");
                            String measurementXml = IOUtils.toString(vmQuoteResponse.getVmMeasurements(), "UTF-8");
                            
                            boolean isVMQuoteValid = false;
                            boolean isTrustPolicyValid = false;
                            
                            switch(vmQuoteResponse.getVmQuoteType()) {
                                
                                case SPRINT7:
                                    // In Sprint7 since we are not getting a signed quote, we are not going to verify anything.
                                    isVMQuoteValid = true;
                                    
                                    // Validate the TrustPolicy signature and the certificate that was used to sign the TrustPolicy
                                    isTrustPolicyValid = true; //ValidateSignature.isValid(trustPolicyXml);
                                    
                                    // Once we have verified the integrity of the files, we need to ensure that the nonce is matching with what 
                                    // was sent to the call. After the nonce verification, the cumulative hash needs to be verfied with the 
                                    // whitelist in the trust policy.
                                    if (isVMQuoteValid && isTrustPolicyValid) {

                                        // Deserialize the TrustPolicy and VMQuote into the autogenerated objects
                                        TrustPolicy vmTrustPolicy = jaxb.read(trustPolicyXml, TrustPolicy.class);
                                        VMQuote vmQuote = jaxb.read(vmQuoteXml, VMQuote.class);

                                        /*
                                        if (nonce == null ? vmQuote.getNonce() != null : !nonce.equals(vmQuote.getNonce())) {
                                            log.error("Error during verification of the VM Attestation report. Nonce does not match.");
                                            throw new RepositoryCreateException();
                                        }*/

                                        boolean isVMTrusted = false;
                                        /*
                                        if (vmQuote.getCumulativeHash() == null ? vmTrustPolicy.getImage().getImageHash().getValue() != null : 
                                                !vmQuote.getCumulativeHash().equals(vmTrustPolicy.getImage().getImageHash().getValue())) {
                                            log.error("Hash value of the VM {} does not match the white list value {} specified in the Trust Policy.",
                                                    vmQuote.getCumulativeHash(), vmTrustPolicy.getImage().getImageHash().getValue());
                                            // TODO: Compare the measurements against the whitelists to see which module failed.
                                            // We will do this in the next sprint.
                                        } else {
                                            isVMTrusted = true;
                                        }*/

                                        // Create a map of the VM attributes that needs to be added to the SAML assertion.
                                        Map<String, String> vmAttributes = new HashMap<>();
                                        vmAttributes.put("VM_Trust_Status", String.valueOf(isVMTrusted));
                                        vmAttributes.put("VM_Instance_Id", vmQuote.getVmInstanceId());
                                        vmAttributes.put("VM_Trust_Policy", vmTrustPolicy.getLaunchControlPolicy());
                                        String samlForHostWithVMData = new HostTrustBO().getSamlForHostWithVMData(obj, item.getId().toString(), vmAttributes);
                                        return samlForHostWithVMData;                                
                                    } else {
                                        log.error("Invalid signature specified.");
                                        return null;
                                    }
                                case X509_ATTR_CERT:
                                    break;
                                case XML_DSIG:
                                    break;
                                default:
                                    break;
                            }
                                                        
                        } catch (RepositoryCreateException ex) {
                            throw ex;
                        } catch (IOException | JAXBException | XMLStreamException ex ){ //| ParserConfigurationException | SAXException | MarshalException | XMLSignatureException ex) {
                            log.error("Error during validation of the VM attestation report. {}", ex.getMessage());
                            throw new RepositoryCreateException(ex);
                        }
                    }
                } else {
                    log.error("Host specified {} does not exist in the system. Please verify the input parameters.", item.getHostName());
                    throw new RepositoryInvalidInputException();
                }
                
            } else {
                // Since there are some missing or invalid inputs, throw an appropriate exception.
                throw new RepositoryInvalidInputException();
            }
        } catch (RepositoryCreateException | RepositoryInvalidInputException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error during generation of host saml assertion. {} ", ex.getMessage());
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
