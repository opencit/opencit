/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.CaCertificate;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateLocator;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.jaxrs2.server.resource.SimpleRepository;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.common.MSException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class CaCertificateRepository implements SimpleRepository<CaCertificate, CaCertificateCollection, CaCertificateFilterCriteria, CaCertificateLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
        
    @Override
    public CaCertificateCollection search(CaCertificateFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CaCertificate retrieve(CaCertificateLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        CaCertificate caCert = new CaCertificate();
        String id = locator.id;
        if ("root".equals(id)) {
            try {
                String certFile = MSConfig.getConfiguration().getString("mtwilson.rootca.certificate.file");
                caCert = readCaCert(certFile);
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
        } else if ("saml".equals(id)) {
            try {
                String certFile = MSConfig.getConfiguration().getString("mtwilson.saml.certificate.file"); 
                caCert = readCaCert(certFile);
            }
            catch (FileNotFoundException e) {
                log.error("SAML certificate file is not found.", e);
                throw new MSException(ErrorCode.MS_SAML_CERT_NOT_FOUND_ERROR, e.getClass().getSimpleName());
            }
            catch (IOException e) {
                log.error("Failed to read SAML certificate file.", e);
                throw new MSException(ErrorCode.MS_SAML_CERT_READ_ERROR, e.getClass().getSimpleName());
            }
            catch (Exception e) {
                log.error("Error during retrieval of SAML certificate chain.", e);
                throw new MSException(ErrorCode.MS_SAML_CERT_ERROR, e.getClass().getSimpleName());
            }      
        } else if ("privacy".equals(id)) {
            try {
                String certFile = My.configuration().getPrivacyCaIdentityCacertsFile().getAbsolutePath();//MSConfig.getConfiguration().getString("mtwilson.privacyca.certificate.list.file");
                caCert = readCaCert(certFile);
            }
            catch (FileNotFoundException e) {
                log.error("Privacy CA certificate file is not found.", e);
                throw new MSException(ErrorCode.MS_PRIVACYCA_CERT_NOT_FOUND_ERROR, e.getClass().getSimpleName());
            }
            catch (IOException e) {
                log.error("Failed to read Privacy CA certificate file.", e);
                throw new MSException(ErrorCode.MS_PRIVACYCA_CERT_READ_ERROR, e.getClass().getSimpleName());
            }
            catch (Exception e) {
                log.error("Error during retrieval of Privacy CA certificate chain.", e);
                throw new MSException(ErrorCode.MS_PRIVACYCA_CERT_ERROR, e.getClass().getSimpleName());
            }
        } else if ("endorsement".equals(id)) {
            try {
                String certFile = My.configuration().getPrivacyCaEndorsementCacertsFile().getAbsolutePath();//MSConfig.getConfiguration().getString("mtwilson.privacyca.certificate.list.file");
                caCert = readCaCert(certFile);
            }
            catch (FileNotFoundException e) {
                log.error("Privacy CA certificate file is not found.", e);
                throw new MSException(ErrorCode.MS_PRIVACYCA_CERT_NOT_FOUND_ERROR, e.getClass().getSimpleName());
            }
            catch (IOException e) {
                log.error("Failed to read Privacy CA certificate file.", e);
                throw new MSException(ErrorCode.MS_PRIVACYCA_CERT_READ_ERROR, e.getClass().getSimpleName());
            }
            catch (Exception e) {
                log.error("Error during retrieval of Privacy CA certificate chain.", e);
                throw new MSException(ErrorCode.MS_PRIVACYCA_CERT_ERROR, e.getClass().getSimpleName());
            }
            
            
        } else if ("tls".equals(id)) {
            try {
                String certFile = MSConfig.getConfiguration().getString("mtwilson.tls.certificate.file");
                caCert = readCaCert(certFile);
            }
            catch (FileNotFoundException e) {
                log.error("Server SSL certificate file is not found.", e);
                throw new MSException(ErrorCode.MS_SSL_CERT_NOT_FOUND_ERROR, e.getClass().getSimpleName());
            }
            catch (IOException e) {
                log.error("Failed to read server SSL certificate file.", e);
                throw new MSException(ErrorCode.MS_SSL_CERT_READ_ERROR, e.getClass().getSimpleName());
            }
            catch (Exception e) {
                log.error("Error during retrieval of SSL CA chain.", e);
                throw new MSException(ErrorCode.MS_SSL_CERT_ERROR, e.getClass().getSimpleName());
            }   
        }
        return caCert;
    }
    
    private CaCertificate readCaCert(String path) throws FileNotFoundException, IOException, CertificateException {
        String certFile = path;
        CaCertificate caCert = new CaCertificate();
        if( certFile != null && !certFile.startsWith(File.separator)) {
            certFile = "/opt/mtwilson/configuration/" + certFile;
        }
        if (certFile != null) {
            if (certFile.endsWith(".pem")) {
                File PemFile = new File(certFile);
                FileInputStream in = new FileInputStream(PemFile);
                String pem = IOUtils.toString(in);
                X509Certificate cert = X509Util.decodePemCertificate(pem);
                caCert.setCertificate(cert.getEncoded());
                IOUtils.closeQuietly(in);
            } else if (certFile.endsWith(".crt")) {
                File crtFile = new File(certFile);
                FileInputStream in = new FileInputStream(crtFile);
                caCert.setCertificate(IOUtils.toByteArray(in));
                IOUtils.closeQuietly(in);
            } else {
                throw new FileNotFoundException("Certificate file is not in .pem or .crt format");
            }
        } else {
            throw new FileNotFoundException("Could not obtain cert chain location from config");
        }
        return caCert;
    }

    @Override
    public void store(CaCertificate item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void create(CaCertificate item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(CaCertificateLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(CaCertificateFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
