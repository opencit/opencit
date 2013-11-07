/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.Service;

/**
 *
 * @author ssbangal
 */

// import com.intel.mountwilson.as.common.ASException;
// import com.intel.mountwilson.as.common.ErrorCode;
import com.intel.mtwilson.ms.common.MSException;
import com.vmware.vim25.*;
import java.io.FileWriter;
import java.io.StringWriter;

//import com.intel.mtwilson.datatypes.*;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import org.apache.commons.codec.binary.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author dsmagadX
 */
public class VMwareClientSDK {
//            static Logger log = LoggerFactory.getLogger(VMwareClientSDK.class.getName());
//
//            private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
//            private static final String SVC_INST_NAME = "ServiceInstance";
//            protected static ServiceContent serviceContent;
//            private static ManagedObjectReference propCollectorRef;
//            private static ManagedObjectReference rootRef;
//            private static VimService vimService;
//            private static VimPortType vimPort;
//            static UserSession session = null;
//
//            private static boolean isConnected = false;
//
//            private static String[] meTree = {
//            "ManagedEntity",
//            "ComputeResource",
//            "ClusterComputeResource",
//            "Datacenter",
//            "Folder",
//            "HostSystem",
//            "ResourcePool",
//            "VirtualMachine"
//        };
//        private static String[] crTree = {
//            "ComputeResource",
//            "ClusterComputeResource"
//        };
//        private static String[] hcTree = {
//            "HistoryCollector",
//            "EventHistoryCollector",
//            "TaskHistoryCollector"
//        };
//
//	private static class TrustAllTrustManager implements javax.net.ssl.TrustManager,
//			javax.net.ssl.X509TrustManager {
//
//		@Override
//		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//			return null;
//		}
//
//		@Override
//		public void checkServerTrusted(
//				java.security.cert.X509Certificate[] certs, String authType)
//				throws java.security.cert.CertificateException {
//			for (java.security.cert.X509Certificate cert : certs) {
//				cert.checkValidity();
//			}
//
//			return;
//		}
//
//		@Override
//		public void checkClientTrusted(
//				java.security.cert.X509Certificate[] certs, String authType)
//				throws java.security.cert.CertificateException {
//			for (java.security.cert.X509Certificate cert : certs) {
//				cert.checkValidity();
//			}
//			return;
//		}
//	}
//
//	private static void trustAllHttpsCertificates() throws NoSuchAlgorithmException, KeyManagementException {
//		// Create a trust manager that does not validate certificate chains:
//		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
//		javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
//		trustAllCerts[0] = tm;
//		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
//				.getInstance("SSL");
//		javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
//		sslsc.setSessionTimeout(0);
//		sc.init(null, trustAllCerts, null);
//		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
//				.getSocketFactory());
//	}
//   
//	/**
//	 * Disconnects the user session.
//	 * 
//	 * @throws Exception
//	 */
//	protected static void disconnect() {
//		try {
//			if (isConnected) {
//				vimPort.logout(serviceContent.getSessionManager());
//			}
//			isConnected = false;
//		} catch (Exception e) {
//			log.error("Error while logging out from VCenter Api." + e.getMessage());
//		}
//	}
//
//	/**
//	 * Establishes session with the virtual center server.
//	 * @throws RuntimeFaultFaultMsg 
//	 * @throws InvalidLoginFaultMsg 
//	 * @throws InvalidLocaleFaultMsg 
//	 * @throws NoSuchAlgorithmException 
//	 * @throws KeyManagementException 
//	 * 
//	 * @throws Exception
//	 *             the exception
//	 */
//	protected static void connect(String url, String userName, String password) throws RuntimeFaultFaultMsg, InvalidLocaleFaultMsg, InvalidLoginFaultMsg, KeyManagementException, NoSuchAlgorithmException {
//
//		HostnameVerifier hostNameVerifier = new HostnameVerifier() {
//
//			@Override
//			public boolean verify(String urlHostName, SSLSession session) {
//				return true;
//			}
//		};
//
//		trustAllHttpsCertificates();
//
//		HttpsURLConnection.setDefaultHostnameVerifier(hostNameVerifier);
//
//		SVC_INST_REF.setType(SVC_INST_NAME);
//		SVC_INST_REF.setValue(SVC_INST_NAME);
//
//		vimService = new VimService();
//		vimPort = vimService.getVimPort();
//		Map<String, Object> ctxt = ((BindingProvider) vimPort)
//				.getRequestContext();
//
//		ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
//		ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
//
//		serviceContent = vimPort.retrieveServiceContent(SVC_INST_REF);
//		
//		session = vimPort.login(serviceContent.getSessionManager(), userName, password,
//				null);
//		
//		printSessionDetails();
//		
//		isConnected = true;
//
//		propCollectorRef = serviceContent.getPropertyCollector();
//		rootRef = serviceContent.getRootFolder();
//	}
//
//	private static void printSessionDetails() {
//		if(session != null){
//			log.debug("Logged in Session key " + session.getKey());
//		}else{
//			log.info("session is null");
//		}
//		
//	}
//
//	protected static void connect(String vCenterConnectionString) throws RuntimeFaultFaultMsg, InvalidLocaleFaultMsg, InvalidLoginFaultMsg, KeyManagementException, NoSuchAlgorithmException, MSException  {
//		String[] vcenterConn = vCenterConnectionString.split(";");
//		if (vcenterConn.length != 3) {
//			throw new MSException(new Exception("The vCenter connection information is not valid."));
//		}
//		// Connect to the vCenter server with the passed in parameters
//		connect(vcenterConn[0], vcenterConn[1], vcenterConn[2]);
//	}
//
//	protected static byte[] toByteArray(List<Byte> list) {
//		byte[] ret = new byte[list.size()];
//		for (int i = 0; i < ret.length; i++) {
//			ret[i] = list.get(i);
//		}
//		return ret;
//	}
//
//	// / <summary>
//	// / Based on the Entity type this function searches through all the objects
//	// and returns
//	// / the matching objects.
//	// / For getting just clusters call it with "ClusterComputeResource"
//	// / For getting just Datacenters call it with "Datacenter"
//	// / For getting just hosts call it with "ComputeResource"
//	// / </summary>
//	// / <param name="entityType">Type of the Entity that should be returned
//	// back</param>
//	// / <returns>Array of Management Oject References to all the objects of the
//	// type specified.</returns>
//	protected static ManagedObjectReference[] getEntitiesByType(String entityType) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
//		List<ManagedObjectReference> resultEntities = null;
//		ManagedObjectReference[] finalEntityList = null;
////		int entityIndex = 0;
//
//		// Since each TraversalSpec does one level of traversal, we need to
//		// define
//		// multiple ones if we need to traverse recursively.
//
//		// Traversal through Cluster branch
//		// Traversal through Cluster branch
//		TraversalSpec crToH = new TraversalSpec();
//		crToH.setName("crToH");
//		crToH.setType("ComputeResource");
//		crToH.setPath("host");
//		crToH.setSkip(false);
//
//		// Traversal through the DataCenter branch
//		SelectionSpec sspecvfolders = new SelectionSpec();
//		sspecvfolders.setName("visitFolders");
//
//		TraversalSpec dcToHf = new TraversalSpec();
//		dcToHf.setSkip(false);
//		dcToHf.setType("Datacenter");
//		dcToHf.setPath("hostFolder");
//		dcToHf.setName("dcToHf");
//		dcToHf.getSelectSet().add(sspecvfolders);
//
//		// Recurse through the folders
//		TraversalSpec tSpec = new TraversalSpec();
//		tSpec.setName("visitFolders");
//		tSpec.setType("Folder");
//		tSpec.setPath("childEntity");
//		tSpec.setSkip(false);
//		SelectionSpec visitFoldersSS = new SelectionSpec();
//		visitFoldersSS.setName("visitFolders");
//
//		tSpec.getSelectSet().add(visitFoldersSS);
//
//		SelectionSpec visitFoldersdcToHf = new SelectionSpec();
//		visitFoldersdcToHf.setName("dcToHf");
//
//		tSpec.getSelectSet().add(visitFoldersdcToHf);
//
//		SelectionSpec visitFolderscrToH = new SelectionSpec();
//		visitFolderscrToH.setName("crToH");
//
//		tSpec.getSelectSet().add(visitFolderscrToH);
//
//		// Create Property Spec
//		PropertySpec propertySpec = new PropertySpec();
//		propertySpec.setAll(false);
//		propertySpec.getPathSet().add("name");// add all the properties that
//												// needs to be retrieved
//
//		propertySpec.setType("ManagedEntity"); // Having ManagedEntity
//												// refers to all managed
//												// objects
//		PropertySpec[] propertySpecs = new PropertySpec[] { propertySpec };
//
//		// Now create Object Spec
//		ObjectSpec objectSpec = new ObjectSpec();
//		objectSpec.setObj(rootRef);
//		objectSpec.setSkip(true);
//
//		objectSpec.getSelectSet().add(tSpec);
//		objectSpec.getSelectSet().add(dcToHf);
//
//		objectSpec.getSelectSet().add(crToH);
//
//		ObjectSpec[] objectSpecs = new ObjectSpec[] { objectSpec };
//
//		// Create PropertyFilterSpec using the PropertySpec and ObjectSpec
//		// created above.
//		PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
//		propertyFilterSpec.getPropSet().addAll(Arrays.asList(propertySpecs));
//		propertyFilterSpec.getObjectSet().addAll(Arrays.asList(objectSpecs));
//
//		List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
//		propertyFilterSpecs.add(propertyFilterSpec);
//
//		List<ObjectContent> oContent = vimPort.retrieveProperties(
//				propCollectorRef, propertyFilterSpecs);
//
//		if (oContent != null) {
//			// allocate memory for all the MOR references found.
//			resultEntities = new ArrayList<ManagedObjectReference>();
//			for (int i = 0; i < oContent.size(); i++) {
//				ManagedObjectReference mor = oContent.get(i).getObj();
//				// DynamicProperty[] dProps = oContent[i].propSet;
//
//				if (mor.getType().equals(entityType)) {
//					resultEntities.add(mor);
//				}
//			}
//		}
//		
//		
//		if(resultEntities != null && resultEntities.size() != 0){
//			finalEntityList = new ManagedObjectReference[resultEntities.size()];
//			finalEntityList = resultEntities.toArray(finalEntityList);
//		}
//		// return back the final entity array
//		return finalEntityList;
//
//	} // end of GetEntitiesByType()
//
//	protected static String getHostInfo(ManagedObjectReference hostObj) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
//		String hostName = "";
//
//		// Create Property Spec
//		PropertySpec propertySpec = new PropertySpec();
//		propertySpec.setAll(false);
//		// We need to retrieve both the name of the host and as well as all
//		// the
//		// VMs associated with the host.
//		propertySpec.getPathSet().add("name");
//		// Specify the entity that will have both of the above parameters.
//		propertySpec.setType("HostSystem");
//		PropertySpec[] propertySpecs = new PropertySpec[] { propertySpec };
//
//		// Now create Object Spec
//		ObjectSpec objectSpec = new ObjectSpec();
//		objectSpec.setObj(hostObj);
//		ObjectSpec[] objectSpecs = new ObjectSpec[] { objectSpec };
//
//		// Create PropertyFilterSpec using the PropertySpec and ObjectPec
//		// created above.
//		PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
//		propertyFilterSpec.getPropSet().addAll(Arrays.asList(propertySpecs));
//		propertyFilterSpec.getObjectSet().addAll(Arrays.asList(objectSpecs));
//
//		List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
//		propertyFilterSpecs.add(propertyFilterSpec);
//
//		List<ObjectContent> contents = vimPort.retrieveProperties(
//				propCollectorRef, propertyFilterSpecs);
//
//		if (contents != null) {
//			for (ObjectContent content : contents) {
//				for (DynamicProperty dps : content.getPropSet()) {
//					if (dps.getName().equals("name")) {
//						hostName = dps.getVal().toString();
//					}
//				}
//
//			}
//		}
//
//		return hostName;
//	}// GetHostInfo
//		
//        // / <summary>
//        // / Retrieves the specified property for the specified entity.
//        // / </summary>
//        // / <param name="moRef">Entity for which the property should be
//        // retrieved</param>
//        // / <param name="propertyName">Name of the property that should be
//        // retrieved</param>
//        // / <returns>Value of the property</returns>
//        protected static Object getMORProperty(ManagedObjectReference moRef, String propertyName) 
//                throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg
//        {
//		return getMORProperties(moRef, new String[] { propertyName })[0];
//	}
//
//	// / <summary>
//	// / Retrieves the list of properties for the speicified entity.
//	// / </summary>
//	// / <param name="moRef">Entity for which the properties should be
//	// retrieved</param>
//	// / <param name="properties">Array of properties that should be
//	// retrieved</param>
//	// / <returns>Array of values corresponding to the properties.</returns>
//	protected static Object[] getMORProperties(ManagedObjectReference moRef,
//			String[] properties) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg  {
//		// Return object array
//		Object[] ret;
//		// PropertySpec specifies what properties to
//		// retrieve and from type of Managed Object
//		PropertySpec pSpec = new PropertySpec();
//		pSpec.setType(moRef.getType());
//		pSpec.getPathSet().addAll(Arrays.asList(properties));
//
//		// ObjectSpec specifies the starting object and
//		// any TraversalSpecs used to specify other objects
//		// for consideration
//		ObjectSpec oSpec = new ObjectSpec();
//		oSpec.setObj(moRef);
//
//		// PropertyFilterSpec is used to hold the ObjectSpec and
//		// PropertySpec for the call
//		PropertyFilterSpec pfSpec = new PropertyFilterSpec();
//		pfSpec.getPropSet().add(pSpec);
//		pfSpec.getObjectSet().add(oSpec);
//
//		// retrieveProperties() returns the properties
//		// selected from the PropertyFilterSpec
//
//		List<PropertyFilterSpec> pfSpecs = new ArrayList<PropertyFilterSpec>();
//		pfSpecs.add(pfSpec);
//
//		List<ObjectContent> ocs = vimPort.retrieveProperties(propCollectorRef,
//				pfSpecs);
//
//		// Return value, one object for each property specified
//		ret = new Object[properties.length];
//
//		for (ObjectContent oc : ocs) {
//			List<DynamicProperty> dps = oc.getPropSet();
//			for (DynamicProperty dp : dps) {
//				for (int p = 0; p < ret.length; ++p) {
//					if (properties[p].equals(dp.getName())) {
//						ret[p] = dp.getVal();
//					}
//				}
//			}
//		}
//		return ret;
//	}
//
//	// / <summary>
//	// / Retrieves the vCenter version information.
//	// / </summary>
//	// / <returns>Version details.</returns>
//	protected static String getVCenterVersion(String vcenterString) throws MSException {
//		try {
//			connect(vcenterString);
//
//			String vCenterVersion = serviceContent.getAbout().getVersion();
//			return vCenterVersion;
//		} catch (Exception e) {
//			throw new MSException(new  Exception("Error while getting VCenter Version.", e));
//		} finally {
//			disconnect();
//		}
//	}
//
//	protected static HostTpmAttestationReport getAttestationReport(
//			ManagedObjectReference hostObj) throws RuntimeFaultFaultMsg  {
//		return vimPort.queryTpmAttestationReport(hostObj);
//
//	}
//
//	protected static ManagedObjectReference getManagedObjectReference(String hostName) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, MSException {
//		// Get the host objects in the vcenter
//		ManagedObjectReference[] hostObjects = getEntitiesByType("HostSystem");
//		if (hostObjects != null && hostObjects.length != 0) {
//			for (ManagedObjectReference hostObj : hostObjects) {
//				String hostNameFromVC = getHostInfo(hostObj);
//				log.debug("getHostObject - comparing hostNameFromVC {0} requested hostName {1}",
//						new Object[] { hostNameFromVC, hostName });
//				if (hostNameFromVC.equals(hostName)) {
//					log.debug(String.format(
//							"Found Managed Object Reference for host %s ",
//							hostName));
//					return hostObj;
//				}
//			}
//		}
//		// If the code reaches here that means that we did not find the host
//		throw new MSException( new Exception(String.format("Requested Host %s does not exist in vCenter.", hostName)));
//	}
//
//	protected static String byteArrayToBase64String(List<Byte> digestValue) {
//		String digest = Base64.encodeBase64String(toByteArray(digestValue));
//		return digest;
//	}
//
//	protected static String byteArrayToHexString(List<Byte> digestValue) {
//
//		return byteArrayToHexString(toByteArray(digestValue));
//	}
//
//	protected static String byteArrayToHexString(byte[] bytes) {
//		BigInteger bi = new BigInteger(1, bytes);
//	    return String.format("%0" + (bytes.length << 1) + "X", bi);
//	}
//
//	protected static byte[] hexStringToByteArray(String s) {
//	    int len = s.length();
//	    byte[] data = new byte[len / 2];
//	    for (int i = 0; i < len; i += 2) {
//	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//	                             + Character.digit(s.charAt(i+1), 16));
//	    }
//	    return data;
//	}
//	
//
//    /**
//     * Added By: Sudhir on June 15, 2012
//     * 
//     * Retrieves the list of Virtual machines for the specified host along with
//     * the power state of the VM.
//     * @param hostName : Name of the host for which the VM details need to be retrieved.
//     * @param vCenterConnectionString : Connection string of the vCenter on which the host
//     * is configured
//     * @return : ArrayList consisting of all the VM along with the power state information.
//     * VM Name::POWERED_ON
//     * @throws Exception 
//     */
//    protected static ArrayList getVMsForHost(String hostName, String vCenterConnectionString) throws MSException{
//        ArrayList vmList = new ArrayList();
//        ManagedObjectReference hostMOR = null;
//        try
//        {
//            connect(vCenterConnectionString);
//
//            if(hostName != null) 
//            {
//                hostMOR = getDecendentMoRef(hostMOR, "HostSystem", hostName);
//                if(hostMOR == null)
//                {
//                    throw new Exception("Host configuration not found in the vCenter database.");
//                } 
//            }
//            
//            vmList = getDecendentMoRefs(hostMOR, "VirtualMachine", null);
//            if(vmList.isEmpty())
//                return vmList;
//
//            for (int i=0; i< vmList.size(); i++)
//            {
//                String vmName = getMORProperty((ManagedObjectReference) vmList.get(i), "name").toString();
//                String vmPowerState = getMORProperty((ManagedObjectReference) vmList.get(i), "runtime.powerState").toString();
//                vmList.set(i, vmName + "::" + vmPowerState);
//            }
//        }
//        catch (Exception ex)
//        {
//            throw new MSException(ex);
//        }
//        finally
//        {
//            disconnect();
//        }
//        return vmList;
//    }
//
//    /**
//     * Added By: Sudhir on June 15, 2012
//     * 
//     * Retrieves the h/w & s/w details of the host including BIOS, OS information
//     * @param hostName : Name of the host for which the details need to be retrieved
//     * @param vCenterConnectionString : Connection string to the vCenter server where the
//     * host is configured.
//     * @return : Host object containing all the details.
//     * @throws Exception 
//     */
//   /* protected static TxtHostRecord getHostDetails(TxtHostRecord hostObj) throws Exception {
//        ManagedObjectReference hostMOR = null;
//        boolean doNotDisconnect = false;
//        
//        try
//        {
//            // If we have already established a connection, we use it. This will
//            // happen when this function will be called by the getHostDetailsForCluster
//            // function, which would have opened the connection to vCenter server.
//            if (!isConnected)
//                connect(hostObj.AddOn_Connection_String);
//            else
//                doNotDisconnect = true;
//
//            hostMOR = getDecendentMoRef(hostMOR, "HostSystem", hostObj.HostName);
//            if(hostMOR == null)
//            {
//                // return an empty object
//                return hostObj;
//            } 
//            
//            hostObj.HostName = getMORProperty(hostMOR, "name").toString();
//            hostObj.Description = serviceContent.getAbout().getVersion();
//            hostObj.VMM_OSName = getMORProperty(hostMOR, "config.product.name").toString();
//            hostObj.VMM_OSVersion = getMORProperty(hostMOR, "config.product.version").toString();
//            hostObj.VMM_Version = getMORProperty(hostMOR, "config.product.build").toString();
//            hostObj.BIOS_Oem = getMORProperty(hostMOR, "hardware.systemInfo.vendor").toString();
//            hostObj.BIOS_Version = getMORProperty(hostMOR, "hardware.biosInfo.biosVersion").toString();
//            
//        }
//        catch (Exception ex)
//        {
//            throw ex;
//        }
//        finally
//        {
//            if (!doNotDisconnect)
//                disconnect();
//        }
//        return hostObj;
//    }
//    */
//    /**
//     * Added By: Sudhir on June 15, 2012
//     * 
//     * Retrieves the list of hosts along with the s/w and h/w configuration details within the VMware Cluster. 
//     * @param clusterName : Name of the cluster from which we need to retrieve the host details
//     * @param connectionString : Connection string to the vCenter server
//     * @return : Array list of all the host names
//     * @throws Exception 
//     */
//    /*protected static ArrayList getHostDetailsForCluster(String clusterName, String connectionString) throws Exception {
//        ArrayList hostList = new ArrayList();
//        ArrayList hostDetailList = new ArrayList <TxtHostRecord> ();
//        ManagedObjectReference clusterMOR = null;
//        try
//        {
//            connect(connectionString);
//
//            if(clusterName != null) 
//            {
//                clusterMOR = getDecendentMoRef(clusterMOR, "ClusterComputeResource", clusterName);
//                if(clusterMOR == null)
//                {
//                    throw new Exception("Cluster configuration not found in the vCenter database.");
//                } 
//            }
//            
//            hostList = getDecendentMoRefs(clusterMOR, "HostSystem", null);
//            if(hostList.isEmpty())
//                return hostList;
//
//            for (int i=0; i< hostList.size(); i++)
//            {
//                String hostName = getMORProperty((ManagedObjectReference) hostList.get(i), "name").toString();
//                TxtHostRecord hostObj = new TxtHostRecord();
//                hostObj.HostName = hostName;
//                hostObj.AddOn_Connection_String = connectionString;
//                hostObj = getHostDetails(hostObj);
//                hostDetailList.add(hostObj);
//            }
//        }
//        catch (Exception ex)
//        {
//            throw ex;
//        }
//        finally
//        {
//            disconnect();
//        }
//        return hostDetailList;
//    }
//    */
//    /**
//     * Added By: Sudhir on June 14, 2012
//     * 
//     * This function provides the power on and power off functionality 
//     * virtual machines
//     * @param vmName : Name of the VM
//     * @param hostName: Name of the host on which VM should be powered on.
//     * For Power Off, this parameter is not needed.
//     * @param powerOn: Flag that indicates whether to power on or off the VM
//     * @param vCenterConnectionString : Connection string to the vCenter server.
//     * @throws Exception 
//     */
//    protected static void powerOnOffVM(String vmName, String hostName, Boolean powerOn, String vCenterConnectionString) throws MSException {
//        ManagedObjectReference hostMOR = null;
//        ManagedObjectReference vmMOR = null;
//        ManagedObjectReference powerTaskMOR = null;
//        try
//        {
//            connect(vCenterConnectionString);
//
//            vmMOR = getDecendentMoRef(vmMOR, "VirtualMachine", vmName);
//            
//            if (vmMOR == null)
//                throw new MSException(new Exception ("Invalid virtual machine specified for the power operation."));
//            
//            if (powerOn)
//            {
//                hostMOR = getDecendentMoRef(hostMOR, "HostSystem", hostName);
//                if (vmMOR == null)
//                    throw new MSException(new Exception ("Invalid host specified for the virtual machine power on operation."));
//                powerTaskMOR = vimPort.powerOnVMTask(vmMOR, hostMOR);
//            }
//            else
//                powerTaskMOR = vimPort.powerOffVMTask(vmMOR);
//            
//            // Wait for the power operation to complete and return back
//            String result = waitForTask(powerTaskMOR);
//            if (!result.toLowerCase().contentEquals("success"))
//            {
//                if (result.contains(".") || result.contains("\n"))
//                {
//                    String delims = "[.\\n]+";
//                    result = result.split(delims)[0];
//                }
//                throw new Exception("Error during the VM power operation." + result);
//            }
//        }
//        catch (Exception ex)
//        {
//            throw new MSException(ex);
//        }
//        finally
//        {
//            disconnect();
//        }        
//    }
//    
//    /**
//     * Added By: Sudhir on June 14, 2012
//     * 
//     * Migrates the VM to the specified destination
//     * @param vmName : VM that needs to be migrated
//     * @param destHostName : Target host on which the VM has to be migrated
//     * @param vCenterConnectionString : Connection string to the vCenter server
//     * @throws Exception 
//     */
//    protected static void migrateVM(String vmName, String destHostName, String vCenterConnectionString) throws MSException {
//        ManagedObjectReference hostMOR = null;
//        ManagedObjectReference vmMOR = null;
//        ManagedObjectReference migrateTaskMOR = null;
//        try
//        {
//            connect(vCenterConnectionString);
//            
//            hostMOR = getDecendentMoRef(hostMOR, "HostSystem", destHostName);
//            vmMOR = getDecendentMoRef(vmMOR, "VirtualMachine", vmName);
//            
//            if (vmMOR == null || hostMOR == null)
//                throw new MSException(new Exception ("Invalid virtual machine or host specified for the VM migration."));
//            
//            String vmPowerState = getMORProperty(vmMOR, "runtime.powerState").toString();
//            
//            if (vmPowerState.equalsIgnoreCase("powered_on"))
//                migrateTaskMOR = vimPort.migrateVMTask(vmMOR, null, hostMOR, 
//                        VirtualMachineMovePriority.HIGH_PRIORITY, VirtualMachinePowerState.POWERED_ON);
//            else
//                migrateTaskMOR = vimPort.migrateVMTask(vmMOR, null, hostMOR, 
//                        VirtualMachineMovePriority.HIGH_PRIORITY, VirtualMachinePowerState.POWERED_OFF);
//            
//            // Wait for the power operation to complete and return back
//            String result = waitForTask(migrateTaskMOR);
//            if (!result.equalsIgnoreCase("success"))
//            {
//                if (result.contains(".") || result.contains("\n"))
//                {
//                    String delims = "[.\\n]+";
//                    result = result.split(delims)[0];
//                }
//                throw new Exception("Error during the VM migration." + result);
//            }
//        }
//        catch (Exception ex)
//        {
//            throw new MSException( ex);
//        }
//        finally
//        {
//            disconnect();
//        }        
//    }
//        
//    /**
//     * Added By: Sudhir on June 15, 2012
//     * 
//     * Retrieves the BIOS PCR 0 value from the attestation reports for the specified host
//     * @param hostMOR: ManagedObjectReference for the host
//     * @return : String containing the BIOS PCR 0 value.
//     * @throws Exception 
//     */
//    protected static String getHostBIOSPCRHash(ManagedObjectReference hostMOR) throws MSException  {
//        String biosPCRHash = "";
//        List<HostTpmDigestInfo> pcrList = null;
//        
//        try
//        {
//            boolean tpmSupport = Boolean.parseBoolean(getMORProperty(hostMOR, "capability.tpmSupported").toString());
//            if (tpmSupport == true && serviceContent.getAbout().getVersion().contains("5.1"))
//            {
//                HostTpmAttestationReport hostTrustReport = vimPort.queryTpmAttestationReport(hostMOR);
//                if (hostTrustReport != null)
//                {
//                    pcrList = hostTrustReport.getTpmPcrValues();
//                    for (int k = 0; k < pcrList.size(); k++)
//                    {
//                        HostTpmDigestInfo pcrInfo = (HostTpmDigestInfo) pcrList.get(k);
//                        switch (pcrInfo.getPcrNumber())
//                        {
//                            case 0:
//                                biosPCRHash = byteArrayToHexString(pcrInfo.getDigestValue());
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//                }
//            }
//            else if (tpmSupport == true && serviceContent.getAbout().getVersion().contains("5.0"))
//            {
//                // Refresh the runtime information
//                HostRuntimeInfo runtimeInfo = (HostRuntimeInfo)getMORProperty(hostMOR, "runtime");
//
//                // Now process the digest information
//                pcrList = runtimeInfo.getTpmPcrValues();
//                for (int k = 0; k < pcrList.size(); k++)
//                {
//                    HostTpmDigestInfo pcrInfo = (HostTpmDigestInfo) pcrList.get(k);
//                    switch (pcrInfo.getPcrNumber())
//                    {
//                        case 0:
//                            biosPCRHash = byteArrayToHexString(pcrInfo.getDigestValue());
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            }
//            else
//            {
//                // Since the host does not support TPM, we will not have these values.
//                biosPCRHash = "";
//            }
//        }
//        catch (Exception ex)
//        {
//            throw new MSException(ex);
//        }
//        return biosPCRHash;
//    } 
//    
//
//    /**
//     * Added By: Sudhir on June 18, 2012
//     * 
//     * Retrieves the attestation report as a XML string
//     * @param hostName : Name of the host for which the attestation report has to be retrieved
//     * @param pcrList : Required PCR list separated by comma
//     * @param vCenterConnectionString : Connection string to the vCenter server on which the host is configured
//     * @return : XML string equivalent of the attestation report.
//     * @throws Exception 
//     */
//    /*protected static String getHostAttestationReport(TxtHostRecord hostObj, String pcrList) throws Exception {
//        ManagedObjectReference hostMOR = null;
//        String attestationReport = "";
//        boolean doNotDisconnect = false;
//
//        XMLOutputFactory xof = XMLOutputFactory.newInstance();
//        XMLStreamWriter xtw = null;
//        StringWriter sw = new StringWriter();
//        
//        try
//        {
//            // Verify if the PCRList is sent. If not set the default values.
//            if (pcrList == null || pcrList.isEmpty())
//                pcrList = "0,17,18,20";
//            
//            // If we have already established a connection, we use it. This will
//            // happen when this function will be called by the getHostDetailsForCluster
//            // function, which would have opened the connection to vCenter server.
//            if (!isConnected)
//                connect(hostObj.AddOn_Connection_String);
//            else
//                doNotDisconnect = true;
//
//            hostMOR = getDecendentMoRef(hostMOR, "HostSystem", hostObj.HostName);
//            if(hostMOR == null)
//            {
//                throw new Exception("Host specified does not exist in the vCenter.");
//            }
//            
//            Boolean tpmSupport = Boolean.parseBoolean(getMORProperty(hostMOR, "capability.tpmSupported").toString());
//            
//            // Lets create the start of the XML document
//            // xtw = xof.createXMLStreamWriter(new FileWriter("c:\\temp\\nb_xml.xml"));
//            xtw = xof.createXMLStreamWriter(sw);
//            xtw.writeStartDocument();
//            xtw.writeStartElement("Host_Attestation_Report");
//            xtw.writeAttribute("Host_Name", hostObj.HostName);
//            xtw.writeAttribute("vCenterVersion", serviceContent.getAbout().getVersion());
//            xtw.writeAttribute("HostVersion", getMORProperty(hostMOR, "config.product.version").toString());
//            xtw.writeAttribute("TXT_Support", tpmSupport.toString());
//            
//            if (tpmSupport == true && serviceContent.getAbout().getVersion().contains("5.1"))
//            {
//                HostTpmAttestationReport hostTrustReport = vimPort.queryTpmAttestationReport(hostMOR);
//                
//                // Process the event log only for the ESXi 5.1 or higher
//                if (hostTrustReport != null && getMORProperty(hostMOR, "config.product.version").toString().contains("5.1"))
//                {
//                    int numOfEvents = hostTrustReport.getTpmEvents().size();
//                    for (int k = 0; k < numOfEvents; k++)
//                    {
//                        HostTpmEventLogEntry eventInfo = (HostTpmEventLogEntry) hostTrustReport.getTpmEvents().get(k);
//                        switch (eventInfo.getPcrIndex())
//                        {
//                            // We will process only the components that gets extended into PCR 19. We
//                            // will ignore the rest of the event entries.
//                            case 0:
//                                break;
//                            case 17:
//                                break;
//                            case 18:
//                                break;
//                            case 20:
//                                break;                            
//                            // All the static components hash values are in this index. So, we will process
//                            // all the entries and store them into the database.
//                            case 19:
//                                String eventName = eventInfo.getEventDetails().getClass().getSimpleName();
//                                if(eventName.equalsIgnoreCase("HostTpmSoftwareComponentEventDetails"))
//                                {
//                                    HostTpmSoftwareComponentEventDetails swEventLog = (HostTpmSoftwareComponentEventDetails)eventInfo.getEventDetails();
//                                    xtw.writeStartElement("EventDetails");
//                                    xtw.writeAttribute("EventName", "Vim25Api.HostTpmSoftwareComponentEventDetails");
//                                    xtw.writeAttribute("ComponentName", swEventLog.getComponentName());
//                                    xtw.writeAttribute("DigestValue", byteArrayToHexString(swEventLog.getDataHash()));
//                                    xtw.writeAttribute("ExtendedToPCR", String.valueOf(eventInfo.getPcrIndex()));
//                                    xtw.writeAttribute("PackageName", swEventLog.getVibName());
//                                    xtw.writeAttribute("PackageVendor", swEventLog.getVibVendor());
//                                    xtw.writeAttribute("PackageVersion", swEventLog.getVibVersion());
//                                    xtw.writeAttribute("UseHostSpecificDigest", "False");
//                                    xtw.writeEndElement();
//                                }
//                                else if (eventName.equalsIgnoreCase("HostTpmOptionEventDetails"))
//                                {
//                                    HostTpmOptionEventDetails optEventLog = (HostTpmOptionEventDetails)eventInfo.getEventDetails();
//                                    xtw.writeStartElement("EventDetails");
//                                    xtw.writeAttribute("EventName", "Vim25Api.HostTpmOptionEventDetails");
//                                    xtw.writeAttribute("ComponentName", optEventLog.getOptionsFileName());
//                                    xtw.writeAttribute("DigestValue", byteArrayToHexString(optEventLog.getDataHash()));
//                                    xtw.writeAttribute("ExtendedToPCR", String.valueOf(eventInfo.getPcrIndex()));
//                                    xtw.writeAttribute("PackageName", "");
//                                    xtw.writeAttribute("PackageVendor", "");
//                                    xtw.writeAttribute("PackageVersion", "");
//                                    xtw.writeAttribute("UseHostSpecificDigest", "False");
//                                    xtw.writeEndElement();
//                                }
//                                else if (eventName.equalsIgnoreCase("HostTpmBootSecurityOptionEventDetails"))
//                                {
//                                    HostTpmBootSecurityOptionEventDetails bootEventLog = (HostTpmBootSecurityOptionEventDetails)eventInfo.getEventDetails();
//                                    xtw.writeStartElement("EventDetails");
//                                    xtw.writeAttribute("EventName", "Vim25Api.HostTpmBootSecurityOptionEventDetails");
//                                    xtw.writeAttribute("ComponentName", bootEventLog.getBootSecurityOption());
//                                    xtw.writeAttribute("DigestValue", byteArrayToHexString(bootEventLog.getDataHash()));
//                                    xtw.writeAttribute("ExtendedToPCR", String.valueOf(eventInfo.getPcrIndex()));
//                                    xtw.writeAttribute("PackageName", "");
//                                    xtw.writeAttribute("PackageVendor", "");
//                                    xtw.writeAttribute("PackageVersion", "");
//                                    xtw.writeAttribute("UseHostSpecificDigest", "False");
//                                    xtw.writeEndElement();                                    
//                                }                            
//                                else if (eventName.equalsIgnoreCase("HostTpmCommandEventDetails"))
//                                {
//                                    HostTpmCommandEventDetails cmdEventLog = (HostTpmCommandEventDetails)eventInfo.getEventDetails();
//                                    xtw.writeStartElement("EventDetails");
//                                    xtw.writeAttribute("EventName", "Vim25Api.HostTpmCommandEventDetails");
//                                    // We should not store the actual command line data here since it is host specific.
//                                    xtw.writeAttribute("ComponentName", ""); //cmdEventLog.commandLine);
//                                    xtw.writeAttribute("DigestValue", byteArrayToHexString(cmdEventLog.getDataHash()));
//                                    xtw.writeAttribute("ExtendedToPCR", String.valueOf(eventInfo.getPcrIndex()));
//                                    xtw.writeAttribute("PackageName", "");
//                                    xtw.writeAttribute("PackageVendor", "");
//                                    xtw.writeAttribute("PackageVersion", "");
//                                    xtw.writeAttribute("UseHostSpecificDigest", "True");
//                                    xtw.writeAttribute("HostName", hostObj.HostName);
//                                    xtw.writeEndElement();
//                                }                                    
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//                }
//                
//                // The TPM values have to be captured for both ESXi 5.0 or 5.1 hosts
//                if (hostTrustReport != null)
//                {
//                    List<String> pcrs = Arrays.asList(pcrList.split(","));
//                    int numTPMValues = hostTrustReport.getTpmPcrValues().size(); 
//                    for (int j = 0; j < numTPMValues; j++)
//                    {
//                        HostTpmDigestInfo pcrInfo = hostTrustReport.getTpmPcrValues().get(j);
//                        if (pcrs.contains(String.valueOf(pcrInfo.getPcrNumber())))
//                        {
//                            xtw.writeStartElement("PCRInfo");
//                            xtw.writeAttribute("ComponentName", String.valueOf(pcrInfo.getPcrNumber()));
//                            xtw.writeAttribute("DigestValue", byteArrayToHexString(pcrInfo.getDigestValue()));
//                            xtw.writeEndElement();
//                        }
//                    }
//                }
//            }
//            else if (tpmSupport == true && serviceContent.getAbout().getVersion().contains("5.0"))
//            {
//                // Refresh the runtime information
//                HostRuntimeInfo runtimeInfo = (HostRuntimeInfo)getMORProperty(hostMOR, "runtime");
//
//                // Now process the digest information
//                List<String> pcrs = Arrays.asList(pcrList.split(","));
//                int numTPMValues = runtimeInfo.getTpmPcrValues().size();
//                for (int j = 0; j < numTPMValues; j++)
//                {
//                    HostTpmDigestInfo pcrInfo = runtimeInfo.getTpmPcrValues().get(j);
//                    if (pcrs.contains(String.valueOf(pcrInfo.getPcrNumber())))
//                    {
//                        xtw.writeStartElement("PCRInfo");
//                        xtw.writeAttribute("ComponentName", String.valueOf(pcrInfo.getPcrNumber()));
//                        xtw.writeAttribute("DigestValue", byteArrayToHexString(pcrInfo.getDigestValue()));
//                        xtw.writeEndElement();
//                    }
//                }
//            }
//            else
//            {
//                xtw.writeStartElement("PCRInfo");
//                xtw.writeAttribute("Error", "Host does not support TPM.");
//                xtw.writeEndElement();
//            }         
//            
//            xtw.writeEndElement();
//            xtw.writeEndDocument();
//            xtw.flush();
//            xtw.close(); 
//            attestationReport = sw.toString();
//        }
//        catch (Exception ex)
//        {
//            throw ex;
//        }
//        finally
//        {
//            if (!doNotDisconnect)
//                disconnect();
//        }
//        return attestationReport;
//    }
//    */
//   // <editor-fold defaultstate="collapsed" desc="Code copied from VMware SDK's VMPowerOps.Java file.">
//   
//   /**
//    * Uses the new RetrievePropertiesEx method to emulate the now deprecated RetrieveProperties method
//    *
//    * @param listpfs
//    * @return list of object content
//    * @throws Exception
//    */
//   private static List<ObjectContent> retrievePropertiesAllObjects(List<PropertyFilterSpec> listpfs)
//      throws MSException {
//
//      RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();
//
//      List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();
//
//      try {
//         RetrieveResult rslts =
//            vimPort.retrievePropertiesEx(propCollectorRef,
//                                         listpfs,
//                                         propObjectRetrieveOpts);
//         if (rslts != null && rslts.getObjects() != null &&
//               !rslts.getObjects().isEmpty()) {
//            listobjcontent.addAll(rslts.getObjects());
//         }
//         String token = null;
//         if(rslts != null && rslts.getToken() != null) {
//            token = rslts.getToken();
//         }
//         while (token != null && !token.isEmpty()) {
//            rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
//            token = null;
//            if (rslts != null) {
//               token = rslts.getToken();
//               if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
//                  listobjcontent.addAll(rslts.getObjects());
//               }
//            }
//         }
//      } catch (Exception e) {
//         throw new MSException(e);
//      }
//
//      return listobjcontent;
//   }
//
//   /**
//    * This code takes an array of [typename, property, property, ...]
//    * and converts it into a PropertySpec[].
//    * handles case where multiple references to the same typename
//    * are specified.
//    *
//    * @param typeinfo 2D array of type and properties to retrieve
//    *
//    * @return Array of container filter specs
//    */
//   private static List<PropertySpec> buildPropertySpecArray(String[][] typeinfo) {
//      // Eliminate duplicates
//      HashMap<String, Set> tInfo = new HashMap<String, Set>();
//      for(int ti = 0; ti < typeinfo.length; ++ti) {
//         Set props = (Set) tInfo.get(typeinfo[ti][0]);
//         if(props == null) {
//            props = new HashSet<String>();
//            tInfo.put(typeinfo[ti][0], props);
//         }
//         boolean typeSkipped = false;
//         for(int pi = 0; pi < typeinfo[ti].length; ++pi) {
//            String prop = typeinfo[ti][pi];
//            if(typeSkipped) {
//               props.add(prop);
//            } else {
//               typeSkipped = true;
//            }
//         }
//      }
//
//      // Create PropertySpecs
//      ArrayList<PropertySpec> pSpecs = new ArrayList<PropertySpec>();
//      for(Iterator<String> ki = tInfo.keySet().iterator(); ki.hasNext();) {
//         String type = (String) ki.next();
//         PropertySpec pSpec = new PropertySpec();
//         Set props = (Set) tInfo.get(type);
//         pSpec.setType(type);
//         pSpec.setAll(props.isEmpty() ? Boolean.TRUE : Boolean.FALSE);
//         for(Iterator pi = props.iterator(); pi.hasNext();) {
//            String prop = (String) pi.next();
//            pSpec.getPathSet().add(prop);
//         }
//         pSpecs.add(pSpec);
//      }
//
//      return pSpecs;
//   }
//
//   /**
//    * Retrieve content recursively with multiple properties.
//    * the typeinfo array contains typename + properties to retrieve.
//    *
//    * @param collector a property collector if available or null for default
//    * @param root a root folder if available, or null for default
//    * @param typeinfo 2D array of properties for each typename
//    * @param recurse retrieve contents recursively from the root down
//    *
//    * @return retrieved object contents
//    */
//   private static List<ObjectContent>
//      getContentsRecursively(ManagedObjectReference collector,
//                             ManagedObjectReference root,
//                             String[][] typeinfo, boolean recurse)
//       {
//      if (typeinfo == null || typeinfo.length == 0) {
//         return null;
//      }
//
//      ManagedObjectReference usecoll = collector;
//      if (usecoll == null) {
//         usecoll = serviceContent.getPropertyCollector();
//      }
//
//      ManagedObjectReference useroot = root;
//      if (useroot == null) {
//         useroot = serviceContent.getRootFolder();
//      }
//
//      List<SelectionSpec> selectionSpecs = null;
//      if (recurse) {
//         selectionSpecs = buildFullTraversal();
//      }
//
//      List<PropertySpec> propspecary = buildPropertySpecArray(typeinfo);
//      ObjectSpec objSpec = new ObjectSpec();
//      objSpec.setObj(useroot);
//      objSpec.setSkip(Boolean.FALSE);
//      objSpec.getSelectSet().addAll(selectionSpecs);
//      List<ObjectSpec> objSpecList = new ArrayList<ObjectSpec>();
//      objSpecList.add(objSpec);
//      PropertyFilterSpec spec = new PropertyFilterSpec();
//      spec.getPropSet().addAll(propspecary);
//      spec.getObjectSet().addAll(objSpecList);
//      List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
//      listpfs.add(spec);
//      List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
//
//      return listobjcont;
//   }
//
//   private static boolean typeIsA(String searchType,
//                                  String foundType) {
//      if(searchType.equals(foundType)) {
//         return true;
//      } else if(searchType.equals("ManagedEntity")) {
//         for(int i = 0; i < meTree.length; ++i) {
//            if(meTree[i].equals(foundType)) {
//               return true;
//            }
//         }
//      } else if(searchType.equals("ComputeResource")) {
//         for(int i = 0; i < crTree.length; ++i) {
//            if(crTree[i].equals(foundType)) {
//               return true;
//            }
//         }
//      } else if(searchType.equals("HistoryCollector")) {
//         for(int i = 0; i < hcTree.length; ++i) {
//            if(hcTree[i].equals(foundType)) {
//               return true;
//            }
//         }
//      }
//      return false;
//   }
//
//   /**
//    * Get the ManagedObjectReference for an item under the
//    * specified root folder that has the type and name specified.
//    *
//    * @param root a root folder if available, or null for default
//    * @param type type of the managed object
//    * @param name name to match
//    *
//    * @return First ManagedObjectReference of the type / name pair found
//    */
//   private static ManagedObjectReference getDecendentMoRef(ManagedObjectReference root,
//                                                           String type,
//                                                           String name)
//       {
//      if (name == null || name.length() == 0) {
//         return null;
//      }
//
//      String[][] typeinfo =
//         new String[][] {new String[] {type, "name"}, };
//
//      List<ObjectContent> ocary =
//         getContentsRecursively(null, root, typeinfo, true);
//
//      if (ocary == null || ocary.size() == 0) {
//         return null;
//      }
//
//      ObjectContent oc = null;
//      ManagedObjectReference mor = null;
//      List<DynamicProperty> propary = null;
//      String propval = null;
//      boolean found = false;
//      for (int oci = 0; oci < ocary.size() && !found; oci++) {
//         oc = ocary.get(oci);
//         mor = oc.getObj();
//         propary = oc.getPropSet();
//
//         propval = null;
//         if (type == null || typeIsA(type, mor.getType())) {
//            if (propary != null && !propary.isEmpty()) {
//               propval = (String) propary.get(0).getVal();
//            }
//            found = propval != null && name.equals(propval);
//         }
//      }
//
//      if (!found) {
//         mor = null;
//      }
//
//      return mor;
//   }
//
//   private static String getProp(ManagedObjectReference obj,
//                                 String prop) throws MSException{
//      String propVal = null;
//      try {
//         List<DynamicProperty> dynaProArray
//            = getDynamicProarray(obj, obj.getType().toString(), prop);
//         if(dynaProArray != null && !dynaProArray.isEmpty()) {
//            if(dynaProArray.get(0).getVal() != null) {
//               propVal = (String) dynaProArray.get(0).getVal();
//            }
//         }
//      }  catch (Exception e) {
//         throw new MSException(e);
//      }
//      return propVal;
//   }
//
//   private static ArrayList filterMOR(ArrayList mors,
//                                      String [][] filter)
//       {
//      ArrayList filteredmors =
//         new ArrayList();
//      for(int i = 0; i < mors.size(); i++) {
//         for(int k = 0; k < filter.length; k++) {
//            String prop = filter[k][0];
//            String reqVal = filter[k][1];
//            String value = getProp(((ManagedObjectReference) mors.get(i)), prop);
//            if(reqVal == null) {
//               continue;
//            } else if(value == null && reqVal == null) {
//               continue;
//            } else if(value == null && reqVal != null) {
//               k = filter.length + 1;
//            } else if(value != null && value.equalsIgnoreCase(reqVal)) {
//               filteredmors.add(mors.get(i));
//            } else {
//               k = filter.length + 1;
//            }
//         }
//      }
//      return filteredmors;
//   }
//
//   private static ArrayList getDecendentMoRefs(ManagedObjectReference root,
//                                               String type,
//                                               String [][] filter)
//       {
//      String[][] typeinfo
//         = new String[][] {new String[] {type, "name"}, };
//
//      List<ObjectContent> ocary =
//         getContentsRecursively(null, root, typeinfo, true);
//
//      ArrayList refs = new ArrayList();
//
//      if (ocary == null || ocary.size() == 0) {
//         return refs;
//      }
//
//      for (int oci = 0; oci < ocary.size(); oci++) {
//         refs.add(ocary.get(oci).getObj());
//      }
//
//      if(filter != null) {
//         ArrayList filtermors = filterMOR(refs, filter);
//         return filtermors;
//      } else {
//         return refs;
//      }
//   }
//    
//   /**
//    *
//    * @return TraversalSpec specification to get to the VirtualMachine managed object.
//    */
//   // commenting out unused function (6/12 1.2)
//   /*
//   private static TraversalSpec getVMTraversalSpec() {
//      // Create a traversal spec that starts from the 'root' objects
//      // and traverses the inventory tree to get to the VirtualMachines.
//      // Build the traversal specs bottoms up
//
//      //Traversal to get to the VM in a VApp
//      TraversalSpec vAppToVM = new TraversalSpec();
//      vAppToVM.setName("vAppToVM");
//      vAppToVM.setType("VirtualApp");
//      vAppToVM.setPath("vm");
//
//      //Traversal spec for VApp to VApp
//      TraversalSpec vAppToVApp = new TraversalSpec();
//      vAppToVApp.setName("vAppToVApp");
//      vAppToVApp.setType("VirtualApp");
//      vAppToVApp.setPath("resourcePool");
//      //SelectionSpec for VApp to VApp recursion
//      SelectionSpec vAppRecursion = new SelectionSpec();
//      vAppRecursion.setName("vAppToVApp");
//      //SelectionSpec to get to a VM in the VApp
//      SelectionSpec vmInVApp = new SelectionSpec();
//      vmInVApp.setName("vAppToVM");
//      //SelectionSpec for both VApp to VApp and VApp to VM
//      List<SelectionSpec> vAppToVMSS = new ArrayList<SelectionSpec>();
//      vAppToVMSS.add(vAppRecursion);
//      vAppToVMSS.add(vmInVApp);
//      vAppToVApp.getSelectSet().addAll(vAppToVMSS);
//
//      //This SelectionSpec is used for recursion for Folder recursion
//      SelectionSpec sSpec = new SelectionSpec();
//      sSpec.setName("VisitFolders");
//
//      // Traversal to get to the vmFolder from DataCenter
//      TraversalSpec dataCenterToVMFolder = new TraversalSpec();
//      dataCenterToVMFolder.setName("DataCenterToVMFolder");
//      dataCenterToVMFolder.setType("Datacenter");
//      dataCenterToVMFolder.setPath("vmFolder");
//      dataCenterToVMFolder.setSkip(false);
//      dataCenterToVMFolder.getSelectSet().add(sSpec);
//
//      // TraversalSpec to get to the DataCenter from rootFolder
//      TraversalSpec traversalSpec = new TraversalSpec();
//      traversalSpec.setName("VisitFolders");
//      traversalSpec.setType("Folder");
//      traversalSpec.setPath("childEntity");
//      traversalSpec.setSkip(false);
//      List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
//      sSpecArr.add(sSpec);
//      sSpecArr.add(dataCenterToVMFolder);
//      sSpecArr.add(vAppToVM);
//      sSpecArr.add(vAppToVApp);
//      traversalSpec.getSelectSet().addAll(sSpecArr);
//      return traversalSpec;
//   }
//   */
//
//   /**
//    * Get the MOR of the Virtual Machine by its name.
//    * @param vmName The name of the Virtual Machine
//    * @return The Managed Object reference for this VM
//    */
//   //commenting out unused function (6/12 1.2)
//   /*
//   private static ManagedObjectReference getVmByVMname(String vmname) throws MSException {
//      ManagedObjectReference retVal = null;
//      try {
//         TraversalSpec tSpec = getVMTraversalSpec();
//         // Create Property Spec
//         PropertySpec propertySpec = new PropertySpec();
//         propertySpec.setAll(Boolean.FALSE);
//         propertySpec.getPathSet().add("name");
//         propertySpec.setType("VirtualMachine");
//
//         // Now create Object Spec
//         ObjectSpec objectSpec = new ObjectSpec();
//         objectSpec.setObj(rootRef);
//         objectSpec.setSkip(Boolean.TRUE);
//         objectSpec.getSelectSet().add(tSpec);
//
//         // Create PropertyFilterSpec using the PropertySpec and ObjectPec
//         // created above.
//         PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
//         propertyFilterSpec.getPropSet().add(propertySpec);
//         propertyFilterSpec.getObjectSet().add(objectSpec);
//
//         List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
//         listpfs.add(propertyFilterSpec);
//         List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
//
//         if (listobjcont != null) {
//            for (ObjectContent oc : listobjcont) {
//               ManagedObjectReference mr = oc.getObj();
//               String vmnm = null;
//               List<DynamicProperty> dps = oc.getPropSet();
//               if (dps != null) {
//                  for (DynamicProperty dp : dps) {
//                     vmnm = (String) dp.getVal();
//                  }
//               }
//               if (vmnm != null && vmnm.equals(vmname)) {
//                  retVal = mr;
//                  break;
//               }
//            }
//         }
//      }  catch (Exception e) {
//         throw new MSException(e);
//      }
//      return retVal;
//   }
//   */
//
//   /*
//    * @return An array of SelectionSpec covering VM, Host, Resource pool,
//    * Cluster Compute Resource and Datastore.
//    */
//   private static List<SelectionSpec> buildFullTraversal() {
//      // Terminal traversal specs
//
//      // RP -> VM
//      TraversalSpec rpToVm = new TraversalSpec();
//      rpToVm.setName("rpToVm");
//      rpToVm.setType("ResourcePool");
//      rpToVm.setPath("vm");
//      rpToVm.setSkip(Boolean.FALSE);
//
//      // vApp -> VM
//      TraversalSpec vAppToVM = new TraversalSpec();
//      vAppToVM.setName("vAppToVM");
//      vAppToVM.setType("VirtualApp");
//      vAppToVM.setPath("vm");
//
//      // HostSystem -> VM
//      TraversalSpec hToVm = new TraversalSpec();
//      hToVm.setType("HostSystem");
//      hToVm.setPath("vm");
//      hToVm.setName("hToVm");
//      hToVm.getSelectSet().add(getSelectionSpec("visitFolders"));
//      hToVm.setSkip(Boolean.FALSE);
//
//      // DC -> DS
//      TraversalSpec dcToDs = new TraversalSpec();
//      dcToDs.setType("Datacenter");
//      dcToDs.setPath("datastore");
//      dcToDs.setName("dcToDs");
//      dcToDs.setSkip(Boolean.FALSE);
//
//        // Recurse through all ResourcePools
//      TraversalSpec rpToRp = new TraversalSpec();
//      rpToRp.setType("ResourcePool");
//      rpToRp.setPath("resourcePool");
//      rpToRp.setSkip(Boolean.FALSE);
//      rpToRp.setName("rpToRp");
//      rpToRp.getSelectSet().add(getSelectionSpec("rpToRp"));
//      rpToRp.getSelectSet().add(getSelectionSpec("rpToVm"));
//
//      TraversalSpec crToRp = new TraversalSpec();
//      crToRp.setType("ComputeResource");
//      crToRp.setPath("resourcePool");
//      crToRp.setSkip(Boolean.FALSE);
//      crToRp.setName("crToRp");
//      crToRp.getSelectSet().add(getSelectionSpec("rpToRp"));
//      crToRp.getSelectSet().add(getSelectionSpec("rpToVm"));
//
//      TraversalSpec crToH = new TraversalSpec();
//      crToH.setSkip(Boolean.FALSE);
//      crToH.setType("ComputeResource");
//      crToH.setPath("host");
//      crToH.setName("crToH");
//
//      TraversalSpec dcToHf = new TraversalSpec();
//      dcToHf.setSkip(Boolean.FALSE);
//      dcToHf.setType("Datacenter");
//      dcToHf.setPath("hostFolder");
//      dcToHf.setName("dcToHf");
//      dcToHf.getSelectSet().add(getSelectionSpec("visitFolders"));
//
//      TraversalSpec vAppToRp = new TraversalSpec();
//      vAppToRp.setName("vAppToRp");
//      vAppToRp.setType("VirtualApp");
//      vAppToRp.setPath("resourcePool");
//      vAppToRp.getSelectSet().add(getSelectionSpec("rpToRp"));
//
//      TraversalSpec dcToVmf = new TraversalSpec();
//      dcToVmf.setType("Datacenter");
//      dcToVmf.setSkip(Boolean.FALSE);
//      dcToVmf.setPath("vmFolder");
//      dcToVmf.setName("dcToVmf");
//      dcToVmf.getSelectSet().add(getSelectionSpec("visitFolders"));
//
//     // For Folder -> Folder recursion
//      TraversalSpec visitFolders = new TraversalSpec();
//      visitFolders.setType("Folder");
//      visitFolders.setPath("childEntity");
//      visitFolders.setSkip(Boolean.FALSE);
//      visitFolders.setName("visitFolders");
//      List <SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
//      sspecarrvf.add(getSelectionSpec("visitFolders"));
//      sspecarrvf.add(getSelectionSpec("dcToVmf"));
//      sspecarrvf.add(getSelectionSpec("dcToHf"));
//      sspecarrvf.add(getSelectionSpec("dcToDs"));
//      sspecarrvf.add(getSelectionSpec("crToRp"));
//      sspecarrvf.add(getSelectionSpec("crToH"));
//      sspecarrvf.add(getSelectionSpec("hToVm"));
//      sspecarrvf.add(getSelectionSpec("rpToVm"));
//      sspecarrvf.add(getSelectionSpec("rpToRp"));
//      sspecarrvf.add(getSelectionSpec("vAppToRp"));
//      sspecarrvf.add(getSelectionSpec("vAppToVM"));
//
//      visitFolders.getSelectSet().addAll(sspecarrvf);
//
//      List <SelectionSpec> resultspec = new ArrayList<SelectionSpec>();
//      resultspec.add(visitFolders);
//      resultspec.add(dcToVmf);
//      resultspec.add(dcToHf);
//      resultspec.add(dcToDs);
//      resultspec.add(crToRp);
//      resultspec.add(crToH);
//      resultspec.add(hToVm);
//      resultspec.add(rpToVm);
//      resultspec.add(vAppToRp);
//      resultspec.add(vAppToVM);
//      resultspec.add(rpToRp);
//
//      return resultspec;
//   }
//
//   private static SelectionSpec getSelectionSpec(String name) {
//      SelectionSpec genericSpec = new SelectionSpec();
//      genericSpec.setName(name);
//      return genericSpec;
//   }
//
//   private static List<DynamicProperty> getDynamicProarray(ManagedObjectReference ref,
//                                                           String type,
//                                                           String propertyString)
//      {
//      PropertySpec propertySpec = new PropertySpec();
//      propertySpec.setAll(Boolean.FALSE);
//      propertySpec.getPathSet().add(propertyString);
//      propertySpec.setType(type);
//
//      // Now create Object Spec
//      ObjectSpec objectSpec = new ObjectSpec();
//      objectSpec.setObj(ref);
//      objectSpec.setSkip(Boolean.FALSE);
//      objectSpec.getSelectSet().addAll(buildFullTraversal());
//      // Create PropertyFilterSpec using the PropertySpec and ObjectPec
//      // created above.
//      PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
//      propertyFilterSpec.getPropSet().add(propertySpec);
//      propertyFilterSpec.getObjectSet().add(objectSpec);
//      List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
//      listpfs.add(propertyFilterSpec);
//      List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
//      ObjectContent contentObj = listobjcont.get(0);
//      List<DynamicProperty> objList = contentObj.getPropSet();
//      return objList;
//   }
//
//   private static boolean getTaskInfo(ManagedObjectReference taskmor) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, MSException
//       {
//      boolean valid = false;
//      String res = waitForTask(taskmor);
//      if(res.equalsIgnoreCase("success")) {
//         valid = true;
//      } else {
//         valid = false;
//      }
//      return valid;
//   }
//
//   private static void updateValues(List<String> props,
//                                    Object[] vals,
//                                    PropertyChange propchg) {
//      for (int findi = 0; findi < props.size(); findi++) {
//         if (propchg.getName().lastIndexOf(props.get(findi)) >= 0) {
//            if (propchg.getOp() == PropertyChangeOp.REMOVE) {
//               vals[findi] = "";
//            } else {
//               vals[findi] = propchg.getVal();
//            }
//         }
//      }
//   }
//
//   /**
//    *  Handle Updates for a single object.
//    *  waits till expected values of properties to check are reached
//    *  Destroys the ObjectFilter when done.
//    *  @param objmor MOR of the Object to wait for </param>
//    *  @param filterProps Properties list to filter
//    *  @param endWaitProps
//    *    Properties list to check for expected values
//    *    these be properties of a property in the filter properties list
//    *  @param expectedVals values for properties to end the wait
//    *  @return true indicating expected values were met, and false otherwise
//    */
//   private static Object[] waitForValues(ManagedObjectReference objmor,
//                                         List<String> filterProps,
//                                         List<String> endWaitProps,
//                                         Object[][] expectedVals) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, MSException
//       {
//      // version string is initially null
//      String version = "";
//      Object[] endVals = new Object[endWaitProps.size()];
//      Object[] filterVals = new Object[filterProps.size()];
//      ObjectSpec objSpec = new ObjectSpec();
//      objSpec.setObj(objmor);
//      objSpec.setSkip(Boolean.FALSE);
//      PropertyFilterSpec spec = new PropertyFilterSpec();
//      spec.getObjectSet().add(objSpec);
//      PropertySpec propSpec = new PropertySpec();
//      propSpec.getPathSet().addAll(filterProps);
//      propSpec.setType(objmor.getType());
//      spec.getPropSet().add(propSpec);
//
//      ManagedObjectReference filterSpecRef =
//         vimPort.createFilter(propCollectorRef, spec, true);
//
//      boolean reached = false;
//
//      UpdateSet updateset = null;
//      List<PropertyFilterUpdate> filtupary = null;
//      PropertyFilterUpdate filtup = null;
//      List<ObjectUpdate> objupary = null;
//      ObjectUpdate objup = null;
//      List<PropertyChange> propchgary = null;
//      PropertyChange propchg = null;
//      while (!reached) {
//         boolean retry = true;
//         while (retry) {
//            try {
//               updateset =
//               vimPort.waitForUpdates(propCollectorRef, version);
//               retry = false;
//            }  catch (Exception e) {
//               throw new MSException(e);
//            }
//         }
//         if(updateset != null) {
//            version = updateset.getVersion();
//         }
//         if (updateset == null || updateset.getFilterSet() == null) {
//            continue;
//         }
//
//         // Make this code more general purpose when PropCol changes later.
//         filtupary = updateset.getFilterSet();
//         filtup = null;
//         for (int fi = 0; fi < filtupary.size(); fi++) {
//            filtup = filtupary.get(fi);
//            objupary = filtup.getObjectSet();
//            objup = null;
//            propchgary = null;
//            for (int oi = 0; oi < objupary.size(); oi++) {
//               objup = objupary.get(oi);
//
//               if (objup.getKind() == ObjectUpdateKind.MODIFY ||
//                   objup.getKind() == ObjectUpdateKind.ENTER ||
//                   objup.getKind() == ObjectUpdateKind.LEAVE) {
//                  propchgary = objup.getChangeSet();
//                  for (int ci = 0; ci < propchgary.size(); ci++) {
//                     propchg = propchgary.get(ci);
//                     updateValues(endWaitProps, endVals, propchg);
//                     updateValues(filterProps, filterVals, propchg);
//                  }
//               }
//            }
//         }
//
//         Object expctdval = null;
//         // Check if the expected values have been reached and exit the loop if done.
//         // Also exit the WaitForUpdates loop if this is the case.
//         for (int chgi = 0; chgi < endVals.length && !reached; chgi++) {
//            for (int vali = 0; vali < expectedVals[chgi].length && !reached; vali++) {
//               expctdval = expectedVals[chgi][vali];
//               reached = expctdval.equals(endVals[chgi]) || reached;
//            }
//         }
//      }
//
//      // Destroy the filter when we are done.
//      vimPort.destroyPropertyFilter(filterSpecRef);
//
//      return filterVals;
//   }
//
//   private static String waitForTask(ManagedObjectReference taskmor) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, MSException  {
//      List<String> infoList = new ArrayList<String>();
//      infoList.add("info.state");
//      infoList.add("info.error");
//      List<String> stateList = new ArrayList<String>();
//      stateList.add("state");
//      Object[] result = waitForValues(
//                        taskmor, infoList, stateList,
//                        new Object[][] {new Object[] {
//                           TaskInfoState.SUCCESS, TaskInfoState.ERROR } });
//      if (result[0].equals(TaskInfoState.SUCCESS)) {
//         return "success";
//      } else {
//         List<DynamicProperty> tinfoProps = new ArrayList<DynamicProperty>();
//         tinfoProps = getDynamicProarray(taskmor, "Task", "info");
//         TaskInfo tinfo = (TaskInfo) tinfoProps.get(0).getVal();
//         LocalizedMethodFault fault = tinfo.getError();
//         String error = "Error Occured";
//         if(fault != null) {
//            error = fault.getLocalizedMessage();
//            System.out.println("Message " + fault.getLocalizedMessage());
//         }
//         return error;
//      }
//   }    
//// </editor-fold>
}
