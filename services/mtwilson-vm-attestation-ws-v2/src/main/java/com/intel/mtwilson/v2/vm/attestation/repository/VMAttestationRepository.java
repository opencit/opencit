/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.v2.vm.attestation.repository;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.rest.v2.model.VMAttestation;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.XmlMeasurementLog;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.VmReport;
import com.intel.mtwilson.policy.VmTrustReport;
import com.intel.mtwilson.policy.rule.VmMeasurementLogEquals;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.trustagent.model.VMAttestationResponse;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.trustagent.model.VMAttestationRequest;
import com.intel.mtwilson.trustagent.model.VMQuoteResponse;
import static com.intel.mtwilson.trustagent.model.VMQuoteResponse.QuoteType.XML_DSIG;
import com.intel.mtwilson.util.xml.dsig.XmlDsigVerify;
import com.intel.mtwilson.v2.vm.attestation.resource.VMAttestations;
import com.intel.mtwilson.vmquote.xml.TrustPolicy;
import com.intel.mtwilson.vmquote.xml.VMQuote;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.cert.CertificateException;
import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.xml.sax.SAXException;


/**
 *
 * @author ssbangal
 */
public class VMAttestationRepository implements DocumentRepository<VMAttestation, VMAttestationCollection, VMAttestationFilterCriteria, VMAttestationLocator> {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VMAttestationRepository.class);
            
    @Override
    @RequiresPermissions("vm_attestations:search")    
    public VMAttestationCollection search(VMAttestationFilterCriteria criteria) {
        log.debug("HostAttestation:Search - Got request to search for VM attestations.");        
        VMAttestationCollection objCollection = new VMAttestationCollection();
        try {
            if (criteria.hostName != null && !criteria.hostName.isEmpty() && 
                    criteria.vmInstanceId != null && !criteria.vmInstanceId.isEmpty()) {

                TblHostsJpaController jpaController = My.jpa().mwHosts();
                TblHosts obj = jpaController.findByName(criteria.hostName);
                if (obj != null) {
                    HostAgentFactory factory = new HostAgentFactory();
                    HostAgent agent = factory.getHostAgent(obj);
                    VMAttestationResponse vmAttestationReport = agent.getVMAttestationStatus(criteria.vmInstanceId);
                    objCollection.getVMAttestations().add(convert(vmAttestationReport, criteria));
                }
                
            }
        } catch (IOException | CryptographyException ex) {
            log.error("Host:Retrieve - Error during search for hosts.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        
        return objCollection;
    }

    @Override
    @RequiresPermissions("vm_attestations:retrieve")    
    public VMAttestation retrieve(VMAttestationLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        log.debug("HostAttestation:Retrieve - Got request to retrieve the host attestation with id {}.", locator.id.toString());        
        return null;
    }
        
    @Override
    @RequiresPermissions("vm_attestations:store")    
    public void store(VMAttestation item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("vm_attestations:create")    
    public void create(VMAttestation item) {
        log.debug("VMAttestation:Create - Got request to create VM attestation with id {}.", item.getId().toString());  
        String nonce;
        JAXB jaxb = new JAXB();        
        if (item.getId() == null) { item.setId(new UUID()); }        
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
                    log.debug("VMAttestation:Create - Retrieved VM attestation report in MTW.");
                    
                    if (vmQuoteResponse == null) {
                        String errorInfo = "VMAttestation:Create - Error retrieving the attestation report for the specified VM.";
                        log.error(errorInfo);
                        throw new RepositoryCreateException(errorInfo);
                    }
                    
                    if (vmQuoteResponse.getVmQuote() == null || vmQuoteResponse.getVmQuote().length == 0) {
                        String errorInfo = "VMAttestation:Create - VM Quote received has null value. Please verify the input parameters.";
                        log.error(errorInfo);
                        throw new RepositoryCreateException(errorInfo);                        
                    }
                    
                    if (vmQuoteResponse.getVmTrustPolicy() == null || vmQuoteResponse.getVmTrustPolicy().length == 0) {
                        String errorInfo = "VMAttestation:Create - VM Quote received does not have an associated Trust Policy. VM Quote cannot be validated.";
                        log.error(errorInfo);
                        throw new RepositoryCreateException(errorInfo);                        
                    }

                    if (vmQuoteResponse.getVmMeasurements() == null || vmQuoteResponse.getVmMeasurements().length == 0) {
                        String errorInfo = "VMAttestation:Create - VM Quote received does not have an associated measurement log. Individual modules cannot be verified.";
                        log.error(errorInfo);
                        throw new RepositoryCreateException(errorInfo);                        
                    }

                    try {

                        String vmQuoteXml = IOUtils.toString(vmQuoteResponse.getVmQuote(), "UTF-8");
                        String trustPolicyXml = IOUtils.toString(vmQuoteResponse.getVmTrustPolicy(), "UTF-8");
                        String measurementXml = IOUtils.toString(vmQuoteResponse.getVmMeasurements(), "UTF-8");

                        log.debug("VMAttestation:Create - VMQuote : {} , TrustPolicy : {}, Measurements : {}", vmQuoteXml, trustPolicyXml, measurementXml);

                        boolean isVMQuoteValid = false;
                        boolean isTrustPolicyValid = false;

                        switch(vmQuoteResponse.getVmQuoteType()) {

                            case XML_DSIG:

                                log.debug("VMAttestation:Create - createSamlAssertion: XML_DSIG section");

                                try {
                                    // Validate the TrustPolicy signature and the certificate that was used to sign the TrustPolicy
                                    log.debug("VMAttestation:Create - About to validate the trust policy.");
                                    isTrustPolicyValid = XmlDsigVerify.isValid(trustPolicyXml, VMAttestations.getSamlCertificate());
                                    log.debug("VMAttestation:Create - Validation result of TrustPolicy is {}", isTrustPolicyValid);
                                } catch (Exception ex) {
                                    log.error("VMAttestation:Create - Error during validation of the TrustPolicy. {}", ex.getMessage());
                                    throw new RepositoryCreateException(ex);
                                }

                                try {
                                    // Validate the VM Quote signature and the certificate that was used to sign the signing key
                                    log.debug("VMAttestation:Create - About to validate the VMQuote using the PrivacyCA cert @ : {}", My.configuration().getPrivacyCaIdentityP12().getAbsolutePath());
                                    X509Certificate privacyCaCert = TpmUtils.certFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
                                    isVMQuoteValid = XmlDsigVerify.isValid(vmQuoteXml, privacyCaCert);
                                    log.debug("VMAttestation:Create - Validation result of VMQuote is {}", isVMQuoteValid);
                                } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | java.security.cert.CertificateException | ParserConfigurationException | SAXException | MarshalException | XMLSignatureException ex) {
                                    log.error("VMAttestation:Create - Error during validation of the VMQuote. {}", ex.getMessage());
                                    throw new RepositoryCreateException(ex);
                                }
                                
                                // Once we have verified the integrity of the files, we need to ensure that the nonce is matching with what 
                                // was sent to the call. After the nonce verification, the cumulative hash needs to be verfied with the 
                                // whitelist in the trust policy.
                                if (isVMQuoteValid && isTrustPolicyValid) {
                                    
                                    
                                    VmReport vmReport = new VmReport();
                                    vmReport.setVmTrustPolicy(trustPolicyXml);
                                    vmReport.setVmMeasurements(measurementXml);
                                    VmTrustReport vmTrustReport = new VmTrustReport(vmReport);
                                    
                                    RuleResult vmRuleResult = null;
                                    
                                    // Deserialize the TrustPolicy and VMQuote into the autogenerated objects
                                    TrustPolicy vmTrustPolicy = jaxb.read(trustPolicyXml, TrustPolicy.class);
                                    VMQuote vmQuote = jaxb.read(vmQuoteXml, VMQuote.class);
                                    
                                    String cumulativeHashFromQuote = vmQuote.getCumulativeHash();
                                    String vmInstanceIdFromQuote = vmQuote.getVmInstanceId();
                                    String nonceFromQuote = vmQuote.getNonce();


                                    if (nonce == null ? nonceFromQuote != null : !nonce.equals(nonceFromQuote)) {
                                        log.error("VMAttestation:Create - Error during verification of the VM Attestation report. Nonce sent {} does not match the nonce in report {}.", nonce, nonceFromQuote);
                                        throw new RepositoryCreateException();
                                    }

                                    boolean isVMTrusted = false;

                                    if (cumulativeHashFromQuote == null ? vmTrustPolicy.getImage().getImageHash().getValue() != null : 
                                            !cumulativeHashFromQuote.equals(vmTrustPolicy.getImage().getImageHash().getValue())) {
                                        log.error("VMAttestation:Create - Hash value of the VM {} does not match the white list value {} specified in the Trust Policy.",
                                                cumulativeHashFromQuote, vmTrustPolicy.getImage().getImageHash().getValue());
                                        
                                        // Compare the measurements against the whitelists to see which module failed.
                                        List<Measurement> actualModules = new ArrayList<>();
                                        List<Measurement> whitelistModules = new ArrayList<>();
                                        
                                        actualModules = new XmlMeasurementLog(PcrIndex.PCR19, measurementXml).getMeasurements();
                                        
                                        String whiteListXml = trustPolicyXml.substring(trustPolicyXml.indexOf("<Whitelist"), (trustPolicyXml.indexOf("</Whitelist>") + "</Whitelist>".length()));
                                        whiteListXml = whiteListXml.replaceFirst("<Whitelist", "<Measurements xmlns=\"mtwilson:trustdirector:measurements:1.1\"");
                                        whiteListXml = whiteListXml.replaceAll("</Whitelist>", "</Measurements>");

                                        whitelistModules = new XmlMeasurementLog(PcrIndex.PCR19, whiteListXml).getMeasurements();
                                        
                                        VmMeasurementLogEquals vmMeasurementLogEqualsRule = new VmMeasurementLogEquals();                                        
                                        vmRuleResult = vmMeasurementLogEqualsRule.apply2(actualModules, whitelistModules);
                                        
                                        vmTrustReport.addResult(vmRuleResult);
                                        
                                    } else {
                                        isVMTrusted = true;
                                        log.debug("VMAttestation:Create - VM Trust status is {}", isVMTrusted);
                                    }

                                    // Create a map of the VM attributes that needs to be added to the SAML assertion.
                                    Map<String, String> vmAttributes = new HashMap<>();
                                    vmAttributes.put("VM_Trust_Status", String.valueOf(isVMTrusted));
                                    vmAttributes.put("VM_Instance_Id", vmInstanceIdFromQuote);
                                    vmAttributes.put("VM_Trust_Policy", vmTrustPolicy.getLaunchControlPolicy());
                                    
                                    log.debug("VMAttestation:Create - About to generate the VM attestation report for VM with ID {}", vmInstanceIdFromQuote);
                                    VMAttestation report = new HostTrustBO().getVMAttestationReport(obj, vmAttributes, item.isIncludeHostReport());
                                    log.debug("VMAttestation:Create - Successfully generated the VM attestation report for VM with ID {}", vmInstanceIdFromQuote);
                                    
                                    // Include the host report only if requested
                                    if (item.isIncludeHostReport()) {
                                        item.setHostAttestation(report.getHostAttestation());
                                        log.debug("VMAttestation:Create - Host SAML assertions is {}.", item.getHostAttestation().getSaml());
                                    }
                                    
                                    log.debug("VMAttestation:Create - VM SAML assertions is {}.", item.getVmSaml());

                                    item.setVmSaml(report.getVmSaml());
                                    item.setTrustStatus(isVMTrusted);
                                    item.setVmTrustReport(vmTrustReport);
                                                                                                                                                
                                } else {
                                    log.error("Invalid signature specified.");
                                    throw new RepositoryCreateException();
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
    }

    @Override
    @RequiresPermissions("vm_attestations:delete")    
    public void delete(VMAttestationLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("vm_attestations:delete")    
    public void delete(VMAttestationFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
    private VMAttestation convert(VMAttestationResponse obj, VMAttestationFilterCriteria criteria) {
        VMAttestation convObj = new VMAttestation();
        convObj.setHostName(criteria.hostName);
        convObj.setVmInstanceId(criteria.vmInstanceId);
        convObj.setTrustStatus(obj.isTrustStatus());
        return convObj;
    }
        
}
