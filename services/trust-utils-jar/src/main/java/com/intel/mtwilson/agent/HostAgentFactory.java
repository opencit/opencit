/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent;

import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.intel.IntelHostAgentFactory;
import com.intel.mtwilson.agent.vmware.VmwareHostAgentFactory;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.mtwilson.model.InternetAddress;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.KeystoreCertificateRepository;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.tls.policy.impl.FirstCertificateTrustDelegate;
import com.intel.mtwilson.tls.policy.TlsPolicyFactory;
//import com.intel.dcsg.cpg.tls.policy.TrustCaAndVerifyHostnameTlsPolicy;
//import com.intel.dcsg.cpg.tls.policy.TrustFirstCertificateTlsPolicy;
//import com.intel.dcsg.cpg.tls.policy.TrustKnownCertificateTlsPolicy;
import java.io.IOException;
import java.security.KeyManagementException;
import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.mtwilson.agent.citrix.CitrixHostAgentFactory;
import com.intel.mtwilson.agent.citrix.CitrixHostAgent;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.datatypes.ConnectionString;
import java.io.File;
import java.io.FileInputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * Use this class to instantiate the appropriate agent or client for a given
 * host. It looks primarily at the "AddOn_Connection_String".
 * @throws UnuspportedOperationException if the appropriate agent type cannot be determined from the given host
 * @author jbuhacoff
 */
public class HostAgentFactory {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Map<Vendor,VendorHostAgentFactory> vendorFactoryMap = new EnumMap<Vendor,VendorHostAgentFactory>(Vendor.class);
    // XXX MOVED TO MTWILSON-TLS-POLICY TLSPOLICYFACTORY
    private TlsPolicyFactory tlsPolicyFactory = TlsPolicyFactory.getInstance();
    
    //private Logger log = LoggerFactory.getLogger(getClass());
    public HostAgentFactory() {
        // we initialize the map with the known vendors; but this could also be done through IoC
        vendorFactoryMap.put(Vendor.INTEL, new IntelHostAgentFactory());
        vendorFactoryMap.put(Vendor.CITRIX, new CitrixHostAgentFactory());
        vendorFactoryMap.put(Vendor.VMWARE, new VmwareHostAgentFactory());
    }
    
    /**
     * It is recommended to supply an EnumMap instance
     * @param map of vendor host agent factories
     */
    public void setVendorFactoryMap(Map<Vendor,VendorHostAgentFactory> map) {
        vendorFactoryMap = map;
    }
    
    
    /**
     * XXX TODO this method is moved here from the previous interface ManifestStrategy.
     * It's currently here to minimize code changes for the current release
     * but its functionality needs to be moved somewhere else - the trust utils
     * library should not know about the mt wilson database structure.
     * This implementation is a combination of getHostAgent and code from the original getManifest.
     * @param host
     * @return 
     */
    public PcrManifest getManifest(TblHosts host) {
        try {
            InternetAddress hostAddress = new InternetAddress(host.getName());
            String connectionString = getConnectionString(host);
            String tlsPolicyName = host.getTlsPolicyName() == null ? My.configuration().getDefaultTlsPolicyName() : host.getTlsPolicyName(); // txtHost.getTlsPolicy();  // XXX TODO TxtHost doesn't have this field yet
//            ByteArrayResource resource = new ByteArrayResource(host.getTlsKeystore() == null ? new byte[0] : host.getTlsKeystore()); // XXX TODO it's the responsibility of the caller to save the TblHosts record after calling this method if the policy is trust first certificate ; we need to get tie the keystore to the database, especially for TRUST_FIRST_CERTIFICATE, so if it's the first connection we can save the certificate back to the database after connecting
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
            // here we figure out if it's vmware or intel  and ensure we have a valid connection string starting with the vendor scheme.  XXX TODO should not be here, everyone should have valid connection strings like vmware:*, intel:*, citrix:*, etc. 
            // no special case for citrix, since that support was added recently they should always come with citrix: prepended.
//            System.out.println("host cs = " + host.getAddOnConnectionInfo());
            String connectionString = getConnectionString(host);
            //System.out.println("stdalex getHostAgent cs =" + connectionString);
            log.debug("Retrieving TLS policy...");
            TlsPolicy tlsPolicy = getTlsPolicy(host);
            log.debug("Creating Host Agent for host: " + host.getName());
            HostAgent ha = getHostAgent(hostAddress, connectionString, tlsPolicy); // XXX TODO need to have a way for the agent using trust-first-certificate to save a new certificate to the TblHosts record... right now it is lost.
            log.debug("Host Agent created.");
            return ha;
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Cannot create Host Agent for "+host.getName()+": "+e.toString(), e);
        }
    }
    
