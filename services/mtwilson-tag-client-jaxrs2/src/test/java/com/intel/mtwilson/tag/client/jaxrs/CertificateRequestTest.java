/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;
import com.intel.mtwilson.tag.model.CertificateRequest;
import java.security.cert.X509Certificate;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class CertificateRequestTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateRequestTest.class);

    private static CertificateRequests client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new CertificateRequests(My.configuration().getClientProperties());
    }
    
    @Test
    public void certificateRequestTest() {

        /*String encryptedContent = "U2FsdGVkX19foheWuO2gmlwyOdHwwnGZydv8BYR9adE+QujdXx/+w2Lm8wa6bZgp+srGGrTC08Zp8cLHaCs4Bep/ARaCfW86PwH0v0obpgm03P0pKkRZcT7NWKDXz105zPONQJ1HyX6PAv1SuplXAD6rgv2lSTG1Q8jc0fdmMphR1mjv2j3nxfVcy4b195jJGXu63upueaY3bRr12YdWgcxMUFY9kwTgCQgXS2V4KqbQ6degKGrTi1ghoDF5r+R35LbKz1sQEiJ6KI+x31/yr4h5MCPbuh58VwxkDC0XHdKNezm2WAGTanYZoUWQDW69cO7oYbI3TFG07299dIlBPY0dgRxPhGgIxKncuMmI28NjKnJrc3klMED7R0AZkS11FzfcBikSbchxGf4C3iGvm/dBG+8sOBAaA8Gkbc5zfSiibTl9maT+WN974P0JoM6aAl8K/CSAni8Q5wl06rg9RrFeVpYCmjshF7KeOqlipK3Ps1CQ8CoZUA9PyWmu2y0mrCmzkkwi+KN0CVbCWOOntmLfrlNXcP3Nh/KbldwTB7VRM+qIoaxgYIUxq6RVT7tRBmXulZlU5fZLTvETnydu1qoFFukhbYo7x3PHm+K4neZukrvytF09QyZJjZCqedP44r0yw7/vWSCR1m4T8uB+PqaqGSriENvVa1uu3o4dQzw5U2abZ767TIcI6h02P63wzCkYbeW+Kell13gPsEeQISRUvIYDD+eVXKmEGHesbbBO9G0pD2SO5bIVyHNqTKZNHQwAzn8M9id5ippNBclJ+J2aWdI8AOxPZDNwT4KoibUh0z3jHf0rXgMmRPyFhW8iLaLKofUaiRm86nQ3NTLBWWCl6Ga7pWsBVshcM2Fh+PIwaDNGQLmbZPKE3s8S/zBBAfTM5TcZTHFsqi18lOi1A+GlgyBUza0ssQF4rqahAhL3gMRc2Gk9NQlQSwZ8p+v1UefTAUxvkBpq4MLLAfVwePomHE1L9LZVjFK+dRm3M6TCis1Qg7Ve2ThBYtgVmer++yFymvXn4QAe1k3ihOjsfTtr106xEL8qDK6/81mRs9fSs6r4wvt90x3uCwWbL6+mSKt0fxy5cgnUJ/jJ7Eoql7uotQsAUUdTLR1AVkvKop31581FtryXCGoTYP00tCMuD+uZH5ZzF6qBsOOk8ukJko3a9Fo9yKLALw==";
        CertificateRequest obj = new CertificateRequest();
        obj.setContent(encryptedContent.getBytes());
        obj.setSubject("064866ea-620d-11e0-b1a9-001e671043c4");
        obj = client.createCertificateRequest(obj);
        
        CertificateRequest eObj = new CertificateRequest();
        eObj.setId(obj.getId());
        eObj.setStatus("APPROVED");
        eObj = client.editCertificateRequest(eObj);
        
        CertificateRequest sObj = client.retrieveCertificateRequest(obj.getId());
        log.debug("Retrieved the certificate request with id {} and status {} successfully.", sObj.getId().toString(), sObj.getStatus());*/
        
        client.deleteCertificateRequest(UUID.valueOf("0eae2c80-9d4a-4abf-9785-de90f0dd0f51"));
    }
         
}
