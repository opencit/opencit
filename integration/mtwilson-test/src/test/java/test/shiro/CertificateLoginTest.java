/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.shiro;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.security.http.RsaAuthorization;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CertificateLoginTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateLoginTest.class);
    private static KeyPair keyPair;
    private static X509Certificate certificate;
    private static String username = "admin";
    private static User user;
    private static UserLoginCertificate userLoginCertificate;

    @BeforeClass
    public static void createUserLoginCertificate() throws Exception {
        user = new User();
        user.setId(new UUID());
        user.setComment("automatically created by setup");
        user.setUsername(username);
        keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        certificate = X509Builder.factory().selfSigned(String.format("CN=%s", username), keyPair).expires(365, TimeUnit.DAYS).build();
        userLoginCertificate = new UserLoginCertificate();
        userLoginCertificate.setId(new UUID());
        userLoginCertificate.setCertificate(certificate.getEncoded());
        userLoginCertificate.setComment("automatically created by setup");
        userLoginCertificate.setEnabled(true);
        userLoginCertificate.setExpires(certificate.getNotAfter());
        userLoginCertificate.setSha1Hash(Sha1Digest.digestOf(certificate.getEncoded()).toByteArray());
        userLoginCertificate.setSha256Hash(Sha256Digest.digestOf(certificate.getEncoded()).toByteArray());
        userLoginCertificate.setStatus(Status.APPROVED);
        userLoginCertificate.setUserId(user.getId());

    }

    private byte[] signature;
    private byte[] digest; // of the docment being signed
    
    /**
     * example output:
     * Authorization: X509 fingerprint="byQSuPelf/1+eiQid7QE1YJwPu9hnpdvh5d/gy1Exts=", headers="X-Nonce,Date", algorithm="SHA256withRSA", signature="iW5NhHULEblcJ1sdhaNtc1y1mBLyXp0Euogj/zQevoTGIgw+bEorWVosxBKODwpByuRWJ62J4NCwzR6iZ5Ncwh8sn8PVwvFfkl6dlR9EcmKd11T7sUFD3ojcI7E1xXKe1Myiir/ASeQj/vAN05VTEKCli2s6KP2+E2axPrZn6pyY1nOQwKbdqAJ0qd3zUH6GMoqix2T8O8tZtznSYaEN1LP59yjZhKVjIoRDpwcccpUPg5zp2PWiRDtc5q/qAUNtK8RMjJ3Vl/Bi8nAzF+z6cYWFi27XzNpWdKI0HwAKlM8OwhqG94lwzbojbMqOYA+8q8IRgSSNGLEyP/xMjEH3ZA=="
     */
    @Test
    public void createAuthorizationHeader() throws Exception {
        RsaCredentialX509 credential = new RsaCredentialX509(keyPair.getPrivate(), certificate);
        RsaAuthorization authorization = new RsaAuthorization(credential);
        HashMap<String, String> map = new HashMap<>();
        map.put("Date", new Date().toString());
        String text = authorization.getAuthorization("GET", "https://localhost", map);
        log.debug("Authorization: {}", text);
    }

    @Test
    public void verifyAuthorizationHeader() {
//        X509AuthenticationToken token = new X509AuthenticationToken(new Fingerprint(userLoginCertificate.getSha256Hash()), new Credential(signature, digest));
    }
}
