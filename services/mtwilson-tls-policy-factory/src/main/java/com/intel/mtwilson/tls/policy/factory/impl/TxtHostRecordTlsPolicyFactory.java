/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactory;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyProvider;
import com.intel.mtwilson.tls.policy.provider.StoredTlsPolicyProvider;
import com.intel.mtwilson.tls.policy.provider.StoredVendorTlsPolicyProvider;
import java.net.MalformedURLException;

/**
 *
 * @author jbuhacoff
 */
public class TxtHostRecordTlsPolicyFactory extends TlsPolicyFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TxtHostRecordTlsPolicyFactory.class);

//    private TxtHostRecord txtHostRecord;
    private TlsPolicyProvider objectTlsPolicyProvider;
    private StoredTlsPolicyProvider.HostDescriptor hostDescriptor;
    private StoredVendorTlsPolicyProvider.VendorDescriptor vendorDescriptor;

    public TxtHostRecordTlsPolicyFactory(TxtHostRecord txtHostRecord) {
        super();
//        try {
//        ObjectMapper mapper = new ObjectMapper();
//        log.debug("TxtHostRecordTlsPolicyFactory constructor: {}", mapper.writeValueAsString(txtHostRecord)); //This statement may contain clear text passwords
//        }
//        catch(Exception e) {
//            log.warn("Cannot write debug log", e);
//        }
//        this.txtHostRecord = txtHostRecord;
        this.objectTlsPolicyProvider = new TxtHostRecordObjectTlsPolicy(txtHostRecord);
        this.hostDescriptor = new TxtHostRecordHostDescriptor(txtHostRecord);
        this.vendorDescriptor = new TxtHostRecordVendorDescriptor(txtHostRecord);
    }

    /*
    @Override
    protected boolean accept(Object tlsPolicySubject) {
        return tlsPolicySubject instanceof TxtHostRecord;
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

    public static class TxtHostRecordObjectTlsPolicy implements TlsPolicyProvider {

        private TlsPolicyChoice tlsPolicyChoice;

        /*
        public TxtHostRecordObjectTlsPolicy(TxtHostRecord txtHostRecord) {
            this.tlsPolicyChoice = null;
        }
        */
        public TxtHostRecordObjectTlsPolicy(TxtHostRecord txtHostRecord) {
            this.tlsPolicyChoice = txtHostRecord.tlsPolicyChoice;
        }

        @Override
        public TlsPolicyChoice getTlsPolicyChoice() {
            return tlsPolicyChoice;
        }
    }

    public static class TxtHostRecordHostDescriptor implements StoredTlsPolicyProvider.HostDescriptor {
        private String hostId;
        private InternetAddress hostname;

        public TxtHostRecordHostDescriptor(TxtHostRecord txtHostRecord) {
            this.hostId = txtHostRecord.HostName;
            ConnectionString str = getConnectionString(txtHostRecord);
            if( str == null ) {
                throw new IllegalArgumentException(String.format("Cannot determine connection string for host: ",txtHostRecord.HostName));
            }
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

    public static class TxtHostRecordVendorDescriptor implements StoredVendorTlsPolicyProvider.VendorDescriptor {
        private String vendor;

        public TxtHostRecordVendorDescriptor(TxtHostRecord txtHostRecord) {
            ConnectionString str = getConnectionString(txtHostRecord);
            if( str != null ) {
                this.vendor = str.getVendor().name();
            }
        }

        @Override
        public String getVendorProtocol() {
            return vendor;
        }
    }

    protected static ConnectionString getConnectionString(TxtHostRecord txtHostRecord) {
        try {
            ConnectionString str = ConnectionString.from(txtHostRecord);
            return str;
        } catch (MalformedURLException e) {
            log.error("Cannot determine connection string from host record", e);
            return null;
        }
    }

}
