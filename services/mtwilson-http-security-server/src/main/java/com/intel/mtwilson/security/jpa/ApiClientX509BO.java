package com.intel.mtwilson.security.jpa;

import com.intel.mtwilson.datatypes.Role;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.ApiRoleX509;
import com.intel.mtwilson.security.core.PublicKeyUserFinder;
import com.intel.mtwilson.security.core.PublicKeyUserInfo;
import com.intel.mtwilson.security.core.X509UserFinder;
import com.intel.mtwilson.security.core.X509UserInfo;
import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It was labeled business logic but it's very closely tied to the JPA layer.
 * 
 * Secret key lookup provider for the authentication filter that secures
 * the REST API.
 * 
 * @since 0.5.1
 * @author jbuhacoff
 */
public class ApiClientX509BO implements X509UserFinder, PublicKeyUserFinder {
    private static Logger log = LoggerFactory.getLogger(ApiClientX509BO.class);
    
    private ApiClientX509JpaController controller;
    
    public ApiClientX509BO(EntityManagerFactory factory) {
        controller = new ApiClientX509JpaController(factory);
    }
    
    private ApiClientX509 getApiClientByFingerprint(byte[] fingerprint) {
        ApiClientX509 apiClient = controller.findEnabledApiClientX509ByFingerprint(fingerprint);
        return apiClient;
    }
    
    private Certificate getCertificate(byte[] certificateBytes) {
        if( certificateBytes != null ) {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
                return cert;
            }
            catch (CertificateException ex) {
                log.error("Cannot load certificate", ex);
                return null;
            }
        }
        return null;
    }
    // commenting out unused function (6/11 1.2)
    /*
    private PublicKey getPublicKey(byte[] publicKeyBytes) {
        if( publicKeyBytes != null ) {
            try {
                KeyFactory kf = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                PublicKey publicKey = (PublicKey)kf.generatePublic(keySpec);
                return publicKey;
            } catch (InvalidKeySpecException ex) {
                log.error("Cannot load public key from database", ex);
                return null;
            }
            catch (NoSuchAlgorithmException ex) {
                log.error("Cannot load public key from database", ex);
                return null;
            }
        }
        return null;
    }
    */
    
    @Override
    public X509UserInfo getUserForX509Identity(byte[] fingerprint) {
        ApiClientX509 apiClient = getApiClientByFingerprint(fingerprint);
        if( apiClient == null ) {
            return null;
        }
        X509UserInfo userInfo = new X509UserInfo();
        userInfo.certificate = getCertificate(apiClient.getCertificate());
        // Let us add the display name of the user       too
        String tempUserName = apiClient.getName();
        // Since we need to ignore the comma character before the OU, the to index is being subtracted by 2
        userInfo.loginName = tempUserName.substring(tempUserName.indexOf("CN=") + 3, tempUserName.indexOf("OU=")-1);
//        userInfo.publicKey = userInfo.certificate.getPublicKey(); // apiClient.getCertificate().getPublicKey();
        userInfo.fingerprint = fingerprint; 
        Collection<ApiRoleX509> roles = apiClient.getApiRoleX509Collection();
        ArrayList<Role> allowedRoles = new ArrayList<Role>();
        for( ApiRoleX509 role : roles ) {
            String roleName = role.getApiRoleX509PK().getRole();
            try {
                allowedRoles.add(Role.valueOf(roleName));
            }
            catch(IllegalArgumentException e) {
                // we ignore unsupported roles, but log the error
                log.error("Unsupported role name ("+roleName+") assigned to "+apiClient.getName(), e);
            }
        }
        userInfo.roles = allowedRoles.toArray(new Role[0]);
        return userInfo;        
    }

    /**
     * For now we are using the same X509 table for both "X509" and "PublicKey"
     * authentication schemes. The "PublicKey" scheme just needs the public key,
     * the "X509" needs the entire certificate. To be normal they should be 
     * in separate tables.
     * @param fingerprint
     * @return 
     */
    @Override
    public PublicKeyUserInfo getUserForIdentity(byte[] fingerprint) {
        X509UserInfo x509 = getUserForX509Identity(fingerprint);
        if( x509 == null ) {
            return null;
        }
        PublicKeyUserInfo userInfo = new PublicKeyUserInfo();
        userInfo.fingerprint = fingerprint;
        if( x509.certificate != null ) { userInfo.publicKey = x509.certificate.getPublicKey(); }
        userInfo.roles = x509.roles;
        return userInfo;
    }

}
