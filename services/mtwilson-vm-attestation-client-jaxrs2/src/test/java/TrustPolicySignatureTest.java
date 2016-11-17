/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.My;
import com.intel.mtwilson.vm.attestation.client.jaxrs2.TrustPolicySignature;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.trustpolicy1.xml.TrustPolicy;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author boskisha
 */
public class TrustPolicySignatureTest{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustPolicySignatureTest.class);

    private static TrustPolicySignature client = null;
    
    public TrustPolicySignatureTest() {        
    }
    
    @BeforeClass
    public static void setUpClass()throws Exception {
        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.class);
        Properties p = My.configuration().getClientProperties();
        client = new TrustPolicySignature(p);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testTrustPolicySignature() throws Exception{
        String policy = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TrustPolicy xmlns=\"mtwilson:trustdirector:policy:1.1\"><Director><CustomerId>testId</CustomerId></Director><Image><ImageId>cb59ff02-a645-410c-bd4e-a80c6729329d</ImageId><ImageHash DigestAlg=\"sha1\">995dd5b5df990267bdfab238f5fe5f3a80e24540</ImageHash></Image><LaunchControlPolicy>MeasureAndEnforce</LaunchControlPolicy><Encryption><Key URL=\"uri\">http://10.1.68.42/v1/keys/e5da7472-222b-4000-8b62-9d3a566b2716/transfer</Key><Checksum DigestAlg=\"md5\">9b760e45a66a2923d29df3a70addd5ea</Checksum></Encryption><Whitelist DigestAlg=\"sha1\"></Whitelist></TrustPolicy>";
        String signedPolicy = client.signTrustPolicy(policy);
        log.debug("Signed Policy Is: "+signedPolicy);
    }

}
