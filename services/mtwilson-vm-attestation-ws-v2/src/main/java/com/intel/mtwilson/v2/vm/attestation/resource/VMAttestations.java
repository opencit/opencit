/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.v2.vm.attestation.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;

import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.vmquote.xml.TrustPolicy;

import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.util.xml.dsig.XmlDsigVerify;

import com.intel.mtwilson.as.rest.v2.model.VMAttestation;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationLocator;
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
import gov.niarl.his.privacyca.TpmUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.IOUtils;

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
        VMAttestationRepository vmAttestationRepo = new VMAttestationRepository();
        vmAttestationRepo.create(item);
        String samlAssertion = item.getVmSaml();
        return samlAssertion;
    }
    
/*    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces(CryptoMediaType.APPLICATION_SAML)    
    @SuppressWarnings("empty-statement")
    public String createSamlAssertion(VMAttestation item) {
        String nonce;
        JAXB jaxb = new JAXB();
        
        if (item.getId() == null) { item.setId(new UUID()); }
        
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
                    
                    if (vmQuoteResponse == null) {
                        String errorInfo = "Error retrieving the attestation report for the specified VM.";
                        log.error(errorInfo);
                        throw new RepositoryCreateException(errorInfo);
                    }
                    
                    if (vmQuoteResponse.getVmQuote() == null || vmQuoteResponse.getVmQuote().length == 0) {
                        String errorInfo = "VM Quote received has null value. Please verify the input parameters.";
                        log.error(errorInfo);
                        throw new RepositoryCreateException(errorInfo);                        
                    }
                    
                    if (vmQuoteResponse.getVmTrustPolicy() == null || vmQuoteResponse.getVmTrustPolicy().length == 0) {
                        String errorInfo = "VM Quote received does not have an associated Trust Policy. VM Quote cannot be validated.";
                        log.error(errorInfo);
                        throw new RepositoryCreateException(errorInfo);                        
                    }

                    if (vmQuoteResponse.getVmMeasurements() == null || vmQuoteResponse.getVmMeasurements().length == 0) {
                        String errorInfo = "VM Quote received does not have an associated measurement log. Individual modules cannot be verified.";
                        log.error(errorInfo);
                        throw new RepositoryCreateException(errorInfo);                        
                    }

                    try {

                        String vmQuoteXml = IOUtils.toString(vmQuoteResponse.getVmQuote(), "UTF-8");
                        String trustPolicyXml = IOUtils.toString(vmQuoteResponse.getVmTrustPolicy(), "UTF-8");
                        String measurementXml = IOUtils.toString(vmQuoteResponse.getVmMeasurements(), "UTF-8");

                        log.debug("VMQuote : {} , TrustPolicy : {}, Measurements : {}", vmQuoteXml, trustPolicyXml, measurementXml);

                        boolean isVMQuoteValid = false;
                        boolean isTrustPolicyValid = false;

                        switch(vmQuoteResponse.getVmQuoteType()) {

                            case SPRINT7:
                                // In Sprint7 since we are not getting a signed quote, we are not going to verify anything.
                                isVMQuoteValid = true;

                                // Validate the TrustPolicy signature and the certificate that was used to sign the TrustPolicy
                                log.debug("createSamlAssertion: About to validate the trust policy.");
                                //isTrustPolicyValid = ValidateSignature.isValid(trustPolicyXml);
                                isTrustPolicyValid = XmlDsigVerify.isValid(trustPolicyXml, getSamlCertificate());
                                log.debug("createSamlAssertion: Validation result of TrustPolicy is {}", isTrustPolicyValid);

                                // Once we have verified the integrity of the files, we need to ensure that the nonce is matching with what 
                                // was sent to the call. After the nonce verification, the cumulative hash needs to be verfied with the 
                                // whitelist in the trust policy.
                                if (isVMQuoteValid && isTrustPolicyValid) {

                                    // Deserialize the TrustPolicy and VMQuote into the autogenerated objects
                                    TrustPolicy vmTrustPolicy = jaxb.read(trustPolicyXml, TrustPolicy.class);
                                    String cumulativeHashFromQuote = vmQuoteXml.substring((vmQuoteXml.indexOf("<cumulative_hash>")+"<cumulative_hash>".length()), vmQuoteXml.indexOf("</cumulative_hash>"));
                                    String vmInstanceIdFromQuote = vmQuoteXml.substring((vmQuoteXml.indexOf("<vm_instance_id>")+"<vm_instance_id>".length()), vmQuoteXml.indexOf("</vm_instance_id>"));
                                    String nonceFromQuote = vmQuoteXml.substring((vmQuoteXml.indexOf("<nonce>")+"<nonce>".length()), vmQuoteXml.indexOf("</nonce>"));


                                    if (nonce == null ? nonceFromQuote != null : !nonce.equals(nonceFromQuote)) {
                                        log.error("Error during verification of the VM Attestation report. Nonce sent {} does not match the nonce in report {}.", nonce, nonceFromQuote);
                                        throw new RepositoryCreateException();
                                    }

                                    boolean isVMTrusted = false;

                                    if (cumulativeHashFromQuote == null ? vmTrustPolicy.getImage().getImageHash().getValue() != null : 
                                            !cumulativeHashFromQuote.equals(vmTrustPolicy.getImage().getImageHash().getValue())) {
                                        log.error("Hash value of the VM {} does not match the white list value {} specified in the Trust Policy.",
                                                cumulativeHashFromQuote, vmTrustPolicy.getImage().getImageHash().getValue());
                                        // TODO: Compare the measurements against the whitelists to see which module failed.
                                        // We will do this in the next sprint.
                                    } else {
                                        isVMTrusted = true;
                                    }

                                    // Create a map of the VM attributes that needs to be added to the SAML assertion.
                                    Map<String, String> vmAttributes = new HashMap<>();
                                    vmAttributes.put("VM_Trust_Status", String.valueOf(isVMTrusted));
                                    vmAttributes.put("VM_Instance_Id", vmInstanceIdFromQuote);
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

                                log.debug("createSamlAssertion: XML_DSIG section");

                                try {
                                    // Validate the TrustPolicy signature and the certificate that was used to sign the TrustPolicy
                                    log.debug("createSamlAssertion: About to validate the trust policy.");
                                    isTrustPolicyValid = XmlDsigVerify.isValid(trustPolicyXml, getSamlCertificate());
                                    log.debug("createSamlAssertion: Validation result of TrustPolicy is {}", isTrustPolicyValid);
                                } catch (Exception ex) {
                                    log.error("Error during validation of the TrustPolicy. {}", ex.getMessage());
                                    throw new RepositoryCreateException(ex);
                                }

                                try {
                                    // Validate the VM Quote signature and the certificate that was used to sign the signing key
                                    log.debug("createSamlAssertion: About to validate the VMQuote using the PrivacyCA cert @ : {}", My.configuration().getPrivacyCaIdentityP12().getAbsolutePath());
                                    X509Certificate privacyCaCert = TpmUtils.certFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
                                    isVMQuoteValid = XmlDsigVerify.isValid(vmQuoteXml, privacyCaCert);
                                    log.debug("createSamlAssertion: Validation result of VMQuote is {}", isVMQuoteValid);
                                    isVMQuoteValid = true;
                                } catch (Exception ex) {
                                    log.error("Error during validation of the VMQuote. {}", ex.getMessage());
                                    isVMQuoteValid = true;
                                    //throw new RepositoryCreateException(ex);
                                }
                                
                                // Once we have verified the integrity of the files, we need to ensure that the nonce is matching with what 
                                // was sent to the call. After the nonce verification, the cumulative hash needs to be verfied with the 
                                // whitelist in the trust policy.
                                if (isVMQuoteValid && isTrustPolicyValid) {

                                    // Deserialize the TrustPolicy and VMQuote into the autogenerated objects
                                    TrustPolicy vmTrustPolicy = jaxb.read(trustPolicyXml, TrustPolicy.class);
                                    VMQuote vmQuote = jaxb.read(vmQuoteXml, VMQuote.class);
                                    
                                    String cumulativeHashFromQuote = vmQuote.getCumulativeHash();
                                    String vmInstanceIdFromQuote = vmQuote.getVmInstanceId();
                                    String nonceFromQuote = vmQuote.getNonce();


                                    if (nonce == null ? nonceFromQuote != null : !nonce.equals(nonceFromQuote)) {
                                        log.error("Error during verification of the VM Attestation report. Nonce sent {} does not match the nonce in report {}.", nonce, nonceFromQuote);
                                        throw new RepositoryCreateException();
                                    }

                                    boolean isVMTrusted = false;

                                    if (cumulativeHashFromQuote == null ? vmTrustPolicy.getImage().getImageHash().getValue() != null : 
                                            !cumulativeHashFromQuote.equals(vmTrustPolicy.getImage().getImageHash().getValue())) {
                                        log.error("Hash value of the VM {} does not match the white list value {} specified in the Trust Policy.",
                                                cumulativeHashFromQuote, vmTrustPolicy.getImage().getImageHash().getValue());
                                        // TODO: Compare the measurements against the whitelists to see which module failed.
                                        // We will do this in the next sprint.
                                    } else {
                                        isVMTrusted = true;
                                    }

                                    // Create a map of the VM attributes that needs to be added to the SAML assertion.
                                    Map<String, String> vmAttributes = new HashMap<>();
                                    vmAttributes.put("VM_Trust_Status", String.valueOf(isVMTrusted));
                                    vmAttributes.put("VM_Instance_Id", vmInstanceIdFromQuote);
                                    vmAttributes.put("VM_Trust_Policy", vmTrustPolicy.getLaunchControlPolicy());
                                    String samlForHostWithVMData = new HostTrustBO().getSamlForHostWithVMData(obj, item.getId().toString(), vmAttributes);
                                    return samlForHostWithVMData;                                
                                } else {
                                    log.error("Invalid signature specified.");
                                    return null;
                                }
                            default:
                                break;
                        }

                    } catch (RepositoryCreateException ex) {
                        throw ex;
                    } catch (IOException | JAXBException | XMLStreamException ex ){ //| ParserConfigurationException | SAXException | MarshalException | XMLSignatureException ex) {
                        log.error("Error during validation of the VM attestation report. {}", ex.getMessage());
                        throw new RepositoryCreateException(ex);
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
    }*/
    
    
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
