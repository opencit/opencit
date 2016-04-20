/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.tls;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
//import com.intel.dcsg.cpg.tls.policy.impl.TrustKnownCertificateTlsPolicy;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.agent.vmware.VmwareHostAgentFactory;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactory;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.impl.TxtHostRecordTlsPolicyFactory;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTrustFirstPublicKeyTlsPolicyCreator;
import java.io.IOException;
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
        Extensions.register(TlsPolicyCreator.class, InsecureTlsPolicyCreator.class);
        TxtHostRecord host = new TxtHostRecord();
        host.HostName = "test1";
        host.AddOn_Connection_String = "intel:https://localhost:1443";
        TlsPolicyChoice insecureChoice = new TlsPolicyChoice();
        insecureChoice.setTlsPolicyId("INSECURE");
        host.tlsPolicyChoice = insecureChoice;
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
        Extensions.register(TlsPolicyCreator.class, InsecureTrustFirstPublicKeyTlsPolicyCreator.class);
        TxtHostRecord host = new TxtHostRecord();
        host.HostName = "test1";
        host.AddOn_Connection_String = "intel:https://localhost:1443";
        TlsPolicyChoice insecureChoice = new TlsPolicyChoice();
        insecureChoice.setTlsPolicyId("TRUST_FIRST_CERTIFICATE");
        host.tlsPolicyChoice = insecureChoice;
        TlsPolicyFactory factory = TlsPolicyFactory.createFactory(host);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        assertEquals(PublicKeyTlsPolicy.class, tlsPolicy.getClass()); // the "trust first certificate" is implemented by TrustKnownCertificateTlsPolicy with a FirstCertificateTrustDelegate
    }
    
    /**
     * Test the TRUST_FIRST_CERTIFICATE policy implementation to save the 
     * remote server certificate back to the TxtHostRecord object.
     * 
     * Sample output:
     * <pre>
     * Original host record: {"HostName":"10.1.71.173","IPAddress":null,"Port":null,"BIOS_Name":null,"BIOS_Version":null,"BIOS_Oem":null,"VMM_Name":null,"VMM_Version":null,"VMM_OSName":null,"VMM_OSVersion":null,"AddOn_Connection_String":"vmware:https://10.1.71.162/sdk;Administrator;intel123!","Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null,"tlsPolicyChoice":{"tlsPolicyId":"TRUST_FIRST_CERTIFICATE","tlsPolicyDescriptor":null}}
     * Edited host record: {"HostName":"10.1.71.173","IPAddress":null,"Port":null,"BIOS_Name":null,"BIOS_Version":null,"BIOS_Oem":null,"VMM_Name":null,"VMM_Version":null,"VMM_OSName":null,"VMM_OSVersion":null,"AddOn_Connection_String":"vmware:https://10.1.71.162/sdk;Administrator;intel123!","Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null,"tlsPolicyChoice":{"tlsPolicyId":"TRUST_FIRST_CERTIFICATE","tlsPolicyDescriptor":null}}
     * </pre>
     * @throws IOException 
     */
    @Test
    public void testTlsPolicyFactoryWithTxtHostRecordTrustFirstCertificateSaveToRecord() throws IOException {
        Extensions.register(TlsPolicyFactory.class, TxtHostRecordTlsPolicyFactory.class);
        Extensions.register(TlsPolicyCreator.class, InsecureTrustFirstPublicKeyTlsPolicyCreator.class);
        Extensions.register(VendorHostAgentFactory.class, VmwareHostAgentFactory.class);
        ObjectMapper mapper = new ObjectMapper();
        TxtHostRecord host = new TxtHostRecord();
        host.HostName = "10.1.71.173";
        host.AddOn_Connection_String = "vmware:https://10.1.71.162/sdk;Administrator;intel123!";
        TlsPolicyChoice insecureChoice = new TlsPolicyChoice();
        insecureChoice.setTlsPolicyId("TRUST_FIRST_CERTIFICATE");
        host.tlsPolicyChoice = insecureChoice;
        TlsPolicyFactory factory = TlsPolicyFactory.createFactory(host);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        assertEquals(PublicKeyTlsPolicy.class, tlsPolicy.getClass()); // the "trust first certificate" is implemented by TrustKnownCertificateTlsPolicy with a FirstCertificateTrustDelegate
        log.debug("Original host record: {}", mapper.writeValueAsString(host));
        HostAgentFactory hostAgentFactory = new HostAgentFactory();
        HostAgent agent = hostAgentFactory.getHostAgent(host);
        TxtHostRecord hostDetails = agent.getHostDetails();
        assertNotNull(hostDetails);
        assertNotNull(hostDetails.BIOS_Name); // just one of the attributes that gets set
        log.debug("Edited host record: {}", mapper.writeValueAsString(host));
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
        Extensions.register(TlsPolicyCreator.class, InsecureTlsPolicyCreator.class);
        TxtHostRecord host = new TxtHostRecord();
        host.HostName = "test1";
        host.AddOn_Connection_String = "intel:https://localhost:1443";
        TlsPolicyFactory factory = TlsPolicyFactory.createFactory(host);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        assertEquals(InsecureTlsPolicy.class, tlsPolicy.getClass());
    }
    
}
