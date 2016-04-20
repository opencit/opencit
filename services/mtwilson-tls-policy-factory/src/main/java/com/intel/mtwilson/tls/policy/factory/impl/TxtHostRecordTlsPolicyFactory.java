/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.FirstCertificateTrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.FirstPublicKeyTrustDelegate;
import com.intel.dcsg.cpg.x509.repository.MutableCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.MutablePublicKeyRepository;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyChoiceReport;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactory;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyProvider;
import com.intel.mtwilson.tls.policy.provider.StoredTlsPolicyProvider;
import com.intel.mtwilson.tls.policy.provider.StoredVendorTlsPolicyProvider;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class TxtHostRecordTlsPolicyFactory extends TlsPolicyFactory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TxtHostRecordTlsPolicyFactory.class);
//    private TxtHostRecord txtHostRecord;
    private TxtHostRecordObjectTlsPolicy objectTlsPolicyProvider;
    private StoredTlsPolicyProvider.HostDescriptor hostDescriptor;
    private StoredVendorTlsPolicyProvider.VendorDescriptor vendorDescriptor;
    private FirstCertificateTrustDelegate firstCertificateTrustDelegate;
    private FirstPublicKeyTrustDelegate firstPublicKeyTrustDelegate;

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
        this.firstCertificateTrustDelegate = new FirstCertificateTrustDelegate(new TxtHostRecordMutableCertificateRepository(txtHostRecord));
        this.firstPublicKeyTrustDelegate = new FirstPublicKeyTrustDelegate(new TxtHostRecordMutablePublicKeyRepository(txtHostRecord));
    }

    @Override
    protected TlsPolicy createTlsPolicy(TlsPolicyChoiceReport report) {
        try {ObjectMapper mapper = new ObjectMapper();        
        log.debug("TxtHostRecordTlsPolicyFactory createTlsPolicy with report: {}", mapper.writeValueAsString(report));}catch(Exception e){ log.error("TxtHostRecordTlsPolicyFactory createTlsPolicy with report"); }
        objectTlsPolicyProvider.setTlsPolicyChoice(report.getChoice());
        return super.createTlsPolicy(report);
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
        private TxtHostRecord txtHostRecord;
//        private TlsPolicyChoice tlsPolicyChoice;

        public TxtHostRecordObjectTlsPolicy(TxtHostRecord txtHostRecord) {
//            this.tlsPolicyChoice = txtHostRecord.tlsPolicyChoice;
            this.txtHostRecord = txtHostRecord;
        }

        @Override
        public TlsPolicyChoice getTlsPolicyChoice() {
//            return tlsPolicyChoice;
            return txtHostRecord.tlsPolicyChoice;
        }
        
        public void setTlsPolicyChoice(TlsPolicyChoice tlsPolicyChoice) {
            log.debug("TblHostsObjectTlsPolicy setTlsPolicyChoice");
            txtHostRecord.tlsPolicyChoice = tlsPolicyChoice;
        }
        
    }

    public static class TxtHostRecordHostDescriptor implements StoredTlsPolicyProvider.HostDescriptor {

        private String hostId;
        private InternetAddress hostname;

        public TxtHostRecordHostDescriptor(TxtHostRecord txtHostRecord) {
            this.hostId = txtHostRecord.HostName;
            ConnectionString str = getConnectionString(txtHostRecord);
            if (str == null) {
                throw new IllegalArgumentException(String.format("Cannot determine connection string for host: ", txtHostRecord.HostName));
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
            if (str != null) {
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

    public static class TxtHostRecordMutableCertificateRepository implements MutableCertificateRepository {

        private TxtHostRecord target;

        public TxtHostRecordMutableCertificateRepository(TxtHostRecord target) {
            this.target = target;
        }

        @Override
        public void addCertificate(X509Certificate certificate) throws KeyManagementException {
            if (target.tlsPolicyChoice == null) {
                throw new KeyManagementException("Cannot store certificate into undefined policy");
            }
//            if( target.tlsPolicyChoice.get) // now need to call the utility function that will either get the descriptor or load it from the db.....  BUT also need that descriptor to be linked to the db so we can save it back when needed..... that's not currently implemented.
            // so AFTER  getting a proper descriptor: 
            if (target.tlsPolicyChoice.getTlsPolicyDescriptor() == null) {
                throw new KeyManagementException("Cannot store certificate into undefined policy");
            }
            if (target.tlsPolicyChoice.getTlsPolicyDescriptor().getData() == null) {
                target.tlsPolicyChoice.getTlsPolicyDescriptor().setData(new ArrayList<String>());
            }
            try {
                target.tlsPolicyChoice.getTlsPolicyDescriptor().getData().add(Base64.encodeBase64String(certificate.getEncoded()));
            } catch (CertificateEncodingException e) {
                throw new KeyManagementException(e);
            }
        }

        @Override
        public List<X509Certificate> getCertificates() {
            if (target.tlsPolicyChoice == null) {
                return Collections.EMPTY_LIST;
            }  // will cause an error if caller tries to add a cert to it, and that's ok
            // same comment here about loading from database .... because if the policy is in the DB 
            //and we just have the id, we need to load the list of certs from THAT descriptor ....
            //maybe that work should be done in contructor ?? because we won't eb able to do anything at ]
            //all if the policy has an id and the id is not found n the db....  so should throw that error early.
            // so AFTER getting a proper descriptor:
            ArrayList<X509Certificate> list = new ArrayList<>();
            return list;
        }
    }

    public static class TxtHostRecordMutablePublicKeyRepository implements MutablePublicKeyRepository {

        private TxtHostRecord target;

        public TxtHostRecordMutablePublicKeyRepository(TxtHostRecord target) {
            this.target = target;
        }

        @Override
        public void addPublicKey(PublicKey publicKey) {
            if (target.tlsPolicyChoice == null) {
                throw new IllegalArgumentException("Cannot store public key into undefined policy");
            }
//            if( target.tlsPolicyChoice.get) // now need to call the utility function that will either get the descriptor or load it from the db.....  BUT also need that descriptor to be linked to the db so we can save it back when needed..... that's not currently implemented.
            // so AFTER  getting a proper descriptor: 
            if (target.tlsPolicyChoice.getTlsPolicyDescriptor() == null) {
                throw new IllegalArgumentException("Cannot store public key into undefined policy");
            }
            if (target.tlsPolicyChoice.getTlsPolicyDescriptor().getData() == null) {
                target.tlsPolicyChoice.getTlsPolicyDescriptor().setData(new ArrayList<String>());
            }
            target.tlsPolicyChoice.getTlsPolicyDescriptor().getData().add(Base64.encodeBase64String(publicKey.getEncoded()));
        }

        @Override
        public List<PublicKey> getPublicKeys() {
            if (target.tlsPolicyChoice == null) {
                return Collections.EMPTY_LIST;
            }  // will cause an error if caller tries to add a cert to it, and that's ok
            // same comment here about loading from database .... because if the policy is in the DB 
            //and we just have the id, we need to load the list of certs from THAT descriptor ....
            //maybe that work should be done in contructor ?? because we won't eb able to do anything at ]
            //all if the policy has an id and the id is not found n the db....  so should throw that error early.
            // so AFTER getting a proper descriptor:
            ArrayList<PublicKey> list = new ArrayList<>();
            return list;
        }
    }
}