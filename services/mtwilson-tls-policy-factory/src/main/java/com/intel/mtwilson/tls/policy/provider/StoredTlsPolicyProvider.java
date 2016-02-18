/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.provider;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
import com.intel.dcsg.cpg.x509.repository.KeystoreCertificateRepository;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyMigrationException;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyProvider;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyDAO;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyJdbiFactory;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyRecord;
import com.intel.mtwilson.tls.policy.codec.impl.JsonTlsPolicyWriter;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactoryUtil;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;

/**
 * Looks up a TLS Policy for the input object in the database based on
 * attributes of the object such as host id. Prefers private over shared
 * policies. This strategy can be subclassed for subnet and vendor specific
 * searches as well as pre-registration of per-host policies (from management
 * tools, done so that registration API doesn't need to reference policy)
 *
 * @author jbuhacoff
 */
public class StoredTlsPolicyProvider implements TlsPolicyProvider {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoredTlsPolicyProvider.class);
    private String hostId;

    public StoredTlsPolicyProvider(HostDescriptor hostDescriptor) {
        this.hostId = hostDescriptor.getHostId();
    }

    @Override
    public TlsPolicyChoice getTlsPolicyChoice() {
        try {
            TblHosts hostRecord;
            if( UUID.isValid(hostId)) {
                hostRecord = My.jpa().mwHosts().findHostByUuid(hostId);
            }
            else {
                hostRecord = My.jpa().mwHosts().findByName(hostId);
            }
            if (hostRecord == null) {
                return null;
            }
            if (hostRecord.getTlsPolicyId() != null) {
                log.debug("StoredTlsPolicy id: {}", hostRecord.getTlsPolicyId());
                TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
                tlsPolicyChoice.setTlsPolicyId(hostRecord.getTlsPolicyId());
                return tlsPolicyChoice;
            }
            if (hostRecord.getTlsPolicyName() != null) {
                log.debug("StoredTlsPolicy name: {}", hostRecord.getTlsPolicyName());
                if (hostRecord.getTlsPolicyName().equals("INSECURE") ) {
                    TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
                    tlsPolicyChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
                    tlsPolicyChoice.getTlsPolicyDescriptor().setPolicyType("INSECURE");
                    return tlsPolicyChoice;
                }
                if (hostRecord.getTlsPolicyName().equals("TRUST_FIRST_CERTIFICATE")) {
                    if (hostRecord.getTlsKeystore() != null) {
                        // we treat a TRUST_FIRST_CERTIFICATE policy for which there is already a cert as a public key policy during migration, and if there is not already a cert then we convert it to the insecure policy
                        // automatic data migration: load the certificates from the keystore, create a TlsPolicyDescriptor with the public keys, save it as a new private mw_tls_policy record, and update the mw_hosts record to point to it
                        TlsPolicyChoice tlsPolicyChoice = migrateExistingTlsKeystoreToTlsPolicy(hostRecord, new PublicKeyTlsPolicyConverter());
                        return tlsPolicyChoice;
                    } else {
                        TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
                        tlsPolicyChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
                        tlsPolicyChoice.getTlsPolicyDescriptor().setPolicyType("INSECURE");
                        return tlsPolicyChoice;
                    }
                }
                if (hostRecord.getTlsPolicyName().equals("TRUST_KNOWN_CERTIFICATE")) {
                    if (hostRecord.getTlsKeystore() != null) {
                        // automatic data migration: load the certificates from the keystore, create a TlsPolicyDescriptor with the public keys, save it as a new private mw_tls_policy record, and update the mw_hosts record to point to it
                        TlsPolicyChoice tlsPolicyChoice = migrateExistingTlsKeystoreToTlsPolicy(hostRecord, new PublicKeyTlsPolicyConverter());
                        return tlsPolicyChoice;
                    } else {
                        log.warn("Missing keystore for TRUST_KNOWN_CERTIFICATE for host {}", hostId);
                        hostRecord.setTlsPolicyName(null);
                    }
                }
                if( hostRecord.getTlsPolicyName().equals("TRUST_CA_VERIFY_HOSTNAME")) {
                    if (hostRecord.getTlsKeystore() != null) {
                        // automatic data migration: load the certificates from the keystore, create a TlsPolicyDescriptor with the certificates, save it as a new private mw_tls_policy record, and update the mw_hosts record to point to it
                        TlsPolicyChoice tlsPolicyChoice = migrateExistingTlsKeystoreToTlsPolicy(hostRecord, new CertificateTlsPolicyConverter());
                        return tlsPolicyChoice;
                    } else {
                        log.warn("Missing keystore for TRUST_CA_VERIFY_HOSTNAME for host {}", hostId);
                        hostRecord.setTlsPolicyName(null);
                    }
                }
                log.warn("Existing TLS policy cannot be automatically migrated: {}", hostRecord.getTlsPolicyName());
            }
        } catch (IOException | CryptographyException e) {
            log.error("Cannot load host record for {}", hostId, e);
        }
        return null;
    }
    
    private static interface TlsKeystoreConverter {
        TlsPolicyDescriptor convert(CertificateRepository repository);
    }
    private static class PublicKeyTlsPolicyConverter implements TlsKeystoreConverter {
        @Override
        public TlsPolicyDescriptor convert(CertificateRepository repository) {
            TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
            tlsPolicyDescriptor.setPolicyType("public-key");
            tlsPolicyDescriptor.setProtection(TlsPolicyFactoryUtil.getAllTlsProtection());
            tlsPolicyDescriptor.setMeta(new HashMap<String,String>());
            tlsPolicyDescriptor.getMeta().put("encoding", "base64");
            tlsPolicyDescriptor.setData(new HashSet<String>());
            for (X509Certificate certificate : repository.getCertificates()) {
                String publicKey = Base64.encodeBase64String(certificate.getPublicKey().getEncoded());
                tlsPolicyDescriptor.getData().add(publicKey);
                log.debug("Added public key to policy: {}", publicKey);
            }
            return tlsPolicyDescriptor;
        }
    }
    
    private static class CertificateTlsPolicyConverter implements TlsKeystoreConverter {
        @Override
        public TlsPolicyDescriptor convert(CertificateRepository repository) {
            TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
            tlsPolicyDescriptor.setPolicyType("certificate");
            tlsPolicyDescriptor.setProtection(TlsPolicyFactoryUtil.getAllTlsProtection());
            tlsPolicyDescriptor.setMeta(new HashMap<String,String>());
            tlsPolicyDescriptor.getMeta().put("encoding", "base64");
            tlsPolicyDescriptor.setData(new HashSet<String>());
            for (X509Certificate certificate : repository.getCertificates()) {
                try {
                    String encoded = Base64.encodeBase64String(certificate.getEncoded());
                    tlsPolicyDescriptor.getData().add(encoded);
                    log.debug("Added certificate to policy: {}", encoded);
                }
                catch(CertificateEncodingException e) {
                    log.error("Cannot add certificate to policy: {}", e.getMessage());
                }
            }
            return tlsPolicyDescriptor;
        }
    }

    private TlsPolicyChoice migrateExistingTlsKeystoreToTlsPolicy(TblHosts hostRecord, TlsKeystoreConverter converter) {
        try {
            // load certificates from keystore
            KeystoreCertificateRepository repository = new KeystoreCertificateRepository(hostRecord.getTlsKeystoreResource(), My.configuration().getTlsKeystorePassword());
            TlsPolicyDescriptor tlsPolicyDescriptor = converter.convert(repository);
            
            // save the descriptor as a new record in mw_tls_policy
            try (TlsPolicyDAO dao = TlsPolicyJdbiFactory.tlsPolicyDAO()) {
                JsonTlsPolicyWriter writer = new JsonTlsPolicyWriter();
                TlsPolicyRecord tlsPolicyRecord = new TlsPolicyRecord();
                tlsPolicyRecord.setId(new UUID());
                tlsPolicyRecord.setName(hostRecord.getUuid_hex());
                tlsPolicyRecord.setPrivate(true);
                tlsPolicyRecord.setContentType(MediaType.APPLICATION_JSON);
                tlsPolicyRecord.setContent(writer.write(tlsPolicyDescriptor));
                tlsPolicyRecord.setComment("automatic migration");
                dao.insertTlsPolicy(tlsPolicyRecord);
                hostRecord.setTlsPolicyId(tlsPolicyRecord.getId().toString());
                try {
                    My.jpa().mwHosts().edit(hostRecord);
                } catch (IllegalOrphanException | NonexistentEntityException | ASDataException | CryptographyException e) {
                    log.error("Cannot store host record for {}: {}", hostId, e.getMessage());
                    throw new TlsPolicyMigrationException("Cannot store host record", e,hostId);
                }
            }
            TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
            tlsPolicyChoice.setTlsPolicyDescriptor(tlsPolicyDescriptor);
            return tlsPolicyChoice;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            log.error("Cannot load keystore for TRUST_KNOWN_CERTIFICATE policy for host {}", hostId, e);
            hostRecord.setTlsPolicyName(null);
        }
        return null;
    }

    public static interface HostDescriptor {
        String getHostId();
        InternetAddress getInternetAddress();
    }
    
}
