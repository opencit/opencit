/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.dcsg.cpg.extensions.Plugins;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tls.policy.TlsProtection;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyDAO;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyJdbiFactory;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyRecord;
import com.intel.mtwilson.tls.policy.provider.DefaultTlsPolicyProvider;
import com.intel.mtwilson.tls.policy.provider.GlobalTlsPolicyProvider;
import com.intel.mtwilson.tls.policy.provider.StoredTlsPolicyProvider;
import com.intel.mtwilson.tls.policy.provider.StoredVendorTlsPolicyProvider;
import com.intel.mtwilson.tls.policy.codec.impl.JsonTlsPolicyReader;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;

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
    private static final String INSECURE = "INSECURE";
    private static final String TRUST_FIRST_CERTIFICATE = "TRUST_FIRST_CERTIFICATE";
    
//    protected abstract boolean accept(Object tlsPolicySubject);
    protected abstract TlsPolicyProvider getObjectTlsPolicyProvider();
    protected abstract StoredTlsPolicyProvider.HostDescriptor getHostDescriptor();
    protected abstract StoredVendorTlsPolicyProvider.VendorDescriptor getVendorDescriptor();
    
    protected List<TlsPolicyProvider> getProviders() {
        ArrayList<TlsPolicyProvider> providers = new ArrayList<>();
        providers.add(new GlobalTlsPolicyProvider());
        providers.add(getObjectTlsPolicyProvider()); //providers.add(new ObjectTlsPolicy(txtHostRecord));
        providers.add(new StoredTlsPolicyProvider(getHostDescriptor()));
        providers.add(new StoredVendorTlsPolicyProvider(getVendorDescriptor()));
        providers.add(new DefaultTlsPolicyProvider());
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
                report.setChoice(tlsPolicyChoice); // choice is what needs to be saved back to the record, if applicable: a shared /tls policy id, or inline descriptor
                report.setDescriptor(getTlsPolicyDescriptor(tlsPolicyChoice)); // this descriptor is the effective policy, so if the choice is a shared policy id this is the descriptor that was loaded from that shared policy. will load it from database if the choice is a policy id, will be null if it's TRUST_FIRST_CERTIFICATE because that would be specified directly in the choice object from the provider;  maybe null for INSECURE or maybe a descriptor with confidentiality,integrity,and authentication false.
                // get the policy type
                String tlsPolicyType = getTlsPolicyType(report); // certificate, certificate-digest, public-key, public-key-digest, INSECURE, TRUST_FIRST_CERTIFICATE, or null
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
        // check the policy id second in case it's one of the special policies   INSECURE or TRUST_FIRST_CERTIFICATE in the field from previous version
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
            // first check for the special policy "id" INSECURE and TRUST_FIRST_CERTIFICATE
            if( tlsPolicyChoice.getTlsPolicyId().equals(INSECURE) ) {
                TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
                tlsPolicyDescriptor.setPolicyType(INSECURE);
                tlsPolicyDescriptor.setProtection(new TlsProtection());
                tlsPolicyDescriptor.getProtection().encryption = false;
                tlsPolicyDescriptor.getProtection().integrity = false;
                tlsPolicyDescriptor.getProtection().authentication = false;
                return tlsPolicyDescriptor;
            }
            if( tlsPolicyChoice.getTlsPolicyId().equals(TRUST_FIRST_CERTIFICATE)) {
                TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
                tlsPolicyDescriptor.setPolicyType(TRUST_FIRST_CERTIFICATE);
                return tlsPolicyDescriptor;
            }
            // it it's not a special ID, then it must be an ID of an existing mw_tls_policy record so try to load it
            TlsPolicyDescriptor tlsPolicyDescriptor = getTlsPolicyDescriptorForId(tlsPolicyChoice.getTlsPolicyId());
            if( tlsPolicyDescriptor != null ) {
                return tlsPolicyDescriptor; // found it
            }
            log.debug("getTlsPolicyDescriptor: Cannot load tls policy id: {}", tlsPolicyChoice.getTlsPolicyId());
            return null; // other policy names are not currently supported, for example TRUST_KNOWN_CERTIFICATE and TRUST_CA_VERIFY_HOSTNAME or their newer names "public-key" and "certificate" are now represented by the descriptor
        }
        log.debug("getTlsPolicyDescriptor: choice did not have id, name, or descriptor");
        return null;
    }
    
    private String getTlsPolicyType(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( tlsPolicyDescriptor.getPolicyType() != null ) {
            return tlsPolicyDescriptor.getPolicyType();// public-key, public-key-digest, certificate, certificate-digest, TRUST_FIRST_CERTIFICATE, INSECURE
        }
        if( tlsPolicyDescriptor.getProtection() != null ) {
        if( !tlsPolicyDescriptor.getProtection().authentication ||
                !tlsPolicyDescriptor.getProtection().encryption  ||
                !tlsPolicyDescriptor.getProtection().integrity) {
            return INSECURE; // theoretically there might be some other type for integrity+authentication without encryption, but for now if it's not integrity+authentication+confidentiality we call it insecure
        }
        }
        return null;
    }
    
    private String getTlsPolicyType(String tlsPolicyName) {
        if( tlsPolicyName.equals(INSECURE) || tlsPolicyName.equals(TRUST_FIRST_CERTIFICATE) ) { return tlsPolicyName; }
        return null;
    }
    
    
    private TlsPolicyDescriptor getTlsPolicyDescriptorFromTlsPolicyRecord(TlsPolicyRecord tlsPolicyRecord) throws IOException {
        JsonTlsPolicyReader reader = new JsonTlsPolicyReader();
        if( reader.accept(tlsPolicyRecord.getContentType()) && tlsPolicyRecord.getContent() != null && tlsPolicyRecord.getContent().length > 0 ) {
            return reader.read(tlsPolicyRecord.getContent());
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

    /**
     * Subclasses can override this method to process the final tls policy
     * as necessary.  Overriding methods should call this super method
     * to continue.
     * @param report
     * @return 
     */
    protected TlsPolicy createTlsPolicy(TlsPolicyChoiceReport report) {
//        return createTlsPolicy(report.getDescriptor());
        return TlsPolicyFactoryUtil.createTlsPolicy(report.getDescriptor());   //called new TlsPolicyFactoryUtil from common-java for method createTlsPolicy to accommodate for descriptor with value: com.intel.mtwilson.tls.policy.TlsPolicyDescriptor
    }
    
    
    /**
     * Can be used to instantiate a TlsPolicy from an abbreviated descriptor
     * using two String variables which can be stored by classes without any
     * dependency on mtwilson-tls-policy projects. 
     * @param policyType for example "certificate", "certificate-digest", "public-key", "public-key-digest", or "INSECURE"
     * @param policyData can be a base64 or hex encoded certificate, certificate digest, public key, or public key digest as appropriate for the selected policy
     * @return 
     */
    public static TlsPolicy createTlsPolicy(String policyType, String policyData) {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType(policyType);
        tlsPolicyDescriptor.setData(new ArrayList<String>());
        tlsPolicyDescriptor.getData().add(policyData);
//        tlsPolicyDescriptor.setMeta(new HashMap<String,String>());
//        if( HexUtil.isHex(policyData) ) { tlsPolicyDescriptor.getMeta().put("digestEncoding", "hex"); }
//        if( Base64Util.isBase64(policyData) ) { tlsPolicyDescriptor.getMeta().put("digestEncoding", "base64"); }
        return createTlsPolicy(tlsPolicyDescriptor);
    }

 
    /**
     * Shortcut for TlsPolicyFactory.createFactory(subject).getTlsPolicy()
     * 
     * @param tlsPolicySubject could be an instance of TxtHostRecord, TblHosts, or any other class for which there exists a TlsPolicyFactory implementation
     * @return 
     */
    public static TlsPolicy createTlsPolicy(Object tlsPolicySubject) {
        TlsPolicyFactory factory = createFactory(tlsPolicySubject);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        return tlsPolicy;
    }


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
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            log.debug("TlsPolicyFactory createFactory: {}", mapper.writeValueAsString(tlsPolicySubject)); //This statement may contain clear text passwords
//        } catch (Exception e) {
//            log.warn("Cannot write debug log", e);
//        }
        log.debug("createFactory for policy class {}", tlsPolicySubject.getClass().getName());
        TlsPolicyFactory factoryExtension = Plugins.findByConstructor(TlsPolicyFactory.class, tlsPolicySubject); //Extensions.require(TlsPolicyFactory.class, tlsPolicySubject);
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

    public static TlsPolicyDescriptor createTlsPolicyDescriptorFromKeystore(SimpleKeystore tlsKeystore) {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("certificate");
        tlsPolicyDescriptor.setProtection(TlsPolicyFactoryUtil.getAllTlsProtection());
        ArrayList<String> encodedCertificates = new ArrayList<>();
        tlsPolicyDescriptor.setData(encodedCertificates);
        ArrayList<X509Certificate> certificates = new ArrayList<>();
        certificates.addAll(V1TlsPolicyFactory.getMtWilsonTrustedTlsCertificates());
        certificates.addAll(TlsPolicyFactoryUtil.getTrustedTlsCertificatesFromSimpleKeystore(tlsKeystore));
        for (X509Certificate cert : certificates) {
            log.debug("Adding trusted TLS certs and cacerts: {}", cert.getSubjectX500Principal().getName());
            try {
                encodedCertificates.add(Base64.encodeBase64String(cert.getEncoded()));
            } catch (CertificateEncodingException e) {
                throw new IllegalArgumentException("Invalid certificate", e);
            }
        }
        return tlsPolicyDescriptor;
    }

    public static TlsPolicyDescriptor getTlsPolicyDescriptorFromResource(Resource resource) {
        return getTlsPolicyDescriptorFromResource(resource, My.configuration().getTlsKeystorePassword());
    }

    public static TlsPolicyDescriptor getTlsPolicyDescriptorFromResource(Resource resource, String password) {
        return getTlsPolicyDescriptorFromResource(resource, password);
    }
    
}
