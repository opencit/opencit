/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.as.data.MwCertificateX509;
import com.intel.mtwilson.crypto.Password;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.ms.business.CertificateAuthorityBO;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.security.annotations.PermitAll;
import com.intel.mtwilson.security.annotations.RolesAllowed;
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
@Path("/ca")
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
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
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
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
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
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
        }
        
    }
    */

    /**
     * Same as getRootCaCertificateChain but with a final path part that suggests the filename
     * @return 
     */
    @GET @Path("/certificate/rootca/current/mtwilson-ca.pem")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getRootCaCertificateChainFilename() {
        return getRootCaCertificateChain();
    }
    
    @GET @Path("/certificate/rootca/current")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getRootCaCertificateChain() {
        try {
            String certFile = MSConfig.getConfiguration().getString("mtwilson.rootca.certificate.file");
            if( certFile != null && !certFile.startsWith(File.separator) ) {
                certFile = "/etc/intel/cloudsecurity/" + certFile; // XXX TODO assuming linux ,but could be windows ... need to use platform-dependent configuration folder location
            }
            File rootCaPemFile = new File(certFile); 
            FileInputStream in = new FileInputStream(rootCaPemFile); // FileNotFoundException
            String content = IOUtils.toString(in); // IOException
            IOUtils.closeQuietly(in);
            return content;
        } 
        catch (FileNotFoundException e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Mt Wilson Root CA certificate file is not found");
        }
        catch (IOException e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Failed to read Mt Wilson Root CA certificate file");
        }
        catch (Exception e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
        }
        
    }
    
    /**
     * Same as getSamlCertificateChain but with a suggested filename
     */
    @GET @Path("/certificate/saml/current/mtwilson-saml.pem")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getSamlCertificateChainFilename() {
        return getSamlCertificateChain();
    }
    
    
    @GET @Path("/certificate/saml/current")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getSamlCertificateChain() {
        try {
            String certFile = MSConfig.getConfiguration().getString("mtwilson.saml.certificate.file"); // PEM format file with possible CA certificate chain; not the same as mtwilson.saml.certificate which is the DER format file mtwilson.saml.certificate that we configured in mtwilson 1.0-RC2 
            if( certFile != null && !certFile.startsWith(File.separator) ) {
                certFile = "/etc/intel/cloudsecurity/" + certFile; // XXX TODO assuming linux ,but could be windows ... need to use platform-dependent configuration folder location
            }
            File samlPemFile = new File(certFile);
            FileInputStream in = new FileInputStream(samlPemFile);
            String content = IOUtils.toString(in);
            IOUtils.closeQuietly(in);
            return content;
        }
        catch (FileNotFoundException e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, "SAML certificate file is not found");
        }
        catch (IOException e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Failed to read SAML certificate file");
        }
        catch (Exception e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
        }
        
    }

    /**
     * Same as getPrivacyCaCertificateChain() but with suggested filename
     */
    @GET @Path("/certificate/privacyca/current/mtwilson-privacyca.pem")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getPrivacyCaCertificateChainFilename() {
        return getPrivacyCaCertificateChain();
    }
    
    @GET @Path("/certificate/privacyca/current")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getPrivacyCaCertificateChain() {
        try {
            String certFile = MSConfig.getConfiguration().getString("mtwilson.privacyca.certificate.list.file");
             if( certFile != null && !certFile.startsWith(File.separator) ) {
                certFile = "/etc/intel/cloudsecurity/" + certFile; // XXX TODO assuming linux ,but could be windows ... need to use platform-dependent configuration folder location
            }
           File privacyCaPemFile = new File(certFile); 
            FileInputStream in = new FileInputStream(privacyCaPemFile);
            String content = IOUtils.toString(in);
            IOUtils.closeQuietly(in);
            return content;
        }
        catch (FileNotFoundException e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Privacy CA certificate file is not found");
        }
        catch (IOException e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Failed to read Privacy CA certificate file");
        }
        catch (Exception e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
        }
        
    }

    @GET @Path("/certificate/tls/current/mtwilson-tls.crt")
    @PermitAll
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public byte[] getTlsCertificateFilename() {
        try {
            String certFile = MSConfig.getConfiguration().getString("mtwilson.tls.certificate.file");
            if( certFile != null && !certFile.startsWith(File.separator) ) {
                certFile = "/etc/intel/cloudsecurity/" + certFile; // XXX TODO assuming linux ,but could be windows ... need to use platform-dependent configuration folder location
            }
            if( certFile.endsWith(".pem") ) {
                File tlsPemFile = new File(certFile);
                FileInputStream in = new FileInputStream(tlsPemFile);
                String content = IOUtils.toString(in);
                IOUtils.closeQuietly(in);
                List<X509Certificate> list = X509Util.decodePemCertificates(content);
                if( list != null && !list.isEmpty() ) {
                    return list.get(0).getEncoded(); // first certificate is this host, all others are CA's
                }
                throw new IOException("Cannot read certificate file: "+certFile);
            }
            if( certFile.endsWith(".crt") ) {
                File tlsPemFile = new File(certFile);
                FileInputStream in = new FileInputStream(tlsPemFile);
                byte[] content = IOUtils.toByteArray(in);
                IOUtils.closeQuietly(in);
                return content;
            }
            throw new FileNotFoundException("Certificate file is not in .pem or .crt format");
        }
        catch (FileNotFoundException e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Server SSL certificate file is not found");
        }
        catch (IOException e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Failed to read server SSL certificate file");
        }
        catch (Exception e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
        }   
    }

    @GET @Path("/certificate/tls/current/mtwilson-tls.pem")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getTlsCertificateChainFilename() {
        return getTlsCertificateChain();
    }
    
    @GET @Path("/certificate/tls/current")
    @PermitAll
    @Produces({MediaType.TEXT_PLAIN})
    public String getTlsCertificateChain() {
        try {
            String certFile = MSConfig.getConfiguration().getString("mtwilson.tls.certificate.file");
            if( certFile != null && !certFile.startsWith(File.separator) ) {
                certFile = "/etc/intel/cloudsecurity/" + certFile; // XXX TODO assuming linux ,but could be windows ... need to use platform-dependent configuration folder location
            }
            if( certFile.endsWith(".pem") ) {
                File tlsPemFile = new File(certFile);
                FileInputStream in = new FileInputStream(tlsPemFile);
                String content = IOUtils.toString(in);
                IOUtils.closeQuietly(in);
                return content;
            }
            if( certFile.endsWith(".crt") ) {
                File tlsPemFile = new File(certFile);
                FileInputStream in = new FileInputStream(tlsPemFile);
                byte[] content = IOUtils.toByteArray(in);
                IOUtils.closeQuietly(in);
                X509Certificate cert = X509Util.decodeDerCertificate(content);
                String pem = X509Util.encodePemCertificate(cert);
                return pem;
            }
            throw new FileNotFoundException("Certificate file is not in .pem or .crt format");
        }
        catch (FileNotFoundException e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Server SSL certificate file is not found");
        }
        catch (IOException e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, "Failed to read server SSL certificate file");
        }
        catch (Exception e) {
            throw new MSException(e, ErrorCode.SYSTEM_ERROR, e.toString());
        }   
    }
}
