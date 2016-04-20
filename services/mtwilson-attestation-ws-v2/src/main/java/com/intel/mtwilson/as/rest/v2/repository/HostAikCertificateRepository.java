/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificate;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificateLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class HostAikCertificateRepository implements DocumentRepository<HostAikCertificate, HostAikCertificateCollection, HostAikCertificateFilterCriteria, HostAikCertificateLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostAikCertificateRepository.class);

    @Override
    @RequiresPermissions("host_aik_certificates:search")    
    public HostAikCertificateCollection search(HostAikCertificateFilterCriteria criteria) {
        log.debug("AikCertificate:Search - Got request to search for the Host Aik Certificates.");        
        HostAikCertificateCollection objCollection = new HostAikCertificateCollection();
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            if (criteria.hostUuid != null) {
                TblHosts obj = jpaController.findHostByUuid(criteria.hostUuid.toString());
                if (obj != null) {
                    objCollection.getAikCertificates().add(convert(obj));
                }
            } 
        } catch (Exception ex) {
            log.error("AikCertificate:Search - Error during search for hosts.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("AikCertificate:Search - Returning back {} of results.", objCollection.getAikCertificates().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("host_aik_certificates:retrieve")    
    public HostAikCertificate retrieve(HostAikCertificateLocator locator) {
        if (locator == null || locator.hostUuid == null) { return null; }
        log.debug("AikCertificate:Retrieve - Got request to retrieve Aik certificate for host with id {}.", locator.hostUuid);                
        String id = locator.hostUuid.toString();
        
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj = jpaController.findHostByUuid(id);
            if (obj != null) {
                HostAikCertificate hostAik = convert(obj);
                return hostAik;
            }
        } catch (Exception ex) {
            log.error("AikCertificate:Retrieve - Error during search for hosts.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("host_aik_certificates:store")    
    public void store(HostAikCertificate item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_aik_certificates:create")    
    public void create(HostAikCertificate item) {
        log.debug("AikCertificate:Create - Got request to create a new Aik certificate.");
        HostAikCertificateLocator locator = new HostAikCertificateLocator();
        locator.hostUuid = UUID.valueOf(item.getHostUuid());
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj = jpaController.findHostByUuid(item.getHostUuid());
            if (obj != null) {
                obj.setAIKCertificate(X509Util.encodePemCertificate(item.getX509Certificate()));
//                Sha1Digest aikSha1 = Sha1Digest.valueOf(item.getCertificate());
                
                jpaController.edit(obj);
            }
        } catch (Exception ex) {
            log.error("AikCertificate:Create - Error during aik update for the host.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("host_aik_certificates:delete")    
    public void delete(HostAikCertificateLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_aik_certificates:delete,search")    
    public void delete(HostAikCertificateFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private HostAikCertificate convert(TblHosts obj) {
        HostAikCertificate convObj = new HostAikCertificate();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setCertificate(obj.getAIKCertificate().getBytes());
        convObj.setAikSha1(obj.getAikSha1());
        return convObj;
    }
    
}
