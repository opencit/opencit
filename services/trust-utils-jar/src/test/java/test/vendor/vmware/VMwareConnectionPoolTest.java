/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.vmware;

import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.tls.ArrayCertificateRepository;
import com.intel.mtwilson.tls.KeystoreCertificateRepository;
import com.intel.mtwilson.tls.TlsPolicy;
import com.intel.mtwilson.tls.TrustFirstCertificateTlsPolicy;
import com.intel.mtwilson.tls.TrustKnownCertificateTlsPolicy;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class VMwareConnectionPoolTest {
    private transient Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String host1 = "vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;10.1.71.173";
    
    public HostAgent getAgentWithMyKeystore() throws KeyManagementException, IOException {
        SimpleKeystore keystore = new SimpleKeystore(My.configuration().getKeystoreFile(), My.configuration().getKeystorePassword());
        TlsPolicy tlsPolicy = new TrustFirstCertificateTlsPolicy(new KeystoreCertificateRepository(keystore));
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent hostAgent = factory.getHostAgent(new ConnectionString(host1), tlsPolicy); //factory.getHostAgent(host);
        return hostAgent;
    }

    public HostAgent getAgentWithDenyAllTlsPolicy() throws KeyManagementException, IOException {
        TlsPolicy tlsPolicy = new TrustKnownCertificateTlsPolicy(new ArrayCertificateRepository(new X509Certificate[0]));
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent hostAgent = factory.getHostAgent(new ConnectionString(host1), tlsPolicy); //factory.getHostAgent(host);
        return hostAgent;
    }
    
    private ByteArrayResource resource;
    private SimpleKeystore keystore;
    private KeystoreCertificateRepository repository;
    public HostAgent getAgentWithEmptyKeystore() throws KeyManagementException, IOException {
        resource = new ByteArrayResource();
        keystore = new SimpleKeystore(resource, My.configuration().getKeystorePassword());
        repository = new KeystoreCertificateRepository(keystore);
        TlsPolicy tlsPolicy = new TrustFirstCertificateTlsPolicy(repository);
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent hostAgent = factory.getHostAgent(new ConnectionString(host1), tlsPolicy); //factory.getHostAgent(host);
        return hostAgent;
    }

    @Test
    public void testConnect() throws KeyManagementException, IOException {
        HostAgent agent = getAgentWithMyKeystore(); //getAgentWithEmptyKeystore(); //getAgentWithDenyAllTlsPolicy(); //
        PcrManifest pcrManifest = agent.getPcrManifest();
        log.debug("Pcr manifest is valid? {}", pcrManifest.isValid());
        List<X509Certificate> certs = repository.getCertificates();
        for(X509Certificate cert : certs) {
            log.debug("Certificate subject: {}", cert.getSubjectX500Principal().getName());
        }
    }

    @Test
    public void testSaveTlsCertificateIntoKeystore() throws KeyManagementException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        ConnectionString connstr = new ConnectionString(host1);
        HostAgent agent =  getAgentWithEmptyKeystore();
        PcrManifest pcrManifest = agent.getPcrManifest();
        log.debug("Pcr manifest is valid? {}", pcrManifest.isValid());
        SimpleKeystore keystore = new SimpleKeystore(My.configuration().getKeystoreFile(), My.configuration().getKeystorePassword());
        List<X509Certificate> certs = repository.getCertificates();
        for(X509Certificate cert : certs) {
            log.debug("Certificate subject: {}", cert.getSubjectX500Principal().getName());
            keystore.addTrustedSslCertificate(cert, connstr.getHostname().toString());
            keystore.save();
        }
    }

}
