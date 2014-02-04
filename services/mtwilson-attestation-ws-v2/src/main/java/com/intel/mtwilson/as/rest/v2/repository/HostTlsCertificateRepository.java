/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.rest.v2.model.HostTlsCertificate;
import com.intel.mtwilson.as.rest.v2.model.HostTlsCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.HostTlsCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.HostTlsCertificateLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class HostTlsCertificateRepository implements SimpleRepository<HostTlsCertificate, HostTlsCertificateCollection, HostTlsCertificateFilterCriteria, HostTlsCertificateLocator> {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    public HostTlsCertificateCollection search(HostTlsCertificateFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HostTlsCertificate retrieve(HostTlsCertificateLocator locator) {
        if (locator.id == null || locator.sha1 == null) { return null; }
        String id = locator.id.toString();
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
                            throw new ASException(ErrorCode.AS_TLS_KEYSTORE_ERROR);
                        }
                    }
                }                
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search for hosts.", ex);
            throw new ASException(ErrorCode.AS_QUERY_HOST_ERROR, ex.getClass().getSimpleName());
        }

        return null;
    }

    @Override
    public void store(HostTlsCertificate item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void create(HostTlsCertificate item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(HostTlsCertificateLocator locator) {
        if (locator.id == null || locator.sha1 == null) { 
            throw new ASException(ErrorCode.AS_INVALID_INPUT);
        }
        String id = locator.id.toString();
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
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search for hosts.", ex);
            throw new ASException(ErrorCode.AS_QUERY_HOST_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    public void delete(HostTlsCertificateFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
