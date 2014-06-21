/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.crypto.digest.DigestUtil;
import com.intel.dcsg.cpg.crypto.digest.UnsupportedAlgorithmException;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.net.Hostname;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.tls.policy.TrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateDigestTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateTlsPolicy;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.dcsg.cpg.tls.policy.impl.FirstCertificateTrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.FirstPublicKeyTrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyDigestTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
import com.intel.dcsg.cpg.x509.repository.DigestRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutableCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutableDigestRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutablePublicKeyRepository;
import com.intel.dcsg.cpg.x509.repository.KeystoreCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.PublicKeyRepository;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.tls.policy.TlsProtection;
import com.intel.mtwilson.tls.policy.factory.impl.TblHostsTlsPolicyFactory;
import com.intel.mtwilson.tls.policy.factory.impl.TxtHostRecordTlsPolicyFactory;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyDAO;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyJdbiFactory;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyRecord;
import com.intel.mtwilson.tls.policy.provider.DefaultTlsPolicy;
import com.intel.mtwilson.tls.policy.provider.GlobalTlsPolicy;
import com.intel.mtwilson.tls.policy.provider.StoredTlsPolicy;
import com.intel.mtwilson.tls.policy.provider.StoredVendorTlsPolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * You can instantiate subclasses directly:
 * <pre>
 * TxtHostRecordTlsPolicyFactory factory = new TxtHostRecordTlsPolicyFactory((TxtHostRecord) tlsPolicySubject);
 * TblHostsTlsPolicyFactory factory = new TblHostsTlsPolicyFactory((TblHosts) tlsPolicySubject);
 * </pre>
 * 
 * Or use the createFactory method on this abstract class:
 * <pre>
 * TlsPolicyFactory factory = TlsPolicyFactory.createFactory((TxtHostRecord)tlsPolicySubject);
 * TlsPolicyFactory factory = TlsPolicyFactory.createFactory((TblHosts)tlsPolicySubject);
 * // to get a TlsPolicy you can use with a TlsConnection:
 * TlsPolicy tlsPolicy = factory.getTlsPolicy();
 * // or if you want to see a description of the TlsPolicy and information about where the TlsPolicy came from:
 * TlsPolicyChoiceReport tlsPolicyChoiceReport = factory.getTlsPolicyChoiceReport();
 * </pre>
 * 
 * The instantiated TlsPolicyFactory maintains a reference to the provided instance
 * of TxtHostRecord or TblHosts so if that instance changes and you call getTlsPolicy
 * or getTlsPolicyChoiceReport again you will get newly generated TlsPolicy and
 * TlsPolicyChoiceReport instances.  If you want to re-use those instances you
 * should cache them because creating the TlsPolicy may involve database access
 * to load stored policies.
 * 
 * The default strategy looks in the following locations, in order:
 * 
 * 1. GlobalTlsPolicy. the global tls policy - if defined in mtwilson.properties or in database configuration table
 * 2. ObjectTlsPolicy. the request - if it includes a TlsPolicyDescriptor or ID it will be used
 * 3. StoredTlsPolicy. per-host policy for existing host - could be private or shared policy
 * 4. StoredTlsPolicy. pre-registered per-host policy for new host - must be private policy 
 * 5. DefaultTlsPolicy. default policy - local mtwilson.properties file or in database configuration table
 * 
 * 
 * @author jbuhacoff
 */
