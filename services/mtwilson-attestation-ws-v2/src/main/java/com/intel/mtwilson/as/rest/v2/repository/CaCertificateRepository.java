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
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author ssbangal
 */
public class CaCertificateRepository implements DocumentRepository<CaCertificate, CaCertificateCollection, CaCertificateFilterCriteria, CaCertificateLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CaCertificateRepository.class);
        
    @Override
    public CaCertificateCollection search(CaCertificateFilterCriteria criteria) {
        if( criteria.domain != null ) { 
            if(  criteria.domain.equals("ek") ||  criteria.domain.equals("endorsement") ) {
            // corresponds to URL /ca-certificates?domain=ek
                String certFile = My.configuration().getPrivacyCaEndorsementCacertsFile().getAbsolutePath();//MSConfig.getConfiguration().getString("mtwilson.privacyca.certificate.list.file");
                try {
                    CaCertificateCollection cacerts = readCaCertCollection(certFile);
                    return cacerts;
                }
                catch(IOException | CertificateException e) {
                    log.debug("Failed to read certificates for domain: {}", criteria.domain);
                    throw new RepositorySearchException(e, criteria);
                }
            }
        }
        return new CaCertificateCollection();
    }

    @Override
    public CaCertificate retrieve(CaCertificateLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        log.debug("CaCertificate:Retrieve - Got request to retrieve CaCertificate with id {}.", locator.id);                
        CaCertificate caCert = new CaCertificate();
        String id = locator.id;
        if ("root".equals(id)) {
            try {
                String certFile = My.configuration().getRootCaCertificateFile().getAbsolutePath(); //MSConfig.getConfiguration().getString("mtwilson.rootca.certificate.file");
                caCert = readCaCert(certFile);
            } 
            catch (FileNotFoundException e) {
                log.error("Mt Wilson Root CA certificate file is not found. ", e);
                throw new RepositoryRetrieveException(e, locator);
            }
            catch (IOException e) {
                log.error("Failed to read Mt Wilson Root CA certificate file. ", e);
                throw new RepositoryRetrieveException(e, locator);
            }
            catch (Exception e) {
                log.error("Error during retrieval of root certificate CA chain. ", e);            
                throw new RepositoryRetrieveException(e, locator);
            }
        } else if ("saml".equals(id)) {
            try {
                String certFile = My.configuration().getSamlCertificateFile().getAbsolutePath(); //MSConfig.getConfiguration().getString("mtwilson.saml.certificate.file"); 
                caCert = readCaCert(certFile);
            }
            catch (FileNotFoundException e) {
                log.error("SAML certificate file is not found.", e);
                throw new RepositoryRetrieveException(e, locator);
            }
            catch (IOException e) {
                log.error("Failed to read SAML certificate file.", e);
                throw new RepositoryRetrieveException(e, locator);
            }
            catch (Exception e) {
                log.error("Error during retrieval of SAML certificate chain.", e);
                throw new RepositoryRetrieveException(e, locator);
            }      
        } else if ("privacy".equals(id) || "aik".equals(id)) {
            try {
                String certFile = My.configuration().getPrivacyCaIdentityCacertsFile().getAbsolutePath(); //MSConfig.getConfiguration().getString("mtwilson.privacyca.certificate.list.file");
                caCert = readCaCert(certFile);
            }
            catch (FileNotFoundException e) {
                log.error("Privacy CA certificate file is not found.", e);
                throw new RepositoryRetrieveException(e, locator);
            }
            catch (IOException e) {
                log.error("Failed to read Privacy CA certificate file.", e);
                throw new RepositoryRetrieveException(e, locator);
            }
            catch (Exception e) {
                log.error("Error during retrieval of Privacy CA certificate chain.", e);
                throw new RepositoryRetrieveException(e, locator);
            }
        } else if ("endorsement".equals(id) || "ek".equals(id)) {
            try {
                String certFile = My.configuration().getPrivacyCaEndorsementCacertsFile().getAbsolutePath(); //MSConfig.getConfiguration().getString("mtwilson.privacyca.certificate.list.file");
                caCert = readCaCert(certFile);
            }
            catch (FileNotFoundException e) {
                log.error("Privacy CA certificate file is not found.", e);
                throw new RepositoryRetrieveException(e, locator);
            }
            catch (IOException e) {
                log.error("Failed to read Privacy CA certificate file.", e);
                throw new RepositoryRetrieveException(e, locator);
            }
            catch (Exception e) {
                log.error("Error during retrieval of Privacy CA certificate chain.", e);
                throw new RepositoryRetrieveException(e, locator);
            }
            
            
        } else if ("tls".equals(id)) {
            try {
                String certFile = My.configuration().getTlsCertificateFile().getAbsolutePath(); //MSConfig.getConfiguration().getString("mtwilson.tls.certificate.file");
                caCert = readCaCert(certFile);
            }
            catch (FileNotFoundException e) {
                log.error("Server SSL certificate file is not found.", e);
                throw new RepositoryRetrieveException(e, locator);
            }
            catch (IOException e) {
                log.error("Failed to read server SSL certificate file.", e);
                throw new RepositoryRetrieveException(e, locator);
            }
            catch (Exception e) {
                log.error("Error during retrieval of SSL CA chain.", e);
                throw new RepositoryRetrieveException(e, locator);
            }   
        }
        return caCert;
    }
    
    private CaCertificate readCaCert(String path) throws FileNotFoundException, IOException, CertificateException {
        File file = new File(path);
        String certFile = path;
        CaCertificate caCert = new CaCertificate();
        if( certFile != null && !file.isAbsolute()) {
            certFile = My.configuration().getDirectoryPath() + File.separator + certFile;
        }
        if (certFile != null) {
            if (certFile.endsWith(".pem")) {
                File pemFile = new File(certFile);
                try (FileInputStream in = new FileInputStream(pemFile)) {
                    String pem = IOUtils.toString(in);
                    X509Certificate cert = X509Util.decodePemCertificate(pem);
                    caCert.setCertificate(cert.getEncoded());
                }
            } else if (certFile.endsWith(".crt")) {
                File crtFile = new File(certFile);
                try (FileInputStream in = new FileInputStream(crtFile)) {
                    caCert.setCertificate(IOUtils.toByteArray(in));
                }
            } else {
                throw new FileNotFoundException("Certificate file is not in .pem or .crt format");
            }
        } else {
            throw new FileNotFoundException("Could not obtain cert chain location from config");
        }
        return caCert;
    }

    private CaCertificateCollection readCaCertCollection(String path) throws FileNotFoundException, IOException, CertificateException {
        File file = new File(path);
        CaCertificateCollection collection = new CaCertificateCollection();
        String certFile = path;
        if( certFile != null && !file.isAbsolute()) {
            certFile = My.configuration().getDirectoryPath() + File.separator + certFile;
        }
        log.debug("CA file: {}", certFile);
        if (certFile != null) {
            if (certFile.endsWith(".pem")) {
                File PemFile = new File(certFile);
                try (FileInputStream in = new FileInputStream(PemFile)) {
                    String pem = IOUtils.toString(in);
                    log.debug("PEM: {}", pem);
                    List<X509Certificate> certs = X509Util.decodePemCertificates(pem);
                    for(X509Certificate cert : certs) {
                        CaCertificate caCert = new CaCertificate();
                        caCert.setCertificate(cert.getEncoded());
                        collection.getCaCertificates().add(caCert);
                    }
                }
            } else if (certFile.endsWith(".crt")) {
                File crtFile = new File(certFile);
                try (FileInputStream in = new FileInputStream(crtFile)) {
                    CaCertificate caCert = new CaCertificate();
                    caCert.setCertificate(IOUtils.toByteArray(in));
                    collection.getCaCertificates().add(caCert);
                }
            } else {
                throw new FileNotFoundException("Certificate file is not in .pem or .crt format");
            }
        } else {
            throw new FileNotFoundException("Could not obtain cert chain location from config");
        }
        return collection;
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
