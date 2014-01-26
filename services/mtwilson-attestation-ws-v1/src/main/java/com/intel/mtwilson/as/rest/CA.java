
package com.intel.mtwilson.as.rest;

import javax.ejb.Stateless;
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
@Stateless
@Path("/AttestationService/resources/ca2")
public class CA {
    private TrustAgentCertificateAuthority ca = new TrustAgentCertificateAuthority();
    
    
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String defaultCaGetAction() {
        return ""; // note:  we are not doing anything here, this function exists only to work around this error: SEVERE: Conflicting URI templates. The URI template /ca for root resource class com.intel.mtwilson.ms.rest.CA and the URI template /ca transform to the same regular expression /ca(/.*)?
    }
    
    /**
     * Sign a Trust Agent's SSL Certificate. 
     * 
     * XXX we are taking a shortcut and using a self-signed certificate as the CSR instead
     * of PKCS#10 because there doesn't seem to be a convenient Java package to work with
     * PKCS#10 files, and all the information we need is already encoded in an SSL certificate - 
     * requestor's public key, algorithm, etc. all signed by requestor's private key.
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
     * 
     * TODO: change this from PermitAll to permit only requests authenticated using our HMAC scheme (proving user knows CA password, without actually sending the password in the request...);  or require user to use an Api Client with RSA private key in order to make this request. which means sysetm admin needs to copy his .jks file to each host when installing trust agent...
     */
    @PermitAll  // or should we require the "Security" role? Or a new "CA" role??  and do we need an annotation to indicate that authentication should be done with HMAC ???   so we could maintain an hmac users table too, and define a "ca" user that way admin can easily set the password for this via website
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_PLAIN})
    @Path("/signTrustAgentSsl")
    public String signTrustAgentSsl(String csrBase64, @QueryParam("password") String caPassword) {
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
