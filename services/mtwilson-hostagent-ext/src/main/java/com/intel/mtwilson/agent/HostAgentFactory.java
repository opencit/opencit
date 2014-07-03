/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent;

import com.intel.mtwilson.My;
//import com.intel.mtwilson.agent.intel.IntelHostAgentFactory;
//import com.intel.mtwilson.agent.vmware.VmwareHostAgentFactory;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.model.InternetAddress;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.tls.policy.TlsPolicyFactory;
//import com.intel.dcsg.cpg.tls.policy.TrustCaAndVerifyHostnameTlsPolicy;
//import com.intel.dcsg.cpg.tls.policy.TrustFirstCertificateTlsPolicy;
//import com.intel.dcsg.cpg.tls.policy.TrustKnownCertificateTlsPolicy;
import java.io.IOException;
import java.security.KeyManagementException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.intel.mtwilson.agent.citrix.CitrixHostAgentFactory;
//import com.intel.mtwilson.agent.citrix.CitrixHostAgent;
import com.intel.mtwilson.datatypes.ConnectionString;
import java.util.HashMap;
import java.util.List;

/**
 * Use this class to instantiate the appropriate agent or client for a given
 * host. It looks primarily at the "AddOn_Connection_String".
 * 
 * Use of the TblHosts object: getName(), getPort(), getTlsPolicyName(), 
 * getTlsPolicyResource().
 * 
 * @throws UnuspportedOperationException if the appropriate agent type cannot be determined from the given host
 * @author jbuhacoff
 */
public class HostAgentFactory {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Map<String,VendorHostAgentFactory> vendorFactoryMap = new HashMap<String,VendorHostAgentFactory>();
    private TlsPolicyFactory tlsPolicyFactory = TlsPolicyFactory.getInstance();
    
    public HostAgentFactory() {
//        vendorFactoryMap.put(Vendor.INTEL, new IntelHostAgentFactory());
//        vendorFactoryMap.put(Vendor.CITRIX, new CitrixHostAgentFactory());
//        vendorFactoryMap.put(Vendor.VMWARE, new VmwareHostAgentFactory());
        List<VendorHostAgentFactory> vendorHostAgentFactories = Extensions.findAll(VendorHostAgentFactory.class);
        for(VendorHostAgentFactory vendorHostAgentFactory : vendorHostAgentFactories) {
            vendorFactoryMap.put(vendorHostAgentFactory.getVendorProtocol(), vendorHostAgentFactory);
            
        }
    }
    
    /**
     * It is recommended to supply an EnumMap instance
     * @param map of vendor host agent factories
     */
    public void setVendorFactoryMap(Map<String,VendorHostAgentFactory> map) {
        vendorFactoryMap = map;
    }
    
    
    /**
     * @deprecated in mtwilson 2.0, use implementations of HostAgent instead
     * @param host
     * @return 
     */
    public PcrManifest getManifest(TblHosts host) {
        try {
            InternetAddress hostAddress = new InternetAddress(host.getName());
            String connectionString = getConnectionString(host);
            String tlsPolicyName = host.getTlsPolicyName() == null ? My.configuration().getDefaultTlsPolicyName() : host.getTlsPolicyName(); // txtHost.getTlsPolicy();  
//            ByteArrayResource resource = new ByteArrayResource(host.getTlsKeystore() == null ? new byte[0] : host.getTlsKeystore());
            String password = My.configuration().getTlsKeystorePassword(); 
            SimpleKeystore tlsKeystore = new SimpleKeystore(host.getTlsKeystoreResource(), password); 
            TlsPolicy tlsPolicy = tlsPolicyFactory.getTlsPolicyWithKeystore(tlsPolicyName, tlsKeystore);
            HostAgent hostAgent = getHostAgent(hostAddress, connectionString, tlsPolicy);
            PcrManifest pcrManifest = hostAgent.getPcrManifest();
//            host.setTlsKeystore(resource.toByteArray()); // if the tls policy is TRUST_FIRST_CERTIFICATE then it's possible a new cert has been saved in it and we have to make sure it gets saved to the host record;  for all other tls policies there would be no change so this is a no-op -  the byte array will be the same as the one we started with
            return pcrManifest;
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Cannot get manifest for "+host.getName()+": "+e.toString(), e);
        }        
    }
    
