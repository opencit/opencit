/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.api.v2;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.tpm.endorsement.client.jaxrs.TpmEndorsements;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsement;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementCollection;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementFilterCriteria;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class TpmEndorsementClientTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmEndorsementClientTest.class);

    private static TpmEndorsements client = null;
    private static ArrayList<TpmEndorsement> created = new ArrayList<>();
    
    @Test
    public void testCreateTpmEndorsement() throws Exception {
        KeyPair key = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate certificate = X509Builder.factory().selfSigned("CN=test", key).build();
        UUID id = new UUID();
        TpmEndorsement endorsement = new TpmEndorsement();
        endorsement.setId(id);
        endorsement.setIssuer(certificate.getIssuerX500Principal().getName());
        endorsement.setCertificate(certificate.getEncoded());
        endorsement.setComment("test");
//        TpmEndorsements client = new TpmEndorsements(testProperties);
//        log.debug("Created the new tpm endorsement with id {}.", testTpmEndorsement.getId());
        client.createTpmEndorsement(endorsement);
        created.add(endorsement);
    }
    
    @Test
    public void testSearchTpmEndorsement() throws Exception {
        testCreateTpmEndorsement();
        for(TpmEndorsement endorsement : created) {
            TpmEndorsementFilterCriteria criteria = new TpmEndorsementFilterCriteria();
            criteria.issuerContains = "new tpm endorsement with";
            TpmEndorsementCollection collection = client.searchTpmEndorsements(criteria);
            
        }
        
    }    
}
