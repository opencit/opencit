/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.tls;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.TrustKnownCertificateTlsPolicy;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactory;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyReader;
import com.intel.mtwilson.tls.policy.factory.impl.TxtHostRecordTlsPolicyFactory;
import com.intel.mtwilson.tls.policy.reader.impl.InsecureDescriptor;
import com.intel.mtwilson.tls.policy.reader.impl.InsecureTrustFirstCertificateDescriptor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyFactoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyFactoryTest.class);

    /**
     * If mtwilson.tls.policy.allow includes INSECURE, this test will succeed
     * If it does not include INSECURE, then TlsPolicyNotAllowedException will
     * be thrown with the message "INSECURE"
     */
    @Test
    public void testTlsPolicyFactoryWithTxtHostRecordInsecure() {
        Extensions.register(TlsPolicyFactory.class, TxtHostRecordTlsPolicyFactory.class);
        Extensions.register(TlsPolicyReader.class, InsecureDescriptor.class);
        TxtHostRecord host = new TxtHostRecord();
        host.HostName = "test1";
        host.AddOn_Connection_String = "intel:https://localhost:1443";
        TlsPolicyChoice insecureChoice = new TlsPolicyChoice();
        insecureChoice.setTlsPolicyId("INSECURE");
        host.setTlsPolicyChoice(insecureChoice);
        TlsPolicyFactory factory = TlsPolicyFactory.createFactory(host);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        assertEquals(InsecureTlsPolicy.class, tlsPolicy.getClass());
    }

    /**
     * If mtwilson.tls.policy.allow includes TRUST_FIRST_CERTIFICATE, this test will succeed
     * If it does not include TRUST_FIRST_CERTIFICATE, then TlsPolicyNotAllowedException will
     * be thrown with the message "TRUST_FIRST_CERTIFICATE"
     */
    @Test
    public void testTlsPolicyFactoryWithTxtHostRecordTrustFirstCertificate() {
        Extensions.register(TlsPolicyFactory.class, TxtHostRecordTlsPolicyFactory.class);
        Extensions.register(TlsPolicyReader.class, InsecureTrustFirstCertificateDescriptor.class);
        TxtHostRecord host = new TxtHostRecord();
        host.HostName = "test1";
        host.AddOn_Connection_String = "intel:https://localhost:1443";
        TlsPolicyChoice insecureChoice = new TlsPolicyChoice();
        insecureChoice.setTlsPolicyId("TRUST_FIRST_CERTIFICATE");
        host.setTlsPolicyChoice(insecureChoice);
        TlsPolicyFactory factory = TlsPolicyFactory.createFactory(host);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        assertEquals(TrustKnownCertificateTlsPolicy.class, tlsPolicy.getClass()); // the "trust first certificate" is implemented by TrustKnownCertificateTlsPolicy with a FirstCertificateTrustDelegate
    }

    /**
     * If mtwilson.tls.policy.allow includes INSECURE, and also if
     * mtwilson.default.tls.policy.id = INSECURE then this test will succeed
     * If it does not include INSECURE, then TlsPolicyNotAllowedException will
     * be thrown with the message "INSECURE", and if there is no default policy
     * set, then it will throw IllegalArgumentException no default tls policy
     */
    @Test
    public void testTlsPolicyFactoryWithTxtHostRecordTrustDefaultInsecure() {
        Extensions.register(TlsPolicyFactory.class, TxtHostRecordTlsPolicyFactory.class);
        Extensions.register(TlsPolicyReader.class, InsecureDescriptor.class);
        TxtHostRecord host = new TxtHostRecord();
        host.HostName = "test1";
        host.AddOn_Connection_String = "intel:https://localhost:1443";
        TlsPolicyFactory factory = TlsPolicyFactory.createFactory(host);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        assertEquals(InsecureTlsPolicy.class, tlsPolicy.getClass());
    }
    
}
