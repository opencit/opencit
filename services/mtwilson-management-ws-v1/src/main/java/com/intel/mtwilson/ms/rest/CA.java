/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.as.data.MwCertificateX509;
import com.intel.mtwilson.launcher.ws.ext.V1;
import com.intel.dcsg.cpg.crypto.PasswordHash;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.ms.business.CertificateAuthorityBO;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.security.annotations.PermitAll;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.My;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author dsmagadX
 */
@V1
@Path("/ManagementService/resources/ca")
public class CA {
	
    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateAuthorityBO dao = new CertificateAuthorityBO();


    public CA() {
    }
    
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String defaultCaGetAction() {
        return ""; // note:  we are not doing anything here, this function exists only to work around this error: SEVERE: Conflicting URI templates. The URI template /ca for root resource class com.intel.mtwilson.ms.rest.CA and the URI template /ca transform to the same regular expression /ca(/.*)?
    }

//    @POST @Path("/enable")
//    @RolesAllowed({"Security"}) 
//    @Consumes("application/json")
//    @Produces({MediaType.TEXT_PLAIN})
//    public String enableCa(String newSaltedPasswordString) {
//        try {
//            ValidationUtil.validate(newSaltedPasswordString);
//            PasswordHash newPassword = PasswordHash.valueOf(newSaltedPasswordString);
//            dao.enableCaWithPassword(newPassword);
//            return Boolean.TRUE.toString();
//        } catch (MSException ex) {
//            throw ex;
//        } catch (Exception e) {
//            log.error("Error during enabling CA. ", e);
//            throw new MSException(ErrorCode.MS_CA_ENABLE_ERROR, e.getClass().getSimpleName());
//        }
//        
//    }
//
//    @POST @Path("/disable")
//    @RolesAllowed({"Security"}) 
//    @Produces({MediaType.TEXT_PLAIN})
//    public String disableCa() {
//        try {
//            dao.disableCa();
//            return Boolean.TRUE.toString();
//        } catch (MSException ex) {
//            throw ex;
//        } catch (Exception e) {
//            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
//            log.error("Error during disabling CA. ", e);
//            throw new MSException(ErrorCode.MS_CA_DISABLE_ERROR, e.getClass().getSimpleName());
//        }
//        
//    }

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
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
        }
        
    }
    */

    /**
     * Same as getRootCaCertificateChain but with a final path part that suggests the filename
     * @return 
     */
    @GET @Path("/certificate/rootca/current/mtwilson-ca.pem")
    //@PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getRootCaCertificateChainFilename() {
        return getRootCaCertificateChain();
    }
    
    @GET @Path("/certificate/rootca/current")
    //@PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getRootCaCertificateChain() {
        try {
//            String certFile = MSConfig.getConfiguration().getString("mtwilson.rootca.certificate.file");
//            if( certFile != null && !certFile.startsWith(File.separator) ) {
//                certFile = "/etc/intel/cloudsecurity/" + certFile; 
//            }
//            if(certFile != null) {
//                File rootCaPemFile = new File(certFile);
                File rootCaPemFile = My.configuration().getRootCaCertificateFile();
                try (FileInputStream in = new FileInputStream(rootCaPemFile)) { // FileNotFoundException
                    String content = IOUtils.toString(in); // IOException
                    return content;
                }
//            }else throw new FileNotFoundException("Could not obtain Root CA cert location from config");
        } 
        catch (FileNotFoundException e) {
            log.error("Mt Wilson Root CA certificate file is not found. ", e);
            throw new MSException(ErrorCode.MS_ROOT_CA_CERT_NOT_FOUND_ERROR, e.getClass().getSimpleName());
        }
        catch (IOException e) {
            log.error("Failed to read Mt Wilson Root CA certificate file. ", e);
            throw new MSException(ErrorCode.MS_ROOT_CA_CERT_READ_ERROR, e.getClass().getSimpleName());
        }
        catch (Exception e) {
            log.error("Error during retrieval of root certificate CA chain. ", e);            
            throw new MSException(ErrorCode.MS_ROOT_CA_CERT_ERROR, e.getClass().getSimpleName());
        }
        
    }
    
    /**
     * Same as getSamlCertificateChain but with a suggested filename
     */
    @GET @Path("/certificate/saml/current/mtwilson-saml.pem")
    //@PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getSamlCertificateChainFilename() {
        return getSamlCertificateChain();
    }
    
    
    @GET @Path("/certificate/saml/current")
    //@PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getSamlCertificateChain() {
        try {
//            String certFile = MSConfig.getConfiguration().getString("mtwilson.saml.certificate.file"); // PEM format file with possible CA certificate chain; not the same as mtwilson.saml.certificate which is the DER format file mtwilson.saml.certificate that we configured in mtwilson 1.0-RC2 
//            if( certFile != null && !certFile.startsWith(File.separator) ) {
//                certFile = "/etc/intel/cloudsecurity/" + certFile; 
//            }
//            if(certFile != null) {
//                File samlPemFile = new File(certFile);
                File samlPemFile = My.configuration().getSamlCertificateFile();
                try (FileInputStream in = new FileInputStream(samlPemFile)) {
                    String content = IOUtils.toString(in);
                    return content;
                }
//            }else throw new FileNotFoundException("Could not load Saml Cert location from config");
        }
        catch (FileNotFoundException e) {
            log.error("SAML certificate file is not found.", e);
            //throw new MSException(e, ErrorCode.SYSTEM_ERROR, "SAML certificate file is not found");
            throw new MSException(ErrorCode.MS_SAML_CERT_NOT_FOUND_ERROR, e.getClass().getSimpleName());
        }
        catch (IOException e) {
            log.error("Failed to read SAML certificate file.", e);
            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Failed to read SAML certificate file");
            throw new MSException(ErrorCode.MS_SAML_CERT_READ_ERROR, e.getClass().getSimpleName());
        }
        catch (Exception e) {
            log.error("Error during retrieval of SAML certificate chain.", e);
            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
            throw new MSException(ErrorCode.MS_SAML_CERT_ERROR, e.getClass().getSimpleName());
        }
        
    }

    /**
     * Same as getPrivacyCaCertificateChain() but with suggested filename
     */
    @GET @Path("/certificate/privacyca/current/mtwilson-privacyca.pem")
    //@PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getPrivacyCaCertificateChainFilename() {
        return getPrivacyCaCertificateChain();
    }
    
    @GET @Path("/certificate/privacyca/current")
    //@PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getPrivacyCaCertificateChain() {
        try {
//            String certFile = MSConfig.getConfiguration().getString("mtwilson.privacyca.certificate.list.file");
//             if( certFile != null && !certFile.startsWith(File.separator) ) {
//                certFile = "/etc/intel/cloudsecurity/" + certFile; 
//            }
//            if(certFile != null) {
//                File privacyCaPemFile = new File(certFile);
                File privacyCaPemFile = My.configuration().getPrivacyCaIdentityCacertsFile();
                try (FileInputStream in = new FileInputStream(privacyCaPemFile)) {
                    String content = IOUtils.toString(in);
                    return content;
                }
//            }else throw new FileNotFoundException("Could not read Privacy CA cert file location from config");
        }
        catch (FileNotFoundException e) {
            log.error("Privacy CA certificate file is not found.", e);
            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Privacy CA certificate file is not found");
            throw new MSException(ErrorCode.MS_PRIVACYCA_CERT_NOT_FOUND_ERROR, e.getClass().getSimpleName());
        }
        catch (IOException e) {
            log.error("Failed to read Privacy CA certificate file.", e);
            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Failed to read Privacy CA certificate file");
            throw new MSException(ErrorCode.MS_PRIVACYCA_CERT_READ_ERROR, e.getClass().getSimpleName());
        }
        catch (Exception e) {
            log.error("Error during retrieval of Privacy CA certificate chain.", e);
            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
            throw new MSException(ErrorCode.MS_PRIVACYCA_CERT_ERROR, e.getClass().getSimpleName());
        }
    }

    @GET @Path("/certificate/tls/current/mtwilson-tls.crt")
    //@PermitAll
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public byte[] getTlsCertificateFilename() {
        try {
            String certFile = MSConfig.getConfiguration().getString("mtwilson.tls.certificate.file");
            if( certFile != null && !certFile.startsWith(File.separator) ) {
                certFile = "/etc/intel/cloudsecurity/" + certFile; 
            }
            if(certFile != null) {
                if( certFile.endsWith(".pem") ) {
                    File tlsPemFile = new File(certFile);
                    try (FileInputStream in = new FileInputStream(tlsPemFile)) {
                        String content = IOUtils.toString(in);
                        List<X509Certificate> list = X509Util.decodePemCertificates(content);
                        if (list != null && !list.isEmpty()) {
                            return list.get(0).getEncoded(); // first certificate is this host, all others are CA's
                        }
                    }
                    throw new IOException("Cannot read certificate file: " + certFile);
                }
                if( certFile.endsWith(".crt") ) {
                    File tlsPemFile = new File(certFile);
                    try (FileInputStream in = new FileInputStream(tlsPemFile)) {
                        byte[] content = IOUtils.toByteArray(in);
                        return content;
                    }
                }
                throw new FileNotFoundException("Certificate file is not in .pem or .crt format");
            }else{
               throw new FileNotFoundException("Could not obtain Tls Cert location from config");
            }
            
        }
        catch (FileNotFoundException e) {
            log.error("Server SSL certificate file is not found.", e);
            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Server SSL certificate file is not found");
            throw new MSException(ErrorCode.MS_SSL_CERT_NOT_FOUND_ERROR, e.getClass().getSimpleName());
        }
        catch (IOException e) {
            log.error("Failed to read server SSL certificate file.", e);
            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Failed to read server SSL certificate file");
            throw new MSException(ErrorCode.MS_SSL_CERT_READ_ERROR, e.getClass().getSimpleName());
        }
        catch (Exception e) {
            log.error("Error during retrieval of SSL certificate.", e);
            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
            throw new MSException(ErrorCode.MS_SSL_CERT_ERROR, e.getClass().getSimpleName());
        }   
    }

    @GET @Path("/certificate/tls/current/mtwilson-tls.pem")
    //@PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getTlsCertificateChainFilename() {
        return getTlsCertificateChain();
    }
    
    @GET @Path("/certificate/tls/current")
    //@PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getTlsCertificateChain() {
        try {

            File tlsPemFile = My.configuration().getTlsCertificateFile();
            try (FileInputStream in = new FileInputStream(tlsPemFile)) {
                String content = IOUtils.toString(in);
                return content;
            }
            
/*            String certFile = MSConfig.getConfiguration().getString("mtwilson.tls.certificate.file");
            if( certFile != null && !certFile.startsWith(File.separator) ) {
                certFile = "/etc/intel/cloudsecurity/" + certFile; 
            }
            if(certFile != null) {
                if( certFile.endsWith(".pem") ) {
                    File tlsPemFile = new File(certFile);
                    try (FileInputStream in = new FileInputStream(tlsPemFile)) {
                        String content = IOUtils.toString(in);
                        return content;
                    }
                }
                if( certFile.endsWith(".crt") ) {
                    File tlsPemFile = new File(certFile);
                    try (FileInputStream in = new FileInputStream(tlsPemFile)) {
                        byte[] content = IOUtils.toByteArray(in);
                        X509Certificate cert = X509Util.decodeDerCertificate(content);
                        String pem = X509Util.encodePemCertificate(cert);
                        return pem;
                    }
                }
                throw new FileNotFoundException("Certificate file is not in .pem or .crt format");
            }else{
                throw new FileNotFoundException("Could not obtain TLS cert chain location from config");
            }*/
            
        }
        catch (FileNotFoundException e) {
            log.error("Server SSL certificate file is not found.", e);
            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Server SSL certificate file is not found");
            throw new MSException(ErrorCode.MS_SSL_CERT_NOT_FOUND_ERROR, e.getClass().getSimpleName());
        }
        catch (IOException e) {
            log.error("Failed to read server SSL certificate file.", e);
            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Failed to read server SSL certificate file");
            throw new MSException(ErrorCode.MS_SSL_CERT_READ_ERROR, e.getClass().getSimpleName());
        }
        catch (Exception e) {
            log.error("Error during retrieval of SSL CA chain.", e);
            // throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
            throw new MSException(ErrorCode.MS_SSL_CERT_ERROR, e.getClass().getSimpleName());
        }   
    }
}