    /**
     * BUG #497   given a host record, this method creates an appropriate HostAgent
     * object (for vmware, citrix, or intel hosts) and its TLS Policy. 
     * Currently only our vmware code implements HostAgent, need to implement it
     * also in our citrix and intel code. 
     * @param txtHost must have Name, AddOnConnectionInfo, SSLPolicy, and SSLCertificate fields set
     * @return 
     */
    public HostAgent getHostAgent(TblHosts host) {
        try {
            InternetAddress hostAddress = new InternetAddress(host.getName()); // switching from Hostname to InternetAddress (better support for both hostname and ip address)
            // here we figure out if it's vmware or intel  and ensure we have a valid connection string starting with the vendor scheme.  
            // no special case for citrix, since that support was added recently they should always come with citrix: prepended.
//            System.out.println("host cs = " + host.getAddOnConnectionInfo());
            String connectionString = getConnectionString(host);
            //System.out.println("stdalex getHostAgent cs =" + connectionString);
            log.debug("Retrieving TLS policy...");
            TlsPolicy tlsPolicy = getTlsPolicy(host);
            log.debug("Creating Host Agent for host: " + host.getName());
            HostAgent ha = getHostAgent(hostAddress, connectionString, tlsPolicy); 
            log.debug("Host Agent created.");
            return ha;
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Cannot create Host Agent for "+host.getName()+": "+e.toString(), e);
        }
    }
    
    /*
     * given a host, it returns the complete connection string starting with vendor scheme
     * 
     * @deprecated
     */
    public String getConnectionString(TblHosts host) {
        String connectionString = host.getAddOnConnectionInfo();
        if( connectionString == null || connectionString.isEmpty() ) {
            if( host.getName() != null  ) {
                connectionString = String.format("intel:https://%s:%d", host.getName(), host.getPort());
                log.debug("Assuming Intel connection string for host " + host.getName());// removed connection string to prevent leaking secrets
            }
        }
        else if( connectionString.startsWith("http") && connectionString.contains("/sdk;") ) {
            connectionString = String.format("vmware:%s", connectionString);
           log.debug("Assuming Vmware connection string for host " + host.getName()); // removed connection string to prevent leaking secrets
        }        
        return connectionString;
    }
    
    public TlsPolicy getTlsPolicy(TblHosts host) throws KeyManagementException, IOException {
        if( host.getTlsPolicyName() == null ) {
            host.setTlsPolicyName(My.configuration().getDefaultTlsPolicyName());
        }
//        ByteArrayResource resource = new ByteArrayResource(host.getTlsKeystore() == null ? new byte[0] : host.getTlsKeystore()); 
//        KeyStore tlsKeystore = txtHost.getTlsKeystore(); 
        TlsPolicy tlsPolicy = tlsPolicyFactory.getTlsPolicyWithKeystore(host.getTlsPolicyName(), host.getTlsKeystoreResource());
        return tlsPolicy;
    }

    /**
     * 
     * @param connectionString what is also known as the "AddOn_Connection_String", in the form vendor:url, for example vmware:https://vcenter.com/sdk;Administrator;password
     * @return 
     */
    private HostAgent getHostAgent(InternetAddress hostAddress, String connectionString, TlsPolicy tlsPolicy) throws IOException {
        if( connectionString == null ) {
            throw new IllegalArgumentException("Connection info missing"); 
        }
        ConnectionString cs = new ConnectionString(connectionString);
        String vendorProtocol = cs.getVendor().name().toLowerCase(); // INTEL, CITRIX, VMWARE becomes intel, citrix, vmware
        if( vendorFactoryMap.containsKey(vendorProtocol) ) {
            VendorHostAgentFactory factory = vendorFactoryMap.get(vendorProtocol);
            if( factory != null ) {
                return factory.getHostAgent(hostAddress, cs.getConnectionString(), tlsPolicy);
            }
        }
        log.error("HostAgentFactory: Unsupported host type: "+vendorProtocol);
        throw new UnsupportedOperationException("Unsupported host type: "+vendorProtocol);
    }
    
    
    public HostAgent getHostAgent(ConnectionString hostConnection, TlsPolicy tlsPolicy) throws IOException {
        if( hostConnection == null ) {
            throw new IllegalArgumentException("Connection info missing"); 
        }
        String vendorProtocol = hostConnection.getVendor().name().toLowerCase();
        if( vendorFactoryMap.containsKey(vendorProtocol) ) { // intel, citrix, vmware
            VendorHostAgentFactory factory = vendorFactoryMap.get(vendorProtocol);
                if( factory != null ) {
                    return factory.getHostAgent(hostConnection.getConnectionString(), tlsPolicy);
                }
        }
        throw new UnsupportedOperationException("No agent factory registered for this host");
    }
    
}
