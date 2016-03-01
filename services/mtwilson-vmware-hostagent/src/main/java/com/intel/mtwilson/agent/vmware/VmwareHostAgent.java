/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.vmware;

import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.model.Aik;
import com.intel.mtwilson.model.Nonce;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.model.TpmQuote;
import com.intel.mtwilson.trustagent.model.VMAttestationRequest;
import com.intel.mtwilson.trustagent.model.VMAttestationResponse;
import com.intel.mtwilson.trustagent.model.VMQuoteResponse;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostTpmAttestationReport;
import com.vmware.vim25.HostTpmDigestInfo;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import java.rmi.RemoteException;
//import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
//import com.vmware.vim25.RuntimeFaultFaultMsg;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of VmwareAgent should be created by the VmwareAgentFactory
 *
 * A single instance of VmwareHostAgent is tied to a specific host/connection --
 * and it maintains a cache of some information received in order to not send
 * multiple redundant requests to the host. For example if it already obtained
 * the OS Name, that is not going to change between one call and the next so it
 * may be cached. TPM Quotes are never cached. If you want to be sure to get
 * fresh data, create a new instance. 
 * @author jbuhacoff
 */
public class VmwareHostAgent implements HostAgent {

    private transient Logger log = LoggerFactory.getLogger(getClass());
    private transient final VMwareClient vmware;
    private final String hostname;
    private transient ManagedObjectReference hostMOR = null;
    private String vCenterVersion = null;
    private String esxVersion = null;
    private Boolean isTpmAvailable = null;
//    private String vendorHostReport = null;
    private PcrManifest pcrManifest = null;

    public VmwareHostAgent(VMwareClient vmwareClient, String hostname) throws RemoteException {
        vmware = vmwareClient;
        this.hostname = hostname;
        hostMOR = vmwareClient.getHostReference(hostname); // issue #784 using more efficient method of getting a reference to the host  //vmwareClient.getManagedObjectReference(hostname);
        vCenterVersion = vmwareClient.getVCenterVersion(); //serviceContent.getAbout().getVersion(); // required so we can choose implementations
        log.debug("VCenter version is {}", vCenterVersion);
        //esxVersion = vmwareClient.getMORProperty(hostMOR, "config.product.version").toString(); // required so we can choose implementations and report on host info
        esxVersion = vmwareClient.getStringMEProperty(hostMOR.type, hostname, "config.product.version");
        log.debug("esxVersion: " + esxVersion);
    }

    public VMwareClient getClient() {
        return vmware;
    }

