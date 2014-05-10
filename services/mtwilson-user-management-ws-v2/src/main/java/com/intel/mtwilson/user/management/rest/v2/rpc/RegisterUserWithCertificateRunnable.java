/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.user.management.rest.v2.model.RegisterUserWithCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.repository.UserLoginCertificateRepository;
import com.intel.mtwilson.user.management.rest.v2.repository.UserRepository;
import java.util.Locale;


/**
 *
 * @author ssbangal
 */
@RPC("register-user-with-certificate")
@JacksonXmlRootElement(localName="register_user_with_certificate")
public class RegisterUserWithCertificateRunnable implements Runnable{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegisterUserWithCertificateRunnable.class);

    private RegisterUserWithCertificate rpcUserWithCert;

    public RegisterUserWithCertificate getRpcUserWithCert() {
        return rpcUserWithCert;
    }

    public void setRpcUserWithCert(RegisterUserWithCertificate rpcUserWithCert) {
        this.rpcUserWithCert = rpcUserWithCert;
    }

    
    
    @Override
    public void run() {
        
        UserRepository userRepo = new UserRepository();
        UserLoginCertificateRepository userLoginCertRepo = new UserLoginCertificateRepository();
        
        try {
            if (rpcUserWithCert != null && rpcUserWithCert.getUser() != null && rpcUserWithCert.getUserCertificate() != null) {
                log.debug("Starting to process the user registration with certificate for {}.", rpcUserWithCert.getUser().getUsername());

                User userObj = new User();
                UserLoginCertificate userCertObj = new UserLoginCertificate();                
                UUID userId = new UUID();
                UUID userCertId = new UUID();
                
                userObj.setId(userId);
                userObj.setUsername(rpcUserWithCert.getUser().getUsername());
                if (rpcUserWithCert.getUser().getLocale() == null)
                    userObj.setLocale(Locale.US);
                else
                    userObj.setLocale(rpcUserWithCert.getUser().getLocale());
                userObj.setComment(rpcUserWithCert.getUser().getComment());
                userRepo.create(userObj);
                
                userCertObj.setId(userCertId);
                userCertObj.setUserId(userId);
                userCertObj.setCertificate(rpcUserWithCert.getUserCertificate().getCertificate());
                userCertObj.setSha1Hash(rpcUserWithCert.getUserCertificate().getSha1Hash());
                userCertObj.setSha256Hash(rpcUserWithCert.getUserCertificate().getSha256Hash());
                userCertObj.setComment(rpcUserWithCert.getUser().getComment());
                userCertObj.setExpires(rpcUserWithCert.getUserCertificate().getExpires());
                userLoginCertRepo.create(userCertObj);
                
                rpcUserWithCert.setResult(Boolean.TRUE);
                log.debug("Completed processing user registration with certificate for {} with result {}", 
                        rpcUserWithCert.getUser().getUsername(), rpcUserWithCert.getResult());
            }
        } catch (Exception ex) {
            throw new ASException(ex);
        }
    }
    
}
