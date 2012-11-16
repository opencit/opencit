package com.intel.mountwilson.util.vmware;

import java.util.HashMap;
import java.util.List;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mountwilson.manifest.data.IManifest;
import com.intel.mtwilson.audit.api.AuditLogger;
import com.intel.mtwilson.audit.data.AuditLog;
import com.intel.mtwilson.audit.helper.AuditHandlerException;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostTpmAttestationReport;
import com.vmware.vim25.HostTpmDigestInfo;
import com.vmware.vim25.ManagedObjectReference;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VCenterHost extends VMwareClient {
        private Logger log = LoggerFactory.getLogger(getClass());

	String hostName = "";
	String connectionString = "";
        Integer hostId;
	HashMap<String, ? extends IManifest> manifestMap = null;

	public VCenterHost(TblHosts host) {
		this.hostName = host.getName();
		this.connectionString = host.getAddOnConnectionInfo();
                this.hostId = host.getId();
                        
		process();
                
	}

        public VCenterHost(String hostName,String connection) {
		this.hostName = hostName;
		this.connectionString = connection;
                this.hostId = null;
                        
		process();
                
	}

        
	private void process() {
		try {
                        
                	connect(connectionString);

			String vCenterVersion = serviceContent.getAbout().getVersion();

			log.info("VCenter version is " + vCenterVersion);
			
			ManagedObjectReference hostObj = getManagedObjectReference(hostName);

			Object isTpmSupported = getMORProperty(hostObj,
					"capability.tpmSupported");

			if ((Boolean) isTpmSupported == false) {
				log.info("Host does not support TPM");
				throw new ASException(ErrorCode.AS_VMW_TPM_NOT_SUPPORTED,
						 hostName);
			} else {
				log.info( "Host supports TPM");
                                String esxVersion = getMORProperty(hostObj, "config.product.version").toString();
				
				if (vCenterVersion.contains("5.1")) {
					HostTpmAttestationReport report = getAttestationReport(hostObj);
                                        if(hostId != null)
                                            auditAttestionReport(hostId,report);
					log.info("Retreived HostTpmAttestationReport.");
					manifestMap = processReport(esxVersion,report);
				}else{
					
					HostRuntimeInfo runtimeInfo = (HostRuntimeInfo) getMORProperty(hostObj, "runtime");
					// Now process the digest information
					List<HostTpmDigestInfo> htdis = runtimeInfo
							.getTpmPcrValues();
					log.info("Retreived HostTpmDigestInfo.");
					manifestMap =  processDigest(esxVersion,htdis);
				}
			}

		} catch (ASException e) {
			throw e;
		} catch (Exception e) {
			throw new ASException(e);
		} finally {
			disconnect();
		}

	}


	/**
	 * This method is called for ESX 5.0 host and VCenter 5.0. Implementing class has to 
	 * process the digest info and build PCR Manifest map and return the map. Key will 
	 * be the pcr number
	 * @param htdis
	 * @return
	 */
	public abstract HashMap<String, ? extends IManifest> processDigest(String esxVersion,
			List<HostTpmDigestInfo> htdis) ;

	/**
	 * Returns the manifest map built by the implementing class
	 * @return
	 */
	public HashMap<String, ? extends IManifest> getManifestMap() {
		return manifestMap;
	}

	/**
	 * This method is called for ESX5.1 host and vcenter version is 5.1.X
	 * Implementing class has to process the digest info and build PCR Manifest map and 
	 * return the map. Key will be the pcr number
	 * @param report
	 * @return
	 */
	public abstract HashMap<String, ? extends IManifest> processReport(String esxVersion,
			HostTpmAttestationReport report);

    private void auditAttestionReport(Integer hostId, HostTpmAttestationReport report) throws JAXBException, AuditHandlerException {
        
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("ATTESTATION");
        auditLog.setEntityId(hostId);
        auditLog.setEntityType("ATTESTATION_REPORT");
        auditLog.setData(getReportInXML(report));
        
        new AuditLogger().addLog(auditLog);
    }

    private String getReportInXML(HostTpmAttestationReport report) throws JAXBException {
        
   
        JAXBElement<HostTpmAttestationReport> attestationReport = 
                new JAXBElement<HostTpmAttestationReport>(new QName("urn:vim25"),HostTpmAttestationReport.class,report);
        StringWriter sw = new StringWriter();

        JAXBContext jc = JAXBContext.newInstance("com.vmware.vim25");
        
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        
        marshaller.marshal(attestationReport, sw);
        
        //log.info(sw.toString());
        
        log.info("Returning Attestation Report for auditing.");
        
        return sw.toString();

    }

}
