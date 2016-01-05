/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.v2.vm.attestation.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.MwVmAttestationReport;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.VMAttestation;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationLocator;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.measurement.xml.FileMeasurementType;
import com.intel.mtwilson.measurement.xml.DirectoryMeasurementType;
import com.intel.mtwilson.measurement.xml.MeasurementType;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.XmlMeasurementLog;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.VmReport;
import com.intel.mtwilson.policy.VmTrustReport;
import com.intel.mtwilson.policy.rule.VmMeasurementLogEquals;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.trustagent.model.VMAttestationRequest;
import com.intel.mtwilson.trustagent.model.VMQuoteResponse;
import static com.intel.mtwilson.trustagent.model.VMQuoteResponse.QuoteType.XML_DSIG;
import com.intel.mtwilson.trustpolicy.xml.DirectoryMeasurement;
import com.intel.mtwilson.util.xml.dsig.XmlDsigVerify;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy;
import com.intel.mtwilson.measurement.xml.Measurements;
import com.intel.mtwilson.model.VmMeasurement;
import com.intel.mtwilson.model.VmMeasurementLog;
import com.intel.mtwilson.vmquote.xml.VMQuote;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            
    @Override
    @RequiresPermissions("vm_attestations:search")    
    public VMAttestationCollection search(VMAttestationFilterCriteria criteria) {
        log.debug("VMAttestation:Search - Got request to search for VM attestations.");
        VMAttestationCollection objCollection = new VMAttestationCollection();
        try {
            if (criteria.id != null) {
                log.debug("VMAttestation:Search - User has specified the UUID of the VM attestation request as the search criteria. {}", criteria.id.toString());
                MwVmAttestationReport obj = My.jpa().mwVmAttestationReport().findById(criteria.id.toString());
                if (obj != null) {
                    objCollection.getVMAttestations().add(convert(obj));
                }
            } else if (criteria.numberOfDays == 0 && criteria.fromDate == null) {
                // No date criteria is specified. We can check if the user has specified the details of the VM or host
                if (criteria.vmInstanceId != null && !criteria.vmInstanceId.isEmpty()) {
                    log.debug("VMAttestation:Search - User has specified the VM instance ID as the search criteria. {}", criteria.vmInstanceId);
                    List<MwVmAttestationReport> objList = My.jpa().mwVmAttestationReport().findByVMInstanceId(criteria.vmInstanceId);
                    if (objList != null && objList.size() > 0) {
                        for (MwVmAttestationReport obj : objList) {
                            objCollection.getVMAttestations().add(convert(obj));
                        }
                    }
                } else if (criteria.hostName != null && !criteria.hostName.isEmpty()) {
                    log.debug("VMAttestation:Search - User has specified the Host name as the search criteria. {}", criteria.hostName);
                    List<MwVmAttestationReport> objList = My.jpa().mwVmAttestationReport().findByHostName(criteria.hostName);
                    if (objList != null && objList.size() > 0) {
                        for (MwVmAttestationReport obj : objList) {
                            objCollection.getVMAttestations().add(convert(obj));
                        }
                    }
                }
            } else {
                // Lets check if the user has specified the date criteria
                log.debug("VMAttestation:Search - User has specified date criteria for searching the VM attestations.");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                Date toDate, fromDate;

                if (criteria.numberOfDays != 0) {
                    log.debug("VMAttestation:Search - Number of days criteria is specified with value {}.", criteria.numberOfDays);
                    // calculate from and to dates
                    toDate = new Date(); // Get the current date and time
                    cal.setTime(toDate);
                    toDate = dateFormat.parse(dateFormat.format(cal.getTime()));
                    // To get the fromDate, we substract the number of days fromm the current date.
                    cal.add(Calendar.DATE, -(criteria.numberOfDays));
                    fromDate = dateFormat.parse(dateFormat.format(cal.getTime()));
                } else {
                    if (criteria.fromDate != null && !criteria.fromDate.isEmpty() && criteria.toDate != null && !criteria.toDate.isEmpty()) {
                        log.debug("VMAttestation:Search - Dates are specified for the search criteria with values {} - {}.", criteria.fromDate, criteria.toDate);
                        Iso8601Date fromIso8601Date = Iso8601Date.valueOf(criteria.fromDate);
                        cal.setTime(fromIso8601Date); // This would set the time to ex:2015-05-30 00:00:00
                        fromDate = dateFormat.parse(dateFormat.format(cal.getTime()));

                        Iso8601Date toIso8601Date = Iso8601Date.valueOf(criteria.toDate);
                        cal.setTime(toIso8601Date);
                        toDate = dateFormat.parse(dateFormat.format(cal.getTime()));
                    } else {
                        String errorMsg = "VMAttestation:Search - Invalid date search criteria specified for attestation search.";
                        log.error(errorMsg);
                        throw new RepositoryInvalidInputException(errorMsg);
                    }
                }
                log.debug("HostAttestation:Search - Calculated the date values {} - {}.", dateFormat.format(fromDate), dateFormat.format(toDate));
                
                if (criteria.vmInstanceId != null && !criteria.vmInstanceId.isEmpty()) {
                    log.debug("VMAttestation:Search - User has specified the VM instance ID as the search criteria for the specified date range. {}", criteria.vmInstanceId);
                    List<MwVmAttestationReport> objList = My.jpa().mwVmAttestationReport().getListByVMAndDateRange(criteria.vmInstanceId, fromDate, toDate);
                    if (objList != null && objList.size() > 0) {
                        for (MwVmAttestationReport obj : objList) {
                            objCollection.getVMAttestations().add(convert(obj));
                        }
                    }
                } else if (criteria.hostName != null && !criteria.hostName.isEmpty()) {
                    log.debug("VMAttestation:Search - User has specified the Host name as the search criteria for the specified date range. {}", criteria.hostName);
                    List<MwVmAttestationReport> objList = My.jpa().mwVmAttestationReport().getListByHostAndDateRange(criteria.hostName, fromDate, toDate);
                    if (objList != null && objList.size() > 0) {
                        for (MwVmAttestationReport obj : objList) {
                            objCollection.getVMAttestations().add(convert(obj));
                        }
                    }
                }
            }
        } catch (RepositoryException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("VMAttestation:Search - Error during search for hosts.", ex);
            throw new RepositorySearchException(ex, criteria);
        }

        return objCollection;
    }

    @Override
    @RequiresPermissions("vm_attestations:retrieve")    
    public VMAttestation retrieve(VMAttestationLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        log.debug("VMAttestation:Retrieve - Got request to retrieve the host attestation with id {}.", locator.id.toString());   
        String id = locator.id.toString();
        try {
            MwVmAttestationReport obj = My.jpa().mwVmAttestationReport().findById(id);
            if (obj != null) {
                return convert(obj);
            }
        } catch (Exception ex) {
            log.error("VMAttestation:Retrieve -  - Error during VM attestation report retrieval.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }        
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
        log.debug("VMAttestation:Create - Got request to create VM attestation report.");  
        
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

                        boolean isVMQuoteValid;
                        boolean isTrustPolicyValid;

                        switch(vmQuoteResponse.getVmQuoteType()) {

                            case XML_DSIG:

                                log.debug("VMAttestation:Create - createSamlAssertion: XML_DSIG section");

                                try {
                                    // Validate the TrustPolicy signature and the certificate that was used to sign the TrustPolicy
                                    log.debug("VMAttestation:Create - About to validate the trust policy.");
                                    isTrustPolicyValid = XmlDsigVerify.isValid(trustPolicyXml, getSamlCertificate());
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
                                    
                                    RuleResult vmRuleResult;
                                    
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
                                    } else {
                                        isVMTrusted = true;
                                        log.debug("VMAttestation:Create - VM Trust status is {}", isVMTrusted);
                                    }
                                    
                                    // Compare the measurements against the whitelists to see which module failed.
                                    List<VmMeasurement> actualModules;
                                    List<VmMeasurement> whitelistModules;

                                    actualModules = new VmMeasurementLog(measurementXml).getMeasurements();

                                    // creating Measurements Object which will be converted into measurement xml string
                                    Measurements whitelistObj = new Measurements();
                                    List<MeasurementType> measurements = whitelistObj.getMeasurements();
                                    for (com.intel.mtwilson.trustpolicy.xml.Measurement measurement: vmTrustPolicy.getWhitelist().getMeasurements()){
                                        MeasurementType measurementType;
                                        if(measurement instanceof DirectoryMeasurement){
                                            DirectoryMeasurementType dirMeasurementType = new DirectoryMeasurementType();
                                            com.intel.mtwilson.trustpolicy.xml.DirectoryMeasurement dirMeasurement = (com.intel.mtwilson.trustpolicy.xml.DirectoryMeasurement) measurement;
                                            dirMeasurementType.setExclude(dirMeasurement.getExclude());
                                            dirMeasurementType.setInclude(dirMeasurement.getInclude());
                                            measurementType = dirMeasurementType;
                                        }
                                        else{
                                            measurementType = new FileMeasurementType();
                                        }
                                        measurementType.setPath(measurement.getPath());
                                        measurementType.setValue(measurement.getValue());
                                        measurements.add(measurementType);
                                    }
                                    String whiteListXml = jaxb.write(whitelistObj);
                                    
                                    whitelistModules = new VmMeasurementLog(whiteListXml).getMeasurements();
                                    VmMeasurementLogEquals vmMeasurementLogEqualsRule = new VmMeasurementLogEquals();                                        
                                    vmRuleResult = vmMeasurementLogEqualsRule.apply2(actualModules, whitelistModules);
                                    if (vmRuleResult.getFaults() != null && vmRuleResult.getFaults().size() > 0)
                                        isVMTrusted = false;
                                    
                                    vmTrustReport.addResult(vmRuleResult);

                                    // Create a map of the VM attributes that needs to be added to the SAML assertion.
                                    Map<String, String> vmAttributes = new HashMap<>();
                                    vmAttributes.put("VM_Trust_Status", String.valueOf(isVMTrusted));
                                    vmAttributes.put("VM_Instance_Id", vmInstanceIdFromQuote);
                                    vmAttributes.put("VM_Trust_Policy", vmTrustPolicy.getLaunchControlPolicy().value());
                                    
                                    log.debug("VMAttestation:Create - About to generate the VM attestation report for VM with ID {}", vmInstanceIdFromQuote);
                                    VMAttestation report = new HostTrustBO().getVMAttestationReport(obj, vmAttributes, item.getIncludeHostReport());
                                    log.debug("VMAttestation:Create - Successfully generated the VM attestation report for VM with ID {}", vmInstanceIdFromQuote);
                                    
                                    // Include the host report only if requested
                                    if (item.getIncludeHostReport()) {
                                        item.setHostAttestationReport(report.getHostAttestationReport());
                                        log.debug("VMAttestation:Create - Host SAML assertions is {}.", item.getHostAttestationReport().getSaml());
                                    }
                                    
                                    log.debug("VMAttestation:Create - VM SAML assertions is {}.", report.getVmSaml());
                                    
                                    // While generating the VM Attestation report, it would have already been checked if the host is also trusted
                                    // and accordingly the vm trust status flag would have been updated.
                                    if (isVMTrusted && !report.isTrustStatus()) {
                                        log.error("VMAttestation:Create - VM trust status is being set to false since the host is not trusted.");
                                        isVMTrusted = false;
                                    }

                                    item.setVmSaml(report.getVmSaml());
                                    item.setTrustStatus(isVMTrusted);
                                    item.setVmTrustReport(vmTrustReport);
                                                                  
                                    // Store the VM attestation report in the DB
                                    try {
                                        
                                        log.debug("getVMAttestationReport: About to store the VM attestation report in the DB");
                                        MwVmAttestationReport mwVmAttestationReport = new MwVmAttestationReport();
                                        mwVmAttestationReport.setId(item.getId().toString());
                                        mwVmAttestationReport.setVmInstanceId(vmInstanceIdFromQuote);
                                        mwVmAttestationReport.setVmTrustStatus(isVMTrusted);
                                        mwVmAttestationReport.setHostName(obj.getName());
                                        mwVmAttestationReport.setVmSaml(report.getVmSaml());
                                        mwVmAttestationReport.setVmTrustReport(mapper.writeValueAsString(vmTrustReport));
                                        if (item.getIncludeHostReport() && report.getHostAttestationReport() != null)
                                            mwVmAttestationReport.setHostAttestationReport(mapper.writeValueAsString(report.getHostAttestationReport()));
                                        
                                        mwVmAttestationReport.setCreatedTs(Calendar.getInstance().getTime());
                                        
                                        Integer samlExpiry = My.configuration().getSamlValidityTimeInSeconds();
                                        Calendar cal = Calendar.getInstance();
                                        cal.add(Calendar.SECOND, samlExpiry);
                                        mwVmAttestationReport.setExpiryTs(cal.getTime());
                                        
                                        My.jpa().mwVmAttestationReport().create(mwVmAttestationReport);
                                    } catch (Exception ex) {
                                        // Do we throw the exception or just log it since we are anyway returning back the report
                                        log.error("VMAttestation:Create - Error during storing the VM attestation report in the DB.", ex);                                        
                                    }
                                    
                                } else {
                                    log.error("VMAttestation:Create - Invalid signature specified.");
                                    throw new RepositoryCreateException();
                                }
                            default:
                                break;
                        }

                    } catch (RepositoryCreateException ex) {
                        throw ex;
                    } catch (IOException | JAXBException | XMLStreamException ex ){ //| ParserConfigurationException | SAXException | MarshalException | XMLSignatureException ex) {
                        log.error("VMAttestation:Create - Error during validation of the VM attestation report. {}", ex.getMessage());
                        throw new RepositoryCreateException(ex);
                    }
                } else {
                    log.error("VMAttestation:Create - Host specified {} does not exist in the system. Please verify the input parameters.", item.getHostName());
                    throw new RepositoryInvalidInputException();
                }
                
            } else {
                // Since there are some missing or invalid inputs, throw an appropriate exception.
                throw new RepositoryInvalidInputException();
            }
        } catch (RepositoryCreateException | RepositoryInvalidInputException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("VMAttestation:Create - Error during generation of host saml assertion. {} ", ex.getMessage());
            throw new RepositoryCreateException(ex);
        } 
    }

    @Override
    @RequiresPermissions("vm_attestations:delete")    
    public void delete(VMAttestationLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("VMAttestation:Delete - Got request to delete VM attestation report with id {}.", locator.id.toString());        
        try {
            VMAttestation obj = retrieve(locator);
            if (obj != null) {
                My.jpa().mwVmAttestationReport().destroy(obj.getId().toString());
                log.debug("VMAttestation:Delete - Deleted the VM attestation report with id {} successfully.", locator.id.toString());
            } else {
                log.error("VMAttestation:Delete - Specified object not found.");
                throw new RepositoryDeleteException();
            }
        } catch (RepositoryException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("VMAttestation:Delete - Error during VM attestation report deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("vm_attestations:delete")    
    public void delete(VMAttestationFilterCriteria criteria) {
        log.debug("VMAttestation:DeleteBySearch - Got request to delete VM attestation report by search criteria.");        
        VMAttestationCollection objCollection = search(criteria);
        try { 
            for (VMAttestation obj : objCollection.getVMAttestations()) {
                VMAttestationLocator locator = new VMAttestationLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("VMAttestation:DeleteBySearch - Error during deletion of VM attestation report by search criteria.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
   
    private VMAttestation convert(MwVmAttestationReport obj) {
        try {
            VMAttestation convObj = new VMAttestation();
            convObj.setId(UUID.valueOf(obj.getId()));
            convObj.setVmInstanceId(obj.getVmInstanceId());
            convObj.setTrustStatus(obj.isVmTrustStatus());
            convObj.setHostName(obj.getHostName());
            if (obj.getVmSaml() != null)
                convObj.setVmSaml(obj.getVmSaml());
            if (obj.getVmTrustReport() != null)
                convObj.setVmTrustReport(mapper.readValue(obj.getVmTrustReport(), VmTrustReport.class));
            if (obj.getHostAttestationReport() != null)
                convObj.setHostAttestationReport(mapper.readValue(obj.getHostAttestationReport(), HostAttestation.class));
            if (obj.getErrorMessage()!= null)
                convObj.setErrorMessage(obj.getErrorMessage());
            return convObj;
        } catch (Exception ex) {
            log.error("VMAttestation:Search - Error during search for hosts.", ex);
            throw new RepositorySearchException(ex);
        }
    }
     
    public static X509Certificate getSamlCertificate() {
        X509Certificate samlCert = null;        
        byte[] samlPemBytes;
        try (FileInputStream samlPemFile = new FileInputStream(My.configuration().getSamlCertificateFile())) {

            samlPemBytes = IOUtils.toByteArray(samlPemFile);
            samlCert = X509Util.decodePemCertificate(new String(samlPemBytes));
            log.debug("Successfully retrieved the SAML certificate for verification. {}", samlCert.getIssuerX500Principal().getName());

            
        } catch (IOException | java.security.cert.CertificateException ex) {
            log.error("Error during verification of the certificate that signed the data. {}", ex.getMessage());
        } 
                
        return samlCert;
    }
    
}