public abstract class TlsPolicyFactory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyFactory.class);
    private long tlsPemLastModified = 0;
    private long tlsCrtLastModified = 0;
    private ArrayList<X509Certificate> tlsAuthorities = new ArrayList<>();
    
    @Deprecated
    public TlsPolicy getTlsPolicyWithKeystore(String tlsPolicyName, Resource resource) throws KeyManagementException, IOException {
        String password = My.configuration().getTlsKeystorePassword();
        SimpleKeystore tlsKeystore = new SimpleKeystore(resource, password); // XXX TODO only because txthost doesn't have the field yet... we should get the keystore from the txthost object
        TlsPolicy tlsPolicy = getTlsPolicyWithKeystore(tlsPolicyName, tlsKeystore); // XXX TODO not sure that this belongs in the http-authorization package, because policy names are an application-level thing (allowed configurations), and creating the right repository is an application-level thing too (mutable vs immutable, and underlying implementation - keystore, array, cms of pem-list.
        return tlsPolicy;
    }

    @Deprecated
    public TlsPolicy getTlsPolicyWithKeystore(SimpleKeystore tlsKeystore) throws IOException, KeyManagementException {
        return getTlsPolicyWithKeystore(null, tlsKeystore);
    }

    @Deprecated
    public TlsPolicy getTlsPolicyWithKeystore(String tlsPolicyName, SimpleKeystore tlsKeystore) throws IOException, KeyManagementException {
        if (tlsPolicyName == null) {
            tlsPolicyName = My.configuration().getDefaultTlsPolicyName();
        } // XXX for backwards compatibility with records that don't have a policy set, but maybe this isn't the place to put it - maybe it should be in the DAO that provides us the txthost object.
        String ucName = tlsPolicyName.toUpperCase();
        if (ucName.equals("TRUST_CA_VERIFY_HOSTNAME")) { // XXX TODO   use TlsPolicyName  
            initTlsTrustedCertificateAuthorities();
            for (X509Certificate cacert : tlsAuthorities) {
                log.debug("Adding trusted TLS CA certificate {}", cacert.getSubjectX500Principal().getName());
                try {
                    tlsKeystore.addTrustedSslCertificate(cacert, cacert.getSubjectX500Principal().getName());
                } catch (KeyManagementException e) {
                    log.error("Cannot add TLS certificate authority to host keystore {}", cacert.getSubjectX500Principal().getName());
                }
            }
//            My.configuration().get tls keystore trusted cas; add them to tlsKeystore  beforee making the policy  so that a global keystore can be used;  or just use the global kesytore...
//            return new TrustCaAndVerifyHostnameTlsPolicy(new KeystoreCertificateRepository(tlsKeystore));
            return TlsPolicyBuilder.factory().strict(tlsKeystore.getRepository()).build();
        }
        if (ucName.equals("TRUST_FIRST_CERTIFICATE")) {// XXX TODO   use TlsPolicyName  
//            return new TrustFirstCertificateTlsPolicy(new KeystoreCertificateRepository(tlsKeystore));
            KeystoreCertificateRepository repository = tlsKeystore.getRepository();
            return TlsPolicyBuilder.factory().strict(repository).trustDelegate(new FirstCertificateTrustDelegate(repository)).skipHostnameVerification().build();
        }
        if (ucName.equals("TRUST_KNOWN_CERTIFICATE")) {// XXX TODO   use TlsPolicyName  
//            return new TrustKnownCertificateTlsPolicy(new KeystoreCertificateRepository(tlsKeystore));
            return TlsPolicyBuilder.factory().strict(tlsKeystore.getRepository()).skipHostnameVerification().build();
        }
        if (ucName.equals("INSECURE")) {// XXX TODO   use TlsPolicyName  
            return new InsecureTlsPolicy();
        }
        throw new IllegalArgumentException("Unknown TLS Policy: " + tlsPolicyName);

    }

    @Deprecated
    private void initTlsTrustedCertificateAuthorities() throws IOException {
        // read the trusted CA's
        String tlsCaFilename = My.configuration().getConfiguration().getString("mtwilson.tls.certificate.file", "mtwilson-tls.pem");
        if (tlsCaFilename != null) {
            if (!tlsCaFilename.startsWith("/")) {
                tlsCaFilename = String.format("/etc/intel/cloudsecurity/%s", tlsCaFilename);// XXX TODO assuming linux ,but could be windows ... need to use platform-dependent configuration folder location
            }
            if (tlsCaFilename.endsWith(".pem")) {
                File tlsPemFile = new File(tlsCaFilename);
                if (tlsPemFile.lastModified() > tlsPemLastModified) {
                    tlsPemLastModified = tlsPemFile.lastModified();
                    tlsAuthorities.clear();
                    try (FileInputStream in = new FileInputStream(tlsPemFile)) {
                        String content = IOUtils.toString(in);
                        List<X509Certificate> cacerts = X509Util.decodePemCertificates(content);
                        tlsAuthorities.addAll(cacerts);
                    } catch (CertificateException e) {
                        log.error("Cannot read trusted TLS CA certificates", e);
                    }
                }
            }
            if (tlsCaFilename.endsWith(".crt")) {
                File tlsCrtFile = new File(tlsCaFilename);
                if (tlsCrtFile.lastModified() > tlsCrtLastModified) {
                    tlsCrtLastModified = tlsCrtFile.lastModified();
                    tlsAuthorities.clear();
                    try (FileInputStream in = new FileInputStream(tlsCrtFile)) {
                        byte[] content = IOUtils.toByteArray(in);
                        X509Certificate cert = X509Util.decodeDerCertificate(content);
                        tlsAuthorities.add(cert);
                    } catch (CertificateException e) {
                        log.error("Cannot read trusted TLS CA certificates", e);
                    }
                }
            }
        }
    }
    
