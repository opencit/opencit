/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.rest.v2.model.HostTlsCertificate;
import com.intel.mtwilson.as.rest.v2.model.HostTlsCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.HostTlsCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.HostTlsCertificateLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class HostTlsCertificateRepository implements DocumentRepository<HostTlsCertificate, HostTlsCertificateCollection, HostTlsCertificateFilterCriteria, HostTlsCertificateLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostTlsCertificateRepository.class);
    
    @Override
    @RequiresPermissions("host_tls_certificates:search")    
    public HostTlsCertificateCollection search(HostTlsCertificateFilterCriteria criteria) {
        log.debug("HostTlsCertificate:Search - Got request to search for the HostTlsCertificates.");        
        HostTlsCertificateCollection objCollection = new HostTlsCertificateCollection();
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            if (criteria.hostUuid != null) {
                TblHosts obj = jpaController.findHostByUuid(criteria.hostUuid);
                if (obj != null) {
                    String ksPassword = My.configuration().getTlsKeystorePassword();
                    SimpleKeystore tlsKeystore = new SimpleKeystore(obj.getTlsKeystoreResource(), ksPassword);
                    for(String alias : tlsKeystore.aliases()) {
                        log.debug("Checking keystore alias: {}", alias);
                        if (alias.equals(criteria.sha1)) {
                            // make sure it has a TLS private key and certificate inside
                            try {
                                RsaCredentialX509 credential = tlsKeystore.getRsaCredentialX509(alias, ksPassword);
                                log.debug("TLS certificate: {}", credential.getCertificate().getSubjectX500Principal().getName());

                                HostTlsCertificate hostTlsCert = new HostTlsCertificate();
                                hostTlsCert.setX509Certificate(credential.getCertificate());
                                hostTlsCert.setHostUuid(criteria.hostUuid);
                                hostTlsCert.setSha1(criteria.sha1);
                                objCollection.getTlsCertificates().add(hostTlsCert);

                            } catch(Exception ex) {
                                log.debug("Cannot read TLS key from keystore", ex);
                                throw new RepositorySearchException(ex, criteria);
                            }
                        }
                    }                
                }
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("HostTlsCertificate:Search - Error during search for HostTlsCertificates.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("HostTlsCertificate:Search - Returning back {} of results.", objCollection.getTlsCertificates().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("host_tls_certificates:retrieve")    
    public HostTlsCertificate retrieve(HostTlsCertificateLocator locator) {
        if (locator.hostUuid == null || locator.sha1 == null) { return null; }
        log.debug("HostTlsCertificate:Retrieve - Got request to retrieve HostTlsCertificate for host with id {}.", locator.hostUuid.toString());                
        String id = locator.hostUuid.toString();
        String sha1 = locator.sha1;
        
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj = jpaController.findHostByUuid(id);
            if (obj != null) {
                // Now we need to iterate through the certificates in the keystore to match the one we are looking for.
                // To open the keystore, we need to retrieve the password
                String ksPassword = My.configuration().getTlsKeystorePassword();
                SimpleKeystore tlsKeystore = new SimpleKeystore(obj.getTlsKeystoreResource(), ksPassword);
                for(String alias : tlsKeystore.aliases()) {
                    log.debug("Checking keystore alias: {}", alias);
                    if (alias.equals(sha1)) {
                        // make sure it has a TLS private key and certificate inside
                        try {
                            RsaCredentialX509 credential = tlsKeystore.getRsaCredentialX509(alias, ksPassword);
                            log.debug("TLS certificate: {}", credential.getCertificate().getSubjectX500Principal().getName());
                            
                            HostTlsCertificate hostTlsCert = new HostTlsCertificate();
                            hostTlsCert.setX509Certificate(credential.getCertificate());
                            hostTlsCert.setHostUuid(id);
                            hostTlsCert.setSha1(sha1);
                            return hostTlsCert;
                            
                        } catch(Exception ex) {
                            log.debug("Cannot read TLS key from keystore", ex);
                            throw new RepositoryRetrieveException(ex, locator);
                        }
                    }
                }                
            }
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("Error during search for hosts.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }

        return null;
    }

    @Override
    @RequiresPermissions("host_tls_certificates:store")    
    public void store(HostTlsCertificate item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_tls_certificates:create")    
    public void create(HostTlsCertificate item) {
        log.debug("HostTlsCertificate:Create - Got request to create a new HostTlsCertificate.");
        HostTlsCertificateLocator locator = new HostTlsCertificateLocator();
        locator.hostUuid = UUID.valueOf(item.getHostUuid());
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj = jpaController.findHostByUuid(item.getHostUuid());
            if (obj != null) {
                // Now we need to iterate through the certificates in the keystore to match the one we are looking for.
                // To open the keystore, we need to retrieve the password
                String ksPassword = My.configuration().getTlsKeystorePassword();
                SimpleKeystore tlsKeystore = new SimpleKeystore(obj.getTlsKeystoreResource(), ksPassword);
                Sha1Digest sha1 = Sha1Digest.digestOf(item.getCertificate());
                tlsKeystore.addTrustedCertificate(item.getX509Certificate(), sha1.toString());
                tlsKeystore.save(); // Calling the save should automatically call the onClose method in the controller which should convert this to byte array and save
                
                //obj.setTlsKeystore(obj.getTlsKeystoreResource());
                jpaController.edit(obj);
            }            
        } catch (Exception ex) {
            log.error("HostTlsCertificate:Create - Error during creation of new HostTlsCertificate", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("host_tls_certificates:delete")    
    public void delete(HostTlsCertificateLocator locator) {
        if (locator.hostUuid == null || locator.sha1 == null) { 
            throw new RepositoryInvalidInputException(locator);
        }
        log.debug("HostTlsCertificate:Delete - Got request to delete HostTlsCertificate for host with id {}.", locator.hostUuid.toString());        
        String id = locator.hostUuid.toString();
        String sha1 = locator.sha1;
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj = jpaController.findHostByUuid(id);
            if (obj != null) {
                // Now we need to iterate through the certificates in the keystore to match the one we are looking for.
                // To open the keystore, we need to retrieve the password
                String ksPassword = My.configuration().getTlsKeystorePassword();
                SimpleKeystore tlsKeystore = new SimpleKeystore(obj.getTlsKeystoreResource(), ksPassword);

                for(String alias : tlsKeystore.aliases()) {
                    log.debug("Checking keystore alias: {}", alias);
                    if (alias.equals(sha1)) {
                        tlsKeystore.delete(alias);
                    }
                }
                
                tlsKeystore.save(); // Calling the save should automatically call the onClose method in the controller which should convert this to byte array and save                
                //obj.setTlsKeystore(obj.getTlsKeystoreResource());
                jpaController.edit(obj);
            }
        } catch (Exception ex) {
            log.error("HostTlsCertificate:Delete - Error during deletion of HostTlsCertificate.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("host_tls_certificates:delete,search")    
    public void delete(HostTlsCertificateFilterCriteria criteria) {
        HostTlsCertificateCollection objCollection = search(criteria);
        try {
            if (objCollection != null && !objCollection.getTlsCertificates().isEmpty()) {
                for (HostTlsCertificate obj : objCollection.getTlsCertificates()) {
                    HostTlsCertificateLocator locator = new HostTlsCertificateLocator();
                    locator.hostUuid = UUID.valueOf(obj.getHostUuid());
                    locator.sha1 = Sha1Digest.valueOf(obj.getCertificate()).toString();
                    delete(locator);
                }
            }
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("HostTlsCertificate:Delete - Error during deletion of HostTlsCertificate.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
}
