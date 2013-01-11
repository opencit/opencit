/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent;

import com.intel.mountwilson.manifest.data.IManifest;
import com.intel.mountwilson.manifest.strategy.TrustAgentStrategy;
import com.intel.mountwilson.manifest.strategy.VMWareManifestStrategy;
import com.intel.mtwilson.agent.intel.IntelHostAgentFactory;
import com.intel.mtwilson.agent.vmware.VCenterHost;
import com.intel.mtwilson.agent.vmware.VmwareHostAgentFactory;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.io.Resource;
import com.intel.mtwilson.tls.InsecureTlsPolicy;
import com.intel.mtwilson.tls.KeystoreCertificateRepository;
import com.intel.mtwilson.tls.TlsConnection;
import com.intel.mtwilson.tls.TlsPolicy;
import com.intel.mtwilson.tls.TrustCaAndVerifyHostnameTlsPolicy;
import com.intel.mtwilson.tls.TrustFirstCertificateTlsPolicy;
import com.intel.mtwilson.tls.TrustKnownCertificateTlsPolicy;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Use this class to instantiate the appropriate agent or client for a given
 * host. It looks primarily at the "AddOn_Connection_String".
 * @throws UnuspportedOperationException if the appropriate agent type cannot be determined from the given host
 * @author jbuhacoff
 */
public class HostAgentFactory {
    private Map<Vendor,VendorHostAgentFactory> vendorFactoryMap = new EnumMap<Vendor,VendorHostAgentFactory>(Vendor.class);
    
