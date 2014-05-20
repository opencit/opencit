/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateLocator;
import com.intel.mtwilson.user.management.rest.v2.repository.UserLoginCertificateRepository;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class UserLoginCertificateTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginCertificateTest.class);
    
    @Test
    public void testUserLoginCertificate() throws Exception {

        UserLoginCertificateRepository repo = new UserLoginCertificateRepository();
        
        KeyPair keyPair;
        X509Certificate certificate;
        String userName = "superadmin1";
        UUID userId = UUID.valueOf("8d29aa87-386d-490d-9491-ab0be4f5e7f9");
        keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        certificate = X509Builder.factory().selfSigned(String.format("CN=%s", userName), keyPair).expires(365, TimeUnit.DAYS).build();
        
        UUID userLoginCertId = new UUID();

        UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
        userLoginCertificate.setId(userLoginCertId);
        userLoginCertificate.setUserId(userId);
        userLoginCertificate.setCertificate(certificate.getEncoded());
        userLoginCertificate.setComment("Self signed cert.");
        userLoginCertificate.setExpires(certificate.getNotAfter());
        userLoginCertificate.setSha1Hash(Sha1Digest.digestOf(certificate.getEncoded()).toByteArray());
        userLoginCertificate.setSha256Hash(Sha256Digest.digestOf(certificate.getEncoded()).toByteArray());
        repo.create(userLoginCertificate);
        
        UserLoginCertificateFilterCriteria criteria = new UserLoginCertificateFilterCriteria();
        criteria.id = userLoginCertId;
        UserLoginCertificateCollection search = repo.search(criteria);
        for (UserLoginCertificate obj : search.getUserLoginCertificates()) {
            log.debug("User login certificate retrieved has roles {}", obj.getRoles().toString());
        }

        userLoginCertificate.setEnabled(true);
        userLoginCertificate.setStatus(Status.APPROVED);
        List<String> roleSet = new ArrayList<>(Arrays.asList("administrator", "tagadmin"));
        userLoginCertificate.setRoles(roleSet);
        repo.store(userLoginCertificate);
        
        UserLoginCertificateLocator locator = new UserLoginCertificateLocator();
        locator.id = userLoginCertId;
        UserLoginCertificate retrieve = repo.retrieve(locator);
        log.debug("User login password retrieved has roles {}", retrieve.getRoles().toString());

        repo.delete(locator);
        
    }    
    
}
