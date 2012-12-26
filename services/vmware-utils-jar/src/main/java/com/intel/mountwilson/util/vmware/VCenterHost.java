package com.intel.mountwilson.util.vmware;

import java.util.HashMap;
import java.util.List;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.manifest.data.IManifest;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.audit.api.AuditLogger;
import com.intel.mtwilson.audit.data.AuditLog;
import com.intel.mtwilson.audit.helper.AuditHandlerException;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.TLSPolicy;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostTpmAttestationReport;
import com.vmware.vim25.HostTpmDigestInfo;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XXX needs to be refactored with vmware client. right now the usage is 
 * backwards, with the vcenterhost class managing a connection. the vcenterhost
 * class should be RETURNED BY the vcenterclient class in order to save the
 * caller the effort of keeping track of vmware's "managed object reference" for
 * every object for every call.
 *
 * TODO:  needs to be refactored... instead of containing the pool (or extending vmwareclient as it did before)
 * this class should be returned BY the vmware client when a user of the client requests host information; and
 * this class should provide convenient access to the host information and hide all of the managed object reference
 * 
 * XXX The user of the vmware client can then choose to use a pool or not use a pool.  current state of the code is
 * a temporary fix because there's not enough time for a refactor but we need to get the pool in here to improve 
 * performance.
 */
public abstract class VCenterHost  {
        private Logger log = LoggerFactory.getLogger(getClass());

	String hostName = "";
	String connectionString = "";
        Integer hostId;
        byte[] sslCertificate = null;
        String sslPolicy = null;
        
	HashMap<String, ? extends IManifest> manifestMap = null;
        protected static VMwareConnectionPool pool = new VMwareConnectionPool(); 
        VMwareClient vmware = null;
        ManagedObjectReference hostObj = null;

	public VCenterHost(TblHosts host) {
		this.hostName = host.getName();
		this.connectionString = host.getAddOnConnectionInfo();
                this.hostId = host.getId();
                this.sslCertificate = host.getSSLCertificate();
                this.sslPolicy = host.getSSLPolicy();
                
		process();
                
	}

        public VCenterHost(String hostName,String connection) {
		this.hostName = hostName;
		this.connectionString = connection;
                this.hostId = null;
                this.sslCertificate = null;
                this.sslPolicy = null;
                        
		process();
                
	}

        boolean isTpmSupported() {
            try {
                Object isTpmSupported = vmware.getMORProperty(hostObj,
                                "capability.tpmSupported");
                return (Boolean)isTpmSupported;
            } catch (InvalidPropertyFaultMsg ex) {
                log.error("VCenter host does not support 'capability.tpmSupported' property: {}", ex.getLocalizedMessage());
                return false;
            } catch (RuntimeFaultFaultMsg ex) {
                log.error("Runtime fault while fetching 'capability.tpmSupported' property: {}", ex.getLocalizedMessage());
                return false;
            }
        }
        
	private void process() {
                String vCenterVersion = null;
                // try twice, in case we got a stale connection the first time
                try {
                    /**
                     * Bug #497
                     * TODO: the following block of SSL policy should be moved
                     * somewhere else when we refactor the different agents
                     * to have a uniform interface. It is placed here for now
                     * because adding some level of security to the vcenter
                     * connection is urgent.
                     * Also we will need a way to access the mt wilson configuration - 
                     * is SSL turned off? some customers choose to turn it off.
                     */
                    if( sslPolicy == null ) {
                        // backwards compatibility: none of the hosts in the database have an ssl policy. 
                        // ****the installer should set a default policy of "SAVE_FIRST"  on existing hosts *** 
                        // even though that one is
                        // insecure... but at least allows business to happen and someone can verify the
                        // certificates later if they choose to do that and make it secure.
                    }
                    else {
                        if( sslPolicy.equals(TLSPolicy.TRUST_FIRST_CERTIFICATE.toString()) ) {
                            // ok to save the host's ssl certificate and make it available to attestation service later via getSslCertificate()
                        }
                        else if( sslPolicy.equals(TLSPolicy.TRUST_KNOWN_CERTIFICATE.toString()) ) {
                            // must have an sslCertificate provided ... 
                        }
                        else if( sslPolicy.equals(TLSPolicy.TRUST_CA_VERIFY_HOSTNAME.toString()) ) {
                            // must have trusted root ca's provided ...  *** TODO *** interface for this is missing.  it's best if we can just
                            // be given a policy object with those things already set .... no way to look it up from here !!!
                        }
                    }
                        
                    log.debug("Connecting to vcenter");
                     vmware = pool.getClientForConnection(connectionString);
//                	connect(connectionString);
                    vCenterVersion = vmware.getVCenterVersion(); //serviceContent.getAbout().getVersion();
                    log.info("VCenter version is " + vCenterVersion);
                    hostObj = vmware.getManagedObjectReference(hostName);
                }
                catch(Exception e) {
                        try {
                              log.debug("Re-connecting to vcenter");
                            vmware = pool.createClientForConnection(connectionString);
                            vCenterVersion = vmware.getVCenterVersion(); //serviceContent.getAbout().getVersion();
                            log.info("VCenter version is " + vCenterVersion);
                            hostObj = vmware.getManagedObjectReference(hostName);
                        } catch (Exception ex) {
                            throw new ASException(ex);
                        }
                }
                    
		try {

			if (!isTpmSupported()) {
				log.info("Host does not support TPM");
				throw new ASException(ErrorCode.AS_VMW_TPM_NOT_SUPPORTED,
						 hostName);
			} else {
				log.info( "Host supports TPM");
                                String esxVersion = vmware.getMORProperty(hostObj, "config.product.version").toString();
				
				if (vCenterVersion.contains("5.1")) {
					HostTpmAttestationReport report = vmware.getAttestationReport(hostObj);
                                        if(hostId != null)
                                            auditAttestionReport(hostId,report);
					log.info("Retreived HostTpmAttestationReport.");
					manifestMap = processReport(esxVersion,report);
				}else{
					
					HostRuntimeInfo runtimeInfo = (HostRuntimeInfo) vmware.getMORProperty(hostObj, "runtime");
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
			//disconnect(); // not necessary because pool manages connections now
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

        
        /**
         * We should not be audit-logging from utility libraries! At least not in a way
         * that is so dependent on a database etc... just use SLF4J and system admin can
         * direct logs from this class to a special file if needed. 
         * 
         * XXX TODO  audit logging currently turned off because we were getting this exception:
         * 
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
         * 
         */
    private void auditAttestionReport(Integer hostId, HostTpmAttestationReport report) throws JAXBException, AuditHandlerException {
        
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("ATTESTATION");
        auditLog.setEntityId(hostId);
        auditLog.setEntityType("ATTESTATION_REPORT");
        auditLog.setData(getReportInXML(report));
        
        //new AuditLogger().addLog(auditLog);
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
