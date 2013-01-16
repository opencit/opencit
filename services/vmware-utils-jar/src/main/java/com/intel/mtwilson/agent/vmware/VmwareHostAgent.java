/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.vmware;

import com.intel.mountwilson.manifest.data.IManifest;
import com.intel.mountwilson.manifest.strategy.helper.VMWare51Esxi51;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.audit.data.AuditLog;
import com.intel.mtwilson.datatypes.Aik;
import com.intel.mtwilson.datatypes.Nonce;
import com.intel.mtwilson.datatypes.PcrIndex;
import com.intel.mtwilson.datatypes.Pcr;
import com.intel.mtwilson.datatypes.TpmQuote;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostTpmAttestationReport;
import com.vmware.vim25.HostTpmDigestInfo;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of VmwareAgent should be created by the VmwareAgentFactory
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
    private String vendorHostReport = null;
    private HashMap<String, ? extends IManifest> manifestMap = null; // XXX TODO needs to change, it's not a clear programming interface
    
    public VmwareHostAgent(VMwareClient vmwareClient, String hostname) throws Exception {
        vmware = vmwareClient;
        this.hostname = hostname;
        hostMOR = vmwareClient.getManagedObjectReference(hostname);
        vCenterVersion = vmwareClient.getVCenterVersion(); //serviceContent.getAbout().getVersion(); // required so we can choose implementations
        log.info("VCenter version is {}", vCenterVersion);
        esxVersion = vmwareClient.getMORProperty(hostMOR, "config.product.version").toString(); // required so we can choose implementations and report on host info
    }
    
    
    
    @Override
    public boolean isTpmAvailable() {
            try {
                if( isTpmAvailable == null ) {
                    isTpmAvailable = (Boolean)vmware.getMORProperty(hostMOR,
                                "capability.tpmSupported");
                }
                return isTpmAvailable;
            } catch (InvalidPropertyFaultMsg ex) {
                log.error("VCenter host does not support 'capability.tpmSupported' property: {}", ex.getLocalizedMessage());
                return false;
            } catch (RuntimeFaultFaultMsg ex) {
                log.error("Runtime fault while fetching 'capability.tpmSupported' property: {}", ex.getLocalizedMessage());
                return false;
            }
    }

    @Override
    public boolean isTpmEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public X509Certificate getAikCertificate() {
        throw new UnsupportedOperationException("Vmware does not provide an AIK Certificate");
//        return null;  // XXX TODO throw exception or return null? call should first check isAikAvailable // vmware does not make the AIK available through its API
    }

    @Override
    public X509Certificate getAikCaCertificate() {
        throw new UnsupportedOperationException("Vmware does not provide a Privacy CA Certificate");
//        return null; // XXX TODO throw exception or return null? call should first check isAikCaAvailable  // vmware does not make the Privacy CA Certificate available through its API, if it even uses a Privacy CA
    }

    @Override
    public String getHostInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Example ESXi report for a host that does NOT have a TPM:
     * <?xml version="1.0" ?><Host_Attestation_Report Host_Name="10.1.71.176" vCenterVersion="5.1.0" HostVersion="5.1.0" TXT_Support="false"><PCRInfo Error="Host does not support TPM."></PCRInfo></Host_Attestation_Report>
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
        }
        catch(VMwareConnectionException e) {
            throw new IOException(String.format("Cannot get attestation report from host '%s': %s", hostname, e.toString()), e);
        }
    }
    
    @Override
    public TpmQuote getTpmQuote(Aik aik, Nonce nonce, Set<PcrIndex> pcr) {
        throw new UnsupportedOperationException("Vmware does not provide TPM Quotes"); // XXX TODO throw exception or return null?
    }

    @Override
    public List<Pcr> getPcrValues() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getModuleManifest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Pcr> getPcrHistory(PcrIndex number) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * XXX TODO  auditing of the host report should happen in attestation service.
     * you can obtain the original "raw" report by calling getVendorHostReport()
     * Here is the code that was used for audit logging in VCenterHost (previous draft)
     * that belongs in attestation service:
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("ATTESTATION");
        auditLog.setEntityId(hostId);
        auditLog.
        auditLog.setEntityType("ATTESTATION_REPORT");
        auditLog.setData(getReportInXML(report));
        new AuditLogger().addLog(auditLog);
     * 
     * Here is the exception that we were getting when the addLog() call executed:
     * 
