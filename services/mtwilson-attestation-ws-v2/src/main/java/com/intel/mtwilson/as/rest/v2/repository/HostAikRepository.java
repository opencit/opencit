/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.rest.v2.model.HostAik;
import com.intel.mtwilson.as.rest.v2.model.HostAikCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAikFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.HostAikLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class HostAikRepository implements DocumentRepository<HostAik, HostAikCollection, HostAikFilterCriteria, HostAikLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostAikRepository.class);

    @Override
    @RequiresPermissions("host_aiks:search")    
    public HostAikCollection search(HostAikFilterCriteria criteria) {
        log.debug("HostAik:Search - Got request to search for host aik.");        
        HostAikCollection objCollection = new HostAikCollection();
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            if (criteria.hostUuid != null) {
                TblHosts obj = jpaController.findHostByUuid(criteria.hostUuid.toString());
                if (obj != null) {
                    objCollection.getAiks().add(convert(obj));
                }
            } 
        } catch (Exception ex) {
            log.error("HostAik:Search - Error during search for host aik.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("HostAik:Search - Returning back {} of results.", objCollection.getAiks().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("host_aiks:retrieve")    
    public HostAik retrieve(HostAikLocator locator) {
        if (locator == null || locator.hostUuid == null) { return null; }
        log.debug("HostAik:Retrieve - Got request to retrieve Aik for host with id {}.", locator.hostUuid);                
        String id = locator.hostUuid.toString();
        
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj = jpaController.findHostByUuid(id);
            if (obj != null) {
                HostAik hostAik = convert(obj);
                return hostAik;
            }
        } catch (Exception ex) {
            log.error("HostAik:Retrieve - Error during retrieval of host aik.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("host_aiks:store")    
    public void store(HostAik item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_aiks:create")    
    public void create(HostAik item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_aiks:delete")    
    public void delete(HostAikLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_aiks:delete,search")    
    public void delete(HostAikFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private HostAik convert(TblHosts obj) {
        HostAik convObj = new HostAik();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setAikPublicKey(obj.getAikPublicKey());
        convObj.setAikSha1(obj.getAikSha1());
        return convObj;
    }
    
}
