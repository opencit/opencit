/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.user.management.rest.v2.model.RegisterUserWithCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.repository.UserLoginCertificateRepository;
import com.intel.mtwilson.user.management.rest.v2.repository.UserRepository;
import java.security.cert.X509Certificate;
import java.util.Locale;


/**
 *
 * @author ssbangal
 */
@RPC("register-user-with-certificate")
@JacksonXmlRootElement(localName="register_user_with_certificate")
public class RegisterUserWithCertificateRunnable implements Runnable{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegisterUserWithCertificateRunnable.class);

    private User user;
    private UserLoginCertificate userLoginCertificate;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserLoginCertificate getUserLoginCertificate() {
        return userLoginCertificate;
    }

    public void setUserLoginCertificate(UserLoginCertificate userLoginCertificate) {
        this.userLoginCertificate = userLoginCertificate;
    }
    
    
    @Override
    public void run() {
        
        UserRepository userRepo = new UserRepository();
        UserLoginCertificateRepository userLoginCertRepo = new UserLoginCertificateRepository();
        
        try {
            if (getUser() != null && getUserLoginCertificate() != null) {
                log.debug("Starting to process the user registration with certificate for {}.", getUser().getUsername());

                User userObj = new User();
                UserLoginCertificate userCertObj = new UserLoginCertificate();                
                UUID userId = new UUID();
                UUID userCertId = new UUID();
                
                userObj.setId(userId);
                userObj.setUsername(getUser().getUsername());
                if (getUser().getLocale() == null)
                    userObj.setLocale(Locale.US);
                else
                    userObj.setLocale(getUser().getLocale());
                userObj.setComment(getUser().getComment());
                userRepo.create(userObj);
                
                userCertObj.setId(userCertId);
                userCertObj.setUserId(userId);
                userCertObj.setCertificate(getUserLoginCertificate().getCertificate());
                userCertObj.setSha1Hash(Sha1Digest.digestOf(getUserLoginCertificate().getCertificate()).toByteArray());
                userCertObj.setSha256Hash(Sha256Digest.digestOf(getUserLoginCertificate().getCertificate()).toByteArray());
                userCertObj.setComment(getUser().getComment());
                userCertObj.setExpires(getUserLoginCertificate().getX509Certificate().getNotAfter());
                userLoginCertRepo.create(userCertObj);
                
                log.debug("Completed processing user registration with certificate for {}.", getUser().getUsername());
            }
        } catch (Exception ex) {
            throw new ASException(ex);
        }
    }
    
}
