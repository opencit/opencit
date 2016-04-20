/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test.tag;

import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;
import com.intel.mtwilson.tag.rest.v2.repository.CertificateRepository;
import com.intel.mtwilson.test.IntegrationTest;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author rksavino
 */
public class RetrieveCertificateTest extends IntegrationTest {
    
    @Test
    public void testRetrieveCertificate() throws IOException {
        CertificateRepository certificateRepository = new CertificateRepository();
        CertificateFilterCriteria criteria = new CertificateFilterCriteria();
        criteria.subjectEqualTo = "e1ca94c1-cb01-11df-a441-001517fa99c0";
        CertificateCollection results = certificateRepository.search(criteria); // DONE: TODO:  order by creation date so we get most recent first, and we pick the most recently created cert that is currently valid. 
        Certificate certificate = results.getCertificates().get(0);
        FileUtils.writeByteArrayToFile(new File("./target/certbytes"), certificate.getCertificate());
        certificate.getX509Certificate();
    }
}
