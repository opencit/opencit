
package com.intel.mtwilson.as.rest;

//import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.intel.mtwilson.as.ca.TrustAgentCertificateAuthority;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.x509.X509Util;
//import javax.annotation.security.RolesAllowed;
import com.intel.mtwilson.security.annotations.*;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.launcher.ws.ext.V1;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.ws.rs.GET;
import org.apache.commons.codec.binary.Base64;
//import org.codehaus.enunciate.jaxrs.TypeHint;

/**
 * REST Web Service
 * * 
 */
@V1
//@Stateless
@Path("/AttestationService/resources/ca2")
public class CA {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CA.class);

    private TrustAgentCertificateAuthority ca = null;
    
    public CA() {
        /*
        try {
            ca = new TrustAgentCertificateAuthority();
        }
        catch(IOException e) {
            log.error("Cannot initialize CA", e);
            ca = null;
        }
        */
    }
    
    
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String defaultCaGetAction() {
        return ""; // note:  we are not doing anything here, this function exists only to work around this error: SEVERE: Conflicting URI templates. The URI template /ca for root resource class com.intel.mtwilson.ms.rest.CA and the URI template /ca transform to the same regular expression /ca(/.*)?
    }
    
    /**
     * Sign a Trust Agent's SSL Certificate. 
     * 
     * Sample request:
     * POST http://localhost:8080/AttestationService/resources/ca/signTrustAgentSsl
     * Authorization: HMAC ... hash of self-signed certificate, nonce, and current time using CA password, so server can verify...
     * (post data is base64-encoded self-signed certificate that is used as a certificate signing request)
     * 
     * Sample output:
     * (output is base64-encoded ca-signed ssl certificate)
     * 
     * See also http://tools.ietf.org/html/rfc2986 (PKCS#10)
     * 
     * @return the CA-signed X509 Certificate with HTTP Status 200 OK on success, or other HTTP Status codes on error (403 if CA signing password is wrong, 400 if request does not include a self-signed cert)
     */
    //@PermitAll  // or should we require the "Security" role? Or a new "CA" role??  and do we need an annotation to indicate that authentication should be done with HMAC ???   so we could maintain an hmac users table too, and define a "ca" user that way admin can easily set the password for this via website
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_PLAIN})
    @Path("/signTrustAgentSsl")
    public String signTrustAgentSsl(String csrBase64, @QueryParam("password") String caPassword) {
        if( ca == null ) {
            log.error("signTrustAgentSsl request cannot be completed because CA is not initialized");
            return null;
        }
        ValidationUtil.validate(csrBase64);
        ValidationUtil.validate(caPassword);

        try {
            X509Certificate csr = X509Util.decodePemCertificate(csrBase64);
            X509Certificate caSignedCertificate = ca.signSslCertificate(csr, caPassword);
            return Base64.encodeBase64String(caSignedCertificate.getEncoded());
        }
        catch(FileNotFoundException e) {
            return null;
        }
        catch(CertificateException e) {
            return null;
        }
        catch(CryptographyException e) {
            return null;
        }
    }


}