[#|2012-11-20T09:56:10.575-0800|WARNING|glassfish3.1.1|javax.enterprise.system.container.ejb.com.sun.ejb.containers|_ThreadID=33;_ThreadName=Thread-2;|A system exception occurred during an invocation on EJB SAML method public java.lang.String com.intel.mountwilson.as.rest.SAML.getHostAssertions(java.lang.String)
javax.ejb.EJBException
...
Caused by: com.intel.mountwilson.as.common.ASException: com.intel.mtwilson.audit.helper.AuditHandlerException: java.lang.ClassCastException: com.sun.enterprise.naming.impl.SerialContext cannot be cast to com.intel.mtwilson.audit.api.AuditWorker
        at com.intel.mountwilson.util.vmware.VCenterHost.process(VCenterHost.java:121)
        at com.intel.mountwilson.util.vmware.VCenterHost.<init>(VCenterHost.java:54)
        at com.intel.mountwilson.manifest.strategy.VMWareManifestStrategy$1.<init>(VMWareManifestStrategy.java:86)
        at com.intel.mountwilson.manifest.strategy.VMWareManifestStrategy.getQuoteInformationForHost(VMWareManifestStrategy.java:86)
        at com.intel.mountwilson.manifest.strategy.VMWareManifestStrategy.getManifest(VMWareManifestStrategy.java:48)
        at com.intel.mountwilson.as.business.trust.HostTrustBO.getTrustStatus(HostTrustBO.java:92)
        at com.intel.mountwilson.as.business.trust.HostTrustBO.getHostWithTrust(HostTrustBO.java:131)
        at com.intel.mountwilson.as.business.trust.HostTrustBO.getTrustWithSaml(HostTrustBO.java:437)
        at com.intel.mountwilson.as.rest.SAML.getHostAssertions(SAML.java:63)
...
Caused by: com.intel.mtwilson.audit.helper.AuditHandlerException: java.lang.ClassCastException: com.sun.enterprise.naming.impl.SerialContext cannot be cast to com.intel.mtwilson.audit.api.AuditWorker
        at com.intel.mtwilson.audit.api.AuditLogger.addLog(AuditLogger.java:54)
        at com.intel.mountwilson.util.vmware.VCenterHost.auditAttestionReport(VCenterHost.java:165)
        at com.intel.mountwilson.util.vmware.VCenterHost.process(VCenterHost.java:104)
        ... 78 more
Caused by: java.lang.ClassCastException: com.sun.enterprise.naming.impl.SerialContext cannot be cast to com.intel.mtwilson.audit.api.AuditWorker
        at com.intel.mtwilson.audit.api.AuditLogger.getAuditWorker(AuditLogger.java:91)
        at com.intel.mtwilson.audit.api.AuditLogger.addLog(AuditLogger.java:51)
        ... 80 more
        * 
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws JAXBException 
     */
    private void getAllPcrAndModuleInformationFromHost() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, JAXBException {
    }

    
    private <T> String toXml(Class<T> clazz, T object) throws JAXBException {
        JAXBElement<T> xmlReport = new JAXBElement<T>(new QName("urn:vim25"),clazz,object);
        StringWriter sw = new StringWriter();
        JAXBContext jc = JAXBContext.newInstance("com.vmware.vim25");
        
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");        
        marshaller.marshal(xmlReport, sw);
        
        //log.info(sw.toString());
        
        return sw.toString();
        
    }

    @Override
    public HashMap<String, ? extends IManifest> getManifest(VCenterHost postProcessing) {
        try {
            if( isTpmAvailable() ) {
				if (vCenterVersion.contains("5.1")) {
					HostTpmAttestationReport report = vmware.getAttestationReport(hostMOR);
//                                        if(hostId != null)
//                                            auditAttestionReport(hostId,report); // XXX TODO  auditing api should not be logging FROM HERE, it should be logging from attestation service, which also knows the database record ID of the host;   we will just add a vmware-specific method to get the original report in xml and maybe there can be something in the HostAgent interface to accomodate this.
                                        vendorHostReport = toXml(HostTpmAttestationReport.class, report);
					log.info("Retreived HostTpmAttestationReport.");
					manifestMap = postProcessing.processReport(esxVersion,report);
				}else{
					
					HostRuntimeInfo runtimeInfo = (HostRuntimeInfo) vmware.getMORProperty(hostMOR, "runtime");
                                        vendorHostReport = toXml(HostRuntimeInfo.class, runtimeInfo);
					// Now process the digest information
					List<HostTpmDigestInfo> htdis = runtimeInfo
							.getTpmPcrValues();
					log.info("Retreived HostTpmDigestInfo.");
					manifestMap =  postProcessing.processDigest(esxVersion,htdis);
				}
            }        
        }
        catch(Exception e) {
            log.error("error during getManifest: "+e.toString(), e);
        }
        return manifestMap;
    }

    /**
     * Content of this method is same as getHostDetails() in VMwareHelper in Management Service and
     * VMwareClient in trust utils library.
     * @return 
     */
    @Override
    public TxtHostRecord getHostDetails() throws IOException {
        try {
            TxtHostRecord host = new TxtHostRecord();
            host.HostName = vmware.getMORProperty(hostMOR, "name").toString();
            // hostObj.Description = serviceContent.getAbout().getVersion();
            host.VMM_OSName = vmware.getMORProperty(hostMOR, "config.product.name").toString();
            host.VMM_OSVersion = vmware.getMORProperty(hostMOR, "config.product.version").toString();
            host.VMM_Version = vmware.getMORProperty(hostMOR, "config.product.build").toString();
            host.BIOS_Oem = vmware.getMORProperty(hostMOR, "hardware.systemInfo.vendor").toString();
            host.BIOS_Version = vmware.getMORProperty(hostMOR, "hardware.biosInfo.biosVersion").toString();
            return host;
        } catch (InvalidPropertyFaultMsg ex) {
            log.error("VCenter host does not support host details property: {}", ex.getLocalizedMessage());
            throw new IOException(ex);
        } catch (RuntimeFaultFaultMsg ex) {
            log.error("Runtime fault while fetching host details: {}", ex.getLocalizedMessage());
            throw new IOException(ex);
        }
        
    }
    

    public String getHostAttestationReport(String pcrList) throws IOException {
        try {
            return vmware.getHostAttestationReport(hostMOR, hostname, pcrList);        
        }
        catch(VMwareConnectionException e) {
            throw new IOException(String.format("Cannot get attestation report from host '%s': %s", hostname, e.toString()), e);
        }
    }
}
