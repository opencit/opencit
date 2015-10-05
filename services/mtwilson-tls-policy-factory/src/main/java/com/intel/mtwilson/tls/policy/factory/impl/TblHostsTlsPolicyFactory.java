/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyChoiceReport;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactory;
//import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactoryUtil;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyProvider;
import com.intel.mtwilson.tls.policy.provider.StoredTlsPolicyProvider;
import com.intel.mtwilson.tls.policy.provider.StoredVendorTlsPolicyProvider;
import java.net.MalformedURLException;

/**
 *
 * @author jbuhacoff
 */
public class TblHostsTlsPolicyFactory extends TlsPolicyFactory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TblHostsTlsPolicyFactory.class);
    private TblHostsObjectTlsPolicy objectTlsPolicyProvider;
    private StoredTlsPolicyProvider.HostDescriptor hostDescriptor;
    private StoredVendorTlsPolicyProvider.VendorDescriptor vendorDescriptor;

    /**
     * Example input:
     * <pre>
     * 2014-07-06 05:17:03,408 DEBUG [http-bio-8443-exec-130] c.i.m.t.p.f.i.TblHostsTlsPolicyFactory [TblHostsTlsPolicyFactory.java:56] TblHostsTlsPolicyFactory constructor: {"tblSamlAssertionCollection":null,"location":null,"id":null,"name":"10.1.71.173","port":0,"description":null,"aikSha1":null,"aikPublicKey":null,"aikPublicKeySha1":null,"tlsPolicyId":null,"tlsPolicyName":null,"tlsKeystore":null,"email":null,"errorCode":null,"errorDescription":null,"vmmMleId":null,"biosMleId":null,"uuid_hex":null,"bios_mle_uuid_hex":null,"vmm_mle_uuid_hex":null,"tlsPolicyChoice":{"tlsPolicyId":null,"tlsPolicyDescriptor":null},"tlsPolicyDescriptor":null,"ipaddress":"10.1.71.173","addOnConnectionInfo":"vmware:https://10.1.71.162:443/sdk;administrator;intel123!","aikcertificate":null,"hardwareUuid":null}
2014-07-06 05:17:03,408 DEBUG [http-bio-8443-exec-130] c.i.m.t.p.f.i.TblHostsTlsPolicyFactory$TblHostsObjectTlsPolicy [TblHostsTlsPolicyFactory.java:144] TblHostsObjectTlsPolicy: policy not found in TblHosts record

     * </pre>
     * @param tblHosts 
     */
    public TblHostsTlsPolicyFactory(TblHosts tblHosts) {
        super();
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            log.debug("TblHostsTlsPolicyFactory constructor: {}", mapper.writeValueAsString(tblHosts)); //This statement may contain clear text passwords
//        } catch (Exception e) {
//            log.warn("Cannot write debug log", e);
//        }
//        this.txtHostRecord = txtHostRecord;
        this.objectTlsPolicyProvider = new TblHostsObjectTlsPolicy(tblHosts);
        this.hostDescriptor = new TblHostsHostDescriptor(tblHosts);
        this.vendorDescriptor = new TblHostsVendorDescriptor(tblHosts);
    }

    @Override
    protected TlsPolicy createTlsPolicy(TlsPolicyChoiceReport report) {
        try {ObjectMapper mapper = new ObjectMapper();        
        log.debug("TblHostsTlsPolicyFactory createTlsPolicy with report: {}", mapper.writeValueAsString(report));}catch(Exception e){ log.error("TblHostsTlsPolicyFactory createTlsPolicy with report"); }
        objectTlsPolicyProvider.setTlsPolicyChoice(report.getChoice());
        return super.createTlsPolicy(report);
    }
    
    /*
     @Override
     protected boolean accept(Object tlsPolicySubject) {
     return tlsPolicySubject instanceof TblHosts;
     }
     */
    @Override
    protected TlsPolicyProvider getObjectTlsPolicyProvider() {
        return objectTlsPolicyProvider;
    }

    @Override
    protected StoredTlsPolicyProvider.HostDescriptor getHostDescriptor() {
        return hostDescriptor;
    }

    @Override
    protected StoredVendorTlsPolicyProvider.VendorDescriptor getVendorDescriptor() {
        return vendorDescriptor;
    }

    public static class TblHostsObjectTlsPolicy implements TlsPolicyProvider {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TblHostsObjectTlsPolicy.class);
        private TblHosts tblHosts;
