package com.intel.mtwilson.agent.vmware;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsClient;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.vmware.vim25.*;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import java.rmi.RemoteException;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * How to use secure SSL connections:
 *
 * setSslHostnameVerifier();
 * setSslTrustManager(SslUtil.createX509TrustManagerWithKeystore(simpleKeystore));
 * connect(...)
 *
 * @author dsmagadX
 */
public class VMwareClient implements TlsClient {

    private Logger log = LoggerFactory.getLogger(getClass());
    private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
    private static final String SVC_INST_NAME = "ServiceInstance";
    protected ServiceContent serviceContent;
    private ManagedObjectReference propCollectorRef;
    private ManagedObjectReference rootRef;
    private ServiceInstance servInst;
    private VimPortType vimPort;
    private Folder rootFolder;
    private static final double MIN_VCENTER_VERSION_FOR_MODULE_ATTESTATION  = 5.1;
    private static final double MIN_ESX_VERSION_FOR_MODULE_ATTESTATION  = 5.1;
    //private VimService vimService;
    //private VimPortType vimPort;
    UserSession session = null;
    private String vcenterEndpoint = null;
    private String vmwareConnectionString;
    private TlsPolicy tlsPolicy = null;
    //private HostnameVerifier hostnameVerifier = null;
    //private X509TrustManager trustManager = null;
    private boolean isConnected = false;
    private static String[] meTree = {
        "ManagedEntity",
        "ComputeResource",
        "ClusterComputeResource",
        "Datacenter",
        "Folder",
        "HostSystem",
        "ResourcePool",
        "VirtualMachine"
    };
    private static String[] crTree = {
        "ComputeResource",
        "ClusterComputeResource"
    };
    private static String[] hcTree = {
        "HistoryCollector",
        "EventHistoryCollector",
        "TaskHistoryCollector"
    };

    public VMwareClient() {
    }

    // Bug: 579 - This method is added so that we can connect to the vCenter without any TLS policy settings and connection pooling options.
    // This would be needed by the Management Console to retrive the list of hosts from the cluster.
    protected void connect2(URL url, String userName, String password) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        log.debug("VMwareClient: connect2 | setting TlsPolicy...");
        setTlsPolicy(new InsecureTlsPolicy());
        log.debug("VMwareClient: calling connect...");
        connect(url.toExternalForm(), userName, password);

    }

    /*
     public void setSslHostnameVerifier(HostnameVerifier hostnameVerifier) {
     this.hostnameVerifier = hostnameVerifier;
     }
        
     public void setSslCertificateTrustManager(X509TrustManager trustManager) {
     this.trustManager = trustManager;
     }
     */
    /**
     * Disconnects the user session.
     *
     * @throws Exception
     */
    public void disconnect() {
        try {
            if (isConnected()) {
                isConnected = false;
                servInst.getServerConnection().logout();
            }
        } catch (Exception e) {
            log.error("Error while logging out from VCenter Api.", e);
        }
    }

    /**
     * Establishes session with the virtual center server.
     *
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidLoginFaultMsg
     * @throws InvalidLocaleFaultMsg
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     *
     * @throws Exception the exception
     */
    public void connect(String url, String userName, String password) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        vcenterEndpoint = url;
        vmwareConnectionString = url + ";" + userName + ";" + password;

        /*
         if( hostnameVerifier == null ) {
         log.warn("SSL Hostname Verifier not set; will accept any remote hostname");
         HttpsURLConnection.setDefaultHostnameVerifier(new NopX509HostnameVerifier());
         }
         else {
         HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
         }*/
        log.debug("Connecting to vcenter with URL: {}", url);
        log.debug("Connecting to vcenter with TlsPolicy: {}", tlsPolicy.getClass().getName());
        log.debug("Connecting to vcenter with HostnameVerifier: {}", tlsPolicy.getHostnameVerifier().getClass().getName());
        log.debug("Connecting to vcenter with TrustManager: {}", tlsPolicy.getTrustManager().getClass().getName());
        log.debug("Connecting to vcenter with ProtocolSelector: {}", tlsPolicy.getProtocolSelector().preferred());
//        TlsConnection tlsConnection = new TlsConnection(new URL(url), tlsPolicy);
//        javax.net.ssl.SSLContext sc = tlsConnection.getSSLContext();
//        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance(tlsPolicy.getProtocolSelector().preferred()); // issue #871 ssl protocol should be configurable; was hardcoded to "SSL"
//        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
//        sslsc.setSessionTimeout(0);
//        sc.init(null, new javax.net.ssl.TrustManager[]{tlsPolicy.getTrustManager()}, null);
//        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//        HttpsURLConnection.setDefaultHostnameVerifier(TlsPolicyManager.getInstance().getHostnameVerifier()); 

//        HttpsURLConnection.setDefaultSSLSocketFactory(new TlsPolicyAwareSSLSocketFactory());
//        HttpsURLConnection.setDefaultHostnameVerifier(TlsPolicyManager.getInstance().getHostnameVerifier());
        TlsConnection tlsConnection = new TlsConnection(new URL(url), tlsPolicy);
        TlsUtil.setHttpsURLConnectionDefaults(tlsConnection);
        
        SVC_INST_REF.setType(SVC_INST_NAME);
        SVC_INST_REF.setVal(SVC_INST_NAME);

        try {
            log.debug("VSPHERE: login to vcenter with username: {} {}", userName, password == null || password.isEmpty() ? "without password" : "with password");
            servInst = new ServiceInstance(new URL(url), userName, password, false);
            log.debug("VSPHERE: ServiceInstance created.");
            vimPort = servInst.getServerConnection().getVimService();
            log.debug("VSPHERE: vimPort created.");
            serviceContent = servInst.getServiceContent();
            log.debug("VSPHERE: serviceContent created.");
            rootFolder = servInst.getRootFolder();
            log.debug("VSPHERE: rootFolder retrieved.");
            session = servInst.getServerConnection().getUserSession(); //vimPort.login(serviceContent.getSessionManager(), userName, password, null);
            log.debug("VSPHERE: login complete.");
        } catch (Exception e) {
            throw new IOException("Cannot login to vcenter: " + e.toString(), e);
        }
        
