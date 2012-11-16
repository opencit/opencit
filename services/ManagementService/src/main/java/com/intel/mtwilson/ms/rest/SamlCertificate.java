/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.security.annotations.PermitAll;
import com.intel.mtwilson.util.CertUtils;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author dsmagadX
 */
@Path("/saml")
public class SamlCertificate {
	
    private Logger log = LoggerFactory.getLogger(getClass());



    /**
     * Creates a new instance of SamlCertificate
     */
    public SamlCertificate() {
    }

    /**
     * Retrieves representation of an instance of com.intel.mountwilson.ms.business.SamlCertificate
     * @return an instance of java.lang.String
     */
    @GET @Path("/certificate")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    @PermitAll
    public byte[] getSamlCertificate() {
        
        try {
        	File certFile = ResourceFinder.getFile(MSConfig.getSamlCertificateName());
        	log.info("Certificate File " + certFile.getPath());
			X509Certificate cert = CertUtils.getX509Certificate(certFile);
			log.info("Read certificate successfully");
			
			return cert.getEncoded();
		} catch (CertificateException e) {
			throw new MSException(ErrorCode.MS_BAD_CERTIFICATE_FILE, ErrorCode.MS_BAD_CERTIFICATE_FILE.getMessage(), e);
                        
		} catch (IOException e) {
			throw new MSException(ErrorCode.MS_MISSING_CERTIFICATE_FILE, ErrorCode.MS_MISSING_CERTIFICATE_FILE.getMessage(), e);
		}
        
    }

    /**
     * PUT method for updating or creating an instance of SamlCertificate
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