//        private TlsPolicyChoice tlsPolicyChoice;

        public TblHostsObjectTlsPolicy(TblHosts tblHosts) {
            this.tblHosts = tblHosts;
//            this.tlsPolicyChoice = determineTlsPolicyChoice(tblHosts);
        }
        
        public void setTlsPolicyChoice(TlsPolicyChoice tlsPolicyChoice) {
            log.debug("TblHostsObjectTlsPolicy setTlsPolicyChoice");
            tblHosts.setTlsPolicyChoice(tlsPolicyChoice);
        }

        private TlsPolicyChoice determineTlsPolicyChoice(TblHosts host) {
            // first look at the new tlsPolicyId field
            if (host.getTlsPolicyId() != null && !host.getTlsPolicyId().isEmpty()) {
                log.debug("TblHostsObjectTlsPolicy: policy id {}", host.getTlsPolicyId());
                // the caller can load the specified policy from the database
                TlsPolicyChoice tlsPolicyIdChoice = new TlsPolicyChoice();
                tlsPolicyIdChoice.setTlsPolicyId(host.getTlsPolicyId());
                return tlsPolicyIdChoice;
            }
            // second, look for a (temporary) tls policy descriptor
            if (host.getTlsPolicyDescriptor() != null) {
                log.debug("TblHostsObjectTlsPolicy: policy descriptor {}", host.getTlsPolicyDescriptor().getPolicyType());
                TlsPolicyChoice tlsPolicyIdChoice = new TlsPolicyChoice();
                tlsPolicyIdChoice.setTlsPolicyDescriptor(host.getTlsPolicyDescriptor());
                return tlsPolicyIdChoice;
            } // third, for backward compatibility, recognize the Mt Wilson 1.x policy types
            else if (host.getTlsPolicyName() != null && !host.getTlsPolicyName().isEmpty()) {
                log.debug("TblHostsObjectTlsPolicy: policy name {}", host.getTlsPolicyName());
                if (host.getTlsPolicyName().equals("INSECURE")) {
                    TlsPolicyChoice tlsPolicyNameChoice = new TlsPolicyChoice();
                    tlsPolicyNameChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
                    tlsPolicyNameChoice.getTlsPolicyDescriptor().setPolicyType(host.getTlsPolicyName());
                    return tlsPolicyNameChoice;
                } else if (host.getTlsPolicyName().equals("TRUST_FIRST_CERTIFICATE")) {
                    TlsPolicyChoice tlsPolicyNameChoice = new TlsPolicyChoice();
                    tlsPolicyNameChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
                    tlsPolicyNameChoice.getTlsPolicyDescriptor().setPolicyType(host.getTlsPolicyName());
                    return tlsPolicyNameChoice;
                } else if (host.getTlsPolicyName().equals("TRUST_KNOWN_CERTIFICATE")) {
                    TlsPolicyChoice tlsPolicyNameChoice = new TlsPolicyChoice();
                    tlsPolicyNameChoice.setTlsPolicyDescriptor(TlsPolicyFactory.getTlsPolicyDescriptorFromResource(host.getTlsKeystoreResource()));
                    tlsPolicyNameChoice.getTlsPolicyDescriptor().setPolicyType("public-key"); // will cause the certs in the data section to be read only for their public keys. without hostname verification
                    return tlsPolicyNameChoice;
                } else if (host.getTlsPolicyName().equals("TRUST_CA_VERIFY_HOSTNAME")) {
                    TlsPolicyChoice tlsPolicyNameChoice = new TlsPolicyChoice();
                    tlsPolicyNameChoice.setTlsPolicyDescriptor(TlsPolicyFactory.getTlsPolicyDescriptorFromResource(host.getTlsKeystoreResource()));
                    return tlsPolicyNameChoice;
                } else {
                    log.debug("TblHostsObjectTlsPolicy: unsupported policy name {}", host.getTlsPolicyName());
                    return null;
                }
            } else {
                log.debug("TblHostsObjectTlsPolicy: policy not found in TblHosts record");
                return null;
            }
        }

        @Override
        public TlsPolicyChoice getTlsPolicyChoice() {
            return determineTlsPolicyChoice(tblHosts); // tlsPolicyChoice;
        }
    }

    public static class TblHostsHostDescriptor implements StoredTlsPolicyProvider.HostDescriptor {

        private String hostId;
        private InternetAddress hostname;

        public TblHostsHostDescriptor(TblHosts tblHosts) {
            this.hostId = tblHosts.getName();
            ConnectionString str = getConnectionString(tblHosts);
            if (str == null) {
                throw new IllegalArgumentException(String.format("Cannot determine connection string for host: ", tblHosts.getName()));
            }
            log.debug("TblHosts connection string with prefix: {}", str.getConnectionStringWithPrefix());
            log.debug("TblHosts connection string: {}", str.getConnectionString());
//            log.debug("TblHosts connection string: {}", str.getURL().toExternalForm());
            log.debug("TblHosts connection string addon conn str: {}", str.getAddOnConnectionString());
            this.hostname = new InternetAddress(str.getManagementServerName()); // not using tblHosts.getName() because in case of vcenter or xencenter the hostname is not the address we're connecting to; but the ConnectionString class always presents the connection target a this attribute
        }

        @Override
        public String getHostId() {
            return hostId;
        }

        @Override
        public InternetAddress getInternetAddress() {
            return hostname;
        }
    }

    public static class TblHostsVendorDescriptor implements StoredVendorTlsPolicyProvider.VendorDescriptor {

        private String vendor;

        public TblHostsVendorDescriptor(TblHosts tblHosts) {
            ConnectionString str = getConnectionString(tblHosts);
            if (str != null) {
                this.vendor = str.getVendor().name();
            }
        }

        @Override
        public String getVendorProtocol() {
            return vendor;
        }
    }

    protected static ConnectionString getConnectionString(TblHosts tblHosts) {
        try {
            TxtHostRecord txtHostRecord = new TxtHostRecord();
            txtHostRecord.AddOn_Connection_String = tblHosts.getAddOnConnectionInfo();
            txtHostRecord.HostName = tblHosts.getName();
            txtHostRecord.Port = tblHosts.getPort();
            txtHostRecord.IPAddress = tblHosts.getIPAddress();
            ConnectionString str = ConnectionString.from(txtHostRecord);
            return str;
        } catch (MalformedURLException e) {
            log.error("Cannot determine connection string from host record", e);
            return null;
        }
    }
}
