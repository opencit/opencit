/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.launcher.ws.ext.V1;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.security.annotations.PermitAll;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.ws.rs.GET;
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
@V1
@Path("/ManagementService/resources/saml")
public class SamlCertificate {

    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Creates a new instance of SamlCertificate
     */
    public SamlCertificate() {
    }
    
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String defaultCaGetAction() {
        return ""; // note:  we are not doing anything here, this function exists only to work around this error: SEVERE: Conflicting URI templates. The URI template /ca for root resource class com.intel.mtwilson.ms.rest.CA and the URI template /ca transform to the same regular expression /ca(/.*)?
    }

    /**
     * Same as getSamlCertificate but with suggested filename
     */
    @GET
    @Path("/certificate/mtwilson-saml.crt")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    //@PermitAll
    public byte[] getSamlCertificateFilename() {
        return getSamlCertificate();
    }    
    
    /**
     * Retrieves representation of an instance of
     * com.intel.mountwilson.ms.business.SamlCertificate
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Path("/certificate")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    //@PermitAll
    public byte[] getSamlCertificate() {

        try {
            File certFile = ResourceFinder.getFile(MSConfig.getConfiguration().getString("mtwilson.saml.certificate.file", "saml.crt.pem"));
            log.debug("Certificate File " + certFile.getPath());
            try (FileInputStream in = new FileInputStream(certFile)) {
//                byte[] certificate = IOUtils.toByteArray(in);
                String certificate = IOUtils.toString(in);
//                X509Certificate cert = X509Util.decodeDerCertificate(certificate);
                X509Certificate cert = X509Util.decodePemCertificate(certificate); 

                log.info("Read certificate successfully");

                return cert.getEncoded();
            }
        } catch (CertificateException e) {
            throw new MSException(ErrorCode.MS_BAD_CERTIFICATE_FILE, ErrorCode.MS_BAD_CERTIFICATE_FILE.getMessage(), e);

        } catch (IOException e) {
            throw new MSException(ErrorCode.MS_MISSING_CERTIFICATE_FILE, ErrorCode.MS_MISSING_CERTIFICATE_FILE.getMessage(), e);
        }

    }
    /**
     * PUT method for updating or creating an instance of SamlCertificate
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    /*
     @PUT
     @Consumes("application/xml")
     public void putXml(String content) {
     }
     * 
     */
}