//        Map<String, Object> ctxt = ((BindingProvider)vimPort).getRequestContext();
//        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
//        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

        printSessionDetails();

        isConnected = true;

        propCollectorRef = serviceContent.getPropertyCollector();
        rootRef = serviceContent.getRootFolder();
    }

    private void printSessionDetails() {
        if (session != null) {
            log.debug("Logged in Session key " + session.getKey());
        } else {
            log.info("session is null");
        }

    }

    public boolean isConnected() {
        if (!isConnected) {
            return false;
        }
        try {
            return vimPort.sessionIsActive(serviceContent.getSessionManager(), session.getKey(), session.getUserName());
        } catch (Exception e) {
            log.warn("session not active: {}", e.toString());
            return false;
        } catch (Error e) {
            log.warn("session not active: {}", e.toString());
            return false;
        }
    }

    public String getEndpoint() {
        return vcenterEndpoint;
    }

    public void connect(String vCenterConnectionString) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException, IOException {
        //ConnectionString cs = ConnectionString.forVmware(new URL(vCenterConnectionString));
        /*
         String[] vcenterConn = vCenterConnectionString.split(";");
         if (vcenterConn.length != 3) {
         throw new ASException(ErrorCode.AS_VMWARE_INVALID_CONNECT_STRING,
         vCenterConnectionString);
         }
         // Connect to the vCenter server with the passed in parameters
         connect(vcenterConn[0], vcenterConn[1], vcenterConn[2]);
         */
//        log.debug("VSPHERE: Connection string: {}", vCenterConnectionString);
        ConnectionString.VmwareConnectionString vmware = ConnectionString.VmwareConnectionString.forURL(vCenterConnectionString);
        log.debug("VSPHERE: Connecting to vcenter: {} for host: {}", vmware.getVCenter().toString(), vmware.getHost().toString());
        connect(vmware.toURL().toExternalForm(), vmware.getUsername(), vmware.getPassword());
    }

    public static byte[] toByteArray(List<Byte> list) {
        byte[] ret = new byte[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = list.get(i);
        }
        return ret;
    }

    /**
     * Issue #784 performance This method returns just the requested host, in
     * contrast to getEntitiesByType which returns all the hosts and then we
     * have to query each one to see if it's the one we want
     */
    public ManagedObjectReference getHostReference(String hostname) throws RemoteException {
        //ManagedObjectReference searchIndex;
        //vimPort.findByDnsName and vimPort.findByIp ....  first parameter is the searchindex mor, second is datacenter (optional, can be null), , third is the dnsname/ip,  fourth is true for vm or false for host.
        // page 20: obtain manageed obejct reference by accessor method, for searchindex
//        ServiceContent sc = vimPort.retrieveServiceContent(hostRef)
        ManagedObjectReference searchIndex = serviceContent.getSearchIndex();
        ManagedObjectReference hostRef = vimPort.findByDnsName(searchIndex, null, hostname, false);
        if (hostRef == null) {
            hostRef = vimPort.findByIp(searchIndex, null, hostname, false);
        }
        return hostRef;

    }

    // / <summary>
    // / Based on the Entity type this function searches through all the objects
    // and returns
    // / the matching objects.
    // / For getting just clusters call it with "ClusterComputeResource"
    // / For getting just Datacenters call it with "Datacenter"
    // / For getting just hosts call it with "ComputeResource"
    // / </summary>
    // / <param name="entityType">Type of the Entity that should be returned
    // back</param>
    // / <returns>Array of Management Oject References to all the objects of the
    // type specified.</returns>
    protected ManagedEntity[] getEntitiesByType(String entityType) throws InvalidProperty, RuntimeFault, RemoteException {
        return new InventoryNavigator(rootFolder).searchManagedEntities(entityType);
    }

    protected String getHostInfo(ManagedObjectReference hostObj) throws InvalidProperty, RuntimeFault, RemoteException {
        ManagedEntity me = new InventoryNavigator(rootFolder).searchManagedEntity(hostObj.type, hostObj.val);
        return me.getName();
    }
    
    protected String getHostInfo(ManagedEntity hostObj) throws InvalidProperty, RuntimeFault, RemoteException {
        return hostObj.getName();
    }

    public String getStringMEProperty(String meType, String meName, String propertyName) throws InvalidProperty, RuntimeFault, RemoteException {
        Object value = getMEProperty(meType, meName, propertyName);
        if( value == null ) { return null; }
        return value.toString();
    }
    public Object getMEProperty(String meType, String meName, String propertyName)
            throws InvalidProperty, RuntimeFault, RemoteException {
        Object value = getMEProperties(meType, meName, new String[]{propertyName}).get(propertyName);
        if (value == null) { throw new InvalidProperty(); }
        return value;
    }
    
    protected Hashtable getMEProperties(String meType, String meName, String[] properties)
            throws InvalidProperty, RuntimeFault, RemoteException {
        log.debug("VSPHERE: meType: " + meType);
        log.debug("VSPHERE: meName: " + meName);
        for (String s : properties) {
            log.debug("VSPHERE: properties: " + s);
        }
        ManagedEntity me = new InventoryNavigator(rootFolder).searchManagedEntity(meType, meName);
        log.debug("VSPHERE: ManagedEntity: " + me.toString());
        Hashtable ht = me.getPropertiesByPaths(properties);
        log.debug("VSPHERE: Hashtable: " + ht.toString());
        
        return ht;
    }

    public List<String> getPropertyNames(TxtHostRecord hostObj) throws InvalidProperty, RuntimeFault, RemoteException, VMwareConnectionException {
        // Return object array
        ArrayList<String> list = new ArrayList<>();
        ManagedObjectReference moRef = getDecendentMoRef(null, "HostSystem", hostObj.HostName);
        if(moRef == null ) { throw new IllegalArgumentException("Cannot get reference to specified host"); }
        // PropertySpec specifiesgetHostAttestationReport what properties to
        // retrieve and from type of Managed Object
        PropertySpec pSpec = new PropertySpec();
        pSpec.setType(moRef.getType());
        //pSpec.getPathSet().addAll(new ArrayList<String>());

        for (String s : new ArrayList<String>()) {
            pSpec.getPathSet()[pSpec.getPathSet().length] = s;
        }
        // ObjectSpec specifies the starting object and
        // any TraversalSpecs used to specify other objects
        // for consideration
        ObjectSpec oSpec = new ObjectSpec();
        oSpec.setObj(moRef);

        // PropertyFilterSpec is used to hold the ObjectSpec and
        // PropertySpec for the call
        PropertyFilterSpec pfSpec = new PropertyFilterSpec();
        pfSpec.getPropSet()[0] = pSpec;
        pfSpec.getObjectSet()[0] = oSpec;

        // retrieveProperties() returns the properties
        // selected from the PropertyFilterSpec

        List<PropertyFilterSpec> pfSpecs = new ArrayList<PropertyFilterSpec>();
        pfSpecs.add(pfSpec);

        ObjectContent[] ocs = vimPort.retrieveProperties(propCollectorRef, (PropertyFilterSpec[])pfSpecs.toArray());

        for (ObjectContent oc : ocs) {
            DynamicProperty[] dps = oc.getPropSet();
            for (DynamicProperty dp : dps) {
                list.add(dp.getName()); // and dp.getVal()
            }
        }
        return list;
    }

    public String getVCenterVersion() {
        return serviceContent.getAbout().getVersion();
    }

    public HostTpmAttestationReport getAttestationReport(
            ManagedObjectReference hostObj) throws RuntimeFault, RemoteException {
        return vimPort.queryTpmAttestationReport(hostObj);

    }
    
    /**
     * performance of this method is very bad, it has been observed at 1 second
     * per iteration of the comparison loop. see getHostReference for obtaining
     * a managed object reference for a specific host (instead of for all hosts
     * and then querying each one for the name)
     */
    public ManagedEntity getManagedObjectReference(String hostName) throws InvalidProperty, RuntimeFault, RemoteException {
        // Get the host objects in the vcenter
        ManagedEntity[] hostObjects = getEntitiesByType("HostSystem");
        if (hostObjects != null && hostObjects.length != 0) {
            for (ManagedEntity hostObj : hostObjects) {
                String hostNameFromVC = getHostInfo(hostObj);
                log.debug("getHostObject - comparing hostNameFromVC {} requested hostName {}", new Object[]{hostNameFromVC, hostName});
                if (hostNameFromVC.equals(hostName)) {
                    log.debug(String.format("Found Managed Object Reference for host %s ", hostName));
                    return hostObj;
                }
            }
        }
        // If the code reaches here that means that we did not find the host
        throw new ASException(ErrorCode.AS_HOST_NOT_FOUND_IN_VCENTER, hostName);
    }

    @Override
    public void setTlsPolicy(TlsPolicy tlsPolicy) {
        this.tlsPolicy = tlsPolicy;
    }
    /*
     private class TrustAllTrustManager implements javax.net.ssl.TrustManager,
     javax.net.ssl.X509TrustManager {

     @Override
     public java.security.cert.X509Certificate[] getAcceptedIssuers() {
     return null;
     }

     @Override
     public void checkServerTrusted(
     java.security.cert.X509Certificate[] certs, String authType)
     throws java.security.cert.CertificateException {
     for (java.security.cert.X509Certificate cert : certs) {
     cert.checkValidity();
     }

     return;
     }

     @Override
     public void checkClientTrusted(
     java.security.cert.X509Certificate[] certs, String authType)
     throws java.security.cert.CertificateException {
     for (java.security.cert.X509Certificate cert : certs) {
     cert.checkValidity();
     }
     return;
     }
     } */

    public String byteArrayToBase64String(List<Byte> digestValue) {
        String digest = Base64.encodeBase64String(toByteArray(digestValue));
        return digest;
    }

    public static String byteArrayToHexString(List<Byte> digestValue) {

        return byteArrayToHexString(toByteArray(digestValue));
    }

    public static String byteArrayToHexString(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Added By: Sudhir on June 15, 2012
     *
     * Retrieves the list of Virtual machines for the specified host along with
     * the power state of the VM.
     *
     * @param hostName : Name of the host for which the VM details need to be
     * retrieved.
     * @param vCenterConnectionString : Connection string of the vCenter on
     * which the host is configured
     * @return : ArrayList consisting of all the VM along with the power state
     * information. VM Name::POWERED_ON
     * @throws Exception
     */
//    public ArrayList getVMsForHost(String hostName, String vCenterConnectionString) throws VMwareConnectionException {
//        ArrayList vmList;
//        ManagedObjectReference hostMOR = null;
//        try {
//            connect(vCenterConnectionString);
//
//            if (hostName != null) {
//                hostMOR = getDecendentMoRef(null, "HostSystem", hostName);
//                if (hostMOR == null) {
//                    throw new Exception("Host configuration not found in the vCenter database.");
//                }
//            }
//
//            vmList = getDecendentMoRefs(hostMOR, "VirtualMachine", null);
//            if (vmList.isEmpty()) {
//                return vmList;
//            }
//
//            for (int i = 0; i < vmList.size(); i++) {
//                String vmName =  getMORProperty((ManagedObjectReference) vmList.get(i), "name").toString();
//                String vmPowerState = getMORProperty((ManagedObjectReference) vmList.get(i), "runtime.powerState").toString();
//                vmList.set(i, vmName + "::" + vmPowerState);
//            }
//        } catch (Exception ex) {
//            throw new VMwareConnectionException(ex);
//        } finally {
//            disconnect();
//        }
//        return vmList;
//    }

    /**
     * Added By: Sudhir on June 15, 2012
     *
     * Retrieves the h/w & s/w details of the host including BIOS, OS
     * information
     *
     * @param hostName : Name of the host for which the details need to be
     * retrieved
     * @param vCenterConnectionString : Connection string to the vCenter server
     * where the host is configured.
     *
     * NOTE: this method modifies the input object and then returns the same
     * object; it does NOT return a new object or a copy
     *
     * @deprecated use VmwareHostAgent.getHostDetails()
     *
     *
     * @return : Host object containing all the details.
     * @throws Exception
     */
    public TxtHostRecord getHostDetails(TxtHostRecord hostObj) throws VMwareConnectionException {
        ManagedObjectReference hostMOR;
        boolean doNotDisconnect = false;

        try {
            // If we have already established a connection, we use it. This will
            // happen when this function will be called by the getHostDetailsForCluster
            // function, which would have opened the connection to vCenter server.
            if (!isConnected) {
                connect(hostObj.AddOn_Connection_String);
            } else {
                doNotDisconnect = true;
            }

            hostMOR = getDecendentMoRef(null, "HostSystem", hostObj.HostName);
            if (hostMOR == null) {
                throw new Exception("Host specified does not exist in the vCenter.");
            }

            hostObj.HostName = getStringMEProperty("HostSystem", hostObj.HostName, "name");
            // hostObj.Description = serviceContent.getAbout().getVersion();
            hostObj.VMM_OSName = getStringMEProperty("HostSystem", hostObj.HostName, "config.product.name");
            hostObj.VMM_OSVersion = getStringMEProperty("HostSystem", hostObj.HostName, "config.product.version");
            hostObj.VMM_Version = getStringMEProperty("HostSystem", hostObj.HostName, "config.product.build");
            hostObj.BIOS_Oem = getStringMEProperty("HostSystem", hostObj.HostName, "hardware.systemInfo.vendor");
            hostObj.BIOS_Version = getStringMEProperty("HostSystem", hostObj.HostName, "hardware.biosInfo.biosVersion");

        } catch (Exception ex) {
            throw new VMwareConnectionException(ex);
        } finally {
            if (!doNotDisconnect) {
                disconnect();
            }
        }
        return hostObj;
    }

    /**
     * Added By: Sudhir on June 15, 2012
     *
     * Retrieves the list of hosts along with the s/w and h/w configuration
     * details within the VMware Cluster.
     *
     * @param clusterName : Name of the cluster from which we need to retrieve
     * the host details
     * @param vCenterConnectionString : Connection string to the vCenter server
     * @return : Array list of all the host names
     * @throws Exception
     */
    public ArrayList getHostDetailsForCluster(String clusterName, String vCenterConnectionString) throws VMwareConnectionException, ASException {
        ArrayList hostList;
        ArrayList hostDetailList = new ArrayList<TxtHostRecord>();
        ManagedObjectReference clusterMOR = null;
        ConnectionString.VmwareConnectionString vmwareURL;
        try {
            vmwareURL = ConnectionString.VmwareConnectionString.forURL(vCenterConnectionString);
        } catch (Exception e) {
            throw new ASException(ErrorCode.AS_VMWARE_INVALID_CONNECT_STRING, clusterName); 
        }
        try {
            // Connect to the vCenter server with the passed in parameters,  but insecure tls policy since we don't know this host yet
            connect2(vmwareURL.toURL(), vmwareURL.getUsername(), vmwareURL.getPassword());

            if (clusterName != null) {
                clusterMOR = getDecendentMoRef(null, "ClusterComputeResource", clusterName);
                if (clusterMOR == null) {
                    throw new Exception("Cluster configuration not found in the vCenter database.");
                }
            }

            hostList = getDecendentMoRefs(clusterMOR, "HostSystem", null);
            if (hostList.isEmpty()) {
                return hostList;
            }

            for (int i = 0; i < hostList.size(); i++) {
                log.debug("VSPHERE: Host " + i + " = " + hostList.get(i).toString());
                String hostName = getStringMEProperty("HostSystem", hostList.get(i).toString(), "name");
                TxtHostRecord hostObj = new TxtHostRecord();
                hostObj.HostName = hostName;
                hostObj.AddOn_Connection_String = vCenterConnectionString;
                hostObj = getHostDetails(hostObj);
                hostDetailList.add(hostObj);
            }
        } catch (Exception ex) {
            throw new VMwareConnectionException(ex);
        } finally {
            disconnect();
        }
        return hostDetailList;
    }

    /**
     * Added By: Savino on June 28, 2013
     *
     * Retrieves the list of datacenters from the vcenter to populate dropdown
     * on webform.
     *
     * @param vCenterConnectionString : Connection string to the vCenter server
     * @return : string list of datacenter names
     * @throws Exception
     */
    public List<String> getDatacenterNames() throws InvalidProperty, RuntimeFault, RemoteException {
        List<String> ret = new ArrayList<>();
        log.debug("Acquiring datacenters...");
        ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter");
        for (ManagedEntity me : mes) {
            log.debug("Datacenter found: " + me.getName());
            ret.add(me.getName());
        }
        
        return ret;
    }

    /**
     * Added By: Savino on June 28, 2013
     *
     * Retrieves the list of clusters from the vcenter to populate dropdown on
     * webform.
     *
     * @param vCenterConnectionString : Connection string to the vCenter server
     * @param datacenterName : Specify the datacenter that contains the desired
     * clusters
     * @return : string list of cluster names
     * @throws Exception
     */
    public List<String> getClusterNamesWithDC() throws InvalidProperty, RuntimeFault, RemoteException  {
        List<String> ret = new ArrayList<String>();
        log.debug("Acquiring clusters...");
        ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("ComputeResource");
        for (ManagedEntity me : mes) {
            log.debug("Cluster found: " + "[" + me.getParent().getParent().getName() + "] " + me.getName());
            ret.add("[" + me.getParent().getParent().getName() + "] " + me.getName());
        }
        
        return ret;
    }
    
    public ArrayList getHostNamesForCluster(String clusterName) throws InvalidProperty, RuntimeFault, RemoteException {
        ArrayList<TxtHostRecord> hostDetailList = new ArrayList<>();
        log.debug("Acquiring host systems...");
        
        ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
        for (ManagedEntity me : mes) {
            if (me.getParent().getName().trim().equalsIgnoreCase(clusterName.trim())) {
                log.debug("Host System found for cluster " + clusterName + ": " + me.getName());
                String connectionState = getMEProperty("HostSystem",  me.getName(), "runtime.connectionState").toString().trim();
                if (connectionState.equalsIgnoreCase("connected")) {
                    log.debug("Adding host {} to the list of connected servers.", me.getName());
                    TxtHostRecord hostObj = new TxtHostRecord();
                    hostObj.HostName = me.getName();
                    hostObj.AddOn_Connection_String = vmwareConnectionString;
                    hostObj.VMM_OSName = getMEProperty("HostSystem", hostObj.HostName, "config.product.name").toString();
                    hostObj.VMM_OSVersion = getMEProperty("HostSystem", hostObj.HostName, "config.product.version").toString();
                    hostObj.VMM_Version = getMEProperty("HostSystem", hostObj.HostName, "config.product.build").toString();
                    hostObj.BIOS_Oem = getMEProperty("HostSystem", hostObj.HostName, "hardware.systemInfo.vendor").toString();
                    hostObj.BIOS_Version = getMEProperty("HostSystem", hostObj.HostName, "hardware.biosInfo.biosVersion").toString();
                    hostDetailList.add(hostObj);
                } else {
                    log.info("Host {} is not currently connected to the vCenter. So, it would not be used for registration.", me.getName());
                }
            }
            else {
                log.debug(me.getName() + ": parent \"" + me.getParent().getName() + "\" does not match cluster name \"" + clusterName + "\"");
            }
        }
        
        return hostDetailList;
    }
    
    /**
     * Added By: Sudhir on June 14, 2012
     *
     * This function provides the power on and power off functionality virtual
     * machines
     *
     * @param vmName : Name of the VM
     * @param hostName: Name of the host on which VM should be powered on. For
     * Power Off, this parameter is not needed.
     * @param powerOn: Flag that indicates whether to power on or off the VM
     * @param vCenterConnectionString : Connection string to the vCenter server.
     * @throws Exception
     */
    public void powerOnOffVM(String vmName, String hostName, Boolean powerOn, String vCenterConnectionString) throws VMwareConnectionException {
        ManagedObjectReference hostMOR;
        ManagedObjectReference vmMOR;
        ManagedObjectReference powerTaskMOR;
        try {
            connect(vCenterConnectionString);

            vmMOR = getDecendentMoRef(null, "VirtualMachine", vmName);

            if (vmMOR == null) {
                throw new Exception("Invalid virtual machine specified for the power operation.");
            }

            if (powerOn) {
                hostMOR = getDecendentMoRef(null, "HostSystem", hostName);
                if (hostMOR == null) {
                    throw new VMwareConnectionException("Invalid host specified for the virtual machine power on operation.");
                }
                powerTaskMOR = vimPort.powerOnVM_Task(vmMOR, hostMOR);
            } else {
                powerTaskMOR = vimPort.powerOffVM_Task(vmMOR);
            }

            // Wait for the power operation to complete and return back
            String result = waitForTask(powerTaskMOR);
            if (!result.toLowerCase().contentEquals("success")) {
                if (result.contains(".") || result.contains("\n")) {
                    String delims = "[.\\n]+";
                    result = result.split(delims)[0];
                }
                throw new Exception("Error during the VM power operation." + result);
            }
        } catch (Exception ex) {
            throw new VMwareConnectionException(ex);
        } finally {
            disconnect();
        }
    }

    /**
     * Added By: Sudhir on June 14, 2012
     *
     * Migrates the VM to the specified destination
     *
     * @param vmName : VM that needs to be migrated
     * @param destHostName : Target host on which the VM has to be migrated
     * @param vCenterConnectionString : Connection string to the vCenter server
     * @throws Exception
     */
    public void migrateVM(String vmName, String destHostName, String vCenterConnectionString) throws VMwareConnectionException {
        ManagedObjectReference hostMOR;
        ManagedObjectReference vmMOR;
        ManagedObjectReference migrateTaskMOR;
        try {
            connect(vCenterConnectionString);

            hostMOR = getDecendentMoRef(null, "HostSystem", destHostName);
            vmMOR = getDecendentMoRef(null, "VirtualMachine", vmName);

            if (vmMOR == null || hostMOR == null) {
                throw new Exception("Invalid virtual machine or host specified for the VM migration.");
            }

            String vmPowerState = getStringMEProperty("VirtualMachine", vmName, "runtime.powerState");

            if (vmPowerState != null && vmPowerState.equalsIgnoreCase("powered_on")) {
                migrateTaskMOR = vimPort.migrateVM_Task(vmMOR, null, hostMOR,
                        VirtualMachineMovePriority.highPriority, VirtualMachinePowerState.poweredOn);
            } else {
                migrateTaskMOR = vimPort.migrateVM_Task(vmMOR, null, hostMOR,
                        VirtualMachineMovePriority.highPriority, VirtualMachinePowerState.poweredOff);
            }

            // Wait for the power operation to complete and return back
            String result = waitForTask(migrateTaskMOR);
            if (!result.equalsIgnoreCase("success")) {
                if (result.contains(".") || result.contains("\n")) {
                    String delims = "[.\\n]+";
                    result = result.split(delims)[0];
                }
                throw new Exception("Error during the VM migration." + result);
            }
        } catch (Exception ex) {
            throw new VMwareConnectionException(ex);
        } finally {
            disconnect();
        }
    }

    /**
     * Added By: Sudhir on June 15, 2012
     *
     * Retrieves the BIOS PCR 0 value from the attestation reports for the
     * specified host
     *
     * @param hostMOR: ManagedObjectReference for the host
     * @return : String containing the BIOS PCR 0 value.
     * @throws Exception
     */
    /*public String getHostBIOSPCRHash(ManagedObjectReference hostMOR, String hostName) throws VMwareConnectionException {
        String biosPCRHash = "";
        HostTpmDigestInfo[] pcrList;

        try {
            boolean tpmSupport = Boolean.parseBoolean(getMEProperty(hostMOR.type, hostName, "capability.tpmSupported").toString());
            if (tpmSupport == true && serviceContent.getAbout().getVersion().contains("5.1")) {
                HostTpmAttestationReport hostTrustReport = vimPort.queryTpmAttestationReport(hostMOR);
                if (hostTrustReport != null) {
                    pcrList = hostTrustReport.getTpmPcrValues();
                    for (int k = 0; k < pcrList.length; k++) {
                        HostTpmDigestInfo pcrInfo = (HostTpmDigestInfo) pcrList[k];
                        switch (pcrInfo.getPcrNumber()) {
                            case 0:
                                biosPCRHash = byteArrayToHexString(pcrInfo.getDigestValue());
                                break;
                            default:
                                break;
                        }
                    }
                }
            } else if (tpmSupport == true && serviceContent.getAbout().getVersion().contains("5.0")) {
                // Refresh the runtime information
                HostRuntimeInfo runtimeInfo = (HostRuntimeInfo) getMEProperty(hostMOR.type, hostName, "runtime");

                // Now process the digest information
                pcrList = runtimeInfo.getTpmPcrValues();
                for (int k = 0; k < pcrList.length; k++) {
                    HostTpmDigestInfo pcrInfo = (HostTpmDigestInfo) pcrList[k];
                    switch (pcrInfo.getPcrNumber()) {
                        case 0:
                            biosPCRHash = byteArrayToHexString(pcrInfo.getDigestValue());
                            break;
                        default:
                            break;
                    }
                }
            } else {
                // Since the host does not support TPM, we will not have these values.
                biosPCRHash = "";
            }
        } catch (Exception ex) {
            throw new VMwareConnectionException(ex);
        }
        return biosPCRHash;
    }*/

    /**
     * @deprecated just an adapter for now ; VmwareHostAgent uses
     * getHostAttestationReport(MOR,HostName,PcrList)
     * @param hostObj
     * @param pcrList
     * @return
     * @throws VMwareConnectionException
     */
    public String getHostAttestationReport(TxtHostRecord hostObj, String pcrList) throws VMwareConnectionException {
        ManagedObjectReference hostMOR = getDecendentMoRef(null, "HostSystem", hostObj.HostName);
        if (hostMOR == null) {
            throw new VMwareConnectionException("Host specified does not exist in the vCenter.");
        }
        return getHostAttestationReport(hostMOR, hostObj.HostName, pcrList);
    }

    public boolean isModuleAttestationSupportedByVcenter(String vCenterVersion) {
        double version = Double.parseDouble(vCenterVersion.substring(0, vCenterVersion.lastIndexOf(".")));
        return (version >= MIN_VCENTER_VERSION_FOR_MODULE_ATTESTATION);
    }

    public boolean isModuleAttestationSupportedByESX(String esxVersion) {
        double version = Double.parseDouble(esxVersion.substring(0, esxVersion.lastIndexOf(".")));
        return (version >= MIN_VCENTER_VERSION_FOR_MODULE_ATTESTATION);
    }

    /**
     * Added By: Sudhir on June 18, 2012
     *
     * Retrieves the attestation report as a XML string
     *
     * @param hostName : Name of the host for which the attestation report has
     * to be retrieved
     * @param pcrList : Required PCR list separated by comma
     * @param vCenterConnectionString : Connection string to the vCenter server
     * on which the host is configured
     * @return : XML string equivalent of the attestation report.
     * @throws Exception
     */
    public String getHostAttestationReport(ManagedObjectReference hostMOR, String hostName, String pcrList) throws VMwareConnectionException {
        log.debug("getHostAttestationReport >> START");

//        boolean doNotDisconnect;

        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw;
        StringWriter sw = new StringWriter();

        try {
            // Verify if the PCRList is sent. If not set the default values.
            if (pcrList == null || pcrList.isEmpty()) {
                pcrList = "0,17,18,20";
            }

            // If we have already established a connection, we use it. This will
            // happen when this function will be called by the getHostDetailsForCluster
            // function, which would have opened the connection to vCenter server.
            /*
             if (!isConnected)
             connect(hostObj.AddOn_Connection_String);
             else
             doNotDisconnect = true;
             */

            Boolean tpmSupport = Boolean.parseBoolean(getStringMEProperty(hostMOR.type, hostName, "capability.tpmSupported"));

            // Lets create the start of the XML document
            // xtw = xof.createXMLStreamWriter(new FileWriter("c:\\temp\\nb_xml.xml"));
            xtw = xof.createXMLStreamWriter(sw);
            xtw.writeStartDocument();
            xtw.writeStartElement("Host_Attestation_Report");
            xtw.writeAttribute("Host_Name", hostName);
            xtw.writeAttribute("vCenterVersion", serviceContent.getAbout().getVersion());
            String hostVer = getStringMEProperty(hostMOR.type, hostName, "config.product.version");
            xtw.writeAttribute("HostVersion", hostVer);
            xtw.writeAttribute("TXT_Support", tpmSupport.toString());

            // if (tpmSupport == true && serviceContent.getAbout().getVersion().contains("5.1")) {
            if (tpmSupport == true && (isModuleAttestationSupportedByVcenter(getVCenterVersion()))) {
                log.debug("Querying TPM attestation report...");
                HostTpmAttestationReport hostTrustReport = vimPort.queryTpmAttestationReport(hostMOR);
                log.debug("Query finished.");

                // Process the event log only for the ESXi 5.1 or higher
                // if (hostTrustReport != null && hostVer.contains("5.1")) {
                if (hostTrustReport != null && hostVer != null && (isModuleAttestationSupportedByESX(hostVer))) {
                    log.debug("Retrieving TPM events...");
                    int numOfEvents = hostTrustReport.getTpmEvents().length;
                    for (int k = 0; k < numOfEvents; k++) {
                        log.debug("");
                        HostTpmEventLogEntry eventInfo = (HostTpmEventLogEntry) hostTrustReport.getTpmEvents()[k];
                        switch (eventInfo.getPcrIndex()) {
                            // We will process only the components that gets extended into PCR 19. We
                            // will ignore the rest of the event entries.
                            case 0:
                                break;
                            case 17:
                                break;
                            case 18:
                                break;
                            case 20:
                                break;
                            // All the static components hash values are in this index. So, we will process
                            // all the entries and store them into the database.
                            case 19:
                                log.debug("PCR 19: writing attributes...");
                                String eventName = eventInfo.getEventDetails().getClass().getSimpleName();
                                if (eventName.equalsIgnoreCase("HostTpmSoftwareComponentEventDetails")) {
                                    HostTpmSoftwareComponentEventDetails swEventLog = (HostTpmSoftwareComponentEventDetails) eventInfo.getEventDetails();
                                    xtw.writeStartElement("EventDetails");
                                    xtw.writeAttribute("EventName", "Vim25Api.HostTpmSoftwareComponentEventDetails");
                                    //xtw.writeAttribute("ComponentName", swEventLog.getComponentName());
                                    // Bug Fix #491 set componentName == to packageName - packageVersion
                                    //              instead of componentName
                                    // Bug: 931: To uniquely identify the component name we need to use a combination of the component name without the autogenerated version,
                                    // VIB name and VIB version. First let us trim the component name to remove the autogenerated version.
                                    String compName = swEventLog.getComponentName().substring(0, swEventLog.getComponentName().lastIndexOf("."));
                                    xtw.writeAttribute("ComponentName", compName+ "-" + swEventLog.getVibName()+ "-" +swEventLog.getVibVersion());
                                    xtw.writeAttribute("DigestValue", byteArrayToHexString(swEventLog.getDataHash()));
                                    xtw.writeAttribute("ExtendedToPCR", String.valueOf(eventInfo.getPcrIndex()));
                                    xtw.writeAttribute("PackageName", swEventLog.getVibName());
                                    xtw.writeAttribute("PackageVendor", swEventLog.getVibVendor());
                                    xtw.writeAttribute("PackageVersion", swEventLog.getVibVersion());
                                    xtw.writeAttribute("UseHostSpecificDigest", "False");
                                    xtw.writeEndElement();
                                } else if (eventName.equalsIgnoreCase("HostTpmOptionEventDetails")) {
                                    HostTpmOptionEventDetails optEventLog = (HostTpmOptionEventDetails) eventInfo.getEventDetails();
                                    xtw.writeStartElement("EventDetails");
                                    xtw.writeAttribute("EventName", "Vim25Api.HostTpmOptionEventDetails");
                                    xtw.writeAttribute("ComponentName", optEventLog.getOptionsFileName());
                                    xtw.writeAttribute("DigestValue", byteArrayToHexString(optEventLog.getDataHash()));
                                    xtw.writeAttribute("ExtendedToPCR", String.valueOf(eventInfo.getPcrIndex()));
                                    xtw.writeAttribute("PackageName", "");
                                    xtw.writeAttribute("PackageVendor", "");
                                    xtw.writeAttribute("PackageVersion", "");
                                    xtw.writeAttribute("UseHostSpecificDigest", "False");
                                    xtw.writeEndElement();
                                } else if (eventName.equalsIgnoreCase("HostTpmBootSecurityOptionEventDetails")) {
                                    HostTpmBootSecurityOptionEventDetails bootEventLog = (HostTpmBootSecurityOptionEventDetails) eventInfo.getEventDetails();
                                    xtw.writeStartElement("EventDetails");
                                    xtw.writeAttribute("EventName", "Vim25Api.HostTpmBootSecurityOptionEventDetails");
                                    xtw.writeAttribute("ComponentName", bootEventLog.getBootSecurityOption());
                                    xtw.writeAttribute("DigestValue", byteArrayToHexString(bootEventLog.getDataHash()));
                                    xtw.writeAttribute("ExtendedToPCR", String.valueOf(eventInfo.getPcrIndex()));
                                    xtw.writeAttribute("PackageName", "");
                                    xtw.writeAttribute("PackageVendor", "");
                                    xtw.writeAttribute("PackageVersion", "");
                                    xtw.writeAttribute("UseHostSpecificDigest", "False");
                                    xtw.writeEndElement();
                                } else if (eventName.equalsIgnoreCase("HostTpmCommandEventDetails")) {
                                    HostTpmCommandEventDetails cmdEventLog = (HostTpmCommandEventDetails) eventInfo.getEventDetails();
                                    xtw.writeStartElement("EventDetails");
                                    xtw.writeAttribute("EventName", "Vim25Api.HostTpmCommandEventDetails");
                                    // We should not store the actual command line data here since it is host specific.
                                    xtw.writeAttribute("ComponentName", ""); //cmdEventLog.commandLine);
                                    xtw.writeAttribute("DigestValue", byteArrayToHexString(cmdEventLog.getDataHash()));
                                    xtw.writeAttribute("ExtendedToPCR", String.valueOf(eventInfo.getPcrIndex()));
                                    xtw.writeAttribute("PackageName", "");
                                    xtw.writeAttribute("PackageVendor", "");
                                    xtw.writeAttribute("PackageVersion", "");
                                    xtw.writeAttribute("UseHostSpecificDigest", "True");
                                    xtw.writeAttribute("HostName", hostName);
                                    xtw.writeEndElement();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }

                // The TPM values have to be captured for both ESXi 5.0 or 5.1 hosts
                if (hostTrustReport != null) {
                    log.debug("Getting TPM PCR values...");
                    List<String> pcrs = Arrays.asList(pcrList.split(","));
                    int numTPMValues = hostTrustReport.getTpmPcrValues().length;
                    for (int j = 0; j < numTPMValues; j++) {
                        HostTpmDigestInfo pcrInfo = hostTrustReport.getTpmPcrValues()[j];
                        if (pcrs.contains(String.valueOf(pcrInfo.getPcrNumber()))) {
                            xtw.writeStartElement("PCRInfo");
                            xtw.writeAttribute("ComponentName", String.valueOf(pcrInfo.getPcrNumber()));
                            xtw.writeAttribute("DigestValue", byteArrayToHexString(pcrInfo.getDigestValue()));
                            xtw.writeEndElement();
                        }
                    }
                }
            } else if (tpmSupport == true && serviceContent.getAbout().getVersion().contains("5.0")) {
                // Refresh the runtime information
                HostRuntimeInfo runtimeInfo = (HostRuntimeInfo) getMEProperty(hostMOR.type, hostName, "runtime");
                if( runtimeInfo != null ) {
                // Now process the digest information
                List<String> pcrs = Arrays.asList(pcrList.split(","));
                int numTPMValues = runtimeInfo.getTpmPcrValues().length;
                for (int j = 0; j < numTPMValues; j++) {
                    HostTpmDigestInfo pcrInfo = runtimeInfo.getTpmPcrValues()[j];
                    if (pcrs.contains(String.valueOf(pcrInfo.getPcrNumber()))) {
                        xtw.writeStartElement("PCRInfo");
                        xtw.writeAttribute("ComponentName", String.valueOf(pcrInfo.getPcrNumber()));
                        xtw.writeAttribute("DigestValue", byteArrayToHexString(pcrInfo.getDigestValue()));
                        xtw.writeEndElement();
                    }
                }
                }
            } else {
                xtw.writeStartElement("PCRInfo");
                xtw.writeAttribute("Error", "Host does not support TPM.");
                xtw.writeEndElement();
            }

            xtw.writeEndElement();
            xtw.writeEndDocument();
            xtw.flush();
            xtw.close();
            String attestationReport = sw.toString();
            return attestationReport;
        } catch (RemoteException | XMLStreamException ex) {
            throw new VMwareConnectionException(ex);
        } finally {/*
             if (!doNotDisconnect)
             disconnect();*/
            log.debug("getHostAttestationReport >> FINISH");
        }
    }


    // <editor-fold defaultstate="collapsed" desc="Code copied from VMware SDK's VMPowerOps.Java file.">
    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method
     *
     * @param listpfs
     * @return list of object content
     * @throws Exception
     */
    private List<ObjectContent> retrievePropertiesAllObjects(List<PropertyFilterSpec> listpfs)
            throws VMwareConnectionException {
        
        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();
        List<ObjectContent> listobjcontent = new ArrayList<>();

        try {
            RetrieveResult rslts = vimPort.retrievePropertiesEx(propCollectorRef, (PropertyFilterSpec[])listpfs.toArray(), propObjectRetrieveOpts);
            if (rslts != null && rslts.getObjects() != null && rslts.getObjects().length != 0) {
                listobjcontent = Arrays.asList(rslts.objects);
            }
            String token = null;
            if (rslts != null && rslts.getToken() != null) {
                token = rslts.getToken();
            }
            while (token != null && !token.isEmpty()) {
                rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
                token = null;
                if (rslts != null) {
                    token = rslts.getToken();
                    if (rslts.getObjects() != null && rslts.getObjects().length != 0) {
                        listobjcontent = Arrays.asList(rslts.getObjects());
                    }
                }
            }
        } catch (Exception e) {
            throw new VMwareConnectionException(e);
        }

        return listobjcontent;
    }

    /**
     * This code takes an array of [typename, property, property, ...] and
     * converts it into a PropertySpec[]. handles case where multiple references
     * to the same typename are specified.
     *
     * @param typeinfo 2D array of type and properties to retrieve
     *
     * @return Array of container filter specs
     */
    /*
    private List<PropertySpec> buildPropertySpecArray(String[][] typeinfo) {
        // Eliminate duplicates
        HashMap<String, Set> tInfo = new HashMap<String, Set>();
        for (int ti = 0; ti < typeinfo.length; ++ti) {
            Set props = (Set) tInfo.get(typeinfo[ti][0]);
            if (props == null) {
                props = new HashSet<String>();
                tInfo.put(typeinfo[ti][0], props);
            }
            boolean typeSkipped = false;
            for (int pi = 0; pi < typeinfo[ti].length; ++pi) {
                String prop = typeinfo[ti][pi];
                if (typeSkipped) {
                    props.add(prop);
                } else {
                    typeSkipped = true;
                }
            }
        }

        // Create PropertySpecs
        ArrayList<PropertySpec> pSpecs = new ArrayList<PropertySpec>();
        for (Iterator<String> ki = tInfo.keySet().iterator(); ki.hasNext();) {
            String type = (String) ki.next();
            PropertySpec pSpec = new PropertySpec();
            Set props = (Set) tInfo.get(type);
            pSpec.setType(type);
            pSpec.setAll(props.isEmpty() ? Boolean.TRUE : Boolean.FALSE);
            //for (Iterator pi = props.iterator(); pi.hasNext();) {
                //String prop = (String) pi.next();
                //pSpec.getPathSet().equals(prop);
            //}
            pSpecs.add(pSpec);
        }

        return pSpecs;
    }*/

    /**
     * Retrieve content recursively with multiple properties. the typeinfo array
     * contains typename + properties to retrieve.
     *
     * @param collector a property collector if available or null for default
     * @param root a root folder if available, or null for default
     * @param typeinfo 2D array of properties for each typename
     * @param recurse retrieve contents recursively from the root down
     *
     * @return retrieved object contents
     */
    private List<ObjectContent> getContentsRecursively(ManagedObjectReference collector,
            ManagedObjectReference root,
            String[][] typeinfo, boolean recurse)
            throws VMwareConnectionException {
        if (typeinfo == null || typeinfo.length == 0) {
            return null;
        }

        ManagedObjectReference usecoll = collector;
        if (usecoll == null) {
            usecoll = serviceContent.getPropertyCollector();
            if (usecoll != null) {
                log.debug("usecoll = " + usecoll.getType());
            }
        }

        ManagedObjectReference useroot = root;
        if (useroot == null) {
            useroot = serviceContent.getRootFolder();
        }

        //List<SelectionSpec> selectionSpecs = new ArrayList<>();
        //if (recurse) {
        //    selectionSpecs = buildFullTraversal();
        //}
    
        //List<PropertySpec> propspecary = buildPropertySpecArray(typeinfo);
        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(useroot);
        objSpec.setSkip(Boolean.FALSE);
        //objSpec.getSelectSet().equals((PropertySpec[])selectionSpecs.toArray()); 
        List<ObjectSpec> objSpecList = new ArrayList<>();
        objSpecList.add(objSpec);
        PropertyFilterSpec spec = new PropertyFilterSpec();
        //spec.getPropSet().equals(propspecary); 
        //spec.getObjectSet().equals(objSpecList); 
        List<PropertyFilterSpec> listpfs = new ArrayList<>();
        listpfs.add(spec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

        return listobjcont;
    }

    private boolean typeIsA(String searchType,
            String foundType) {
        if (searchType.equals(foundType)) {
            return true;
        } else if (searchType.equals("ManagedEntity")) {
            for (int i = 0; i < meTree.length; ++i) {
                if (meTree[i].equals(foundType)) {
                    return true;
                }
            }
        } else if (searchType.equals("ComputeResource")) {
            for (int i = 0; i < crTree.length; ++i) {
                if (crTree[i].equals(foundType)) {
                    return true;
                }
            }
        } else if (searchType.equals("HistoryCollector")) {
            for (int i = 0; i < hcTree.length; ++i) {
                if (hcTree[i].equals(foundType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the ManagedObjectReference for an item under the specified root
     * folder that has the type and name specified.
     *
     * @param root a root folder if available, or null for default
     * @param type type of the managed object
     * @param name name to match
     *
     * @return First ManagedObjectReference of the type / name pair found
     */
    private ManagedObjectReference getDecendentMoRef(ManagedObjectReference root,
            String type,
            String name)
            throws VMwareConnectionException {
        if (name == null || name.length() == 0) {
            return null;
        }

        String[][] typeinfo =
                new String[][]{new String[]{type, "name"},};

        List<ObjectContent> ocary =
                getContentsRecursively(null, root, typeinfo, true);

        if (ocary == null || ocary.size() == 0) {
            return null;
        }

        ObjectContent oc;
        ManagedObjectReference mor = null;
        List<DynamicProperty> propary;
        String propval;
        boolean found = false;
        for (int oci = 0; oci < ocary.size() && !found; oci++) {
            oc = ocary.get(oci);
            mor = oc.getObj();
            DynamicProperty[] temp_propary = oc.getPropSet();
            propary = Arrays.asList(temp_propary);

            propval = null;
            if (type == null || typeIsA(type, mor.getType())) {
                if (propary != null && !propary.isEmpty()) {
                    propval = (String) propary.get(0).getVal();
                }
                found = propval != null && name.equals(propval);
            }
        }

        if (!found) {
            mor = null;
        }

        return mor;
    }

    private String getProp(ManagedObjectReference obj,
            String prop) throws VMwareConnectionException {
        String propVal = null;
        try {
            List<DynamicProperty> dynaProArray = getDynamicProarray(obj, obj.getType(), prop);
            if (dynaProArray != null && !dynaProArray.isEmpty()) {
                if (dynaProArray.get(0).getVal() != null) {
                    propVal = (String) dynaProArray.get(0).getVal();
                }
            }
        } catch (Exception e) {
            throw new VMwareConnectionException(e);
        }
        return propVal;
    }

    private ArrayList filterMOR(ArrayList mors,
            String[][] filter)
            throws VMwareConnectionException {
        ArrayList filteredmors =
                new ArrayList();
        for (int i = 0; i < mors.size(); i++) {
            for (int k = 0; k < filter.length; k++) {
                String prop = filter[k][0];
                String reqVal = filter[k][1];
                String value = getProp(((ManagedObjectReference) mors.get(i)), prop);
                if (value == null && reqVal == null) {
                    continue;
                } else if (value == null && reqVal != null) {
                    k = filter.length + 1;
                } else if (value != null && value.equalsIgnoreCase(reqVal)) {
                    filteredmors.add(mors.get(i));
                } else {
                    k = filter.length + 1;
                }
            }
        }
        return filteredmors;
    }

    private ArrayList getDecendentMoRefs(ManagedObjectReference root,
            String type,
            String[][] filter)
            throws VMwareConnectionException {
        String[][] typeinfo = new String[][]{new String[]{type, "name"},};

        List<ObjectContent> ocary =
                getContentsRecursively(null, root, typeinfo, true);

        ArrayList refs = new ArrayList();

        if (ocary == null || ocary.size() == 0) {
            return refs;
        }

        for (int oci = 0; oci < ocary.size(); oci++) {
            refs.add(ocary.get(oci).getObj());
        }

        if (filter != null) {
            ArrayList filtermors = filterMOR(refs, filter);
            return filtermors;
        } else {
            return refs;
        }
    }

    /**
     *
     * @return TraversalSpec specification to get to the VirtualMachine managed
     * object.
     */
    /* not used ...
     private TraversalSpec getVMTraversalSpec() {
     // Create a traversal spec that starts from the 'root' objects
     // and traverses the inventory tree to get to the VirtualMachines.
     // Build the traversal specs bottoms up

     //Traversal to get to the VM in a VApp
     TraversalSpec vAppToVM = new TraversalSpec();
     vAppToVM.setName("vAppToVM");
     vAppToVM.setType("VirtualApp");
     vAppToVM.setPath("vm");

     //Traversal spec for VApp to VApp
     TraversalSpec vAppToVApp = new TraversalSpec();
     vAppToVApp.setName("vAppToVApp");
     vAppToVApp.setType("VirtualApp");
     vAppToVApp.setPath("resourcePool");
     //SelectionSpec for VApp to VApp recursion
     SelectionSpec vAppRecursion = new SelectionSpec();
     vAppRecursion.setName("vAppToVApp");
     //SelectionSpec to get to a VM in the VApp
     SelectionSpec vmInVApp = new SelectionSpec();
     vmInVApp.setName("vAppToVM");
     //SelectionSpec for both VApp to VApp and VApp to VM
     List<SelectionSpec> vAppToVMSS = new ArrayList<SelectionSpec>();
     vAppToVMSS.add(vAppRecursion);
     vAppToVMSS.add(vmInVApp);
     vAppToVApp.getSelectSet().addAll(vAppToVMSS);

     //This SelectionSpec is used for recursion for Folder recursion
     SelectionSpec sSpec = new SelectionSpec();
     sSpec.setName("VisitFolders");

     // Traversal to get to the vmFolder from DataCenter
     TraversalSpec dataCenterToVMFolder = new TraversalSpec();
     dataCenterToVMFolder.setName("DataCenterToVMFolder");
     dataCenterToVMFolder.setType("Datacenter");
     dataCenterToVMFolder.setPath("vmFolder");
     dataCenterToVMFolder.setSkip(false);
     dataCenterToVMFolder.getSelectSet().add(sSpec);

     // TraversalSpec to get to the DataCenter from rootFolder
     TraversalSpec traversalSpec = new TraversalSpec();
     traversalSpec.setName("VisitFolders");
     traversalSpec.setType("Folder");
     traversalSpec.setPath("childEntity");
     traversalSpec.setSkip(false);
     List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
     sSpecArr.add(sSpec);
     sSpecArr.add(dataCenterToVMFolder);
     sSpecArr.add(vAppToVM);
     sSpecArr.add(vAppToVApp);
     traversalSpec.getSelectSet().addAll(sSpecArr);
     return traversalSpec;
     }*/
    // unused
    /**
     * Get the MOR of the Virtual Machine by its name.
     *
     * @param vmName The name of the Virtual Machine
     * @return The Managed Object reference for this VM
     */
    /*
     private ManagedObjectReference getVmByVMname(String vmname) throws VMwareConnectionException {
     ManagedObjectReference retVal = null;
     try {
     TraversalSpec tSpec = getVMTraversalSpec();
     // Create Property Spec
     PropertySpec propertySpec = new PropertySpec();
     propertySpec.setAll(Boolean.FALSE);
     propertySpec.getPathSet().add("name");
     propertySpec.setType("VirtualMachine");

     // Now create Object Spec
     ObjectSpec objectSpec = new ObjectSpec();
     objectSpec.setObj(rootRef);
     objectSpec.setSkip(Boolean.TRUE);
     objectSpec.getSelectSet().add(tSpec);

     // Create PropertyFilterSpec using the PropertySpec and ObjectPec
     // created above.
     PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
     propertyFilterSpec.getPropSet().add(propertySpec);
     propertyFilterSpec.getObjectSet().add(objectSpec);

     List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
     listpfs.add(propertyFilterSpec);
     List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

     if (listobjcont != null) {
     for (ObjectContent oc : listobjcont) {
     ManagedObjectReference mr = oc.getObj();
     String vmnm = null;
     List<DynamicProperty> dps = oc.getPropSet();
     if (dps != null) {
     for (DynamicProperty dp : dps) {
     vmnm = (String) dp.getVal();
     }
     }
     if (vmnm != null && vmnm.equals(vmname)) {
     retVal = mr;
     break;
     }
     }
     }
     }  catch (Exception e) {
     throw new VMwareConnectionException(e);
     }
     return retVal;
     }*/

    /*
     * @return An array of SelectionSpec covering VM, Host, Resource pool,
     * Cluster Compute Resource and Datastore.
     */
    /*
    private List<SelectionSpec> buildFullTraversal() {
        // Terminal traversal specs

        // RP -> VM
        TraversalSpec rpToVm = new TraversalSpec();
        rpToVm.setName("rpToVm");
        rpToVm.setType("ResourcePool");
        rpToVm.setPath("vm");
        rpToVm.setSkip(Boolean.FALSE);

        // vApp -> VM
        TraversalSpec vAppToVM = new TraversalSpec();
        vAppToVM.setName("vAppToVM");
        vAppToVM.setType("VirtualApp");
        vAppToVM.setPath("vm");

        // HostSystem -> VM
        TraversalSpec hToVm = new TraversalSpec();
        hToVm.setType("HostSystem");
        hToVm.setPath("vm");
        hToVm.setName("hToVm");
        //hToVm.getSelectSet().equals(getSelectionSpec("visitFolders"));
        hToVm.setSkip(Boolean.FALSE);

        // DC -> DS
        TraversalSpec dcToDs = new TraversalSpec();
        dcToDs.setType("Datacenter");
        dcToDs.setPath("datastore");
        dcToDs.setName("dcToDs");
        dcToDs.setSkip(Boolean.FALSE);

        // Recurse through all ResourcePools
        TraversalSpec rpToRp = new TraversalSpec();
        rpToRp.setType("ResourcePool");
        rpToRp.setPath("resourcePool");
        rpToRp.setSkip(Boolean.FALSE);
        rpToRp.setName("rpToRp");
        List<SelectionSpec> respools = new ArrayList<SelectionSpec>();
        respools.add(getSelectionSpec("rpToRp"));
        respools.add(getSelectionSpec("rpToVm"));
        //rpToRp.getSelectSet().equals((SelectionSpec[])respools.toArray());

        TraversalSpec crToRp = new TraversalSpec();
        crToRp.setType("ComputeResource");
        crToRp.setPath("resourcePool");
        crToRp.setSkip(Boolean.FALSE);
        crToRp.setName("crToRp");
        List<SelectionSpec> crrp = new ArrayList<SelectionSpec>();
        crrp.add(getSelectionSpec("rpToRp"));
        crrp.add(getSelectionSpec("rpToVm"));
        //crToRp.getSelectSet().equals((SelectionSpec[])crrp.toArray());

        TraversalSpec crToH = new TraversalSpec();
        crToH.setSkip(Boolean.FALSE);
        crToH.setType("ComputeResource");
        crToH.setPath("host");
        crToH.setName("crToH");

        TraversalSpec dcToHf = new TraversalSpec();
        dcToHf.setSkip(Boolean.FALSE);
        dcToHf.setType("Datacenter");
        dcToHf.setPath("hostFolder");
        dcToHf.setName("dcToHf");
        //dcToHf.getSelectSet().equals(getSelectionSpec("visitFolders"));

        TraversalSpec vAppToRp = new TraversalSpec();
        vAppToRp.setName("vAppToRp");
        vAppToRp.setType("VirtualApp");
        vAppToRp.setPath("resourcePool");
        //vAppToRp.getSelectSet().equals(getSelectionSpec("rpToRp"));

        TraversalSpec dcToVmf = new TraversalSpec();
        dcToVmf.setType("Datacenter");
        dcToVmf.setSkip(Boolean.FALSE);
        dcToVmf.setPath("vmFolder");
        dcToVmf.setName("dcToVmf");
        //dcToVmf.getSelectSet().equals(getSelectionSpec("visitFolders"));

        // For Folder -> Folder recursion
        TraversalSpec visitFolders = new TraversalSpec();
        visitFolders.setType("Folder");
        visitFolders.setPath("childEntity");
        visitFolders.setSkip(Boolean.FALSE);
        visitFolders.setName("visitFolders");
        List<SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
        sspecarrvf.add(getSelectionSpec("visitFolders"));
        sspecarrvf.add(getSelectionSpec("dcToVmf"));
        sspecarrvf.add(getSelectionSpec("dcToHf"));
        sspecarrvf.add(getSelectionSpec("dcToDs"));
        sspecarrvf.add(getSelectionSpec("crToRp"));
        sspecarrvf.add(getSelectionSpec("crToH"));
        sspecarrvf.add(getSelectionSpec("hToVm"));
        sspecarrvf.add(getSelectionSpec("rpToVm"));
        sspecarrvf.add(getSelectionSpec("rpToRp"));
        sspecarrvf.add(getSelectionSpec("vAppToRp"));
        sspecarrvf.add(getSelectionSpec("vAppToVM"));

        //visitFolders.getSelectSet().equals((SelectionSpec[])sspecarrvf.toArray());

        List<SelectionSpec> resultspec = new ArrayList<SelectionSpec>();
        resultspec.add(visitFolders);
        resultspec.add(dcToVmf);
        resultspec.add(dcToHf);
        resultspec.add(dcToDs);
        resultspec.add(crToRp);
        resultspec.add(crToH);
        resultspec.add(hToVm);
        resultspec.add(rpToVm);
        resultspec.add(vAppToRp);
        resultspec.add(vAppToVM);
        resultspec.add(rpToRp);

        return resultspec;
    }*/

    /*
    private SelectionSpec getSelectionSpec(String name) {
        SelectionSpec genericSpec = new SelectionSpec();
        genericSpec.setName(name);
        return genericSpec;
    }*/

    private List<DynamicProperty> getDynamicProarray(ManagedObjectReference ref,
            String type,
            String propertyString)
            throws VMwareConnectionException {
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        //propertySpec.getPathSet().equals(propertyString);
        propertySpec.setType(type);

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(ref);
        objectSpec.setSkip(Boolean.FALSE);
        //objectSpec.getSelectSet().equals((SelectionSpec[])buildFullTraversal().toArray());
        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        //propertyFilterSpec.getPropSet().equals(propertySpec);
        //propertyFilterSpec.getObjectSet().equals(objectSpec);
        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
        ObjectContent contentObj = listobjcont.get(0);
        List<DynamicProperty> objList = Arrays.asList(contentObj.getPropSet());
        return objList;
    }

    // unused
   /*
     private boolean getTaskInfo(ManagedObjectReference taskmor)
     throws VMwareConnectionException {
     boolean valid = false;
     String res = waitForTask(taskmor);
     if(res.equalsIgnoreCase("success")) {
     valid = true;
     } else {
     valid = false;
     }
     return valid;
     }
     */
    private void updateValues(List<String> props,
            Object[] vals,
            PropertyChange propchg) {
        for (int findi = 0; findi < props.size(); findi++) {
            if (propchg.getName().lastIndexOf(props.get(findi)) >= 0) {
                if (propchg.getOp() == PropertyChangeOp.remove) {
                    vals[findi] = "";
                } else {
                    vals[findi] = propchg.getVal();
                }
            }
        }
    }

    /**
     * Handle Updates for a single object. waits till expected values of
     * properties to check are reached Destroys the ObjectFilter when done.
     *
     * @param objmor MOR of the Object to wait for </param>
     * @param filterProps Properties list to filter
     * @param endWaitProps Properties list to check for expected values these be
     * properties of a property in the filter properties list
     * @param expectedVals values for properties to end the wait
     * @return true indicating expected values were met, and false otherwise
     */
    private Object[] waitForValues(ManagedObjectReference objmor,
            List<String> filterProps,
            List<String> endWaitProps,
            Object[][] expectedVals)
            throws VMwareConnectionException {
        // version string is initially null
        String version = "";
        Object[] endVals = new Object[endWaitProps.size()];
        Object[] filterVals = new Object[filterProps.size()];
        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(objmor);
        objSpec.setSkip(Boolean.FALSE);
        PropertyFilterSpec spec = new PropertyFilterSpec();
        //spec.getObjectSet().equals(objSpec);
        PropertySpec propSpec = new PropertySpec();
        //propSpec.getPathSet().equals((String[])filterProps.toArray());
        propSpec.setType(objmor.getType());
        //spec.getPropSet().equals(propSpec);

        ManagedObjectReference filterSpecRef;
        try {
            filterSpecRef = vimPort.createFilter(propCollectorRef, spec, true);
        } catch (Exception e) {
            throw new VMwareConnectionException(e);
        }

        boolean reached = false;

        UpdateSet updateset = null;
        List<PropertyFilterUpdate> filtupary;
        PropertyFilterUpdate filtup;
        List<ObjectUpdate> objupary;
        ObjectUpdate objup;
        List<PropertyChange> propchgary;
        PropertyChange propchg;
        while (!reached) {
            boolean retry = true;
            while (retry) {
                try {
                    updateset =
                            vimPort.waitForUpdates(propCollectorRef, version);
                    retry = false;
                } catch (Exception e) {
                    throw new VMwareConnectionException(e);
                }
            }
            if (updateset != null) {
                version = updateset.getVersion();
            }
            if (updateset == null || updateset.getFilterSet() == null) {
                continue;
            }

            // Make this code more general purpose when PropCol changes later.
            filtupary = Arrays.asList(updateset.getFilterSet());
            for (int fi = 0; fi < filtupary.size(); fi++) {
                filtup = filtupary.get(fi);
                objupary = Arrays.asList(filtup.getObjectSet());
                for (int oi = 0; oi < objupary.size(); oi++) {
                    objup = objupary.get(oi);
                    if (objup.getKind() == ObjectUpdateKind.modify
                            || objup.getKind() == ObjectUpdateKind.enter
                            || objup.getKind() == ObjectUpdateKind.leave) {
                        propchgary = Arrays.asList(objup.getChangeSet());
                        for (int ci = 0; ci < propchgary.size(); ci++) {
                            propchg = propchgary.get(ci);
                            updateValues(endWaitProps, endVals, propchg);
                            updateValues(filterProps, filterVals, propchg);
                        }
                    }
                }
            }

            Object expctdval;
            // Check if the expected values have been reached and exit the loop if done.
            // Also exit the WaitForUpdates loop if this is the case.
            for (int chgi = 0; chgi < endVals.length && !reached; chgi++) {
                for (int vali = 0; vali < expectedVals[chgi].length && !reached; vali++) {
                    expctdval = expectedVals[chgi][vali];
                    reached = expctdval.equals(endVals[chgi]) || reached;
                }
            }
        }

        // Destroy the filter when we are done.
        try {
            vimPort.destroyPropertyFilter(filterSpecRef);
        } catch (Exception e) {
            throw new VMwareConnectionException(e);
        }

        return filterVals;
    }

    private String waitForTask(ManagedObjectReference taskmor) throws VMwareConnectionException {
        List<String> infoList = new ArrayList<String>();
        infoList.add("info.state");
        infoList.add("info.error");
        List<String> stateList = new ArrayList<String>();
        stateList.add("state");
        Object[] result = waitForValues(
                taskmor, infoList, stateList,
                new Object[][]{new Object[]{
                TaskInfoState.success, TaskInfoState.error}});
        if (result[0].equals(TaskInfoState.success)) {
            return "success";
        } else {
            List<DynamicProperty> tinfoProps;
            tinfoProps = getDynamicProarray(taskmor, "Task", "info");
            TaskInfo tinfo = (TaskInfo) tinfoProps.get(0).getVal();
            LocalizedMethodFault fault = tinfo.getError();
            String error = "Error Occured";
            if (fault != null) {
                error = fault.getLocalizedMessage();
                log.error("Message " + fault.getLocalizedMessage());
            }
            return error;
        }
    }
// </editor-fold>
}
