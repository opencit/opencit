/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.as.rest.v2.repository.CaCertificateRepository;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.ms.business.HostBO;
import com.intel.mtwilson.user.management.rest.v2.model.RegisterUserWithCertificate;
import com.intel.mtwilson.user.management.rest.v2.repository.UserLoginCertificateRepository;
import com.intel.mtwilson.user.management.rest.v2.repository.UserRepository;
import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;


/**
 *
 * @author ssbangal
 */
@RPC("register-user-with-certificate")
@JacksonXmlRootElement(localName="register_user_with_certificate")
public class RegisterUserWithCertificateRunnable implements Runnable{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegisterUserWithCertificateRunnable.class);

    private RegisterUserWithCertificate input;

    public RegisterUserWithCertificate getInput() {
        return input;
    }

    public void setInput(RegisterUserWithCertificate input) {
        this.input = input;
    }
    
    
    @Override
    public void run() {
        
        UserRepository userRepo = new UserRepository();
        UserLoginCertificateRepository userLoginCertRepo = new UserLoginCertificateRepository();
        
//        try {
//            if (input != null && input.getInput().) {
//                log.debug("Starting to process the user registration with certificate for {}.", user.getUsername());
//                UUID userId = new UUID();
//                User userObj = new User();
//                userObj.setId(userId);
//                userObj.setUsername(user.getUsername());
//                if (user.getLocale() == null)
//                    userObj.setLocale(Locale.US);
//                else
//                    userObj.setLocale(user.getLocale());
//                userObj.setComment(user.getComment());
//                                
//                
//                log.debug("Completed processing user registration with certificate for {} with result {}", user.getUsername(), result);
//            }
//        } catch (Exception ex) {
//            throw new ASException(ex);
//        }
    }
    
}