//    protected abstract boolean accept(Object tlsPolicySubject);
    protected abstract TlsPolicyProvider getObjectTlsPolicyProvider();
    protected abstract StoredTlsPolicy.HostDescriptor getHostDescriptor();
    protected abstract StoredVendorTlsPolicy.VendorDescriptor getVendorDescriptor();
    
    protected List<TlsPolicyProvider> getProviders() {
        ArrayList<TlsPolicyProvider> providers = new ArrayList<>();
        providers.add(new GlobalTlsPolicy());
        providers.add(getObjectTlsPolicyProvider()); //providers.add(new ObjectTlsPolicy(txtHostRecord));
        providers.add(new StoredTlsPolicy(getHostDescriptor()));
        providers.add(new StoredVendorTlsPolicy(getVendorDescriptor()));
        providers.add(new DefaultTlsPolicy());
        return providers;
    }
    
    public TlsPolicy getTlsPolicy() {
        TlsPolicyChoiceReport tlsPolicyChoiceReport = getTlsPolicyChoiceReport();
        if( tlsPolicyChoiceReport == null ) {
            throw new TlsPolicyNotFoundException(getHostDescriptor().getInternetAddress()); 
        }
        TlsPolicy tlsPolicy = createTlsPolicy(tlsPolicyChoiceReport); // throws illegal argument exception if cannot create it
        return tlsPolicy;
    }
    
    public TlsPolicyChoiceReport getTlsPolicyChoiceReport() {
//        TlsPolicyChoiceReport report = new TlsPolicyChoiceReport();
        // find out which policy types are allowed
        Set<String> allowedPolicyTypes = My.configuration().getTlsPolicyAllow();
        // initalize the provider list
        List<TlsPolicyProvider> providers = getProviders();
        for(TlsPolicyProvider tlsPolicyProvider : providers) {
            log.debug("Checking provider {} for TLS policy for host {}", tlsPolicyProvider.getClass().getName(), getHostDescriptor().getInternetAddress());
            TlsPolicyChoice tlsPolicyChoice = tlsPolicyProvider.getTlsPolicyChoice();
            if( tlsPolicyChoice != null ) {
                // got a candidate choice from the provider, now we have to validate it
                TlsPolicyChoiceReport report = new TlsPolicyChoiceReport();
                report.setProviderClassName(tlsPolicyProvider.getClass().getName());
                report.setChoice(tlsPolicyChoice);
                report.setDescriptor(getTlsPolicyDescriptor(tlsPolicyChoice)); // will load it from database if the choice is a policy id, will be null if it's TRUST_FIRST_CERTIFICATE because that would be specified directly in the choice object from the provider;  maybe null for INSECURE or maybe a descriptor with confidentiality,integrity,and authentication false.
                // get the policy type
                String tlsPolicyType = getTlsPolicyType(report); // public-key, certificate, INSECURE, TRUST_FIRST_CERTIFICATE, or null
                if( tlsPolicyType == null ) {
                    log.debug("Cannot determine policy type; skipping choice");
                    continue;
                }
                if( !allowedPolicyTypes.contains(tlsPolicyType)) {
                    //log.debug("TLS policy type {} not in allowed list; skipping choice", tlsPolicyType);
                    //continue;
                    throw new TlsPolicyNotAllowedException(tlsPolicyType, getHostDescriptor().getInternetAddress());
                }
//                report.setPolicyType(tlsPolicyType);
                return report;
            }
        }
        return null;
    }
    
    private String getTlsPolicyType(TlsPolicyChoiceReport report) {
        String policyTypeFromDescriptor = null;
        String policyTypeFromName = null;
        // check the descriptor first because it's either the provided descriptor or the one loaded from database
        if( report.getDescriptor() != null ) { 
            policyTypeFromDescriptor = getTlsPolicyType(report.getDescriptor());
        }
        if( policyTypeFromDescriptor != null ) {
            return policyTypeFromDescriptor;
        }
        // check the policy name second in case it's one of the special policies   INSECURE or TRUST_FIRST_CERTIFICATE in the field from previous version
        if( report.getChoice().getTlsPolicyId() != null ) {
            policyTypeFromName = getTlsPolicyType(report.getChoice().getTlsPolicyId());
        }
        if( policyTypeFromName != null ) {
            return policyTypeFromName;
        }
        // intentionally not checking the tls policy id from the choice because if the choice did specify an id, it would already be loaded into the effective descriptor we checked above
        return null;
    }
    
    private TlsPolicyDescriptor getTlsPolicyDescriptor(TlsPolicyChoice tlsPolicyChoice) {
        if( tlsPolicyChoice.getTlsPolicyDescriptor() != null ) {
            return tlsPolicyChoice.getTlsPolicyDescriptor();
        }
        if( tlsPolicyChoice.getTlsPolicyId() != null ) {
            if( tlsPolicyChoice.getTlsPolicyId().equals("INSECURE") ) {
                TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
                tlsPolicyDescriptor.setName("INSECURE");
                tlsPolicyDescriptor.setProtection(new TlsProtection());
                tlsPolicyDescriptor.getProtection().encryption = false;
                tlsPolicyDescriptor.getProtection().integrity = false;
                tlsPolicyDescriptor.getProtection().authentication = false;
                return tlsPolicyDescriptor;
            }
            if( tlsPolicyChoice.getTlsPolicyId().equals("TRUST_FIRST_CERTIFICATE")) {
                TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
                tlsPolicyDescriptor.setName("TRUST_FIRST_CERTIFICATE");
                return tlsPolicyDescriptor;
            }
            log.debug("getTlsPolicyDescriptor: Unsupported tls policy name: {}", tlsPolicyChoice.getTlsPolicyId());
            return null; // other policy names are not currently supported, for example TRUST_KNOWN_CERTIFICATE and TRUST_CA_VERIFY_HOSTNAME or their newer names "public-key" and "certificate" are now represented by the descriptor
        }

        if( tlsPolicyChoice.getTlsPolicyId() != null ) {
            // need to load the actual policy it's pointing to
            TlsPolicyDescriptor tlsPolicyDescriptor = getTlsPolicyDescriptorForId(tlsPolicyChoice.getTlsPolicyId());
            if( tlsPolicyDescriptor == null ) {
                log.debug("Cannot load TLS Policy with ID {}", tlsPolicyChoice.getTlsPolicyId());
                return null;
            }
            return tlsPolicyDescriptor;
        }
        log.debug("getTlsPolicyDescriptor: choice did not have id, name, or descriptor");
        return null;
    }
    
    private String getTlsPolicyType(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( tlsPolicyDescriptor.getProtection() != null ) {
        if( !tlsPolicyDescriptor.getProtection().authentication ||
                !tlsPolicyDescriptor.getProtection().encryption  ||
                !tlsPolicyDescriptor.getProtection().integrity) {
            return "INSECURE"; // theoretically there might be some other type for integrity+authentication without encryption, but for now if it's not integrity+authentication+confidentiality we call it insecure
        }
        }
        return tlsPolicyDescriptor.getName(); // public-key, public-key-digest, certificate, certificate-digest, TRUST_FIRST_CERTIFICATE, INSECURE
        /*
        if( tlsPolicyDescriptor.getPublicKeys() != null && !tlsPolicyDescriptor.getPublicKeys().isEmpty() ) {
            return "public-key";
        }
        if( tlsPolicyDescriptor.getPublicKeyDigests() != null && !tlsPolicyDescriptor.getPublicKeyDigests().isEmpty() ) {
            return "public-key-digest";
        }
        if( tlsPolicyDescriptor.getCertificates() != null && !tlsPolicyDescriptor.getCertificates().isEmpty() ) {
            return "certificate";
        }
        if( tlsPolicyDescriptor.getCertificateDigests() != null && !tlsPolicyDescriptor.getCertificateDigests().isEmpty() ) {
            return "certificate-digest";
        }
        return null;
        */
    }
    
    private String getTlsPolicyType(String tlsPolicyName) {
        if( tlsPolicyName.equals("INSECURE") || tlsPolicyName.equals("TRUST_FIRST_CERTIFICATE") ) { return tlsPolicyName; }
        return null;
    }
    
    private ObjectMapper createObjectMapper() {
            ObjectMapper json = new ObjectMapper();
        json.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        json.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        json.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return json;
    }
    
    private TlsPolicyDescriptor getTlsPolicyDescriptorFromTlsPolicyRecord(TlsPolicyRecord tlsPolicyRecord) throws IOException {
        if( tlsPolicyRecord.getContentType() != null && tlsPolicyRecord.getContentType().equals(MediaType.APPLICATION_JSON)) {
             ObjectMapper json = createObjectMapper();
             TlsPolicyDescriptor tlsPolicyDescriptor = json.readValue(new String(tlsPolicyRecord.getContent(), Charset.forName("UTF-8")), TlsPolicyDescriptor.class);
             return tlsPolicyDescriptor;
        }
        return null;
    }
    
    private TlsPolicyDescriptor getTlsPolicyDescriptorForId(String tlsPolicyId) {
        if( !UUID.isValid(tlsPolicyId)) {
            log.warn("Invalid tlsPolicyId: {}", tlsPolicyId);
            return null;
        }
                    try (TlsPolicyDAO dao = TlsPolicyJdbiFactory.tlsPolicyDAO()) {
                        TlsPolicyRecord tlsPolicyRecord = dao.findTlsPolicyById(UUID.valueOf(tlsPolicyId));
                        TlsPolicyDescriptor tlsPolicyDescriptor = getTlsPolicyDescriptorFromTlsPolicyRecord(tlsPolicyRecord);
                        return tlsPolicyDescriptor; // could be null
                    }
                    catch(IOException e) {
                        log.error("Cannot close DAO", e);
                    return null;
                    }
    }

    private TlsPolicy createTlsPolicy(TlsPolicyChoiceReport report) {
        String policyName = report.getDescriptor().getName();
        log.debug("Trying to read TlsPolicy name {}", policyName);
        List<TlsPolicyReader> readers = Extensions.findAll(TlsPolicyReader.class);
        for(TlsPolicyReader reader : readers ) {
            try {
                log.debug("Trying to read TlsPolicy with {}", reader.getClass().getName());
                TlsPolicy tlsPolicy = reader.createTlsPolicy(report.getDescriptor()); // throws IllegalArgumentException
                if( tlsPolicy == null ) {
                    continue; // reader does not support the given descriptor
                }
                log.debug("Successfully created TlsPolicy with {}", reader.getClass().getName());
                return tlsPolicy;
            }
            catch(TlsPolicyDescriptorInvalidException e) { throw e; }
            catch(IllegalArgumentException e) {
                throw new TlsPolicyDescriptorInvalidException(e, report.getDescriptor());
            }
        }
        
        throw new IllegalArgumentException("Unsupported TLS policy choice");
    }
    

 

        
    /*
    public TlsPolicyBuilder descriptor(TlsPolicyDescriptor descriptor) {
        providesConfidentiality = descriptor.isConfidentialityRequired();
        providesAuthentication = descriptor.isAuthenticationRequired();
        providesIntegrity = descriptor.isIntegrityRequired();  
        hostnameVerification = descriptor.getCertificates() != null && !descriptor.getCertificates().isEmpty();
        // TODO: ciphers, protocols
        // TODO: build the certificate repository from the certificatse list...
        if( descriptor.getCertificates() != null && !descriptor.getCertificates().isEmpty() ) {
            HashSetMutableCertificateRepository repository = new HashSetMutableCertificateRepository();
            for(String certificateBase64 : descriptor.getCertificates()) {
                try {
                    X509Certificate certificate = X509Util.decodeDerCertificate(Base64.decodeBase64(certificateBase64));
                    repository.addCertificate(certificate);
                }
                catch(CertificateException e) {
                    log.error("Cannot read certificate: {}", certificateBase64, e);
                }
                catch(KeyManagementException e) {
                    log.debug("Cannot add certificate to repository: {}", certificateBase64, e);
                }
            }
            certificateRepository = repository;
        }
        else if( descriptor.getPublicKeys() != null && !descriptor.getPublicKeys().isEmpty() ) {
            HashSetMutablePublicKeyRepository repository = new HashSetMutablePublicKeyRepository();
            for(String publicKeyBase64 : descriptor.getCertificates()) {
                try {
                    PublicKey publicKey = RsaUtil.decodeDerPublicKey(Base64.decodeBase64(publicKeyBase64));
                    repository.addPublicKey(publicKey);
                }
                catch(CryptographyException e) {
                    log.error("Cannot read public key: {}", publicKeyBase64, e);
                }
                catch(KeyManagementException e) {
                    log.debug("Cannot add public key to repository: {}", publicKeyBase64, e);
                }
            }
            certificateRepository = repository;
            throw new UnsupportedOperationException("public key policy"); // TODO: also need a corresponding public key policy implementation that will compare just the public keys and not do anything with certificates (also no hostname verification)
        }
        trustDelegate = null;
        return this;
    }
*/
    
    /**
     * This method creates a TlsPolicyFactory instance appropriate for the 
     * given input. If one cannot be created, it throws UnsupportedOperationException.
     * 
     * To add new input types, create a subclass of TlsPolicyFactory for that
     * input type. Register the subclass using Java SPI or Mt Wilson Extensions
     * (when that is implemented) or edit this method if SPI or Extensions are
     * not yet implemented.
     * 
     * @param tlsPolicySubject can be a TxtHostRecord instance or a TblHosts record instance
     * @return a new TlsPolicyFactory that can create a TlsPolicyDescriptor and a TlsPolicy for the given tlsPolicySubject
     */
    public static TlsPolicyFactory createFactory(Object tlsPolicySubject) {
        TlsPolicyFactory factoryExtension = Extensions.require(TlsPolicyFactory.class, tlsPolicySubject);
        return factoryExtension;
        /*
        // we could use Java's SPI or Mt Wilson's Extensions here to dynamically find a subclasses of TlsPolicyFactory for the given input
        if( tlsPolicySubject instanceof TxtHostRecord ) {
            return new TxtHostRecordTlsPolicyFactory((TxtHostRecord) tlsPolicySubject);
        }
        if( tlsPolicySubject instanceof TblHosts ) {
            return new TblHostsTlsPolicyFactory((TblHosts) tlsPolicySubject);
        }
        throw new UnsupportedOperationException("No TlsPolicyFactory available for subject of type "+tlsPolicySubject.getClass().getName());
        */
    }

}
