/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.as.data.MwCertificateX509;
import com.intel.mtwilson.crypto.Password;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.ms.business.CertificateAuthorityBO;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.security.annotations.PermitAll;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import java.io.File;
import java.io.FileInputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author dsmagadX
 */
@Path("/ca")
public class CA {
	
    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateAuthorityBO dao = new CertificateAuthorityBO();


    public CA() {
    }

    @POST @Path("/enable")
    @RolesAllowed({"Security"}) // XXX TODO maybe need a separate "CA" role
    @Consumes("application/json")
    @Produces({MediaType.TEXT_PLAIN})
    public String enableCa(String newSaltedPasswordString) {
        try {
            Password newPassword = Password.valueOf(newSaltedPasswordString);
            dao.enableCaWithPassword(newPassword);
            return Boolean.TRUE.toString();
        } catch (Exception e) {
            throw new MSException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), e);
        }
        
    }

    @POST @Path("/disable")
    @RolesAllowed({"Security"}) // XXX TODO maybe need a separate "CA" role
    @Produces({MediaType.TEXT_PLAIN})
    public String disableCa() {
        try {
            dao.disableCa();
            return Boolean.TRUE.toString();
        } catch (Exception e) {
            throw new MSException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), e);
        }
        
    }

    /*
    @GET @Path("/certificate/current")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public byte[] getCaCertificate() {
        try {
            MwCertificateX509 cacert = dao.getCaCertificate();
            if( cacert == null ) {
                throw new MSException(ErrorCode.MS_MISSING_CERTIFICATE_FILE, ErrorCode.MS_MISSING_CERTIFICATE_FILE.getMessage());
            }
            return cacert.getCertificate();
        } catch (Exception e) {
            throw new MSException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), e);
        }
        
    }
    */

    
    @GET @Path("/certificate/rootca/current")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getRootCaCertificateChain() {
        try {
            File rootCaPemFile = new File("/etc/intel/cloudsecurity/MtWilsonRootCA.crt.pem"); // XXX TODO needs to be obtained from configuration object
            if( rootCaPemFile.canRead() ) {
                FileInputStream in = new FileInputStream(rootCaPemFile);
                String content = IOUtils.toString(in);
                IOUtils.closeQuietly(in);
                return content;
            }
            throw new MSException(ErrorCode.MS_MISSING_CERTIFICATE_FILE, ErrorCode.MS_MISSING_CERTIFICATE_FILE.getMessage()); // XXX TODO it's strange having errorcode, then errorcode.getmessage everywhere these exceptions are formed... it would be better to have a constructor new MSException(ErrorCode, String... args)  so if the message has any placeholders it would be filled in by the arguments
        } catch (Exception e) {
            throw new MSException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), e);
        }
        
    }
    
    @GET @Path("/certificate/saml/current")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getSamlCertificateChain() {
        try {
            File samlPemFile = new File("/etc/intel/cloudsecurity/saml.cer.pem"); // XXX TODO needs to be obtained from configuration object
            if( samlPemFile.canRead() ) {
                FileInputStream in = new FileInputStream(samlPemFile);
                String content = IOUtils.toString(in);
                IOUtils.closeQuietly(in);
                return content;
            }
            throw new MSException(ErrorCode.MS_MISSING_CERTIFICATE_FILE, ErrorCode.MS_MISSING_CERTIFICATE_FILE.getMessage()); // XXX TODO it's strange having errorcode, then errorcode.getmessage everywhere these exceptions are formed... it would be better to have a constructor new MSException(ErrorCode, String... args)  so if the message has any placeholders it would be filled in by the arguments
        } catch (Exception e) {
            throw new MSException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), e);
        }
        
    }

    @GET @Path("/certificate/privacyca/current")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getPrivacyCaCertificateChain() {
        try {
            File privacyCaPemFile = new File("/etc/intel/cloudsecurity/PrivacyCA.p12.pem"); // XXX TODO needs to be obtained from configuration object
            if( privacyCaPemFile.canRead() ) {
                FileInputStream in = new FileInputStream(privacyCaPemFile);
                String content = IOUtils.toString(in);
                IOUtils.closeQuietly(in);
                return content;
            }
            throw new MSException(ErrorCode.MS_MISSING_CERTIFICATE_FILE, ErrorCode.MS_MISSING_CERTIFICATE_FILE.getMessage()); // XXX TODO it's strange having errorcode, then errorcode.getmessage everywhere these exceptions are formed... it would be better to have a constructor new MSException(ErrorCode, String... args)  so if the message has any placeholders it would be filled in by the arguments
        } catch (Exception e) {
            throw new MSException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), e);
        }
        
    }

    @GET @Path("/certificate/tls/current")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getTlsCertificateChain() {
        try {
            File tlsPemFile = new File("/usr/share/glassfish3/glassfish/domains/domain1/config/ssl.10.1.71.80.crt.pem"); // XXX TODO needs to be obtained from configuration object
            if( tlsPemFile.canRead() ) {
                FileInputStream in = new FileInputStream(tlsPemFile);
                String content = IOUtils.toString(in);
                IOUtils.closeQuietly(in);
                return content;
            }
            throw new MSException(ErrorCode.MS_MISSING_CERTIFICATE_FILE, ErrorCode.MS_MISSING_CERTIFICATE_FILE.getMessage()); // XXX TODO it's strange having errorcode, then errorcode.getmessage everywhere these exceptions are formed... it would be better to have a constructor new MSException(ErrorCode, String... args)  so if the message has any placeholders it would be filled in by the arguments
        } catch (Exception e) {
            throw new MSException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), e);
        }
        
    }
    
}