    /**
     * Currently this is getting called every time we request a PcrManifest --
     * that's not necessary, we only need to check the host's TPM once when we
     * add it. After that we can assume that it is there, and only check it
     * again if we get an error while trying to read the PcrManifest.
     *
     * @return
     */
    @Override
    public boolean isTpmPresent() {
        try {
            if (isTpmAvailable == null) {
                isTpmAvailable = (Boolean) vmware.getMEProperty(hostMOR.type, hostname, "capability.tpmSupported");
            }
            return isTpmAvailable;
        } catch (InvalidProperty ex) {
            log.error("VCenter host does not support 'capability.tpmSupported' property: {}", ex.getLocalizedMessage());
            return false;
        } catch (RuntimeFault ex) {
            log.error("Runtime fault while fetching 'capability.tpmSupported' property: {}", ex.getLocalizedMessage());
            return false;
        } catch (RemoteException ex) {
            log.error("Runtime fault while fetching 'capability.tpmSupported' property: {}", ex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public boolean isTpmEnabled() {
        return true; 
    }

    @Override
    public boolean isEkAvailable() {
        return false; // vmware does not make the EK available through its API
    }

    @Override
    public boolean isAikAvailable() {
        return false; // vmware does not make the AIK available through its API
    }

    @Override
    public boolean isAikCaAvailable() {
        return false; // vmware does not make the Privacy CA Certificate available through its API, if it even uses a Privacy CA
    }

    @Override
    public boolean isDaaAvailable() {
        return false; // vmware does not support DAA
    }

    /**
     * Throws an exception because it is a programming error to call
     * getAikCertificate without first checking the result of isAikCaAvailable()
     *
     * @return
     */
    @Override
    public X509Certificate getAikCertificate() {
        throw new UnsupportedOperationException("Vmware does not provide an AIK Certificate");
//        return null;  
    }

    /**
     * Throws an exception because it is a programming error to call
     * getAikCaCertificate without first checking the result of
     * isAikCaAvailable()
     *
     * @return
     */
    @Override
    public X509Certificate getAikCaCertificate() {
        throw new UnsupportedOperationException("Vmware does not provide a Privacy CA Certificate");
//        return null; 
    }

    @Override
    public String getHostInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Example ESXi report for a host that does NOT have a TPM:
     * <?xml version="1.0" ?><Host_Attestation_Report Host_Name="10.1.71.176"
     * vCenterVersion="5.1.0" HostVersion="5.1.0" TXT_Support="false"><PCRInfo
     * Error="Host does not support TPM."></PCRInfo></Host_Attestation_Report>
     *
     * @return
     * @throws IOException E
     */
    @Override
    public String getVendorHostReport() throws IOException {
        /*
         try {
         getAllPcrAndModuleInformationFromHost();
         return vendorHostReport;
         }
         catch(Exception e) {
         log.error("Cannot get vendor report", e);
         return null;
         }*/
        try {
            return vmware.getHostAttestationReport(hostMOR, hostname, null);         // will get default pcr list
        } catch (VMwareConnectionException e) {
            throw new IOException(String.format("Cannot get attestation report from host '%s': %s", hostname, e.toString()), e);
        }
    }

    @Override
    public TpmQuote getTpmQuote(Aik aik, Nonce nonce, Set<PcrIndex> pcr) {
        throw new UnsupportedOperationException("Vmware does not provide TPM Quotes"); 
    }

    //private void getAllPcrAndModuleInformationFromHost() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, JAXBException {
    //}
    // Commenting the below function since it is not being used and klocwork is throwing a warning
    /*private <T> String toXml(Class<T> clazz, T object) throws JAXBException {
     JAXBElement<T> xmlReport = new JAXBElement<T>(new QName("urn:vim25"),clazz,object);
     StringWriter sw = new StringWriter();
     JAXBContext jc = JAXBContext.newInstance("com.vmware.vim25");
        
     Marshaller marshaller = jc.createMarshaller();
     marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
     marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");        
     marshaller.marshal(xmlReport, sw);
        
     //log.info(sw.toString());
        
     return sw.toString();
        
     }*/
    
    @Override
    public PcrManifest getPcrManifest(Nonce challenge) throws IOException {
        log.error("vmware does not provide TPM quotes; ignoring challenge nonce: {}", challenge);
        return getPcrManifest();
    }
    
    @Override
    public PcrManifest getPcrManifest() throws IOException {
        try {
//            if( isTpmPresent() ) { // issue #784 performance; no need to check if tpm is present, just try to get the report and if there's an error we can run diagnostics after
            // if (vCenterVersion.contains("5.1")) {
            if (vmware.isModuleAttestationSupportedByVcenter(vCenterVersion)) {
                HostTpmAttestationReport report = vmware.getAttestationReport(hostMOR);
//                                        if(hostId != null)
//                                            auditAttestionReport(hostId,report); 
                log.debug("Retrieved HostTpmAttestationReport: {}", report);
//                                        vendorHostReport = toXml(HostTpmAttestationReport.class, report);
//                log.debug("Parsed HostTpmAttestationReport.");
//					manifestMap = postProcessing.processReport(esxVersion,report);
                //if(esxVersion.contains("5.1")) {
                if (vmware.isModuleAttestationSupportedByESX(esxVersion)) {
                    pcrManifest = VMWare51Esxi51.createPcrManifest(report);
                } else {
                    //                    return new VMWare50Esxi50().getPcrManiFest(report, 
                    //        getRequestedPcrs(host));
                }
            } else if (vCenterVersion.contains("5.0")) { 

                HostRuntimeInfo runtimeInfo = (HostRuntimeInfo) vmware.getMEProperty(hostMOR.type, hostname, "runtime");
                if (runtimeInfo == null) {
                    throw new IllegalArgumentException("Cannot get host information");
                }
//                                        vendorHostReport = toXml(HostRuntimeInfo.class, runtimeInfo);
                // Now process the digest information
                List<HostTpmDigestInfo> htdis = Arrays.asList(runtimeInfo.getTpmPcrValues());
                log.debug("Retrieved HostTpmDigestInfo");
                // ESX 5.0 did not support module measurement so we return only the PCR's
                pcrManifest = VMWare50Esxi50.createPcrManifest(htdis); // bug #607 new
//					pcrManifest =  postProcessing.processDigest(esxVersion,htdis);
            } else {
                throw new UnsupportedOperationException("Not supported yet.");
            }
//            }        
        } catch (Exception e) {
            log.warn("error during getManifest: " + e.toString(), e);
            boolean isTpmPresent = false;
            try {
                if (isTpmPresent()) {
                    isTpmPresent = true;
                }
            } catch (Exception e2) {
                throw new IOException("Cannot retrieve PCR Manifest from " + hostname + ": cannot determine if TPM is present", e2);
            }
            throw new IOException("Cannot retrieve PCR Manifest from " + hostname + ": " + (isTpmPresent ? "TPM is present" : "TPM is not present") + ": " + e.toString(), e);
        }
        return pcrManifest;
    }

    /**
     * Content of this method is same as getHostDetails() in VMwareHelper in
     * Management Service and VMwareClient in trust utils library.
     *
     * @return
     */
    @Override
    public TxtHostRecord getHostDetails() throws IOException {
        try {
            TxtHostRecord host = new TxtHostRecord();
            host.HostName = vmware.getStringMEProperty(hostMOR.type, hostname, "name");
            // hostObj.Description = serviceContent.getAbout().getVersion();
            host.VMM_Name = vmware.getStringMEProperty(hostMOR.type, hostname, "config.product.name"); 
            host.VMM_OSName = vmware.getStringMEProperty(hostMOR.type, hostname, "config.product.name");
            host.VMM_OSVersion = vmware.getStringMEProperty(hostMOR.type, hostname, "config.product.version");
            host.VMM_Version = vmware.getStringMEProperty(hostMOR.type, hostname, "config.product.build");
            host.BIOS_Oem = vmware.getStringMEProperty(hostMOR.type, hostname, "hardware.systemInfo.vendor");
            host.BIOS_Name = vmware.getStringMEProperty(hostMOR.type, hostname, "hardware.systemInfo.vendor"); 
            host.BIOS_Version = vmware.getStringMEProperty(hostMOR.type, hostname, "hardware.biosInfo.biosVersion");

            /*
             // Possible values for this processor Info includes. So, if there is a "-", we are assuming that it is either a Sandy Bridge or a IVY bridge system
             // For others starting with X56, they are Westmere systems belonging to Thurley platform
             // Romley: "Intel(R) Xeon(R) CPU E5-2680 0 @ 2.70GHz"
             // Thurley: "Intel(R) Xeon(R) CPU X5680 @ 3.33GHz"
             String processorInfo = vmware.getMORProperty(hostMOR, "summary.hardware.cpuModel").toString();
             processorInfo = processorInfo.substring((processorInfo.indexOf("CPU") + ("CPU").length())).trim();
             if (processorInfo.contains("-") || processorInfo.contains("-")) {
             processorInfo = processorInfo.substring(0, processorInfo.indexOf("-"));
             } else {
             processorInfo = processorInfo.substring(0, 3);
             }*/
            // There is one more attribute in the vCenter that actually provides the processor name directly unlike the open source hosts where we
            // need to do the mapping
            // Possible values include: "intel-westmere", "intel-sandybridge"
            String processorInfo = vmware.getStringMEProperty(hostMOR.type, hostname, "summary.maxEVCModeKey");
            if (processorInfo != null) {
                processorInfo = processorInfo.toLowerCase();
                if (processorInfo.contains("intel")) {
                    processorInfo = processorInfo.substring("intel".length() + 1);
                }
                host.Processor_Info = processorInfo.substring(0, 1).toUpperCase() + processorInfo.substring(1);
            }
            return host;
        } catch (InvalidProperty ex) {
            log.error("VCenter host does not support host details property: {}", ex.getLocalizedMessage());
            throw new IOException(ex);
        } catch (RuntimeFault ex) {
            log.error("Runtime fault while fetching host details: {}", ex.getLocalizedMessage());
            throw new IOException(ex);
        }

    }

    @Override
    public String getHostAttestationReport(String pcrList, Nonce challenge) throws IOException {
        log.error("vmware does not support user-specified nonce, ignoring challenge: {}", challenge);
        return getHostAttestationReport(pcrList);
    }
    
    @Override
    public String getHostAttestationReport(String pcrList) throws IOException {
        try {
            return vmware.getHostAttestationReport(hostMOR, hostname, pcrList);
        } catch (VMwareConnectionException e) {
            throw new IOException(String.format("Cannot get attestation report from host '%s': %s", hostname, e.toString()), e);
        }
    }

    @Override
    public boolean isIntelTxtSupported() {
        return true; 
    }

    @Override
    public boolean isIntelTxtEnabled() {
        return true; 
    }

    @Override
    public PublicKey getAik() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public X509Certificate getEkCertificate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, String> getHostAttributes() throws IOException {
        HashMap<String, String> hm = new HashMap<>();
        // Retrieve the data from the host and add it into the hashmap
        // Currently we are just adding the UUID of th host. Going ahead we can add additional details
        String hostUUID = vmware.getStringMEProperty("HostSystem", hostname, "hardware.systemInfo.uuid");

        hm.put("Host_UUID", hostUUID);
        return hm;
    }

    @Override
    public void setAssetTag(com.intel.dcsg.cpg.crypto.Sha1Digest tag) throws IOException {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public X509Certificate getBindingKeyCertificate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VMAttestationResponse getVMAttestationStatus(String vmInstanceId) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VMQuoteResponse getVMAttestationReport(VMAttestationRequest obj) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