    public HostAgentFactory() {
        // we initialize the map with the known vendors; but this could also be done through IoC
        vendorFactoryMap.put(Vendor.INTEL, new IntelHostAgentFactory());
        vendorFactoryMap.put(Vendor.CITRIX, null);
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
    public HashMap<String, ? extends IManifest> getManifest(TblHosts host, VCenterHost postProcessing) {
        try {
            InternetAddress hostAddress = new InternetAddress(host.getName());
            String connectionString = getConnectionString(host);
            String tlsPolicyName = host.getSSLPolicy() == null ? "TRUST_FIRST_CERTIFICATE" : host.getSSLPolicy(); // txtHost.getTlsPolicy();  // XXX TODO TxtHost doesn't have this field yet
            ByteArrayResource resource = new ByteArrayResource(host.getSSLCertificate()); // XXX TODO it's the responsibility of the caller to save the TblHosts record after calling this method if the policy is trust first certificate ; we need to get tie the keystore to the database, especially for TRUST_FIRST_CERTIFICATE, so if it's the first connection we can save the certificate back to the database after connecting
            String password = "password"; // XXX TODO uh oh... opening a keystore requires a password, so we can verify its signed contents, which is important. putting the password in the txthost record won't be secure.  password needs to  come from attestation service configuration - or from the user.  this isn't an issue for the factory because the factory is supposed to get the keystore AFTER it has been opened with the password.  but when this code moves to the JPA/DAO/Repository layer, we'll need to have a password from somewhere.         
            SimpleKeystore tlsKeystore = new SimpleKeystore(resource, password); // XXX TODO see above commment about password;  the per-host trusted certificates keystore has to either be protected by a password known to all mt wilson instances (stored in database or sync'd across each server's configuration files) or it has to be protected by a group secret known to all authorized clients (and then we need a mechanism for the api client to send us the secret in the request, and a way get secrets in and out of api client's keystore so it can be sync'd across the authorized group of clients) or we can just not store it encrypted and use a pem-format keystore instead of a java KeyStore 
            TlsPolicy tlsPolicy = getTlsPolicy(tlsPolicyName, tlsKeystore); // XXX TODO not sure that this belongs in the http-authorization package, because policy names are an application-level thing (allowed configurations), and creating the right repository is an application-level thing too (mutable vs immutable, and underlying implementation - keystore, array, cms of pem-list.
            HostAgent hostAgent = getHostAgent(hostAddress, connectionString, tlsPolicy);
            HashMap<String, ? extends IManifest> manifest = hostAgent.getManifest(postProcessing);
            host.setSSLCertificate(resource.toByteArray()); // if the tls policy is TRUST_FIRST_CERTIFICATE then it's possible a new cert has been saved in it and we have to make sure it gets saved to the host record;  for all other tls policies there would be no change so this is a no-op -  the byte array will be the same as the one we started with
            return manifest;
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
            String connectionString = getConnectionString(host);
            TlsPolicy tlsPolicy = getTlsPolicy(host);
            return getHostAgent(hostAddress, connectionString, tlsPolicy); // XXX TODO need to have a way for the agent using trust-first-certificate to save a new certificate to the TblHosts record... right now it is lost.
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Cannot create Host Agent for "+host.getName()+": "+e.toString(), e);
        }
    }
    
    // given a host, it returns the complete connection string starting with vendor scheme
    public String getConnectionString(TblHosts host) {
        String connectionString = host.getAddOnConnectionInfo();
        if( connectionString == null || connectionString.isEmpty() ) {
            if( host.getIPAddress() != null  ) {
                connectionString = String.format("intel:https://%s:%d", host.getIPAddress(), host.getPort());
            }
        }
        else if( connectionString.startsWith("http") && connectionString.contains("/sdk;") ) {
            connectionString = String.format("vmware:%s", connectionString);
        }        
        return connectionString;
    }
    
    public TlsPolicy getTlsPolicy(TblHosts host) throws KeyManagementException {
        String tlsPolicyName = host.getSSLPolicy() == null ? "TRUST_FIRST_CERTIFICATE" : host.getSSLPolicy(); // txtHost.getTlsPolicy();  // XXX TODO TxtHost doesn't have this field yet
//            String tlsPolicyName = "TRUST_FIRST_CERTIFICATE"; // txtHost.getTlsPolicy();  // XXX TODO TxtHost doesn't have this field yet
        ByteArrayResource resource = new ByteArrayResource(host.getSSLCertificate()); // XXX TODO we need to get tie the keystore to the database, especially for TRUST_FIRST_CERTIFICATE, so if it's the first connection we can save the certificate back to the database after connecting
//        KeyStore tlsKeystore = txtHost.getTlsKeystore(); // XXX TODO TxtHost doesn't have this field yet
        TlsPolicy tlsPolicy = getTlsPolicy(tlsPolicyName, resource);
        return tlsPolicy;
    }

    public TlsPolicy getTlsPolicy(String tlsPolicyName, Resource resource) throws KeyManagementException {
        String password = "password"; // XXX TODO uh oh... opening a keystore requires a password, so we can verify its signed contents, which is important. putting the password in the txthost record won't be secure.  password needs to  come from attestation service configuration - or from the user.  this isn't an issue for the factory because the factory is supposed to get the keystore AFTER it has been opened with the password.  but when this code moves to the JPA/DAO/Repository layer, we'll need to have a password from somewhere.         
        SimpleKeystore tlsKeystore = new SimpleKeystore(resource, password); // XXX TODO only because txthost doesn't have the field yet... we should get the keystore from the txthost object
        TlsPolicy tlsPolicy = getTlsPolicy(tlsPolicyName, tlsKeystore); // XXX TODO not sure that this belongs in the http-authorization package, because policy names are an application-level thing (allowed configurations), and creating the right repository is an application-level thing too (mutable vs immutable, and underlying implementation - keystore, array, cms of pem-list.
        return tlsPolicy;
    }
    
    
    private TlsPolicy getTlsPolicy(String tlsPolicyName, SimpleKeystore tlsKeystore) {
        if( tlsPolicyName == null ) { tlsPolicyName = "TRUST_FIRST_CERTIFICATE"; } // XXX for backwards compatibility with records that don't have a policy set, but maybe this isn't the place to put it - maybe it should be in the DAO that provides us the txthost object.
        String ucName = tlsPolicyName.toUpperCase();
        if( ucName.equals("TRUST_CA_VERIFY_HOSTNAME") ) {
            return new TrustCaAndVerifyHostnameTlsPolicy(new KeystoreCertificateRepository(tlsKeystore));
        }
        if( ucName.equals("TRUST_FIRST_CERTIFICATE") ) {
            return new TrustFirstCertificateTlsPolicy(new KeystoreCertificateRepository(tlsKeystore));
        }
        if( ucName.equals("TRUST_KNOWN_CERTIFICATE") ) {
            return new TrustKnownCertificateTlsPolicy(new KeystoreCertificateRepository(tlsKeystore));
        }
        if( ucName.equals("INSECURE") ) {
            return new InsecureTlsPolicy();
        }
        throw new IllegalArgumentException("Unknown TLS Policy: "+tlsPolicyName);
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
        for(Vendor vendor : vendors) {
            String prefix = vendor.name().toLowerCase()+":"; // "INTEL" becomes "intel:"
            if( connectionString.startsWith(prefix) ) {
                String urlpart = connectionString.substring(prefix.length());
                VendorHostAgentFactory factory = vendorFactoryMap.get(vendor);
                if( factory != null ) {
                    return factory.getHostAgent(hostAddress, urlpart, tlsPolicy);
                }
            }
        }
        throw new UnsupportedOperationException("No agent factory registered for this host");
    }
    
}