    /*
     * XXX TODO this functionality moved to ConnectionString.from() , need to change all references & delete this one
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
//        ByteArrayResource resource = new ByteArrayResource(host.getTlsKeystore() == null ? new byte[0] : host.getTlsKeystore()); // XXX TODO we need to get tie the keystore to the database, especially for TRUST_FIRST_CERTIFICATE, so if it's the first connection we can save the certificate back to the database after connecting
//        KeyStore tlsKeystore = txtHost.getTlsKeystore(); // XXX TODO TxtHost doesn't have this field yet
        TlsPolicy tlsPolicy = tlsPolicyFactory.getTlsPolicyWithKeystore(host.getTlsPolicyName(), host.getTlsKeystoreResource());
        return tlsPolicy;
    }

    /**
     * 
     * @param connectionString what is also known as the "AddOn_Connection_String", in the form vendor:url, for example vmware:https://vcenter.com/sdk;Administrator;password
     * @return 
     */
    private HostAgent getHostAgent(InternetAddress hostAddress, String connectionString, TlsPolicy tlsPolicy) throws IOException {
        Vendor[] vendors = Vendor.values();
        if( connectionString == null ) {
            throw new IllegalArgumentException("Connection info missing"); // XXX it is missing for intel trust agents configured in 1.0-RC2 or earlier -- should we attempt to guess intel:https://hostaddress:9999 for backwards compatibility?  also we don't have a vendor host agent factory for intel trust agent yet!!
        }
        ConnectionString cs = new ConnectionString(connectionString);
        String vendorName = cs.getVendor().name(); // INTEL, CITRIX, VMWARE
        for(Vendor vendor : vendors) {
            /*
            String prefix = vendor.name().toLowerCase()+":"; // "INTEL" becomes "intel:"
            if( connectionString.startsWith(prefix) ) {
                String urlpart = connectionString.substring(prefix.length());
                VendorHostAgentFactory factory = vendorFactoryMap.get(vendor);
                if( factory != null ) {
                    return factory.getHostAgent(hostAddress, urlpart, tlsPolicy);
                }
            }*/
            if( vendor.name().equals(vendorName) ) {
                VendorHostAgentFactory factory = vendorFactoryMap.get(vendor);
                if( factory != null ) {
                    return factory.getHostAgent(hostAddress, cs.getConnectionString(), tlsPolicy);
                }
            }
        }
        log.error("HostAgentFactory: Unsupported host type: "+vendorName);
        throw new UnsupportedOperationException("Unsupported host type: "+vendorName);
    }
    
    
    public HostAgent getHostAgent(ConnectionString hostConnection, TlsPolicy tlsPolicy) throws IOException {
        Vendor[] vendors = Vendor.values();
        if( hostConnection == null ) {
            throw new IllegalArgumentException("Connection info missing"); // XXX it is missing for intel trust agents configured in 1.0-RC2 or earlier -- should we attempt to guess intel:https://hostaddress:9999 for backwards compatibility?  also we don't have a vendor host agent factory for intel trust agent yet!!
        }
        String vendorName = hostConnection.getVendor().name().toLowerCase();
        for(Vendor vendor : vendors) {
            if(vendor.name().toLowerCase().equals(vendorName)) { // intel, citrix, vmware
                VendorHostAgentFactory factory = vendorFactoryMap.get(vendor);
                if( factory != null ) {
                    return factory.getHostAgent(hostConnection.getConnectionString(), tlsPolicy);
                }
            }
        }
        throw new UnsupportedOperationException("No agent factory registered for this host");
    }
    
}