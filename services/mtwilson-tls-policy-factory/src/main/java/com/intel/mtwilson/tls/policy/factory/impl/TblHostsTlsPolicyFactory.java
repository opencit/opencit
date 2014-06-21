/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory.impl;

import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactory;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyProvider;
import com.intel.mtwilson.tls.policy.provider.StoredTlsPolicy;
import com.intel.mtwilson.tls.policy.provider.StoredVendorTlsPolicy;
import java.net.MalformedURLException;

/**
 *
 * @author jbuhacoff
 */
public class TblHostsTlsPolicyFactory extends TlsPolicyFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TblHostsTlsPolicyFactory.class);
    private TlsPolicyProvider objectTlsPolicyProvider;
    private StoredTlsPolicy.HostDescriptor hostDescriptor;
    private StoredVendorTlsPolicy.VendorDescriptor vendorDescriptor;

    public TblHostsTlsPolicyFactory(TblHosts tblHosts) {
        super();
//        this.txtHostRecord = txtHostRecord;
        this.objectTlsPolicyProvider = new TblHostsObjectTlsPolicy(tblHosts);
        this.hostDescriptor = new TblHostsHostDescriptor(tblHosts);
        this.vendorDescriptor = new TblHostsVendorDescriptor(tblHosts);
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
    protected StoredTlsPolicy.HostDescriptor getHostDescriptor() {
        return hostDescriptor;
    }

    @Override
    protected StoredVendorTlsPolicy.VendorDescriptor getVendorDescriptor() {
        return vendorDescriptor;
    }

    public static class TblHostsObjectTlsPolicy implements TlsPolicyProvider {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TblHostsObjectTlsPolicy.class);

        private TlsPolicyChoice tlsPolicyChoice;

        public TblHostsObjectTlsPolicy(TblHosts tblHosts) {
            this.tlsPolicyChoice = determineTlsPolicyChoice(tblHosts);
        }
        
        private TlsPolicyChoice determineTlsPolicyChoice(TblHosts host) {
            // first look at the new tlsPolicyId field
            if( host.getTlsPolicyId() != null && !host.getTlsPolicyId().isEmpty() ) {
                log.debug("TblHostsObjectTlsPolicy: policy id {}", host.getTlsPolicyId());
                // the caller can load the specified policy from the database
                TlsPolicyChoice tlsPolicyIdChoice = new TlsPolicyChoice();
                tlsPolicyIdChoice.setTlsPolicyId(host.getTlsPolicyId());
                return tlsPolicyIdChoice;
            }
            else if( host.getTlsPolicyName() != null && !host.getTlsPolicyName().isEmpty() ) {
                log.debug("TblHostsObjectTlsPolicy: policy name {}", host.getTlsPolicyName());
                if( host.getTlsPolicyName().equals("INSECURE") ) {
                    TlsPolicyChoice tlsPolicyNameChoice = new TlsPolicyChoice();
                    tlsPolicyNameChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
                    tlsPolicyNameChoice.getTlsPolicyDescriptor().setName(host.getTlsPolicyName());
                    return tlsPolicyNameChoice;
                }
                else if( host.getTlsPolicyName().equals("TRUST_FIRST_CERTIFICATE") ) {
                    TlsPolicyChoice tlsPolicyNameChoice = new TlsPolicyChoice();
                    tlsPolicyNameChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
                    tlsPolicyNameChoice.getTlsPolicyDescriptor().setName(host.getTlsPolicyName());
                    // TODO:  need to provide something here for savnig the cert back???? no... must be provided via some other interface... because the choice/descriptor objects are data contains only, not pure oo...
                    return tlsPolicyNameChoice;
                }
                else {
                    log.debug("TblHostsObjectTlsPolicy: unsupported policy name {}", host.getTlsPolicyName());
                    return null;
                }
            }
            else {
                log.debug("TblHostsObjectTlsPolicy: policy not found in TblHosts record");
                return null;
            }
        }

        @Override
        public TlsPolicyChoice getTlsPolicyChoice() {
        return tlsPolicyChoice;
        }
    }

    public static class TblHostsHostDescriptor implements StoredTlsPolicy.HostDescriptor {
        private String hostId;
        private InternetAddress hostname;

        public TblHostsHostDescriptor(TblHosts tblHosts) {
            this.hostId = tblHosts.getName();
            ConnectionString str = getConnectionString(tblHosts);
            if( str == null ) {
                throw new IllegalArgumentException(String.format("Cannot determine connection string for host: ",tblHosts.getName()));
            }
            this.hostname = new InternetAddress(str.getHostname().toString()); // not using tblHosts.getName() because in case of vcenter or xencenter the hostname is not the address we're connecting to
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

    
    public static class TblHostsVendorDescriptor implements StoredVendorTlsPolicy.VendorDescriptor {
        private String vendor;

        public TblHostsVendorDescriptor(TblHosts tblHosts) {
            ConnectionString str = getConnectionString(tblHosts);
            if( str != null ) {
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
