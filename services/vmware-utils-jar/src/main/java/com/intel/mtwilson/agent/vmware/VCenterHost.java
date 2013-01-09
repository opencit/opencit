package com.intel.mtwilson.agent.vmware;

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
import com.intel.mtwilson.tls.TlsPolicy;
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
 * BUG #497 moved all the non-abstract functionality of this class to HostAgentFactory
 * and HostAgent, pending complete rewrite of the interfaces IManifest, IManifestStrategy, IManifestStrategyFactory 
 */
public abstract class VCenterHost  {
//        private Logger log = LoggerFactory.getLogger(getClass());

        

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
         * BUG #497 temporarily moved to HostAgent/HostAgentFactory 
	 * Returns the manifest map built by the implementing class
	 * @return
	 */  /*
	public HashMap<String, ? extends IManifest> getManifestMap() {
		return manifestMap;
	}*/

	/**
	 * This method is called for ESX5.1 host and vcenter version is 5.1.X
	 * Implementing class has to process the digest info and build PCR Manifest map and 
	 * return the map. Key will be the pcr number
	 * @param report
	 * @return
	 */
	public abstract HashMap<String, ? extends IManifest> processReport(String esxVersion,
			HostTpmAttestationReport report);

        

}
