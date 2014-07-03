/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificate;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificateLocator;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
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
                TblHosts obj = jpaController.findHostByUuid(criteria.hostUuid);
                if (obj != null) {
                    objCollection.getAikCertificates().add(convert(obj));
                }
            } // TODO: Need to add the AIKSha1 search criteria later when we have the capability of multiple AIKs
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search for hosts.", ex);
            throw new ASException(ErrorCode.AS_QUERY_HOST_ERROR, ex.getClass().getSimpleName());
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
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search for hosts.", ex);
            throw new ASException(ErrorCode.AS_QUERY_HOST_ERROR, ex.getClass().getSimpleName());
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
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj = jpaController.findHostByUuid(item.getHostUuid());
            if (obj != null) {
                obj.setAIKCertificate(item.getCertificate().toString());
//                Sha1Digest aikSha1 = Sha1Digest.valueOf(item.getCertificate());
                
                jpaController.edit(obj);
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during aik update for the host.", ex);
            throw new ASException(ErrorCode.AS_AIK_CREATE_ERROR, ex.getClass().getSimpleName());
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
